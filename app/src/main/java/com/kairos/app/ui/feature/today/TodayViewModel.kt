package com.kairos.app.ui.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.db.CapsuleEntity
import com.kairos.app.data.repo.DayStatus
import com.kairos.app.data.repo.Today
import com.kairos.app.data.repo.today
import com.kairos.app.engine.Mood
import com.kairos.app.work.RefillWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodayState(
    val status: DayStatus = DayStatus.Working,
    val name: String = "",
    val today: Today? = null,
    val mood: Mood? = null,
    val activeDays: Set<Long> = emptySet(),
    val nextCapsule: CapsuleEntity? = null,
    val readyCapsules: Int = 0,
    val due: Int = 0,
    val weekMoods: Map<Long, Mood> = emptyMap(),
    val feedback: String? = null,
)

class TodayViewModel(private val c: AppContainer) : ViewModel() {
    private val day = today()
    private val feedback = MutableStateFlow<String?>(null)

    init { refresh() }

    fun refresh() = viewModelScope.launch {
        c.kairos.ensureDay(day)
        RefillWorker.enqueue(c.appContext)
    }

    private val capsules = c.capsules.observeAll().map { all ->
        val now = System.currentTimeMillis()
        val sealed = all.map { it.capsule }.filter { it.openedAt == null }
        sealed.filter { it.opensAt > now }.minByOrNull { it.opensAt } to sealed.count { it.opensAt <= now }
    }

    val state: StateFlow<TodayState> = combine(
        c.kairos.status, c.kairos.observeToday(day), c.kairos.observeMood(day), c.prefs.settings,
        c.kairos.observeActiveDays(day - 7, day + 7),
    ) { status, t, mood, s, active ->
        TodayState(status = if (t != null) DayStatus.Ready else status, name = s.name, today = t, mood = Mood.of(mood?.mood), activeDays = active.toSet())
    }.combine(capsules) { s, (next, ready) -> s.copy(nextCapsule = next, readyCapsules = ready) }
        .combine(c.kairos.observeDueCount()) { s, due -> s.copy(due = due) }
        .combine(feedback) { s, f -> s.copy(feedback = f) }
        .combine(c.kairos.observeMoods(day - 7, day + 7)) { s, moods ->
            s.copy(weekMoods = moods.groupBy { it.epochDay }.mapNotNull { (d, v) -> Mood.of(v.maxBy { it.createdAt }.mood)?.let { d to it } }.toMap())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayState())

    fun logMood(m: Mood) = viewModelScope.launch { c.kairos.logMood(m) }
    fun toggleWord(id: String) = viewModelScope.launch { c.kairos.toggleWordSaved(id) }
    fun toggleQuote(id: String) = viewModelScope.launch { c.kairos.toggleQuoteSaved(id) }
    fun anotherQuote() = viewModelScope.launch { c.kairos.shuffleQuote(day) }
    fun completeSpark() = viewModelScope.launch { c.kairos.completeSpark(day) }

    fun wordFeedback(id: String, kind: WordFeedback) = viewModelScope.launch {
        when (kind) {
            WordFeedback.TooEasy -> c.kairos.setWordDifficulty(id, -1)
            WordFeedback.TooHard -> c.kairos.setWordDifficulty(id, 1)
            WordFeedback.More -> c.kairos.setWordTaste(id, 1)
            WordFeedback.NotForMe -> { c.kairos.setWordTaste(id, -1); c.kairos.swapWord(day) }
        }
        feedback.value = kind.reply
        RefillWorker.enqueue(c.appContext)
    }

    fun quoteFeedback(id: String, more: Boolean) = viewModelScope.launch {
        c.kairos.setQuoteTaste(id, if (more) 1 else -1)
        if (!more) c.kairos.shuffleQuote(day)
        feedback.value = if (more) "Noted. More thoughts in this spirit." else "Got it. That one won't come back."
    }

    fun clearFeedback() { feedback.value = null }
}

enum class WordFeedback(val label: String, val reply: String, val glyph: String) {
    TooEasy("Too easy", "Noted. Next time I'll reach a little further.", "↑"),
    TooHard("Too hard", "Noted. I'll pick something friendlier.", "↓"),
    More("More like this", "Lovely. More words like this one, coming up.", "♥"),
    NotForMe("Not for me", "Swapped. I won't suggest words like that again.", "✕"),
}
