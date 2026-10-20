package com.kairos.app.ui.screens.futuremessage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.data.local.entity.FutureMessageEntity
import com.kairos.app.domain.repository.FutureMessageRepository
import com.kairos.app.util.ShareManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FutureMessageUiState(
    val deliveredMessages: List<FutureMessageEntity> = emptyList(),
    val pendingMessages: List<FutureMessageEntity> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val shareSuccess: Boolean = false
)

@HiltViewModel
class FutureMessageViewModel @Inject constructor(
    private val futureMessageRepository: FutureMessageRepository,
    private val shareManager: ShareManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FutureMessageUiState())
    val uiState: StateFlow<FutureMessageUiState> = _uiState.asStateFlow()

    companion object {
        private const val TAG = "FutureMessageViewModel"
    }

    init {
        loadMessages()
        viewModelScope.launch {
            while (true) {
                checkForDeliveredMessages()
                kotlinx.coroutines.delay(30_000)
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            try {
                combine(
                    futureMessageRepository.observeDeliveredMessages(),
                    futureMessageRepository.observePendingMessages(),
                    futureMessageRepository.observeUnreadCount()
                ) { delivered, pending, unread ->
                    FutureMessageUiState(
                        deliveredMessages = delivered,
                        pendingMessages = pending,
                        unreadCount = unread,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error loading messages", e)
                _uiState.update { it.copy(isLoading = false, error = "Failed to load messages. Please try again.") }
            }
        }
    }

    private fun checkForDeliveredMessages() {
        viewModelScope.launch {
            try {
                val readyMessages = futureMessageRepository
                    .getMessagesReadyForDelivery(System.currentTimeMillis())
                    .getOrDefault(emptyList())
                readyMessages.forEach { message ->
                    futureMessageRepository.markDelivered(message.id).onError { error ->
                        android.util.Log.e(
                            TAG,
                            "Error marking message as delivered: ${message.id}",
                            error.exception
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error checking for delivered messages", e)
            }
        }
    }

    fun markAsRead(messageId: Long) {
        viewModelScope.launch {
            futureMessageRepository.markRead(messageId).onError { error ->
                android.util.Log.e(TAG, "Error marking message as read: $messageId", error.exception)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun retry() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadMessages()
        checkForDeliveredMessages()
    }

    /**
     * Share a delivered future message as an image card.
     * Creates a beautiful shareable card and launches the system share sheet.
     */
    fun shareMessage(message: FutureMessageEntity) {
        viewModelScope.launch {
            try {
                val success = shareManager.shareFutureMessage(
                    title = message.title,
                    content = message.content,
                    deliveryDate = message.deliveredAt ?: message.deliveryDate,
                    isDarkTheme = true
                )
                
                if (success) {
                    _uiState.update { it.copy(shareSuccess = true) }
                } else {
                    _uiState.update { it.copy(error = "Failed to share message. Please try again.") }
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Error sharing message", e)
                _uiState.update { it.copy(error = "Failed to share message. Please try again.") }
            }
        }
    }

    /**
     * Clear share success state.
     */
    fun clearShareSuccess() {
        _uiState.update { it.copy(shareSuccess = false) }
    }
}
