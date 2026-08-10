package com.kairos.app.ui.screens.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.data.auth.UserIdProvider
import com.kairos.app.data.local.entity.VocabularyEntity
import com.kairos.app.domain.recommendation.ContentInteractionType
import com.kairos.app.domain.recommendation.DailyContentType
import com.kairos.app.domain.repository.DailyPlanRepository
import com.kairos.app.domain.repository.VocabularyRepository
import com.kairos.app.ui.browse.filterVocabulary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class VocabularyFilter(val key: String, val label: String) {
    ALL("all", "All"),
    NEW("new", "New"),
    SAVED("saved", "Saved")
}

data class VocabularyListUiState(
    val words: List<VocabularyEntity> = emptyList(),
    val learnedCount: Int = 0,
    val savedCount: Int = 0,
    val totalCount: Int = 0,
    val dueReviewCount: Int = 0,
    val showFavoritesOnly: Boolean = false,
    val currentFilter: String = VocabularyFilter.ALL.key,
    val query: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

private data class VocabularyBrowseCriteria(
    val favoritesOnly: Boolean = false,
    val filter: VocabularyFilter = VocabularyFilter.ALL,
    val query: String = ""
)

@HiltViewModel
class VocabularyListViewModel @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val dailyPlanRepository: DailyPlanRepository,
    private val userIdProvider: UserIdProvider,
    private val onlineContentRefresher: com.kairos.app.data.content.OnlineContentRefresher
) : ViewModel() {

    private val criteria = MutableStateFlow(VocabularyBrowseCriteria())
    private val reloadSignal = MutableStateFlow(0)
    private val transientError = MutableStateFlow<String?>(null)

    private val allWords = reloadSignal
        .flatMapLatest { vocabularyRepository.getAllWords() }

    private val dueReviewCount = reloadSignal
        .flatMapLatest { vocabularyRepository.observeDueReviewCount() }

    val uiState: StateFlow<VocabularyListUiState> = combine(
        allWords,
        dueReviewCount,
        criteria,
        transientError
    ) { words, dueCount, browse, error ->
        val filtered = filterVocabulary(
            words = words,
            favoritesOnly = browse.favoritesOnly,
            filterKey = browse.filter.key,
            query = browse.query
        )

        VocabularyListUiState(
            words = filtered,
            learnedCount = words.count { it.isLearned },
            savedCount = words.count { it.isFavorite },
            totalCount = words.size,
            dueReviewCount = dueCount,
            showFavoritesOnly = browse.favoritesOnly,
            currentFilter = browse.filter.key,
            query = browse.query,
            isLoading = false,
            error = error
        )
    }
        .catch { error ->
            emit(
                VocabularyListUiState(
                    isLoading = false,
                    error = error.message ?: "Vocabulary could not be loaded."
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VocabularyListUiState()
        )

    fun toggleFavoritesOnly() {
        criteria.update { it.copy(favoritesOnly = !it.favoritesOnly) }
    }

    fun setFilter(filter: String) {
        val selected = VocabularyFilter.entries.firstOrNull { it.key == filter } ?: VocabularyFilter.ALL
        criteria.update { it.copy(filter = selected) }
    }

    fun setQuery(query: String) {
        criteria.update { it.copy(query = query) }
    }

    fun toggleFavorite(wordId: Long) {
        viewModelScope.launch {
            val word = vocabularyRepository.getWordById(wordId).getOrNull()
            if (word == null) {
                transientError.value = "This word is no longer available."
                return@launch
            }
            val saving = !word.isFavorite
            vocabularyRepository.updateFavoriteStatus(wordId, saving)
                .onError { transientError.value = it.userMessage }
                .onSuccess {
                    // Liking a word is a first-class recommendation signal.
                    dailyPlanRepository.recordInteraction(
                        userId = userIdProvider.getUserId(),
                        localDate = java.time.LocalDate.now(),
                        type = DailyContentType.VOCABULARY,
                        contentId = wordId,
                        interaction = if (saving) ContentInteractionType.SAVED else ContentInteractionType.UNSAVED,
                        category = word.category,
                        sourceKey = word.partOfSpeech.ifBlank { "vocabulary" },
                        difficulty = word.difficulty
                    )
                }
        }
    }

    fun clearError() {
        transientError.value = null
    }

    private val _refreshMessage = MutableStateFlow<String?>(null)
    val refreshMessage: StateFlow<String?> = _refreshMessage.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    /** Pull fresh curated content from the online provider (best-effort). */
    fun refreshOnlineContent() {
        if (_refreshing.value) return
        viewModelScope.launch {
            _refreshing.value = true
            val message = runCatching { onlineContentRefresher.refreshIfDue(force = true) }
                .fold(
                    onSuccess = { result ->
                        when {
                            result.skipped -> "Online refresh is not configured; showing the curated library"
                            result.nothingNew -> "You are up to date — no new words to add"
                            else -> "Added ${result.wordsAdded} words and ${result.quotesAdded} quotes"
                        }
                    },
                    onFailure = { "Could not refresh right now; the curated library is ready" }
                )
            reloadSignal.update { it + 1 }
            _refreshing.value = false
            _refreshMessage.value = message
        }
    }

    fun clearRefreshMessage() {
        _refreshMessage.value = null
    }

    fun retry() {
        transientError.value = null
        reloadSignal.update { it + 1 }
    }
}
