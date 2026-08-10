package com.kairos.app.data.local.dao

import androidx.room.*
import com.kairos.app.data.local.entity.VocabularyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {

    @Query("SELECT * FROM vocabulary ORDER BY word ASC")
    fun getAllVocabulary(): Flow<List<VocabularyEntity>>

    /** Promoted-surface catalog: curated roots only, no auto-derived family rows. */
    @Query(
        "SELECT * FROM vocabulary WHERE origin IS NULL OR origin NOT LIKE 'kairos:family:%' ORDER BY word ASC"
    )
    fun getAllCuratedVocabulary(): Flow<List<VocabularyEntity>>

    /** Promoted-surface catalog: curated roots only, no auto-derived family rows. */
    @Query(
        "SELECT * FROM vocabulary WHERE origin IS NULL OR origin NOT LIKE 'kairos:family:%' ORDER BY word ASC"
    )
    suspend fun getAllCuratedVocabularySync(): List<VocabularyEntity>

    @Query("SELECT * FROM vocabulary WHERE id = :id")
    suspend fun getWordById(id: Long): VocabularyEntity?

    @Query("SELECT * FROM vocabulary WHERE id = :id")
    fun observeWordById(id: Long): Flow<VocabularyEntity?>

    @Query("SELECT * FROM vocabulary WHERE word = :word LIMIT 1")
    suspend fun getWordByName(word: String): VocabularyEntity?

    @Query("SELECT * FROM vocabulary WHERE isLearned = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomUnlearnedWord(): VocabularyEntity?

    @Query("SELECT * FROM vocabulary WHERE shownAsDaily = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getWordOfTheDay(): VocabularyEntity?

    @Query("SELECT * FROM vocabulary WHERE isLearned = 1 ORDER BY learnedAt DESC")
    fun getLearnedWords(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE isFavorite = 1 ORDER BY word ASC")
    fun getFavoriteWords(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE category = :category ORDER BY word ASC")
    fun getWordsByCategory(category: String): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE difficulty = :difficulty ORDER BY word ASC")
    fun getWordsByDifficulty(difficulty: Int): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE nextReviewAt <= :currentTime AND isLearned = 1 ORDER BY nextReviewAt ASC LIMIT :limit")
    suspend fun getWordsForReview(currentTime: Long, limit: Int = 10): List<VocabularyEntity>

    @Query("SELECT * FROM vocabulary WHERE word LIKE '%' || :query || '%' OR definition LIKE '%' || :query || '%' ORDER BY word ASC")
    fun searchVocabulary(query: String): Flow<List<VocabularyEntity>>

    @Query("SELECT COUNT(*) FROM vocabulary WHERE isLearned = 1")
    fun getLearnedCount(): Flow<Int>

    /**
     * Get count of words learned within a specific time range (for weekly stats).
     * @param startTime The start timestamp (e.g., beginning of the week)
     */
    @Query("SELECT COUNT(*) FROM vocabulary WHERE isLearned = 1 AND learnedAt >= :startTime")
    fun getLearnedCountSince(startTime: Long): Flow<Int>

    /**
     * Suspend version of getLearnedCountSince for one-time queries.
     */
    @Query("SELECT COUNT(*) FROM vocabulary WHERE isLearned = 1 AND learnedAt >= :startTime")
    suspend fun getLearnedCountSinceSync(startTime: Long): Int

    @Query("SELECT COUNT(*) FROM vocabulary")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary")
    suspend fun countAll(): Int

    @Query("SELECT AVG(masteryLevel) FROM vocabulary WHERE isLearned = 1")
    fun getAverageMastery(): Flow<Float?>

    @Query("SELECT DISTINCT category FROM vocabulary")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: VocabularyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<VocabularyEntity>)

    @Update
    suspend fun updateWord(word: VocabularyEntity)

    @Delete
    suspend fun deleteWord(word: VocabularyEntity)

    @Query("UPDATE vocabulary SET isLearned = 1, learnedAt = :learnedAt WHERE id = :id")
    suspend fun markAsLearned(id: Long, learnedAt: Long = System.currentTimeMillis())

    @Query("UPDATE vocabulary SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("UPDATE vocabulary SET shownAsDaily = 1, shownAt = :shownAt WHERE id = :id")
    suspend fun markAsShownDaily(id: Long, shownAt: Long = System.currentTimeMillis())

    @Query("UPDATE vocabulary SET reviewCount = reviewCount + 1, lastReviewedAt = :reviewedAt, nextReviewAt = :nextReview, masteryLevel = :mastery WHERE id = :id")
    suspend fun updateReviewProgress(id: Long, reviewedAt: Long, nextReview: Long, mastery: Int)

    @Query("SELECT * FROM vocabulary WHERE isLearned = 1 ORDER BY masteryLevel ASC LIMIT :limit")
    suspend fun getWordsNeedingPractice(limit: Int = 5): List<VocabularyEntity>

    // Backup methods
    @Query("SELECT * FROM vocabulary WHERE isLearned = 1 OR isFavorite = 1 OR reviewCount > 0 ORDER BY word ASC")
    suspend fun getVocabularyProgressSync(): List<VocabularyEntity>

    @Query("SELECT * FROM vocabulary ORDER BY word ASC")
    suspend fun getAllVocabularySync(): List<VocabularyEntity>

    // Reset progress
    @Query("UPDATE vocabulary SET isLearned = 0, isFavorite = 0, learnedAt = NULL, lastReviewedAt = NULL, nextReviewAt = NULL, reviewCount = 0, masteryLevel = 0, shownAsDaily = 0, shownAt = NULL")
    suspend fun resetAllProgress()

    // ==================== ACTIVE PROGRESS QUERIES ====================

    /**
     * Get count of words learned today (for daily progress)
     */
    @Query("SELECT COUNT(*) FROM vocabulary WHERE isLearned = 1 AND learnedAt >= :todayStart")
    suspend fun getLearnedCountTodaySync(todayStart: Long): Int

    /**
     * Get count of words that need review (for "Next Action" suggestions)
     */
    @Query("SELECT COUNT(*) FROM vocabulary WHERE isLearned = 1 AND nextReviewAt <= :now")
    suspend fun getPendingReviewCount(now: Long = System.currentTimeMillis()): Int

    // ==================== CONTEXT BLOOM QUERIES ====================

    /**
     * Get all learned words (suspend version for Context Bloom detection)
     */
    @Query("SELECT * FROM vocabulary WHERE isLearned = 1")
    suspend fun getLearnedWordsSync(): List<VocabularyEntity>

    /**
     * Check if a word has ever been used in context (bloomed)
     */
    @Query("SELECT hasBloomedInContext FROM vocabulary WHERE id = :wordId")
    suspend fun hasBloomedInContext(wordId: Long): Boolean

    /**
     * Mark a word as bloomed in context (used naturally in writing)
     */
    @Query("UPDATE vocabulary SET hasBloomedInContext = 1, bloomedAt = :bloomedAt, bloomedInEntryId = :entryId, bloomCount = bloomCount + 1 WHERE id = :wordId")
    suspend fun markBloomedInContext(wordId: Long, entryId: Long, bloomedAt: Long = System.currentTimeMillis())

    /**
     * Get total count of context blooms across all words
     */
    @Query("SELECT SUM(bloomCount) FROM vocabulary WHERE hasBloomedInContext = 1")
    suspend fun getTotalBloomCount(): Int

    /**
     * Get count of unique words that have bloomed
     */
    @Query("SELECT COUNT(*) FROM vocabulary WHERE hasBloomedInContext = 1")
    suspend fun getUniqueWordsBloomed(): Int

    /**
     * Get words that have bloomed (for displaying in stats/achievements)
     */
    @Query("SELECT * FROM vocabulary WHERE hasBloomedInContext = 1 ORDER BY bloomedAt DESC")
    fun getBloomedWords(): Flow<List<VocabularyEntity>>
}
