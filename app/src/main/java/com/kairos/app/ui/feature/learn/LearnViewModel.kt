package com.kairos.app.ui.feature.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.ai.AiError
import com.kairos.app.data.db.QuoteEntity
import com.kairos.app.data.db.WordEntity
import com.kairos.app.data.db.BookEntity
import com.kairos.app.engine.Pick
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Filter(val label: String) { All("All"), Saved("Saved"), Learning("Learning"), Remembered("Remembered") }

data class Explore(val running: Boolean = false, val topic: String = "", val message: String? = null, val freshIds: Set<String> = emptySet())

data class LearnState(
    val words: List<WordEntity> = emptyList(),
    val quotes: List<QuoteEntity> = emptyList(),
    val total: Int = 0,
    val remembered: Int = 0,
    val saved: Int = 0,
    val due: Int = 0,
    val hasKey: Boolean = false,
    val explore: Explore = Explore(),
    val books: List<BookEntity> = emptyList(),
)

/** The endless "fresh for you" strip: ranked, diversified unseen items, refilled as you go. */
data class Discover(
    val words: List<Pick<WordEntity>> = emptyList(),
    val quotes: List<Pick<QuoteEntity>> = emptyList(),
    val loading: Boolean = false,
    val writingBooks: Boolean = false,
    val bookMessage: String? = null,
)

class LearnViewModel(private val c: AppContainer) : ViewModel() {
    val query = MutableStateFlow("")
    val filter = MutableStateFlow(Filter.All)
    private val explore = MutableStateFlow(Explore())

    val state: StateFlow<LearnState> = combine(c.kairos.observeWords(), c.kairos.observeQuotes(), query, filter, explore) { words, quotes, q, f, ex ->
        val needle = q.trim().lowercase()
        // Unseen pool words stay hidden so Today keeps its surprise; explored words show at once.
        val shown = words.filter { it.taste >= 0 && (it.timesShown > 0 || it.saved || it.inPractice || it.id in ex.freshIds) }
            .filter { needle.isEmpty() || it.word.lowercase().contains(needle) || it.definition.lowercase().contains(needle) }
            .filter {
                when (f) {
                    Filter.All -> true
                    Filter.Saved -> it.saved
                    Filter.Learning -> it.inPractice && !it.mastered
                    Filter.Remembered -> it.mastered
                }
            }
            .sortedWith(compareByDescending<WordEntity> { it.id in ex.freshIds }.thenByDescending { it.lastShownDay ?: 0 }.thenByDescending { it.generatedAt })
        val qs = quotes.filter { it.taste >= 0 && (needle.isEmpty() || it.text.lowercase().contains(needle) || it.author.lowercase().contains(needle)) && (f != Filter.Saved || it.saved) }
        LearnState(shown, qs, words.size, words.count { it.mastered }, words.count { it.saved }, explore = ex)
    }.combine(c.kairos.observeDueCount()) { s, d -> s.copy(due = d) }
        .combine(c.keys.awake) { s, k -> s.copy(hasKey = k) }
        .combine(c.kairos.observeBooks()) { s, b -> s.copy(books = b) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LearnState())

    fun explore(topic: String) {
        if (topic.isBlank() || explore.value.running) return
        explore.value = Explore(running = true, topic = topic)
        viewModelScope.launch {
            try {
                val words = c.kairos.explore(topic)
                explore.value = Explore(topic = topic, message = "${words.size} new words about “$topic”", freshIds = words.map { it.id }.toSet())
                query.value = ""
                filter.value = Filter.All
            } catch (e: AiError) {
                explore.update { it.copy(running = false, message = e.message) }
            }
        }
    }

    private val _discover = MutableStateFlow(Discover())
    val discover: StateFlow<Discover> = _discover.asStateFlow()

    init { refreshDiscover() }

    fun refreshDiscover() {
        if (_discover.value.loading) return
        _discover.update { it.copy(loading = true) }
        viewModelScope.launch {
            val w = runCatching { c.kairos.discoverWords() }.getOrDefault(emptyList())
            val q = runCatching { c.kairos.discoverQuotes() }.getOrDefault(emptyList())
            _discover.update { it.copy(words = w, quotes = q, loading = false) }
        }
    }

    /** Opening a discovered item moves it into the library; when the strip runs low, write more. */
    fun seenWord(id: String) = viewModelScope.launch {
        c.kairos.markWordSeen(id)
        _discover.update { d -> d.copy(words = d.words.filterNot { it.item.id == id }) }
        if (_discover.value.words.size < 4) refreshDiscover()
    }

    fun seenQuote(id: String) = viewModelScope.launch {
        c.kairos.markQuoteSeen(id)
        _discover.update { d -> d.copy(quotes = d.quotes.filterNot { it.item.id == id }) }
        if (_discover.value.quotes.size < 4) refreshDiscover()
    }

    fun moreBooks() {
        if (_discover.value.writingBooks) return
        _discover.update { it.copy(writingBooks = true, bookMessage = null) }
        viewModelScope.launch {
            val msg = runCatching { c.kairos.moreBooks(4) }.fold({ n -> if (n == 0) "No new books this time. Try again in a moment." else null }, { (it as? AiError)?.message ?: "Couldn't write books just now." })
            _discover.update { it.copy(writingBooks = false, bookMessage = msg) }
        }
    }

    fun toggleWord(id: String) = viewModelScope.launch { c.kairos.toggleWordSaved(id) }
    fun toggleQuote(id: String) = viewModelScope.launch { c.kairos.toggleQuoteSaved(id) }
}
