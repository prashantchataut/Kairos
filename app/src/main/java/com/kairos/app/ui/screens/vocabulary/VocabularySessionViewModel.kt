package com.kairos.app.ui.screens.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kairos.app.data.auth.UserIdProvider
import com.kairos.app.data.local.entity.VocabularyEntity
import com.kairos.app.data.local.entity.VocabularyLearningEntity
import com.kairos.app.data.local.preferences.PreferencesManager
import com.kairos.app.domain.recommendation.ContentInteractionType
import com.kairos.app.domain.recommendation.DailyContentType
import com.kairos.app.domain.recommendation.PersonalizationProfile
import com.kairos.app.domain.recommendation.PersonalizedStudyQueue
import com.kairos.app.domain.recommendation.StudyCandidate
import com.kairos.app.domain.repository.DailyPlanRepository
import com.kairos.app.domain.repository.VocabularyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SessionMode { REVIEW_DUE, NEW_WORDS, MIXED }

enum class SessionPhase { FLASHCARD, QUIZ, SUMMARY }

/** Self-graded recall quality, mapped to the SM-2 quality scale (0..5). */
enum class SessionGrade(val quality: Int, val label: String) {
    AGAIN(1, "Again"),
    HARD(3, "Hard"),
    GOOD(4, "Good"),
    EASY(5, "Easy");

    val isPassing: Boolean get() = quality >= 3
}

data class SessionCard(
    val word: VocabularyEntity,
    val revealed: Boolean = false,
    val grade: SessionGrade? = null,
    val quizCorrect: Boolean? = null
)

data class QuizOption(
    val wordId: Long,
    val definition: String,
    val isCorrect: Boolean
)

data class SessionSummary(
    val totalCards: Int,
    val remembered: Int,
    val quizAnswered: Int,
    val quizCorrect: Int,
    val secondsSpent: Long
)

data class VocabularySessionUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val mode: SessionMode = SessionMode.MIXED,
    val phase: SessionPhase = SessionPhase.FLASHCARD,
    /** Active deck: the flashcard deck first, then the quiz deck of weak cards. */
    val cards: List<SessionCard> = emptyList(),
    val currentIndex: Int = 0,
    /** Size of the original flashcard deck (kept for the summary). */
    val sessionTotal: Int = 0,
    /** Cards self-graded as passing during the flashcard phase. */
    val rememberedCount: Int = 0,
    val quizOptions: List<QuizOption> = emptyList(),
    val quizAnswered: Boolean = false,
    val sessionStartMillis: Long = System.currentTimeMillis(),
    val summary: SessionSummary? = null
) {
    val currentCard: SessionCard? get() = cards.getOrNull(currentIndex)
    val progress: Float
        get() = if (cards.isEmpty()) 0f else (currentIndex + 1).toFloat() / cards.size
}

/**
 * The hybrid vocabulary learning session: flashcard recall with self-grading,
 * followed by a definition-matching quiz for the weak cards, then a summary.
 * The queue is composed by [PersonalizedStudyQueue] — due spaced-repetition
 * reviews first, then fresh words matched to the user's profile.
 */
