package com.kairos.app.ui.garden

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.data.auth.UserIdProvider
import com.kairos.app.data.local.dao.QuoteDao
import com.kairos.app.data.local.entity.*
import com.kairos.app.domain.common.Result
import com.kairos.app.domain.futuremessage.CapsulePolicy
import com.kairos.app.domain.model.DailyPlan
import com.kairos.app.domain.repository.DailyPlanRepository
import com.kairos.app.domain.repository.FutureMessageRepository
import com.kairos.app.domain.repository.VocabularyRepository
import com.kairos.app.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class GardenViewModel @Inject constructor(
    private val vocabulary: VocabularyRepository,
    private val quotes: QuoteDao,
    private val capsules: FutureMessageRepository,
    private val daily: DailyPlanRepository,
    private val user: UserIdProvider,
    private val attachments: CapsuleAttachmentStore,
    private val scheduler: NotificationScheduler,
    private val preferences: com.kairos.app.data.local.preferences.PreferencesManager,
    private val onboarding: com.kairos.app.domain.repository.OnboardingRepository
) : ViewModel() {
    val setupComplete = preferences.onboardingCompleted.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    private val _settingUp = MutableStateFlow(false)
    val settingUp = _settingUp.asStateFlow()
    fun finishSetup(interests: Set<String>, difficulty: Int) = launchSafely {
        _settingUp.value = true
        try {
            onboarding.completeSetup(com.kairos.app.domain.repository.OnboardingPreferences(difficulty, setOf("wisdom", "life", "motivation"), interests))
                .onSuccess { _plan.value = null; refresh() }.onError { _error.value = it.userMessage }
        } finally { _settingUp.value = false }
    }
    val words = vocabulary.getAllWords().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val thoughts = quotes.getAllQuotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val letters = combine(capsules.observePendingMessages(), capsules.observeDeliveredMessages()) { a, b ->
        (a + b).filter { !it.isDeleted }.distinctBy { it.id }.sortedBy { it.deliveryDate }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _plan = MutableStateFlow<DailyPlan?>(null)
    val plan = _plan.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()
    private val _saving = MutableStateFlow(false)
    val saving = _saving.asStateFlow()
    private val _sealing = MutableStateFlow(false)
    val sealing = _sealing.asStateFlow()
    private val _sealed = MutableStateFlow(false)
    val sealed = _sealed.asStateFlow()
    private val _draftFiles = MutableStateFlow<List<String>>(emptyList())
    val draftFiles = _draftFiles.asStateFlow()
    private val _copying = MutableStateFlow(false)
    val copying = _copying.asStateFlow()

    init { refresh() }

    fun refresh() = launchSafely {
        val today = LocalDate.now()
        if (_plan.value?.localDate != today || _plan.value?.word == null || _plan.value?.quote == null) _plan.value = daily.getOrCreateDailyPlan(user.getUserId(), today, System.currentTimeMillis())
        capsules.getMessagesReadyForDelivery(System.currentTimeMillis()).getOrDefault(emptyList()).forEach {
            capsules.markDelivered(it.id).onError { e -> _error.value = e.userMessage }
        }
    }

    fun clearError() { _error.value = null }
    fun consumeSealed() { _sealed.value = false }
    fun saveWord(word: VocabularyEntity) = mutate { vocabulary.updateFavoriteStatus(word.id, !word.isFavorite) }
    fun saveThought(quote: QuoteEntity) = launchSafely { quotes.updateFavoriteStatus(quote.id, !quote.isFavorite) }
    fun review(word: VocabularyEntity, remembered: Boolean, onComplete: () -> Unit) = launchSafely {
        if (_saving.value) return@launchSafely
        _saving.value = true
        try {
            vocabulary.processWordReview(word.id, if (remembered) 4 else 1)
                .onSuccess { onComplete() }.onError { _error.value = it.userMessage }
        } finally { _saving.value = false }
    }

    fun addAttachments(uris: List<Uri>) {
        if (_copying.value || _sealing.value) return
        _copying.value = true
        viewModelScope.launch {
            try {
                require(uris.size + _draftFiles.value.size <= CapsulePolicy.MAX_ATTACHMENTS) { "Each capsule holds up to six attachments." }
                uris.forEach { uri -> val kept = attachments.keep(uri); _draftFiles.update { it + kept } }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _error.value = e.message ?: "Could not keep this attachment. Try a smaller file." }
            finally { _copying.value = false }
        }
    }

    fun removeAttachment(uri: String) = launchSafely {
        if (!_sealing.value) { _draftFiles.update { it - uri }; attachments.discard(uri) }
    }

    fun discardDraft() = launchSafely {
        if (!_sealing.value && !_copying.value) {
            val files = _draftFiles.value; _draftFiles.value = emptyList()
            files.forEach { attachments.discard(it) }
        }
    }

    fun seal(title: String, content: String, date: LocalDate) {
        if (_sealing.value || _copying.value || _sealed.value) return
        val at = CapsulePolicy.deliveryAt(date, ZoneId.systemDefault())
        val validation = CapsulePolicy.validate(content, at, System.currentTimeMillis(), _draftFiles.value.size)
        if (validation != null) { _error.value = validation; return }
        _sealing.value = true
        viewModelScope.launch {
            try {
                val message = FutureMessageEntity(
                    userId = user.getUserId(), title = title.trim().ifBlank { "A little note from today" },
                    content = content.trim(), deliveryDate = at,
                    attachedPhotos = _draftFiles.value.joinToString(",")
                )
                when (val result = capsules.createMessage(message)) {
                    is Result.Success -> {
                        _draftFiles.value = emptyList(); _sealed.value = true
                        // A denied notification or worker failure cannot undo a saved letter.
                        runCatching { scheduler.scheduleFutureMessage(result.data, at, message.title, message.content) }
                    }
                    is Result.Error -> _error.value = result.userMessage
                    Result.Loading -> Unit
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _error.value = "Your letter could not be sealed. Your draft is still here." }
            finally { _sealing.value = false }
        }
    }

    fun open(letter: FutureMessageEntity) = launchSafely {
        if (CapsulePolicy.canOpen(letter.deliveryDate, System.currentTimeMillis())) {
            capsules.markRevealed(letter.id).onError { _error.value = it.userMessage }
        }
    }

    private fun mutate(block: suspend () -> Result<Unit>) = launchSafely {
        if (_saving.value) return@launchSafely
        _saving.value = true
        try { block().onError { _error.value = it.userMessage } } finally { _saving.value = false }
    }

    private fun launchSafely(block: suspend () -> Unit) = viewModelScope.launch {
        try { block() } catch (e: CancellationException) { throw e }
        catch (e: Exception) { _error.value = "Kairos couldn’t finish that. Please try again." }
    }
}
