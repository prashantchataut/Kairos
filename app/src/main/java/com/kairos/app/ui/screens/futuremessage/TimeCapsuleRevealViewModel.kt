package com.kairos.app.ui.screens.futuremessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.data.auth.UserIdProvider
import com.kairos.app.data.local.entity.FutureMessageEntity
import com.kairos.app.data.local.entity.JournalEntryEntity
import com.kairos.app.domain.gamification.PointCalculator
import com.kairos.app.domain.gamification.Skill
import com.kairos.app.domain.repository.FutureMessageReplyRepository
import com.kairos.app.domain.repository.FutureMessageRepository
import com.kairos.app.domain.repository.GamificationRepository
import com.kairos.app.domain.repository.JournalRepository
import com.kairos.app.domain.repository.StreakActivityType
import com.kairos.app.notification.TimeCapsuleNotifications
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for Time Capsule Reveal screen
 */
data class TimeCapsuleRevealUiState(
    val message: FutureMessageEntity? = null,
    val isLoading: Boolean = true,
    val hasBeenRevealed: Boolean = false, // Controls envelope animation
    val isFavorite: Boolean = false,
    val hasReply: Boolean = false,
    val timeAgoText: String = "",
    val error: String? = null,
    val isSavingReply: Boolean = false
)

/**
 * ViewModel for Time Capsule Reveal experience.
 *
 * Manages the emotional, immersive reveal of a message from past self,
 * including animations, haptic feedback coordination, and reply functionality.
 */
