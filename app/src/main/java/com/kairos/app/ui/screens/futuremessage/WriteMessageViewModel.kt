package com.kairos.app.ui.screens.futuremessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.data.auth.UserIdProvider
import com.kairos.app.data.local.entity.FutureMessageEntity
import com.kairos.app.domain.gamification.GameSessionManager
import com.kairos.app.domain.repository.FutureMessageRepository
import com.kairos.app.domain.gamification.SessionResult
import com.kairos.app.util.AudioRecorderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class WriteMessageUiState(
    val title: String = "",
    val content: String = "",
    val deliveryDate: Long = getDefaultDeliveryDate(),
    val selectedPreset: DatePreset = DatePreset.ONE_MONTH,
    val selectedCategory: MessageCategory = MessageCategory.GENERAL,
    val selectedCategories: Set<MessageCategory> = setOf(MessageCategory.GENERAL),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val showDatePicker: Boolean = false,
    // Media attachments
    val attachedPhotos: List<String> = emptyList(),
    val attachedVideos: List<String> = emptyList(),
    // Voice recording state
    val isRecording: Boolean = false,
    val voiceRecordingUri: String? = null,
    val voiceRecordingDuration: Long = 0,
    val recordingTimeElapsed: Long = 0,
    val isPlayingVoice: Boolean = false,
    // Animation state
    val showSealingAnimation: Boolean = false,
    // Unsaved changes
    val hasUnsavedChanges: Boolean = false,
    val showDiscardDialog: Boolean = false,
    // Session result for completion feedback
    val sessionResult: SessionResult? = null,
    val showSessionResult: Boolean = false
) {
    val canSave: Boolean
        get() = content.isNotBlank() && deliveryDate > System.currentTimeMillis()

    val hasContent: Boolean
        get() = title.isNotBlank() || content.isNotBlank() ||
                attachedPhotos.isNotEmpty() || attachedVideos.isNotEmpty() ||
                voiceRecordingUri != null
}

private fun getDefaultDeliveryDate(): Long {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, 1)
    return calendar.timeInMillis
}

