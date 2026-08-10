package com.kairos.app.data.repository

import android.util.Log
import com.kairos.app.data.ai.GeminiResult
import com.kairos.app.data.ai.GeminiService
import com.kairos.app.data.local.dao.VocabularyDao
import com.kairos.app.data.local.dao.VocabularyLearningDao
import com.kairos.app.data.local.entity.VocabularyEntity
import com.kairos.app.data.local.entity.VocabularyLearningEntity
import com.kairos.app.data.network.NetworkConnectivityManager
import com.kairos.app.domain.common.ErrorType
import com.kairos.app.domain.common.Result
import com.kairos.app.domain.common.runSuspendCatching
import com.kairos.app.domain.learning.SpacedRepetitionEngine
import com.kairos.app.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of VocabularyRepository using Room database with AI-powered vocabulary generation.
 * 
 * Features:
 * - Standard CRUD operations for vocabulary words
 * - Spaced Repetition System (SRS) for learning tracking
 * - AI-powered word of the day generation (when online)
 * - Local database fallback when offline
 * - Prevention of word repetition using recent words tracking
 */
@Singleton
class VocabularyRepositoryImpl @Inject constructor(
    private val vocabularyDao: VocabularyDao,
    private val vocabularyLearningDao: VocabularyLearningDao,
    private val spacedRepetitionEngine: SpacedRepetitionEngine,
    private val geminiService: GeminiService,
    private val networkConnectivityManager: NetworkConnectivityManager
) : VocabularyRepository {

    companion object {
        private const val TAG = "VocabularyRepository"
    }

    override fun getAllWords(): Flow<List<VocabularyEntity>> {
        // Promoted surface: curated roots only (auto-derived family rows are
        // hidden so the catalog reads as hand-picked, not dictionary spam).
        return vocabularyDao.getAllCuratedVocabulary()
    }

    override suspend fun getWordById(id: Long): Result<VocabularyEntity> {
        return runSuspendCatching(ErrorType.DATABASE, "Failed to load word") {
            vocabularyDao.getWordById(id) ?: throw NoSuchElementException("Word not found")
        }
    }

    override fun observeWordById(id: Long): Flow<VocabularyEntity?> {
        return vocabularyDao.observeWordById(id)
    }

    override suspend fun getWordOfTheDay(): Result<VocabularyEntity> {
        return runSuspendCatching(ErrorType.DATABASE, "Failed to get word of the day") {
            vocabularyDao.getWordOfTheDay()
                ?: vocabularyDao.getRandomUnlearnedWord()
                ?: throw NoSuchElementException("No words available")
        }
    }

    override fun getLearnedWords(): Flow<List<VocabularyEntity>> {
        return vocabularyDao.getLearnedWords()
    }

    override fun getFavoriteWords(): Flow<List<VocabularyEntity>> {
        return vocabularyDao.getFavoriteWords()
    }

    override suspend fun getWordsForReview(limit: Int): List<VocabularyEntity> {
        return vocabularyDao.getWordsForReview(System.currentTimeMillis(), limit)
    }

    override suspend fun getWordsNeedingPractice(limit: Int): List<VocabularyEntity> {
        return vocabularyDao.getWordsNeedingPractice(limit)
    }

    override fun getWordsByCategory(category: String): Flow<List<VocabularyEntity>> {
        return vocabularyDao.getWordsByCategory(category)
    }

    override fun getWordsByDifficulty(difficulty: Int): Flow<List<VocabularyEntity>> {
        return vocabularyDao.getWordsByDifficulty(difficulty)
    }

    override fun searchVocabulary(query: String): Flow<List<VocabularyEntity>> {
        return vocabularyDao.searchVocabulary(query)
    }

    override fun getLearnedCount(): Flow<Int> {
        return vocabularyDao.getLearnedCount()
    }

    override fun getTotalCount(): Flow<Int> {
        return vocabularyDao.getTotalCount()
    }

    override fun getAverageMastery(): Flow<Float?> {
        return vocabularyDao.getAverageMastery()
    }

    override fun getAllCategories(): Flow<List<String>> {
        return vocabularyDao.getAllCategories()
    }

    override suspend fun insertWord(word: VocabularyEntity): Result<Long> {
        return runSuspendCatching(ErrorType.DATABASE, "Failed to save word") {
            vocabularyDao.insertWord(word)
        }
    }

    override suspend fun insertWords(words: List<VocabularyEntity>): Result<Unit> {
        return runSuspendCatching(ErrorType.DATABASE, "Failed to save words") {
            vocabularyDao.insertWords(words)
        }
    }

    override suspend fun updateWord(word: VocabularyEntity): Result<Unit> {
        return runSuspendCatching(ErrorType.DATABASE, "Failed to update word") {
            vocabularyDao.updateWord(word)
        }
    }

    override suspend fun deleteWord(word: VocabularyEntity): Result<Unit> {
        return runSuspendCatching(ErrorType.DATABASE, "Failed to delete word") {
            vocabularyDao.deleteWord(word)
        }
    }

    override suspend fun markAsLearned(id: Long): Result<Unit> {
        return runSuspendCatching(ErrorType.DATABASE, "Failed to mark word as learned") {
            vocabularyDao.markAsLearned(id)
        }
    }

    override suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean): Result<Unit> {
        return runSuspendCatching(ErrorType.DATABASE, "Failed to update favorite status") {
            vocabularyDao.updateFavoriteStatus(id, isFavorite)
        }
    }

    override suspend fun markAsShownDaily(id: Long): Result<Unit> {
        return runSuspendCatching(ErrorType.DATABASE, "Failed to mark word as shown") {
            vocabularyDao.markAsShownDaily(id)
        }
    }

    override suspend fun updateReviewProgress(
        id: Long,
        nextReviewAt: Long,
        masteryLevel: Int
    ): Result<Unit> {
        return runSuspendCatching(ErrorType.DATABASE, "Failed to update review progress") {
            vocabularyDao.updateReviewProgress(
                id = id,
                reviewedAt = System.currentTimeMillis(),
                nextReview = nextReviewAt,
                mastery = masteryLevel
            )
        }
    }

    override suspend fun processWordReview(wordId: Long, quality: Int): Result<Unit> {
        return runSuspendCatching(ErrorType.DATABASE, "Failed to process review") {
            // 1. Get current learning data or create new if not exists
            var currentData = vocabularyLearningDao.getLearningForWord(wordId)
            
            if (currentData == null) {
                currentData = spacedRepetitionEngine.createInitialLearningEntity(wordId)
            }

            // 2. Calculate new state using SRS Engine
            val reviewResult = spacedRepetitionEngine.calculateNextReview(quality, currentData)

            // 3. Update learning entity with new values
            val updatedData = currentData.copy(
                easeFactor = reviewResult.newEaseFactor,
                interval = reviewResult.newInterval,
                repetitions = reviewResult.newRepetitions,
                nextReviewDate = reviewResult.nextReviewDate,
                boxLevel = reviewResult.newBoxLevel,
                stage = reviewResult.newStage.name,
                correctStreak = reviewResult.newCorrectStreak,
                lastReviewDate = System.currentTimeMillis(),
                totalReviews = currentData.totalReviews + 1,
                correctReviews = if (quality >= 3) currentData.correctReviews + 1 else currentData.correctReviews,
                masteredDate = if (reviewResult.isMastered && currentData.masteredDate == null) System.currentTimeMillis() else currentData.masteredDate
            )

            // 4. Save to database
            vocabularyLearningDao.insertLearning(updatedData)

            // 5. Also update the simpler VocabularyEntity stats for backward compatibility/UI
            vocabularyDao.updateReviewProgress(
                id = wordId,
                reviewedAt = System.currentTimeMillis(),
                nextReview = reviewResult.nextReviewDate,
                mastery = reviewResult.newBoxLevel
            )
            
            // 6. If mastered, ensure isLearned is set
            if (reviewResult.isMastered) {
                vocabularyDao.markAsLearned(wordId)
            }
        }
    }

    /**
     * Get AI-generated word of the day.
     * 
     * Strategy:
     * 1. If online and AI is configured, generate a unique word via Gemini AI
     * 2. Save the AI-generated word to local database for SRS tracking
     * 3. Fall back to local database if AI is unavailable or fails
     * 
     * The AI is prompted to avoid recently shown words to prevent repetition.
     */
    override suspend fun getAiWordOfTheDay(recentWordsLimit: Int): Result<VocabularyEntity> {
        return runSuspendCatching(ErrorType.NETWORK, "Failed to get AI word of the day") {
            // Check if we're online and AI is configured
            val isOnline = networkConnectivityManager.isOnline
            val isAiConfigured = geminiService.isConfigured()

            if (isOnline && isAiConfigured) {
                // Get recent words to exclude from AI generation
                val recentWords = getRecentlyShownWords(recentWordsLimit)
                
                
                // Request AI-generated word
                val aiResult = geminiService.generateVocabularyWord(recentWords)
                
                when (aiResult) {
                    is GeminiResult.Success -> {
                        val wordEntity = parseAiVocabularyResponse(aiResult.data)
                        if (wordEntity != null) {
                            // Check if word already exists in database
                            val existingWord = vocabularyDao.getWordByName(wordEntity.word)
                            if (existingWord != null) {
                                // Mark as shown and return existing
                                vocabularyDao.markAsShownDaily(existingWord.id)
                                return@runSuspendCatching existingWord
                            }
                            
                            // Insert new AI-generated word
                            val newId = vocabularyDao.insertWord(wordEntity)
                            vocabularyDao.markAsShownDaily(newId)
                            
                            return@runSuspendCatching wordEntity.copy(id = newId, shownAsDaily = true)
                        } else {
                            Log.w(TAG, "Failed to parse AI response, falling back to local")
                        }
                    }
                    is GeminiResult.Error -> {
                        Log.w(TAG, "AI generation failed: ${aiResult.message}, falling back to local")
                    }
                    is GeminiResult.ApiKeyNotSet -> {
                    }
                    is GeminiResult.Loading -> {
                        // Shouldn't happen for non-streaming calls
                    }
                }
            } else {
            }
            
            // Fallback: Use local database
            val localWord = vocabularyDao.getWordOfTheDay()
                ?: vocabularyDao.getRandomUnlearnedWord()
                ?: throw NoSuchElementException("No words available")
            
            vocabularyDao.markAsShownDaily(localWord.id)
            localWord
        }
    }

    /**
     * Get list of recently shown words for AI exclusion.
     */
    override suspend fun getRecentlyShownWords(limit: Int): List<String> {
        return try {
            // Get all words that have been shown as daily, ordered by most recent
            val allWords = vocabularyDao.getAllVocabularySync()
            allWords
                .filter { it.shownAsDaily && it.shownAt != null }
                .sortedByDescending { it.shownAt }
                .take(limit)
                .map { it.word }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent words", e)
            emptyList()
        }
    }

    override fun observeAllLearning(): Flow<List<VocabularyLearningEntity>> {
        return vocabularyLearningDao.getAllLearningEntries()
    }

    override fun observeDueReviewCount(): Flow<Int> {
        return vocabularyLearningDao.getDueReviewCount()
    }

    override suspend fun getDueReviewCountSync(): Int {
        return vocabularyLearningDao.getDueReviewCountSync()
    }

    /**
     * Parse AI-generated vocabulary JSON response into a VocabularyEntity.
     * 
     * Expected JSON format:
     * {
     *   "word": "resilience",
     *   "partOfSpeech": "noun",
     *   "pronunciation": "ri-ZIL-yuhns",
     *   "definition": "The capacity to recover quickly from difficulties",
     *   "example": "Her resilience helped her overcome challenges.",
     *   "category": "self-improvement",
     *   "difficulty": 2
     * }
     */
    private fun parseAiVocabularyResponse(jsonString: String): VocabularyEntity? {
        return try {
            val json = JSONObject(jsonString)
            
            VocabularyEntity(
                word = json.getString("word"),
                partOfSpeech = json.optString("partOfSpeech", "noun"),
                pronunciation = json.optString("pronunciation", ""),
                definition = json.getString("definition"),
                exampleSentence = json.optString("example", ""),
                category = json.optString("category", "ai-generated"),
                difficulty = json.optInt("difficulty", 2),
                // Mark as AI-generated for tracking
                origin = "ai-generated",
                shownAsDaily = true,
                shownAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse AI vocabulary response: $jsonString", e)
            null
        }
    }
}
