package com.kairos.app.ui.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.AppContainer
import com.kairos.app.data.repo.FeedItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class FeedFilter(val label: String, val code: String?) { All("For you", null), Words("Words", "w"), Thoughts("Thoughts", "q"), Affirmations("Affirmations", "a"), Books("Books", "b") }

data class FeedState(val items: List<FeedItem> = emptyList(), val loading: Boolean = false, val filter: FeedFilter = FeedFilter.All, val awake: Boolean = true)

/**
 * The Feed: an endless reel. Each page you settle on is timed; when you move on, the time is
 * recorded, and every few pages the rest of the queue is re-ranked, so what comes next reflects what
 * you just lingered on, liked or shared.
 */
class FeedViewModel(private val c: AppContainer) : ViewModel() {
    private val _s = MutableStateFlow(FeedState(awake = c.keys.awake.value))
    val state: StateFlow<FeedState> = _s.asStateFlow()
    private var settledAt = 0L
    private var settledIndex = -1
    private var sinceRerank = 0

    init { more() }

    fun setFilter(f: FeedFilter) {
        if (f == _s.value.filter) return
        _s.value = FeedState(filter = f, awake = c.keys.awake.value)
        settledIndex = -1
        more()
    }

    fun more() {
        if (_s.value.loading) return
        _s.update { it.copy(loading = true) }
        viewModelScope.launch {
            val have = _s.value.items
            val next = runCatching { c.kairos.feedBatch(have.map { it.key }.toSet(), 12, _s.value.filter.code) }.getOrDefault(emptyList())
            _s.update { it.copy(items = it.items + next.filter { n -> it.items.none { o -> o.key == n.key } }, loading = false, awake = c.keys.awake.value) }
        }
    }

    /** Called when the pager comes to rest on [index]. */
    fun settle(index: Int) {
        val now = System.currentTimeMillis()
        val items = _s.value.items
        if (settledIndex in items.indices && settledIndex != index) {
            val dwell = now - settledAt
            val item = items[settledIndex]
            viewModelScope.launch { c.kairos.recordDwell(item, dwell) }
            if (++sinceRerank >= 5) { sinceRerank = 0; rerank(index) }
        }
        settledIndex = index
        settledAt = now
        if (index >= items.size - 4) more()
    }

    /** Keeps what's on screen and just ahead, and re-asks the recommender for everything after. */
    private fun rerank(index: Int) = viewModelScope.launch {
        val keep = _s.value.items.take(index + 2)
        val fresh = runCatching { c.kairos.feedBatch(keep.map { it.key }.toSet(), 12, _s.value.filter.code) }.getOrNull() ?: return@launch
        if (fresh.isNotEmpty()) _s.update { it.copy(items = keep + fresh) }
    }

    fun like(item: FeedItem) = viewModelScope.launch {
        when (item) {
            is FeedItem.Word -> c.kairos.toggleWordSaved(item.word.id)
            is FeedItem.Thought -> c.kairos.toggleQuoteSaved(item.quote.id)
            is FeedItem.Book -> c.kairos.toggleBookSaved(item.book.id)
            else -> {}
        }
        _s.update { s -> s.copy(items = s.items.map { if (it.key == item.key) item.toggled() else it }) }
    }

    fun less(item: FeedItem) = viewModelScope.launch {
        when (item) {
            is FeedItem.Word -> c.kairos.setWordTaste(item.word.id, -1)
            is FeedItem.Thought -> c.kairos.setQuoteTaste(item.quote.id, -1)
            is FeedItem.Book -> c.kairos.setBookTaste(item.book.id, -1)
            else -> {}
        }
    }

    fun shared(item: FeedItem) = viewModelScope.launch { c.kairos.recordShare(item) }
}

private fun FeedItem.toggled(): FeedItem = when (this) {
    is FeedItem.Word -> copy(word = word.copy(saved = !word.saved))
    is FeedItem.Thought -> copy(quote = quote.copy(saved = !quote.saved))
    is FeedItem.Book -> copy(book = book.copy(saved = !book.saved))
    else -> this
}

val FeedItem.liked get() = when (this) {
    is FeedItem.Word -> word.saved
    is FeedItem.Thought -> quote.saved
    is FeedItem.Book -> book.saved
    else -> false
}
