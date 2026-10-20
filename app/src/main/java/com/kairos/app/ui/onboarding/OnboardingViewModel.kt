package com.kairos.app.ui.onboarding

import androidx.lifecycle.ViewModel
import com.kairos.app.core.util.Clock
import com.kairos.app.data.KairosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class OnboardingUi(
    val step: Int = 0,
    val name: String = "",
    val interests: Set<String> = emptySet(),
    val quoteThemes: Set<String> = emptySet(),
    val pace: Int = 3,
)

class OnboardingViewModel(
    private val repository: KairosRepository,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUi())
    val state: StateFlow<OnboardingUi> = _state

    fun setName(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun toggleInterest(theme: String) {
        val current = _state.value
        val next = if (theme in current.interests) current.interests - theme else current.interests + theme
        _state.value = current.copy(interests = next)
    }

    fun toggleQuoteTheme(theme: String) {
        val current = _state.value
        val next = if (theme in current.quoteThemes) current.quoteThemes - theme else current.quoteThemes + theme
        _state.value = current.copy(quoteThemes = next)
    }

    fun setPace(value: Int) {
        _state.value = _state.value.copy(pace = value.coerceIn(3, 12))
    }

    fun next() {
        _state.value = _state.value.copy(step = (_state.value.step + 1).coerceAtMost(2))
    }

    fun back() {
        _state.value = _state.value.copy(step = (_state.value.step - 1).coerceAtLeast(0))
    }

    fun finish() {
        val current = _state.value
        repository.completeOnboarding(
            name = current.name,
            interests = current.interests,
            themes = current.quoteThemes,
            pace = current.pace,
            joinedOn = clock.today().toString(),
        )
    }
}
