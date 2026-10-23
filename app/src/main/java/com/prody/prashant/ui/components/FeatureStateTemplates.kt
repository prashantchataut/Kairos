package com.prody.prashant.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.prody.prashant.domain.common.Result
import com.prody.prashant.ui.common.ErrorScreen

/**
 * Unified state templates for feature screens.
 *
 * Screen implementations should use this contract so loading/empty/error states
 * feel consistent across Journal, Haven, Social, Future Messages and all other modules.
 */
sealed interface FeatureUiState {
    data object Loading : FeatureUiState
    data class Empty(
        val title: String,
        val message: String,
        val actionLabel: String? = null,
        val onAction: (() -> Unit)? = null,
        val icon: ImageVector = Icons.Outlined.Inbox
    ) : FeatureUiState

    data class Error(
        val error: Result.Error,
        val onRetry: (() -> Unit)? = null
    ) : FeatureUiState

    data object Content : FeatureUiState
}

@Composable
fun FeatureStateTemplate(
    state: FeatureUiState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    when (state) {
        FeatureUiState.Loading -> {
            ProdyFullScreenLoading(
                modifier = modifier,
                message = "Loading your space..."
            )
        }

        is FeatureUiState.Empty -> {
            if (state.actionLabel != null && state.onAction != null) {
                ProdyEmptyStateWithAction(
                    icon = state.icon,
                    title = state.title,
                    message = state.message,
                    actionLabel = state.actionLabel,
                    onActionClick = state.onAction,
                    modifier = modifier
                )
            } else {
                ProdyEmptyState(
                    icon = state.icon,
                    title = state.title,
                    message = state.message,
                    modifier = modifier
                )
            }
        }

        is FeatureUiState.Error -> {
            ErrorScreen(
                error = state.error,
                onRetry = state.onRetry,
                modifier = modifier
            )
        }

        FeatureUiState.Content -> content()
    }
}
