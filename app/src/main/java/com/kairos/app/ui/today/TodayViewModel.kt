package com.kairos.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.core.util.Clock
import com.kairos.app.core.util.KairosDates
import com.kairos.app.data.AppState
import com.kairos.app.data.KairosRepository
import com.kairos.app.data.Profile
import com.kairos.app.data.WordMemory
import com.kairos.app.data.catalog.Quote
import com.kairos.app.data.catalog.QuoteCatalog
import com.kairos.app.data.catalog.VocabCatalog
import com.kairos.app.data.catalog.Word
import com.kairos.app.domain.capsule.CapsuleEngine
import com.kairos.app.domain.progress.Progress
import com.kairos.app.domain.quote.QuoteEngine
import com.kairos.app.domain.vocab.SrsEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalTime

data class TodayUi(
    val profile: Profile = Profile(),
    val greeting: String = "Good morning",
    val dateLabel: String = "",
    val weekday: String = "",
    val dayNumber: Int = 1,
    val quote: Quote = QuoteCatalogFallback,
    val quoteReason: String = "",
    val quoteAlternatives: Int = 0,
    val quoteLoved: Boolean = false,
    val word: Word = VocabCatalog.all.first(),
    val wordMemory: WordMemory = WordMemory(""),
    val streak: Int = 0,
    val week: List<Progress.DayMark> = emptyList(),
    val mood: String? = null,
    val dueCount: Int = 0,
    val learnedCount: Int = 0,
    val savedCount: Int = 0,
    val readyCapsules: Int = 0,
    val nextCapsuleDays: String? = null,
    val todayLine: String = "",
)

private val QuoteCatalogFallback = QuoteCatalog.all.first()

class TodayViewModel(
    private val repository: KairosRepository,
    private val clock: Clock,
) : ViewModel() {

    private val offset = MutableStateFlow(0)

    val state: StateFlow<TodayUi> = combine(repository.state, offset) { app, index ->
        build(app, index)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = build(repository.state.value, 0),
    )

    private fun build(app: AppState, index: Int): TodayUi {
        val today = clock.today()
        val now = clock.millis()
        val taste = QuoteEngine.taste(app.profile, app.quoteSignals)
        val shortlist = QuoteEngine.shortlist(today, taste, 6)
        val selection = QuoteEngine.forDay(today, taste)
        val quote = shortlist.getOrNull(index % shortlist.size) ?: selection.quote

        val word = VocabCatalog.forThemes(app.profile.interests)
            .let { pool ->
                val seed = today.toEpochDay()
                pool.sortedBy { QuoteEngine.mix(seed + it.id.hashCode()) }.firstOrNull()
            } ?: VocabCatalog.all.first()
        val memory = app.memories[word.id] ?: WordMemory(wordId = word.id)

        val vault = CapsuleEngine.vault(app.capsules, now)
        val next = CapsuleEngine.nextUnlock(app.capsules, now)
        val todayLog = app.days[today.toString()]

        return TodayUi(
            profile = app.profile,
            greeting = KairosDates.greeting(LocalTime.now(clock.zone()).hour),
            dateLabel = KairosDates.monthDay(today),
            weekday = KairosDates.weekdayName(today),
            dayNumber = QuoteEngine.dayNumber(app.profile.joinedOn, today),
            quote = quote,
            quoteReason = if (quote.id == selection.quote.id) selection.reason else "another from the reading room",
            quoteAlternatives = shortlist.size,
            quoteLoved = app.quoteSignals.lastOrNull { it.quoteId == quote.id }?.loved == true,
            word = word,
            wordMemory = memory,
            streak = Progress.streak(app.days, today),
            week = Progress.week(app.days, today),
            mood = todayLog?.mood,
            dueCount = Progress.dueCount(app.memories, now),
            learnedCount = app.memories.values.count { SrsEngine.isLearned(it) },
            savedCount = app.memories.values.count { it.saved },
            readyCapsules = vault.ready.size,
            nextCapsuleDays = next?.let { KairosDates.coarseDistance(java.time.Instant.ofEpochMilli(now), java.time.Instant.ofEpochMilli(it.unlockAt)) },
            todayLine = line(app, today),
        )
    }

    private fun line(app: AppState, today: LocalDate): String {
        val streak = Progress.streak(app.days, today)
        val due = Progress.dueCount(app.memories, clock.millis())
        return when {
            due > 0 -> "$due ${if (due == 1) "word" else "words"} ready for a look"
            streak >= 3 -> "$streak days in a row. Quietly impressive."
            app.capsules.any { CapsuleEngine.canOpen(it, clock.millis()) && it.openedAt == null } ->
                "Something you wrote is waiting."
            else -> "Nothing is late. Take the day at your pace."
        }
    }

    fun nextQuote() {
        offset.value = offset.value + 1
    }

    fun loveQuote(loved: Boolean) {
        val quote = state.value.quote
        repository.reactToQuote(quote.id, loved, clock.today().toString())
    }

    fun logMood(mood: String) {
        repository.logMood(clock.today().toString(), mood)
    }

    fun toggleSaveWord() {
        val word = state.value.word
        repository.toggleSaved(word.id)
        repository.markMet(word.id, clock.today().toString())
    }

    fun markWordSeen() {
        val word = state.value.word
        if (state.value.wordMemory.seen == 0) {
            repository.markMet(word.id, clock.today().toString())
        }
    }
}
