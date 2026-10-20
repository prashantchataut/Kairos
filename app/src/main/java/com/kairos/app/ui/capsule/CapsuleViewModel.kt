package com.kairos.app.ui.capsule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.core.util.Clock
import com.kairos.app.data.Attachment
import com.kairos.app.data.AttachmentStore
import com.kairos.app.data.Capsule
import com.kairos.app.data.KairosRepository
import com.kairos.app.domain.capsule.CapsuleEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class CapsuleDraft(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val body: String = "",
    val unlockAt: Long = 0L,
    val mood: String? = null,
    val attachments: List<Attachment> = emptyList(),
)

data class CapsulesUi(
    val now: Long = 0L,
    val vault: CapsuleEngine.Vault = CapsuleEngine.Vault(emptyList(), emptyList(), emptyList()),
    val presets: List<CapsuleEngine.Preset> = emptyList(),
    val draft: CapsuleDraft? = null,
    val error: String? = null,
    val sealed: Capsule? = null,
    val busy: Boolean = false,
)

class CapsuleViewModel(
    private val repository: KairosRepository,
    private val attachments: AttachmentStore,
    private val clock: Clock,
) : ViewModel() {

    private val draft = MutableStateFlow<CapsuleDraft?>(null)
    private val error = MutableStateFlow<String?>(null)
    private val sealed = MutableStateFlow<Capsule?>(null)
    private val busy = MutableStateFlow(false)
    private val tick = MutableStateFlow(clock.millis())

    private data class Local(
        val draft: CapsuleDraft?,
        val error: String?,
        val sealed: Capsule?,
        val busy: Boolean,
    )

    private val local = combine(draft, error, sealed, busy) { d, e, s, b ->
        Local(d, e, s, b)
    }

    val state: StateFlow<CapsulesUi> = combine(repository.state, local, tick) { app, l, now ->
        CapsulesUi(
            now = now,
            vault = CapsuleEngine.vault(app.capsules, now),
            presets = CapsuleEngine.presets(now, clock.zone()),
            draft = l.draft,
            error = l.error,
            sealed = l.sealed,
            busy = l.busy,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CapsulesUi(
            now = clock.millis(),
            vault = CapsuleEngine.vault(repository.state.value.capsules, clock.millis()),
            presets = CapsuleEngine.presets(clock.millis(), clock.zone()),
        ),
    )

    fun tick() {
        tick.value = clock.millis()
    }

    fun startDraft() {
        val now = clock.millis()
        val presets = CapsuleEngine.presets(now, clock.zone())
        draft.value = CapsuleDraft(unlockAt = presets.firstOrNull()?.at ?: (now + 7 * 86_400_000L))
        error.value = null
        sealed.value = null
    }

    fun discardDraft() {
        draft.value?.let { attachments.deleteFor(it.id) }
        draft.value = null
        error.value = null
    }

    fun updateTitle(value: String) {
        draft.value = draft.value?.copy(title = value)
        error.value = null
    }

    fun updateBody(value: String) {
        draft.value = draft.value?.copy(body = value)
        error.value = null
    }

    fun setUnlockAt(value: Long) {
        draft.value = draft.value?.copy(unlockAt = value)
        error.value = null
    }

    fun setMood(value: String?) {
        draft.value = draft.value?.copy(mood = value)
    }

    fun attach(uri: android.net.Uri) {
        val current = draft.value ?: return
        busy.value = true
        viewModelScope.launch {
            val attachment = withContext(Dispatchers.IO) { attachments.import(current.id, uri) }
            busy.value = false
            if (attachment == null) {
                error.value = "That file could not be read."
            } else {
                draft.value = draft.value?.copy(
                    attachments = draft.value?.attachments.orEmpty() + attachment,
                )
            }
        }
    }

    fun removeAttachment(attachment: Attachment) {
        draft.value = draft.value?.copy(
            attachments = draft.value?.attachments.orEmpty().filterNot { it == attachment },
        )
    }

    fun seal() {
        val current = draft.value ?: return
        val now = clock.millis()
        val validation = CapsuleEngine.validate(
            CapsuleEngine.Draft(current.title, current.body, current.unlockAt, current.mood),
            now,
        )
        if (!validation.ok) {
            error.value = validation.message
            return
        }
        val capsule = Capsule(
            id = current.id,
            title = current.title.trim(),
            body = current.body.trim(),
            createdAt = now,
            unlockAt = current.unlockAt,
            attachments = current.attachments,
            mood = current.mood,
        )
        repository.sealCapsule(capsule, clock.today().toString())
        sealed.value = capsule
        draft.value = null
        error.value = null
    }

    fun dismissSealed() {
        sealed.value = null
    }

    fun open(capsule: Capsule) {
        if (CapsuleEngine.canOpen(capsule, clock.millis()) && capsule.openedAt == null) {
            repository.openCapsule(capsule.id, clock.millis())
        }
    }

    fun delete(capsule: Capsule) {
        viewModelScope.launch(Dispatchers.IO) { attachments.deleteFor(capsule.id) }
        repository.deleteCapsule(capsule.id)
    }

    fun clearError() {
        error.value = null
    }
}