@HiltViewModel
class VocabularySessionViewModel @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
    private val dailyPlanRepository: DailyPlanRepository,
    private val userIdProvider: UserIdProvider,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VocabularySessionUiState())
    val uiState: StateFlow<VocabularySessionUiState> = _uiState.asStateFlow()

    private var catalog: List<VocabularyEntity> = emptyList()
    private var learning: Map<Long, VocabularyLearningEntity> = emptyMap()
    private var loadJob: Job? = null

    companion object {
        private const val MIN_SESSION_SIZE = 5
    }

    fun startSession(mode: SessionMode) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, mode = mode) }
            val words = runCatching { vocabularyRepository.getAllWords().first() }
                .getOrElse { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message ?: "Practice could not be loaded.") }
                    return@launch
                }
            val learningEntries = runCatching { vocabularyRepository.observeAllLearning().first() }
                .getOrElse { emptyList() }
            catalog = words
            learning = learningEntries.associateBy { it.wordId }
            val sessionSize = runCatching { preferencesManager.practiceSessionSize.first() }.getOrDefault(5)

            val queue = buildQueue(words, learningEntries, mode, loadProfile(), sessionSize)
            val toppedUp = if (mode == SessionMode.NEW_WORDS && queue.size < MIN_SESSION_SIZE) {
                queue + topUpWithGeneratedWords(MIN_SESSION_SIZE - queue.size, queue)
            } else {
                queue
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    cards = toppedUp,
                    sessionTotal = toppedUp.size,
                    currentIndex = 0,
                    phase = SessionPhase.FLASHCARD,
                    quizOptions = emptyList(),
                    quizAnswered = false,
                    sessionStartMillis = System.currentTimeMillis(),
                    summary = null
                )
            }
        }
    }

    fun revealCurrent() {
        _uiState.update { state ->
            val card = state.currentCard ?: return@update state
            state.copy(cards = state.cards.toMutableList().also { list ->
                list[state.currentIndex] = card.copy(revealed = true)
            })
        }
    }

    /** Save or un-save the current word — a first-class recommendation signal. */
    fun toggleSaveCurrent() {
        val state = _uiState.value
        val card = state.currentCard ?: return
        viewModelScope.launch {
            val saving = !card.word.isFavorite
            val updatedWord = card.word.copy(isFavorite = saving)
            runCatching { vocabularyRepository.updateFavoriteStatus(card.word.id, saving) }
            dailyPlanRepository.recordInteraction(
                userId = userIdProvider.getUserId(),
                localDate = java.time.LocalDate.now(),
                type = DailyContentType.VOCABULARY,
                contentId = card.word.id,
                interaction = if (saving) ContentInteractionType.SAVED else ContentInteractionType.UNSAVED,
                category = card.word.category,
                sourceKey = card.word.partOfSpeech.ifBlank { "vocabulary" },
                difficulty = card.word.difficulty
            )
            _uiState.update { current ->
                current.copy(cards = current.cards.toMutableList().also { list ->
                    list[current.currentIndex] = current.cards[current.currentIndex].copy(word = updatedWord)
                })
            }
        }
    }

    fun gradeCurrent(grade: SessionGrade) {
        val state = _uiState.value
        val card = state.currentCard ?: return
        viewModelScope.launch {
            persistGrade(card.word, grade)
            _uiState.update { current ->
                val updated = current.cards.toMutableList().also { list ->
                    list[current.currentIndex] = card.copy(revealed = true, grade = grade)
                }
                val remembered = current.rememberedCount + if (grade.isPassing) 1 else 0
                val isLast = current.currentIndex >= updated.lastIndex
                if (!isLast) {
                    current.copy(
                        cards = updated,
                        currentIndex = current.currentIndex + 1,
                        rememberedCount = remembered
                    )
                } else {
                    startQuizPhase(
                        current.copy(
                            cards = updated,
                            rememberedCount = remembered
                        )
                    )
                }
            }
        }
    }

    fun answerQuiz(option: QuizOption) {
        if (_uiState.value.quizAnswered) return
        _uiState.update { state ->
            val card = state.cards.getOrNull(state.currentIndex) ?: return@update state
            val updated = state.cards.toMutableList().also { list ->
                list[state.currentIndex] = card.copy(quizCorrect = option.isCorrect)
            }
            state.copy(
                cards = updated,
                quizAnswered = true
            )
        }
    }

    fun nextQuizQuestion() {
        _uiState.update { state ->
            val nextIndex = state.currentIndex + 1
            if (nextIndex >= state.cards.size) {
                state.copy(
                    phase = SessionPhase.SUMMARY,
                    summary = buildSummary(state)
                )
            } else {
                state.copy(
                    currentIndex = nextIndex,
                    quizAnswered = false,
                    quizOptions = buildQuizOptions(state.cards[nextIndex], state.cards)
                )
            }
        }
    }

    fun retry() = startSession(_uiState.value.mode)

    fun clearError() = _uiState.update { it.copy(error = null) }

    // ------------------------------------------------------------------ internals

    /**
     * Build the personalization profile from the user's stored preferences and
     * recorded interaction history. Falls back to a neutral profile when the
     * history is empty or unavailable — the queue still works, it just leans on
     * spaced-repetition urgency and novelty.
     */
    private suspend fun loadProfile(): PersonalizationProfile {
        return runCatching {
            val userId = userIdProvider.getUserId()
            val preferred = preferencesManager.selectedWisdomCategories.first()
                .associateWith { 1.0 }
            val signals = dailyPlanRepository.recentInteractionSignals(
                userId = userId,
                sinceMillis = System.currentTimeMillis() - PersonalizationProfile.DAY_MILLIS * 60
            )
            PersonalizationProfile.compute(
                preferredCategories = preferred,
                interactions = signals,
                now = System.currentTimeMillis()
            )
        }.getOrElse {
            PersonalizationProfile(
                categoryAffinity = emptyMap(),
                sourceAffinity = emptyMap(),
                difficultyDelta = 0,
                confidence = 0.0
            )
        }
    }

    private fun buildQueue(
        words: List<VocabularyEntity>,
        learningEntries: List<VocabularyLearningEntity>,
        mode: SessionMode,
        profile: PersonalizationProfile,
        sessionSize: Int
    ): List<SessionCard> {
        val now = System.currentTimeMillis()
        val byWordId = learningEntries.associateBy { it.wordId }

        val candidates = words.mapNotNull { word ->
            val entry = byWordId[word.id]
            val isDue = entry?.nextReviewDate != null && entry.nextReviewDate <= now
            val isNew = !word.isLearned && entry?.isIntroduced != true
            when (mode) {
                SessionMode.REVIEW_DUE -> if (!isDue) return@mapNotNull null
                // NEW_WORDS accepts any unlearned word, even one already introduced
                // (e.g. today's daily word), so small catalogs never starve a session.
                SessionMode.NEW_WORDS -> if (word.isLearned) return@mapNotNull null
                SessionMode.MIXED -> Unit
            }
            val effectiveIsNew = isNew || (mode == SessionMode.NEW_WORDS && !word.isLearned)
            StudyCandidate(
                contentId = word.id,
                category = word.category.ifBlank { "general" },
                difficulty = word.difficulty.coerceIn(1, 5),
                isNew = effectiveIsNew,
                isDueForReview = isDue,
                isMastered = entry?.isMastered == true,
                lastSeenAt = entry?.lastReviewDate ?: word.shownAt,
                reviewDueAt = entry?.nextReviewDate,
                quality = (word.masteryLevel.coerceIn(0, 100) / 100.0).coerceIn(0.0, 1.0)
            )
        }

        val newRatio = when (mode) {
            SessionMode.REVIEW_DUE -> 0.0f
            SessionMode.NEW_WORDS -> 1.0f
            SessionMode.MIXED -> 0.4f
        }
        val assignments = PersonalizedStudyQueue.build(
            candidates = candidates,
            profile = profile,
            now = now,
            maxItems = sessionSize.coerceIn(3, 20),
            newWordRatio = newRatio
        )

        val byId = words.associateBy { it.id }
        return assignments.mapNotNull { assignment ->
            byId[assignment.contentId]?.let { word -> SessionCard(word = word) }
        }
    }

    /**
     * Hybrid top-up: when the local queue is too small and AI generation is
     * available, ask the repository for generated words. Every call is guarded —
     * offline or misconfigured setups simply get a shorter queue, never an error.
     */
    private suspend fun topUpWithGeneratedWords(
        count: Int,
        existing: List<SessionCard>
    ): List<SessionCard> {
        if (count <= 0) return emptyList()
        val existingNames = existing.mapTo(hashSetOf()) { it.word.word.lowercase() }
        val added = mutableListOf<SessionCard>()
        repeat(count.coerceAtMost(3)) {
            val result = runCatching { vocabularyRepository.getAiWordOfTheDay(recentWordsLimit = 40) }.getOrNull()
            val word = result?.getOrNull() ?: return@repeat
            if (word.word.lowercase() in existingNames) return@repeat
            existingNames.add(word.word.lowercase())
            added.add(SessionCard(word = word))
        }
        return added
    }

    private fun startQuizPhase(state: VocabularySessionUiState): VocabularySessionUiState {
        val weak = state.cards.filter { it.grade != null && it.grade!!.isPassing.not() }
        if (weak.isEmpty()) {
            return state.copy(
                phase = SessionPhase.SUMMARY,
                currentIndex = 0,
                summary = buildSummary(state)
            )
        }
        // Rebuild the deck as only the weak cards and land on the first one.
        val quizDeck = weak
        return state.copy(
            cards = quizDeck,
            currentIndex = 0,
            phase = SessionPhase.QUIZ,
            quizAnswered = false,
            quizOptions = buildQuizOptions(quizDeck.first(), quizDeck)
        )
    }

    private fun buildQuizOptions(card: SessionCard, deck: List<SessionCard>): List<QuizOption> {
        val distractors = (deck + catalog.map { SessionCard(word = it) })
            .map { it.word }
            .filter { it.id != card.word.id && it.definition.isNotBlank() }
            .distinctBy { it.definition.trim().lowercase() }
            .take(3)
            .map { QuizOption(wordId = it.id, definition = it.definition, isCorrect = false) }
        return (distractors + QuizOption(wordId = card.word.id, definition = card.word.definition, isCorrect = true))
            .shuffled()
    }

    private fun buildSummary(state: VocabularySessionUiState): SessionSummary {
        val quizAnswered = state.cards.count { it.quizCorrect != null }
        val quizCorrect = state.cards.count { it.quizCorrect == true }
        val seconds = ((System.currentTimeMillis() - state.sessionStartMillis) / 1000L).coerceAtLeast(0L)
        return SessionSummary(
            totalCards = state.sessionTotal,
            remembered = state.rememberedCount,
            quizAnswered = quizAnswered,
            quizCorrect = quizCorrect,
            secondsSpent = seconds
        )
    }

    private suspend fun persistGrade(word: VocabularyEntity, grade: SessionGrade) {
        // Only spaced-repetition scheduling is written here. "Learned" is not
        // decided by a single grade — the user saves words explicitly instead.
        runCatching { vocabularyRepository.processWordReview(word.id, grade.quality) }
    }
}