@HiltViewModel
class TimeCapsuleRevealViewModel @Inject constructor(
    private val futureMessageRepository: FutureMessageRepository,
    private val futureMessageReplyRepository: FutureMessageReplyRepository,
    private val journalRepository: JournalRepository,
    private val gamificationRepository: GamificationRepository,
    private val userIdProvider: UserIdProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimeCapsuleRevealUiState())
    val uiState: StateFlow<TimeCapsuleRevealUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "TimeCapsuleRevealVM"
    }

    /**
     * Load the message to reveal
     */
    fun loadMessage(messageId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val message = futureMessageRepository.getMessage(messageId).getOrNull()
                if (message == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Message not found. It may have been deleted."
                        )
                    }
                    return@launch
                }

                if (message.deliveryDate > System.currentTimeMillis()) {
                    _uiState.update { it.copy(isLoading = false, message = null, error = "This letter is still sealed. Come back on its delivery date.") }
                    return@launch
                }

                // Check if there's a reply
                val hasReply = futureMessageReplyRepository.hasReplyForMessage(messageId)

                // Calculate time ago
                val timeAgo = TimeCapsuleNotifications.getDurationText(
                    message.createdAt,
                    System.currentTimeMillis()
                )

                _uiState.update {
                    it.copy(
                        message = message,
                        isLoading = false,
                        hasBeenRevealed = message.isRead, // If already read, skip animation
                        isFavorite = message.isFavorite,
                        hasReply = hasReply,
                        timeAgoText = timeAgo
                    )
                }


            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error loading message: $messageId", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load message. Please try again."
                    )
                }
            }
        }
    }

    /**
     * Trigger the reveal animation (called after initial load)
     */
    fun reveal() {
        val state = _uiState.value
        if (state.hasBeenRevealed) return
        val message = state.message ?: return
        _uiState.update { it.copy(hasBeenRevealed = true) }
        viewModelScope.launch {
            // Reading state changes only when the user deliberately opens the sealed message.
            // Navigation alone must never consume the reveal experience.
            futureMessageRepository.markRevealed(message.id).onError { error ->
                android.util.Log.w(
                    TAG,
                    "Letter revealed but read state could not be persisted",
                    error.exception
                )
            }

            runCatching {
                gamificationRepository.awardSkillXp(
                    skill = Skill.COURAGE,
                    amount = PointCalculator.Courage.OPEN_DELIVERED_MESSAGE,
                    rewardKey = "future_message_opened_${message.id}",
                    respectDailyCap = true
                )
                gamificationRepository.recordDailyActivity(StreakActivityType.OPENED_MESSAGE)
                gamificationRepository.checkAndUpdateAchievements()
            }.onFailure {
                android.util.Log.w(TAG, "Letter opened but progression could not be updated", it)
            }
        }
    }

    /**
     * Toggle favorite status
     */
    fun toggleFavorite() {
        viewModelScope.launch {
            try {
                val message = _uiState.value.message ?: return@launch
                val newFavoriteState = !_uiState.value.isFavorite

                futureMessageRepository.setFavorite(message.id, newFavoriteState)
                    .onSuccess {
                        _uiState.update { it.copy(isFavorite = newFavoriteState) }
                    }
                    .onError { error ->
                        _uiState.update { it.copy(error = error.userMessage) }
                    }

            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error toggling favorite", e)
            }
        }
    }

    /**
     * Save message to favorites (same as toggleFavorite but explicit action)
     */
    fun saveToFavorites() {
        viewModelScope.launch {
            try {
                val message = _uiState.value.message ?: return@launch
                futureMessageRepository.setFavorite(message.id, true)
                    .onSuccess {
                        _uiState.update { it.copy(isFavorite = true) }
                    }
                    .onError { error ->
                        _uiState.update { it.copy(error = error.userMessage) }
                    }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error saving to favorites", e)
            }
        }
    }

    /**
     * Create a journal entry as reply to past self
     *
     * @param reflectionText The user's reflection on reading the message
     * @return The journal entry ID if successful
     */
    suspend fun replyToPastSelf(reflectionText: String): Long? {
        return try {
            _uiState.update { it.copy(isSavingReply = true) }

            val message = _uiState.value.message ?: return null

            // Create journal entry with context
            val journalEntry = JournalEntryEntity(
                userId = userIdProvider.getUserId(),
                title = "Reply to Past Self",
                content = buildReplyContent(message, reflectionText),
                mood = "reflective",
                moodIntensity = 5,
                tags = "time_capsule,reflection,past_self",
                createdAt = System.currentTimeMillis()
            )

            val journalResult = journalRepository.saveEntry(journalEntry)
            val journalId = journalResult.getOrNull()
            if (journalId == null) {
                val messageText = (journalResult as? com.kairos.app.domain.common.Result.Error)
                    ?.userMessage
                    ?: "Kairos could not save this reflection."
                _uiState.update { it.copy(isSavingReply = false, error = messageText) }
                return null
            }

            // Linkage is secondary; the private journal entry is already safe.
            futureMessageRepository.linkReplyJournal(message.id, journalId).onError { error ->
                android.util.Log.w(TAG, "Reflection saved but letter linkage failed", error.exception)
            }

            _uiState.update {
                it.copy(
                    isSavingReply = false,
                    hasReply = true
                )
            }

            journalId
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error creating reply", e)
            _uiState.update { it.copy(isSavingReply = false) }
            null
        }
    }

    /**
     * Build the content for the reply journal entry
     */
    private fun buildReplyContent(message: FutureMessageEntity, reflectionText: String): String {
        val timeAgo = _uiState.value.timeAgoText
        val category = message.category.replaceFirstChar { it.uppercase() }

        return buildString {
            appendLine("## Message from $timeAgo ago")
            appendLine()
            appendLine("**Category:** $category")
            appendLine("**Written on:** ${java.text.SimpleDateFormat("MMMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date(message.createdAt))}")
            appendLine()
            appendLine("### What I wrote then:")
            appendLine("> ${message.content.replace("\n", "\n> ")}")
            appendLine()
            appendLine("### How this lands now:")
            appendLine(reflectionText)
        }
    }

    /**
     * Get the original message content
     */
    fun getOriginalMessage(): String {
        return _uiState.value.message?.content ?: ""
    }

    /**
     * Get message category for context
     */
    fun getCategory(): String {
        return _uiState.value.message?.category ?: "general"
    }

    /**
     * Clear any errors
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Retry loading the message
     */
    fun retry(messageId: Long) {
        loadMessage(messageId)
    }
}
