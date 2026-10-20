package com.kairos.app.ui.you

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.core.util.Clock
import com.kairos.app.data.KairosRepository
import com.kairos.app.data.Profile
import com.kairos.app.domain.progress.Progress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File

data class YouUi(
    val profile: Profile = Profile(),
    val stats: Progress.Stats = Progress.Stats(0, 0, 0, 0, 0, 0, 0, 0),
    val streak: Int = 0,
    val readyCapsules: Int = 0,
    val sealedCapsules: Int = 0,
)

class YouViewModel(
    private val repository: KairosRepository,
    private val clock: Clock,
) : ViewModel() {

    val state: StateFlow<YouUi> = repository.state
        .map { app ->
            YouUi(
                profile = app.profile,
                stats = Progress.stats(app),
                streak = Progress.streak(app.days, clock.today()),
                readyCapsules = app.capsules.count { it.openedAt == null && it.unlockAt <= clock.millis() },
                sealedCapsules = app.capsules.count { it.openedAt == null && it.unlockAt > clock.millis() },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = YouUi(profile = repository.state.value.profile),
        )

    fun setName(name: String) = repository.updateProfile { it.copy(name = name.trim()) }

    fun setThemeMode(mode: String) = repository.updateProfile { it.copy(themeMode = mode) }

    fun setReduceMotion(value: Boolean) = repository.updateProfile { it.copy(reduceMotion = value) }

    fun setAccent(value: String) = repository.updateProfile { it.copy(accent = value) }

    fun setPace(value: Int) = repository.updateProfile { it.copy(dailyPace = value.coerceIn(3, 12)) }

    fun toggleInterest(theme: String) = repository.updateProfile {
        val next = if (theme in it.interests) it.interests - theme else it.interests + theme
        it.copy(interests = next)
    }

    fun toggleQuoteTheme(theme: String) = repository.updateProfile {
        val next = if (theme in it.quoteThemes) it.quoteThemes - theme else it.quoteThemes + theme
        it.copy(quoteThemes = next)
    }

    fun exportTo(file: File) {
        repository.exportTo(file)
    }

    fun reset() {
        repository.resetEverything()
    }
}
