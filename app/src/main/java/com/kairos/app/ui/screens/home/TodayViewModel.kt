package com.kairos.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.data.auth.AuthRepository
import com.kairos.app.data.auth.AuthState
import com.kairos.app.data.auth.UserIdProvider
import com.kairos.app.domain.recommendation.ContentInteractionType
import com.kairos.app.domain.recommendation.DailyContentType
import com.kairos.app.domain.common.Result
import com.kairos.app.domain.repository.DailyPlanRepository
import com.kairos.app.domain.repository.TodayProgressRepository
import com.kairos.app.domain.repository.TodayProgressSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject

data class TodayUiState(
    val userName: String = "",
    val currentStreak: Int = 0,
    val dailyQuote: String = "",
    val dailyQuoteAuthor: String = "",
    val dailyQuoteReason: String = "",
    val wordOfTheDay: String = "",
    val wordDefinition: String = "",
    val wordPronunciation: String = "",
    val wordPartOfSpeech: String = "",
    val wordExampleSentence: String = "",
    val wordRecommendationReason: String = "",
    val journalEntriesThisWeek: Int = 0,
    val wordsLearnedThisWeek: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val wordCompletedToday: Boolean = false,
    val wordSaved: Boolean = false,
    val quoteSaved: Boolean = false
)

