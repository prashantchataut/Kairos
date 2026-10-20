package com.kairos.app.ui.screens.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.db.QuoteEntity
import com.kairos.app.data.db.WordEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class WordFilter(val label: String) { All("All"), Saved("Saved"), Learning("Learning"), Fresh("New") }

data class LibraryState(
    val words: List<WordEntity> = emptyList(),
    val quotes: List<QuoteEntity> = emptyList(),
    val totalWords: Int = 0,
    val savedWords: Int = 0,
    val mastered: Int = 0,
    val due: Int = 0,
)

class LibraryViewModel(private val c: AppContainer) : ViewModel() {
    val query = MutableStateFlow("")
    val filter = MutableStateFlow(WordFilter.All)
    val quotesSavedOnly = MutableStateFlow(false)
    val theme = MutableStateFlow<String?>(null)

    val state: StateFlow<LibraryState> = combine(
        c.kairos.observeWords(), c.kairos.observeQuotes(), query, filter, combine(quotesSavedOnly, theme) { a, b -> a to b },
    ) { words, quotes, q, f, (savedOnly, th) ->
        val needle = q.trim().lowercase()
        val shownWords = words.filter { w ->
            (needle.isEmpty() || w.word.lowercase().contains(needle) || w.definition.lowercase().contains(needle)) &&
                when (f) {
                    WordFilter.All -> true
                    WordFilter.Saved -> w.saved
                    WordFilter.Learning -> w.inPractice
                    WordFilter.Fresh -> w.timesShown == 0 && !w.inPractice
                }
        }.let { list -> if (f == WordFilter.Saved) list.sortedByDescending { it.savedAt ?: 0 } else list }
        val shownQuotes = quotes.filter { qu ->
            (needle.isEmpty() || qu.text.lowercase().contains(needle) || qu.author.lowercase().contains(needle)) &&
                (!savedOnly || qu.saved) && (th == null || qu.theme == th)
        }
        LibraryState(
            words = shownWords, quotes = shownQuotes, totalWords = words.size,
            savedWords = words.count { it.saved }, mastered = words.count { it.mastered },
        )
    }.combine(c.kairos.observeDueCount()) { s, due -> s.copy(due = due) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryState())

    fun toggleWord(id: String) = viewModelScope.launch { c.kairos.toggleWordSaved(id) }
    fun toggleQuote(id: String) = viewModelScope.launch { c.kairos.toggleQuoteSaved(id) }
}
