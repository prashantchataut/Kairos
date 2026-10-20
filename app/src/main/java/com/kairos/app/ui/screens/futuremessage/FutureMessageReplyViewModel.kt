package com.kairos.app.ui.screens.futuremessage

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.data.auth.UserIdProvider
import com.kairos.app.data.local.entity.FutureMessageEntity
import com.kairos.app.data.local.entity.FutureMessageReplyEntity
import com.kairos.app.domain.futuremessage.FutureReplyPromptPolicy
import com.kairos.app.domain.gamification.PointCalculator
import com.kairos.app.domain.gamification.Skill
import com.kairos.app.domain.model.Mood
import com.kairos.app.domain.repository.FutureMessageReplyRepository
import com.kairos.app.domain.repository.FutureMessageRepository
import com.kairos.app.domain.repository.GamificationRepository
import com.kairos.app.domain.repository.StreakActivityType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * UI State for Future Message Reply
 */
data class FutureMessageReplyUiState(
    val isLoading: Boolean = true,
    val originalMessage: FutureMessageEntity? = null,
    val existingReply: FutureMessageReplyEntity? = null,
    val hasReplied: Boolean = false,
    // Reply input
    val replyContent: String = "",
    val selectedMood: Mood? = null,
    val currentPrompt: String = "",
    // Time context
    val daysSinceWritten: Long = 0,
    val timePassedFormatted: String = "",
    val isAnniversary: Boolean = false,
    val anniversaryType: String? = null, // "1 month", "1 year", etc.
    // Chain creation
    val wantsToCreateChain: Boolean = false,
    val chainDeliveryDate: Long? = null,
    // Saving
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    // Error handling
    val errorMessage: String? = null,
    // Navigation
    val shouldNavigateToJournal: Boolean = false,
    val prefilledJournalContent: String = "",
    val shouldNavigateBack: Boolean = false
)