/**
 * Purpose-built state holder for the focused Today loop.
 *
 * It intentionally excludes the old Home dashboard's AI, social, therapeutic,
 * gamification, and analytics dependencies. Those systems no longer initialize
 * during the primary app-open path.
 */
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val dailyPlanRepository: DailyPlanRepository,
    private val todayProgressRepository: TodayProgressRepository,
    private val userIdProvider: UserIdProvider,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var currentUserId: String = "local"
    private var currentDate: LocalDate = LocalDate.now()
    private var currentWordId: Long = 0
    private var currentWordCategory: String = ""
    private var currentWordDifficulty: Int? = null
    private var currentQuoteId: Long = 0
    private var currentQuoteCategory: String = ""
    private var currentQuoteSource: String = ""
    private var wordImpressionRecorded = false
    private var quoteImpressionRecorded = false

    init {
        load()
    }

    fun retry() = load()

    /**
     * Save (or un-save) today's word. Saving is the explicit "I like this" signal
     * and also completes the daily moment, so the loop stays honest: engage with
     * the word, and the recommendation engine learns from it.
     */
    fun toggleWordSaved() {
        if (currentWordId <= 0) return
        viewModelScope.launch {
            val saving = !_uiState.value.wordSaved
            val saveResult = todayProgressRepository.setWordSaved(currentUserId, currentWordId, saving)
            if (saveResult is Result.Error) {
                _uiState.update { it.copy(error = saveResult.userMessage) }
                return@launch
            }
            dailyPlanRepository.recordInteraction(
                userId = currentUserId,
                localDate = currentDate,
                type = DailyContentType.VOCABULARY,
                contentId = currentWordId,
                interaction = if (saving) ContentInteractionType.SAVED else ContentInteractionType.UNSAVED,
                category = currentWordCategory,
                difficulty = currentWordDifficulty
            )
            _uiState.update { it.copy(wordSaved = saving) }
            if (saving) {
                when (val result = todayProgressRepository.completeWord(
                    userId = currentUserId,
                    wordId = currentWordId,
                    completedAtMillis = System.currentTimeMillis()
                )) {
                    is Result.Success -> {
                        dailyPlanRepository.recordInteraction(
                            userId = currentUserId,
                            localDate = currentDate,
                            type = DailyContentType.VOCABULARY,
                            contentId = currentWordId,
                            interaction = ContentInteractionType.COMPLETED,
                            category = currentWordCategory,
                            difficulty = currentWordDifficulty
                        )
                        _uiState.update { it.copy(wordCompletedToday = true) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = result.userMessage) }
                    }
                    Result.Loading -> Unit
                }
            }
        }
    }

    /** Save or un-save today's quote — the same like-signal for thoughts. */
    fun toggleQuoteSaved() {
        if (currentQuoteId <= 0) return
        viewModelScope.launch {
            val saving = !_uiState.value.quoteSaved
            val saveResult = todayProgressRepository.setQuoteSaved(currentUserId, currentQuoteId, saving)
            if (saveResult is Result.Error) {
                _uiState.update { it.copy(error = saveResult.userMessage) }
                return@launch
            }
            dailyPlanRepository.recordInteraction(
                userId = currentUserId,
                localDate = currentDate,
                type = DailyContentType.QUOTE,
                contentId = currentQuoteId,
                interaction = if (saving) ContentInteractionType.SAVED else ContentInteractionType.UNSAVED,
                category = currentQuoteCategory,
                sourceKey = currentQuoteSource
            )
            _uiState.update { it.copy(quoteSaved = saving) }
        }
    }

    fun onDailyContentVisible() {
        if (wordImpressionRecorded && quoteImpressionRecorded) return
        viewModelScope.launch {
            if (!wordImpressionRecorded && currentWordId > 0) {
                dailyPlanRepository.recordImpression(
                    currentUserId, currentDate, DailyContentType.VOCABULARY, currentWordId
                )
                wordImpressionRecorded = true
            }
            if (!quoteImpressionRecorded && currentQuoteId > 0) {
                dailyPlanRepository.recordImpression(
                    currentUserId, currentDate, DailyContentType.QUOTE, currentQuoteId
                )
                quoteImpressionRecorded = true
            }
        }
    }

    fun onDailyWordFeedback(interaction: ContentInteractionType) {
        if (currentWordId <= 0 || interaction !in WORD_FEEDBACK_TYPES) return
        viewModelScope.launch {
            dailyPlanRepository.recordInteraction(
                userId = currentUserId,
                localDate = currentDate,
                type = DailyContentType.VOCABULARY,
                contentId = currentWordId,
                interaction = interaction,
                category = currentWordCategory,
                difficulty = currentWordDifficulty
            )
        }
    }

    fun onDailyQuoteFeedback(interaction: ContentInteractionType) {
        if (currentQuoteId <= 0 || interaction !in QUOTE_FEEDBACK_TYPES) return
        viewModelScope.launch {
            dailyPlanRepository.recordInteraction(
                userId = currentUserId,
                localDate = currentDate,
                type = DailyContentType.QUOTE,
                contentId = currentQuoteId,
                interaction = interaction,
                category = currentQuoteCategory,
                sourceKey = currentQuoteSource
            )
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            currentUserId = userIdProvider.getUserId()
            currentDate = LocalDate.now()
            currentWordId = 0
            currentWordCategory = ""
            currentWordDifficulty = null
            currentQuoteId = 0
            currentQuoteCategory = ""
            currentQuoteSource = ""
            wordImpressionRecorded = false
            quoteImpressionRecorded = false

            val plan = runCatching {
                dailyPlanRepository.getOrCreateDailyPlan(currentUserId, currentDate)
            }.getOrElse { error ->
                _uiState.update { it.copy(isLoading = false, error = error.userMessage("Today could not be loaded.")) }
                return@launch
            }

            plan.word?.let { recommendation ->
                currentWordId = recommendation.item.id
                currentWordCategory = recommendation.item.category
                currentWordDifficulty = recommendation.item.difficulty
            }
            plan.quote?.let { recommendation ->
                currentQuoteId = recommendation.item.id
                currentQuoteCategory = recommendation.item.category
                currentQuoteSource = recommendation.item.author
            }
            // Seed saved flags from the local catalog; toggles keep them
            // authoritative afterwards. The daily plan carries domain models,
            // so the favorite state is read from the repository instead.
            _uiState.update {
                it.copy(
                    wordSaved = todayProgressRepository.isWordSaved(currentWordId),
                    quoteSaved = todayProgressRepository.isQuoteSaved(currentQuoteId)
                )
            }

            val weekStart = getWeekStartTimestamp()
            todayProgressRepository.observeProgress(
                userId = currentUserId,
                weekStartMillis = weekStart,
                nowMillis = System.currentTimeMillis()
            ).catch { error ->
                _uiState.update {
                    it.copy(isLoading = false, error = error.userMessage("Progress could not be loaded."))
                }
            }.collect { progress ->
                _uiState.value = buildState(plan, progress)
            }
        }
    }

    private fun buildState(
        plan: com.kairos.app.domain.model.DailyPlan,
        progress: TodayProgressSnapshot
    ): TodayUiState {
        val word = plan.word?.item
        val quote = plan.quote?.item
        return TodayUiState(
            userName = progress.displayName
                ?.takeIf { it.isNotBlank() && it != "Growth Seeker" }
                ?: (authRepository.authState.value as? AuthState.Authenticated)
                    ?.displayName
                    .orEmpty(),
            currentStreak = progress.currentStreak,
            dailyQuote = quote?.content.orEmpty(),
            dailyQuoteAuthor = quote?.author.orEmpty(),
            dailyQuoteReason = plan.quote?.reason.orEmpty(),
            wordOfTheDay = word?.word.orEmpty(),
            wordDefinition = word?.definition.orEmpty(),
            wordPronunciation = word?.pronunciation.orEmpty(),
            wordPartOfSpeech = word?.partOfSpeech.orEmpty(),
            wordExampleSentence = word?.exampleSentence.orEmpty(),
            wordRecommendationReason = plan.word?.reason.orEmpty(),
            journalEntriesThisWeek = progress.journalEntriesThisWeek,
            wordsLearnedThisWeek = progress.wordsLearnedThisWeek,
            isLoading = false,
            error = null,
            wordCompletedToday = plan.word?.completedAt != null || _uiState.value.wordCompletedToday,
            wordSaved = _uiState.value.wordSaved,
            quoteSaved = _uiState.value.quoteSaved
        )
    }

    private fun getWeekStartTimestamp(): Long = Calendar.getInstance().run {
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        timeInMillis
    }

    private fun Throwable.userMessage(fallback: String): String = fallback

    private companion object {
        val WORD_FEEDBACK_TYPES = setOf(
            ContentInteractionType.OPENED,
            ContentInteractionType.TOO_EASY,
            ContentInteractionType.TOO_HARD,
            ContentInteractionType.MORE_LIKE_THIS,
            ContentInteractionType.LESS_LIKE_THIS,
            ContentInteractionType.DISMISSED,
            ContentInteractionType.SAVED
        )
        val QUOTE_FEEDBACK_TYPES = setOf(
            ContentInteractionType.OPENED,
            ContentInteractionType.MORE_LIKE_THIS,
            ContentInteractionType.LESS_LIKE_THIS,
            ContentInteractionType.DISMISSED,
            ContentInteractionType.SAVED,
            ContentInteractionType.COMPLETED
        )
    }
}
