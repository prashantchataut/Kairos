package com.kairos.app.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.core.util.Clock
import com.kairos.app.data.Grade
import com.kairos.app.data.KairosRepository
import com.kairos.app.data.WordMemory
import com.kairos.app.data.catalog.VocabCatalog
import com.kairos.app.data.catalog.Word
import com.kairos.app.domain.progress.Progress
import com.kairos.app.domain.vocab.SessionBuilder
import com.kairos.app.domain.vocab.SrsEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class WordFilter(val label: String) {
    All("All"),
    New("New"),
    Saved("Saved"),
    Due("Due"),
}

data class WordItem(
    val word: Word,
    val memory: WordMemory,
    val mastery: Float,
    val due: Boolean,
)

data class WordsUi(
    val query: String = "",
    val filter: WordFilter = WordFilter.All,
    val items: List<WordItem> = emptyList(),
    val dueCount: Int = 0,
    val savedCount: Int = 0,
    val learnedCount: Int = 0,
    val metCount: Int = 0,
    val total: Int = VocabCatalog.all.size,
    val pace: Int = 3,
)

data class ReviewUi(
    val cards: List<SessionBuilder.Card> = emptyList(),
    val index: Int = 0,
    val revealed: Boolean = false,
    val startedAt: Long = 0L,
    val finished: Boolean = false,
    val againCount: Int = 0,
    val newCount: Int = 0,
    val grades: Map<String, Grade> = emptyMap(),
) {
    val card: SessionBuilder.Card? get() = cards.getOrNull(index)
    val progress: Float get() = if (cards.isEmpty()) 0f else index.toFloat() / cards.size
}

class WordsViewModel(
    private val repository: KairosRepository,
    private val clock: Clock,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(WordFilter.All)
    private val _session = MutableStateFlow<ReviewUi?>(null)
    val session: StateFlow<ReviewUi?> = _session

    val state: StateFlow<WordsUi> = combine(
        repository.state,
        query,
        filter,
    ) { app, q, f ->
        val now = clock.millis()
        val all = VocabCatalog.search(q).map { word ->
            val memory = app.memories[word.id] ?: WordMemory(wordId = word.id)
            WordItem(
                word = word,
                memory = memory,
                mastery = SrsEngine.mastery(memory),
                due = SrsEngine.isDue(memory, now),
            )
        }
        val filtered = when (f) {
            WordFilter.All -> all
            WordFilter.New -> all.filter { it.memory.seen == 0 }
            WordFilter.Saved -> all.filter { it.memory.saved }
            WordFilter.Due -> all.filter { it.due }
        }.sortedWith(
            compareByDescending<WordItem> { it.due }
                .thenByDescending { it.memory.saved }
                .thenBy { it.memory.seen > 0 }
                .thenBy { it.word.term },
        )
        WordsUi(
            query = q,
            filter = f,
            items = filtered,
            dueCount = Progress.dueCount(app.memories, now),
            savedCount = app.memories.values.count { it.saved },
            learnedCount = app.memories.values.count { SrsEngine.isLearned(it) },
            metCount = app.memories.values.count { it.seen > 0 },
            pace = app.profile.dailyPace,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WordsUi(),
    )

    fun setQuery(value: String) {
        query.value = value
    }

    fun setFilter(value: WordFilter) {
        filter.value = value
    }

    fun startSession(size: Int? = null) {
        val app = repository.state.value
        val session = SessionBuilder.build(
            date = clock.today(),
            memories = app.memories,
            profile = app.profile,
            now = clock.millis(),
            size = size ?: app.profile.dailyPace.coerceIn(3, 12),
        )
        _session.value = ReviewUi(
            cards = session.cards,
            startedAt = clock.millis(),
            newCount = session.newCount,
        )
        session.cards.filter { it.isNew }.forEach {
            repository.markMet(it.word.id, clock.today().toString())
        }
    }

    fun reveal() {
        _session.value = _session.value?.copy(revealed = true)
    }

    fun grade(grade: Grade) {
        val current = _session.value ?: return
        val card = current.card ?: return
        val now = clock.millis()
        val updatedMemory = SrsEngine.review(card.memory, grade, now)
        repository.recordReview(card.word.id, updatedMemory, clock.today().toString())

        val cards = current.cards.toMutableList()
        // A missed card comes back once more in the same sitting, but never endlessly.
        val repeats = cards.count { it.word.id == card.word.id }
        if (grade == Grade.Again && repeats < 2) {
            cards.add(
                card.copy(
                    memory = updatedMemory,
                    reason = "again, while it's fresh",
                    isNew = false,
                ),
            )
        }

        val nextIndex = current.index + 1
        val finished = nextIndex >= cards.size
        if (finished) {
            repository.recordSession(
                reviewed = current.grades.size + 1,
                correct = current.grades.count { it.value != Grade.Again } +
                    if (grade != Grade.Again) 1 else 0,
                durationMillis = now - current.startedAt,
            )
        }
        _session.value = current.copy(
            cards = cards,
            index = nextIndex,
            revealed = false,
            finished = finished,
            againCount = current.againCount + if (grade == Grade.Again) 1 else 0,
            grades = current.grades + (card.word.id to grade),
        )
    }

    fun closeSession() {
        _session.value = null
    }

    fun toggleSaved(wordId: String) {
        repository.toggleSaved(wordId)
    }

    fun preview(grade: Grade): String {
        val card = _session.value?.card ?: return ""
        return SrsEngine.preview(card.memory, grade, clock.millis())
    }
}
