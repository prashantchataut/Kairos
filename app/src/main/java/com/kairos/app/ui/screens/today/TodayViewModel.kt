package com.kairos.app.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.db.CapsuleEntity
import com.kairos.app.data.db.JoyDropEntity
import com.kairos.app.data.repo.Today
import com.kairos.app.data.repo.today
import com.kairos.app.engine.Mood
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

data class TodayState(
    val loading: Boolean = true,
    val name: String = "",
    val today: Today? = null,
    val mood: Mood? = null,
    val joy: List<JoyDropEntity> = emptyList(),
    val week: List<LocalDate> = emptyList(),
    val activeDays: Set<Long> = emptySet(),
    val nextCapsule: CapsuleEntity? = null,
    val readyCapsules: Int = 0,
    val dueWords: Int = 0,
)

class TodayViewModel(private val c: AppContainer) : ViewModel() {
    private val day = today()
    private val monday = LocalDate.ofEpochDay(day).with(DayOfWeek.MONDAY)
    private val week = (0..6).map { monday.plusDays(it.toLong()) }

    init {
        viewModelScope.launch { c.kairos.ensureDay(day) }
    }

    private val capsules = c.capsules.observeAll().map { all ->
        val now = System.currentTimeMillis()
        val sealed = all.map { it.capsule }.filter { it.openedAt == null }
        sealed.filter { it.opensAt > now }.minByOrNull { it.opensAt } to sealed.count { it.opensAt <= now }
    }

    val state: StateFlow<TodayState> = combine(
        c.kairos.observeToday(day),
        c.kairos.observeMood(day),
        c.prefs.settings,
        c.kairos.observeJoy(),
        c.kairos.observeActiveDays(week.first().toEpochDay(), week.last().toEpochDay()),
    ) { t, mood, settings, joy, active ->
        TodayState(
            loading = t == null, name = settings.name, today = t, mood = Mood.of(mood?.mood),
            joy = joy, week = week, activeDays = active.toSet(),
        )
    }.combine(capsules) { s, (next, ready) -> s.copy(nextCapsule = next, readyCapsules = ready) }
        .combine(c.kairos.observeDueCount()) { s, due -> s.copy(dueWords = due) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayState(week = week))

    fun logMood(mood: Mood) = viewModelScope.launch { c.kairos.logMood(mood) }
    fun toggleWord(id: String) = viewModelScope.launch { c.kairos.toggleWordSaved(id) }
    fun toggleQuote(id: String) = viewModelScope.launch { c.kairos.toggleQuoteSaved(id) }
    fun anotherQuote() = viewModelScope.launch { c.kairos.shuffleQuote(day) }
    fun completeSpark() = viewModelScope.launch { c.kairos.completeSpark(day) }
}