@HiltViewModel
class WriteMessageViewModel @Inject constructor(
    private val futureMessageRepository: FutureMessageRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val appContext: android.content.Context,
    private val userIdProvider: UserIdProvider,
    private val audioRecorderManager: AudioRecorderManager,
    private val gameSessionManager: GameSessionManager
) : ViewModel() {

    companion object {
        private const val TAG = "WriteMessageViewModel"
    }

    private val _uiState = MutableStateFlow(WriteMessageUiState())
    val uiState: StateFlow<WriteMessageUiState> = _uiState.asStateFlow()

    init {
        observeAudioRecorderStates()
    }

    /**
     * Observe audio recorder state changes and update UI state accordingly.
     */
    private fun observeAudioRecorderStates() {
        viewModelScope.launch {
            audioRecorderManager.isRecording.collect { isRecording ->
                _uiState.update { it.copy(isRecording = isRecording) }
            }
        }
        viewModelScope.launch {
            audioRecorderManager.recordingDuration.collect { duration ->
                _uiState.update { it.copy(recordingTimeElapsed = duration) }
            }
        }
        viewModelScope.launch {
            audioRecorderManager.isPlaying.collect { isPlaying ->
                _uiState.update { it.copy(isPlayingVoice = isPlaying) }
            }
        }
        viewModelScope.launch {
            audioRecorderManager.error.collect { error ->
                error?.let {
                    _uiState.update { state -> state.copy(error = it) }
                    audioRecorderManager.clearError()
                }
            }
        }
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, hasUnsavedChanges = true) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content, hasUnsavedChanges = true) }
    }

    fun updateCategory(category: MessageCategory) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                selectedCategories = setOf(category),
                hasUnsavedChanges = true
            )
        }
    }

    /**
     * Kept for compatibility with older call sites. Future letters intentionally
     * use one primary intention so filtering and recommendation signals stay clear.
     */
    fun toggleCategory(category: MessageCategory) = updateCategory(category)

    fun selectDatePreset(preset: DatePreset) {
        val calendar = Calendar.getInstance()
        when (preset) {
            DatePreset.ONE_WEEK -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            DatePreset.ONE_MONTH -> calendar.add(Calendar.MONTH, 1)
            DatePreset.SIX_MONTHS -> calendar.add(Calendar.MONTH, 6)
            DatePreset.ONE_YEAR -> calendar.add(Calendar.YEAR, 1)
            DatePreset.CUSTOM -> {
                // Show date picker for custom selection
                _uiState.update { it.copy(showDatePicker = true) }
                return
            }
        }

        _uiState.update {
            it.copy(
                selectedPreset = preset,
                deliveryDate = calendar.timeInMillis,
                hasUnsavedChanges = true
            )
        }
    }

    fun selectCustomDate(date: Long) {
        _uiState.update {
            it.copy(
                selectedPreset = DatePreset.CUSTOM,
                deliveryDate = date,
                showDatePicker = false,
                hasUnsavedChanges = true
            )
        }
    }

    fun showDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun hideDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    fun saveMessage() {
        val state = _uiState.value
        if (state.content.isBlank()) {
            _uiState.update { it.copy(error = "Write something before sealing this letter.") }
            return
        }
        if (state.deliveryDate <= System.currentTimeMillis()) {
            _uiState.update { it.copy(error = "Choose a delivery date in the future.") }
            return
        }
        if (state.isSaving || state.isSaved) return

        viewModelScope.launch {
            _uiState.update { it.copy(showSealingAnimation = true, isSaving = true, error = null) }

            runCatching {
                val message = FutureMessageEntity(
                    userId = userIdProvider.getUserId(),
                    title = state.title.trim().ifBlank { "A letter to my future self" },
                    content = state.content.trim(),
                    deliveryDate = state.deliveryDate,
                    category = state.selectedCategory.name.lowercase(),
                    createdAt = System.currentTimeMillis(),
                    attachedPhotos = state.attachedPhotos.distinct().joinToString(","),
                    attachedVideos = state.attachedVideos.distinct().joinToString(","),
                    voiceRecordingUri = state.voiceRecordingUri,
                    voiceRecordingDuration = state.voiceRecordingDuration
                )

                val createResult = futureMessageRepository.createMessage(message)
                val messageId = createResult.getOrNull()
                    ?: throw IllegalStateException(
                        (createResult as? com.kairos.app.domain.common.Result.Error)?.userMessage
                            ?: "Kairos could not seal this letter."
                    )

                // Rewards are secondary to preserving the letter. A reward failure must
                // never make the user believe their already-saved writing was lost.
                val sessionResult = runCatching {
                    val result = gameSessionManager.completeCommitSession(
                        messageId = messageId,
                        content = state.content,
                        deliveryDate = state.deliveryDate
                    )
                    result
                }.onFailure { error ->
                    android.util.Log.w(TAG, "Letter saved, but reward processing failed", error)
                }.getOrNull()

                sessionResult
            }.onSuccess { sessionResult ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSaved = true,
                        showSealingAnimation = false,
                        hasUnsavedChanges = false,
                        sessionResult = sessionResult,
                        showSessionResult = sessionResult != null
                    )
                }
            }.onFailure { error ->
                android.util.Log.e(TAG, "Unable to save future letter", error)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        showSealingAnimation = false,
                        error = "Kairos could not seal this letter. Your draft is still here."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Dismiss the session result card.
     */
    fun dismissSessionResult() {
        _uiState.update { it.copy(showSessionResult = false) }
    }

    // =========================================================================
    // MEDIA ATTACHMENT METHODS
    // =========================================================================

    /**
     * Add photos to the message.
     */
    /** Copy picked memories into private storage so they survive provider permission expiry. */
    fun importAttachments(uris: List<android.net.Uri>, videos: Boolean) {
        viewModelScope.launch {
            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                runCatching {
                    val directory = java.io.File(appContext.filesDir, "capsule_memories").apply { mkdirs() }
                    val imported = mutableListOf<java.io.File>()
                    try {
                        uris.take(10).map { uri ->
                            val extension = if (videos) ".mp4" else ".jpg"
                            val file = java.io.File(directory, java.util.UUID.randomUUID().toString() + extension)
                            imported.add(file)
                            appContext.contentResolver.openInputStream(uri).use { input ->
                                requireNotNull(input) { "Cannot read this attachment" }
                                file.outputStream().use { output ->
                                    val buffer = ByteArray(8192)
                                    var total = 0L
                                    while (true) {
                                        val count = input.read(buffer)
                                        if (count < 0) break
                                        total += count
                                        require(total <= 50L * 1024 * 1024) { "Attachment exceeds 50 MB" }
                                        output.write(buffer, 0, count)
                                    }
                                }
                            }
                            file.toURI().toString()
                        }
                    } catch (error: Exception) {
                        imported.forEach { it.delete() }
                        throw error
                    }
                }
            }
            result.onSuccess { paths -> if (videos) addVideos(paths) else addPhotos(paths) }
                .onFailure { _uiState.update { it.copy(error = "Could not keep these memories. Choose readable files under 50 MB and try again.") } }
        }
    }

    fun addPhotos(photoUris: List<String>) {
        _uiState.update {
            it.copy(
                attachedPhotos = it.attachedPhotos + photoUris,
                hasUnsavedChanges = true
            )
        }
    }

    /**
     * Remove a photo from the message.
     */
    fun removePhoto(uri: String) {
        _uiState.update {
            it.copy(
                attachedPhotos = it.attachedPhotos - uri,
                hasUnsavedChanges = true
            )
        }
    }

    /**
     * Add videos to the message.
     */
    fun addVideos(videoUris: List<String>) {
        _uiState.update {
            it.copy(
                attachedVideos = it.attachedVideos + videoUris,
                hasUnsavedChanges = true
            )
        }
    }

    /**
     * Remove a video from the message.
     */
    fun removeVideo(uri: String) {
        _uiState.update {
            it.copy(
                attachedVideos = it.attachedVideos - uri,
                hasUnsavedChanges = true
            )
        }
    }

    // =========================================================================
    // VOICE RECORDING METHODS
    // =========================================================================

    /**
     * Start voice recording using AudioRecorderManager.
     * Returns true if recording started successfully.
     */
    fun startRecording(): Boolean {
        val uri = audioRecorderManager.startRecording()
        return uri != null
    }

    /**
     * Stop voice recording and save the URI.
     * The result is automatically handled via state observation.
     */
    fun stopRecording() {
        val result = audioRecorderManager.stopRecording()
        if (result != null) {
            val (recordingUri, recordingDuration) = result
            _uiState.update {
                it.copy(
                    voiceRecordingUri = recordingUri.toString(),
                    voiceRecordingDuration = recordingDuration,
                    hasUnsavedChanges = true
                )
            }
        }
    }

    /**
     * Cancel voice recording without saving.
     */
    fun cancelRecording() {
        audioRecorderManager.cancelRecording()
    }

    /**
     * Remove the voice recording and delete the file.
     */
    fun removeVoiceRecording() {
        val uri = _uiState.value.voiceRecordingUri
        if (uri != null) {
            try {
                audioRecorderManager.deleteRecording(android.net.Uri.parse(uri))
            } catch (e: Exception) {
                android.util.Log.w(TAG, "Failed to delete recording file", e)
            }
        }
        audioRecorderManager.stopPlayback()
        _uiState.update {
            it.copy(
                voiceRecordingUri = null,
                voiceRecordingDuration = 0,
                hasUnsavedChanges = true
            )
        }
    }

    /**
     * Toggle voice playback state.
     */
    fun toggleVoicePlayback() {
        val uri = _uiState.value.voiceRecordingUri ?: return
        if (_uiState.value.isPlayingVoice) {
            audioRecorderManager.togglePlaybackPause()
        } else {
            audioRecorderManager.startPlayback(android.net.Uri.parse(uri))
        }
    }

    /**
     * Stop voice playback.
     */
    fun stopVoicePlayback() {
        audioRecorderManager.stopPlayback()
    }

    /**
     * Clean up audio resources when ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        audioRecorderManager.release()
    }

    // =========================================================================
    // UNSAVED CHANGES DIALOG METHODS
    // =========================================================================

    /**
     * Show the discard changes confirmation dialog.
     */
    fun showDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = true) }
    }

    /**
     * Hide the discard changes confirmation dialog.
     */
    fun hideDiscardDialog() {
        _uiState.update { it.copy(showDiscardDialog = false) }
    }

    /**
     * Check if there are unsaved changes and handle back navigation.
     * Returns true if navigation should proceed, false if dialog should be shown.
     */
    fun handleBackNavigation(): Boolean {
        val state = _uiState.value
        return if (state.hasContent && !state.isSaved) {
            showDiscardDialog()
            false
        } else {
            true
        }
    }
}
