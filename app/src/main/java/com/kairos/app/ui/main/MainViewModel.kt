package com.kairos.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.data.local.preferences.PreferencesManager
import com.kairos.app.ui.navigation.Screen
import com.kairos.app.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val onlineContentRefresher: com.kairos.app.data.content.OnlineContentRefresher
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainActivityUiState())
    val uiState: StateFlow<MainActivityUiState> = _uiState.asStateFlow()

    init {
        // --- CRITICAL PATH ---
        // Immediately determine the start destination to unblock the UI.
        viewModelScope.launch {
            val onboardingCompleted = preferencesManager.onboardingCompleted
                .catch {
                    // In case of error, default to showing onboarding
                    emit(false)
                }
                .first() // We only need the initial value

            val startDestination = if (onboardingCompleted) {
                Screen.Home.route
            } else {
                Screen.Onboarding.route
            }

            // Use atomic update to set initial state and dismiss splash screen
            _uiState.update {
                it.copy(
                    isLoading = false,
                    startDestination = startDestination
                )
            }
        }

        // --- ONLINE CONTENT (optional, non-blocking) ---
        // When a provider key is configured and the device is online, top up the
        // curated catalog with fresh preference-matched words and quotes. Never
        // blocks launch and never fails the app.
        viewModelScope.launch {
            runCatching { onlineContentRefresher.refreshIfDue() }
                .onFailure { android.util.Log.w("MainViewModel", "Online refresh skipped", it) }
        }

        // --- NON-CRITICAL PATH ---
        // Asynchronously load user preferences and apply them.
        viewModelScope.launch {
            combine(
                preferencesManager.themeMode.catch { emit("system") },
                preferencesManager.hapticFeedbackEnabled.catch { emit(true) },
                preferencesManager.dynamicColors.catch { emit(false) }
            ) { themeModeString, hapticEnabled, dynamicColors ->
                val themeMode = when (themeModeString.lowercase()) {
                    "light" -> ThemeMode.LIGHT
                    "dark" -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                }
                // Return a triple of the loaded preferences
                Triple(themeMode, hapticEnabled, dynamicColors)
            }.collectLatest { (themeMode, hapticEnabled, dynamicColors) ->
                // Use atomic update to apply non-critical preferences
                _uiState.update {
                    it.copy(
                        themeMode = themeMode,
                        hapticFeedbackEnabled = hapticEnabled,
                        dynamicColor = dynamicColors
                    )
                }
            }
        }
    }
}