@HiltViewModel
class FutureMessageReplyViewModel @Inject constructor(
    private val futureMessageReplyRepository: FutureMessageReplyRepository,
    private val futureMessageRepository: FutureMessageRepository,
    private val gamificationRepository: GamificationRepository,
    private val userIdProvider: UserIdProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(FutureMessageReplyUiState())
    val uiState: StateFlow<FutureMessageReplyUiState> = _uiState.asStateFlow()

    private val messageId: Long = savedStateHandle.get<Long>("messageId") ?: 0

    init {
        if (messageId > 0) {
            loadMessage()
            loadExistingReply()
        }
    }

    private fun loadMessage() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val message = futureMessageRepository.getMessage(messageId).getOrNull()
                if (message != null && !com.kairos.app.domain.capsule.CapsuleClock.canOpen(message.deliveryDate, System.currentTimeMillis())) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "This capsule is still sealed.") }
                    return@launch
                }
                if (message != null) {
                    val daysSince = calculateDaysSince(message.createdAt)
                    val (isAnniversary, anniversaryType) = checkAnniversary(message.createdAt)

                    _uiState.update { it.copy(
                        isLoading = false,
                        originalMessage = message,
                        daysSinceWritten = daysSince,
                        timePassedFormatted = formatTimePassed(daysSince),
                        isAnniversary = isAnniversary,
                        anniversaryType = anniversaryType,
                        currentPrompt = FutureReplyPromptPolicy.contextualPrompt(daysSince)
                    )}
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Message not found"
                    )}
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorMessage = "Failed to load message"
                )}
            }
        }
    }

    private fun loadExistingReply() {
        viewModelScope.launch {
            futureMessageReplyRepository.observeReplyForMessage(messageId)
                .collect { reply ->
                    _uiState.update { it.copy(
                        existingReply = reply,
                        hasReplied = reply != null,
                        replyContent = reply?.replyContent ?: it.replyContent,
                        selectedMood = reply?.reactionMood?.let { mood -> Mood.fromString(mood) }
                    )}
                }
        }
    }

    // ==================== INPUT HANDLERS ====================

    fun onReplyContentChanged(content: String) {
        _uiState.update { it.copy(replyContent = content) }
    }

    fun onMoodSelected(mood: Mood?) {
        _uiState.update { it.copy(selectedMood = mood) }
    }

    fun onNewPromptRequested() {
        _uiState.update { it.copy(
            currentPrompt = FutureReplyPromptPolicy.nextPrompt(it.currentPrompt)
        )}
    }

    // ==================== TIME CAPSULE CHAIN ====================

    fun toggleChainCreation() {
        _uiState.update { it.copy(wantsToCreateChain = !it.wantsToCreateChain) }
    }

    fun setChainDeliveryDate(date: Long) {
        _uiState.update { it.copy(chainDeliveryDate = date) }
    }

    // ==================== SAVE REPLY ====================

    fun saveReply() {
        val state = _uiState.value
        if (state.isSaving || state.hasReplied) return
        if (state.replyContent.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please write a reply") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val reply = FutureMessageReplyEntity(
                userId = userIdProvider.getUserId(),
                originalMessageId = messageId,
                replyContent = state.replyContent.trim(),
                promptShown = state.currentPrompt,
                reactionMood = state.selectedMood?.name,
                repliedAt = System.currentTimeMillis()
            )

            futureMessageReplyRepository.createReply(reply)
                .onSuccess { replyId ->
                    // If user wants to create a chain, create the new future message
                    if (state.wantsToCreateChain && state.chainDeliveryDate != null) {
                        createChainMessage(replyId, state.chainDeliveryDate)
                    }

                    runCatching {
                        gamificationRepository.awardSkillXp(
                            skill = Skill.COURAGE,
                            amount = PointCalculator.Courage.REPLY_TO_PAST_SELF,
                            rewardKey = "future_message_reply_${messageId}",
                            respectDailyCap = true
                        )
                        gamificationRepository.recordDailyActivity(StreakActivityType.FUTURE_MESSAGE)
                        gamificationRepository.checkAndUpdateAchievements()
                    }.onFailure {
                        android.util.Log.w("FutureMessageReply", "Reply saved but progression could not be updated", it)
                    }

                    _uiState.update { it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        hasReplied = true
                    )}
                }
                .onError { error ->
                    _uiState.update { it.copy(
                        isSaving = false,
                        errorMessage = error.userMessage
                    )}
                }
        }
    }

    private suspend fun createChainMessage(replyId: Long, deliveryDate: Long) {
        val state = _uiState.value

        val chainMessage = FutureMessageEntity(
            userId = userIdProvider.getUserId(),
            title = "Time Capsule Chain",
            content = buildString {
                appendLine("This is a continuation of your time capsule chain.")
                appendLine()
                appendLine("**Original message (${state.timePassedFormatted} ago):**")
                appendLine(state.originalMessage?.content?.take(200) ?: "")
                appendLine()
                appendLine("**Your reply:**")
                appendLine(state.replyContent.take(200))
            },
            deliveryDate = deliveryDate,
            isDelivered = false,
            createdAt = System.currentTimeMillis()
        )

        val chainResult = futureMessageRepository.createMessage(chainMessage)
        val chainId = chainResult.getOrNull() ?: return
        futureMessageReplyRepository.setChainedMessage(replyId, chainId)
    }

    // ==================== SAVE TO JOURNAL ====================

    fun saveReplyAsJournal() {
        val state = _uiState.value

        val journalContent = buildString {
            appendLine("# Conversation with My Past Self")
            appendLine()
            appendLine("**${state.timePassedFormatted} ago, I wrote:**")
            appendLine()
            appendLine("> ${state.originalMessage?.content}")
            appendLine()
            appendLine("**Today, I reply:**")
            appendLine()
            appendLine(state.replyContent)
            appendLine()
            state.selectedMood?.let {
                appendLine("*Feeling ${it.displayName.lowercase()} reading this.*")
            }
        }

        _uiState.update { it.copy(
            shouldNavigateToJournal = true,
            prefilledJournalContent = journalContent
        )}
    }

    /**
     * Called after journal entry is created to link the reply to it.
     */
    fun markSavedAsJournal(journalId: Long) {
        viewModelScope.launch {
            val reply = _uiState.value.existingReply ?: return@launch
            futureMessageReplyRepository.markSavedAsJournal(reply.id, journalId)
        }
    }

    fun clearNavigation() {
        _uiState.update { it.copy(
            shouldNavigateToJournal = false,
            prefilledJournalContent = "",
            shouldNavigateBack = false
        )}
    }

    // ==================== ERROR HANDLING ====================

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // ==================== HELPERS ====================

    private fun calculateDaysSince(timestamp: Long): Long {
        val then = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val now = Instant.now()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return ChronoUnit.DAYS.between(then, now)
    }

    private fun formatTimePassed(days: Long): String {
        return when {
            days < 1 -> "today"
            days == 1L -> "1 day"
            days < 7 -> "$days days"
            days < 30 -> "${days / 7} week${if (days / 7 > 1) "s" else ""}"
            days < 365 -> "${days / 30} month${if (days / 30 > 1) "s" else ""}"
            else -> {
                val years = days / 365
                val months = (days % 365) / 30
                if (months > 0) {
                    "$years year${if (years > 1) "s" else ""}, $months month${if (months > 1) "s" else ""}"
                } else {
                    "$years year${if (years > 1) "s" else ""}"
                }
            }
        }
    }

    private fun checkAnniversary(timestamp: Long): Pair<Boolean, String?> {
        val then = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val now = Instant.now()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        // Check for yearly anniversary (within 3 days)
        val yearsSince = ChronoUnit.YEARS.between(then, now)
        if (yearsSince >= 1) {
            val anniversary = then.plusYears(yearsSince)
            val daysFromAnniversary = ChronoUnit.DAYS.between(anniversary, now).let { kotlin.math.abs(it) }
            if (daysFromAnniversary <= 3) {
                return Pair(true, "$yearsSince year${if (yearsSince > 1) "s" else ""}")
            }
        }

        // Check for monthly anniversary (within 2 days, for first 6 months)
        val monthsSince = ChronoUnit.MONTHS.between(then, now)
        if (monthsSince in 1..6) {
            val anniversary = then.plusMonths(monthsSince)
            val daysFromAnniversary = ChronoUnit.DAYS.between(anniversary, now).let { kotlin.math.abs(it) }
            if (daysFromAnniversary <= 2) {
                return Pair(true, "$monthsSince month${if (monthsSince > 1) "s" else ""}")
            }
        }

        return Pair(false, null)
    }

    fun getFormattedCreatedDate(): String {
        val timestamp = _uiState.value.originalMessage?.createdAt ?: return ""
        val date = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
    }

    fun getDeliveryDateSuggestions(): List<Pair<String, Long>> {
        val now = System.currentTimeMillis()
        val oneWeek = now + (7 * 24 * 60 * 60 * 1000L)
        val oneMonth = now + (30 * 24 * 60 * 60 * 1000L)
        val threeMonths = now + (90 * 24 * 60 * 60 * 1000L)
        val sixMonths = now + (180 * 24 * 60 * 60 * 1000L)
        val oneYear = now + (365 * 24 * 60 * 60 * 1000L)

        return listOf(
            "1 week" to oneWeek,
            "1 month" to oneMonth,
            "3 months" to threeMonths,
            "6 months" to sixMonths,
            "1 year" to oneYear
        )
    }
}
