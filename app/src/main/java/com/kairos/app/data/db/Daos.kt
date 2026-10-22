package com.kairos.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    /** Returns row ids; -1 for words that already existed (the unique index on `word`). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(words: List<WordEntity>): List<Long>

    @Query("SELECT * FROM words ORDER BY generatedAt DESC")
    fun observeAll(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words")
    suspend fun all(): List<WordEntity>

    @Query("SELECT word FROM words")
    suspend fun allWords(): List<String>

    @Query("SELECT * FROM words WHERE id = :id")
    fun observe(id: String): Flow<WordEntity?>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun get(id: String): WordEntity?

    @Query("SELECT COUNT(*) FROM words WHERE timesShown = 0 AND taste >= 0")
    suspend fun unseenCount(): Int

    @Query("SELECT COUNT(*) FROM words WHERE timesShown = 0 AND taste >= 0")
    fun observeUnseenCount(): Flow<Int>

    @Update
    suspend fun update(word: WordEntity)

    @Query("SELECT * FROM words WHERE dueDay IS NOT NULL AND dueDay <= :today ORDER BY dueDay")
    suspend fun due(today: Long): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words WHERE dueDay IS NOT NULL AND dueDay <= :today")
    fun observeDueCount(today: Long): Flow<Int>

    @Query("SELECT * FROM words WHERE dueDay IS NULL AND taste >= 0 AND (saved = 1 OR timesShown > 0) ORDER BY saved DESC, savedAt DESC, lastShownDay DESC LIMIT :limit")
    suspend fun freshForPractice(limit: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE partOfSpeech = :pos AND id != :exclude ORDER BY RANDOM() LIMIT :limit")
    suspend fun distractors(pos: String, exclude: String, limit: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE id != :exclude ORDER BY RANDOM() LIMIT :limit")
    suspend fun anyDistractors(exclude: String, limit: Int): List<WordEntity>

    /** Words already in practice, soonest due first: a free round when nothing is due yet. */
    @Query("SELECT * FROM words WHERE dueDay IS NOT NULL AND taste >= 0 ORDER BY dueDay, intervalDays LIMIT :limit")
    suspend fun upcoming(limit: Int): List<WordEntity>

    /** Anything the person has met, for a practice round when nothing else qualifies. */
    @Query("SELECT * FROM words WHERE taste >= 0 AND (saved = 1 OR timesShown > 0 OR dueDay IS NOT NULL) ORDER BY RANDOM() LIMIT :limit")
    suspend fun met(limit: Int): List<WordEntity>

    @Query("SELECT MIN(dueDay) FROM words WHERE dueDay IS NOT NULL AND dueDay > :today")
    suspend fun nextDue(today: Long): Long?

    @Query("DELETE FROM words WHERE timesShown = 0 AND saved = 0 AND dueDay IS NULL AND generatedAt < :before")
    suspend fun retireStale(before: Long): Int

    @Query("SELECT * FROM words WHERE timesShown = 0 AND taste >= 0 ORDER BY generatedAt DESC")
    suspend fun unseen(): List<WordEntity>
}

@Dao
interface QuoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(quotes: List<QuoteEntity>): List<Long>

    @Query("SELECT * FROM quotes ORDER BY generatedAt DESC")
    fun observeAll(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes")
    suspend fun all(): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE id = :id")
    fun observe(id: String): Flow<QuoteEntity?>

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun get(id: String): QuoteEntity?

    @Query("SELECT COUNT(*) FROM quotes WHERE timesShown = 0 AND taste >= 0")
    suspend fun unseenCount(): Int

    @Update
    suspend fun update(quote: QuoteEntity)

    /** Unseen, unsaved quotes older than [before]: retired so the pool keeps feeling fresh. */
    @Query("DELETE FROM quotes WHERE timesShown = 0 AND saved = 0 AND generatedAt < :before")
    suspend fun retireStale(before: Long): Int

    @Query("SELECT * FROM quotes WHERE timesShown = 0 AND taste >= 0 ORDER BY generatedAt DESC")
    suspend fun unseen(): List<QuoteEntity>
}

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(books: List<BookEntity>): List<Long>

    @Query("SELECT * FROM books WHERE taste >= 0 ORDER BY saved DESC, generatedAt DESC")
    fun observeAll(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books")
    suspend fun all(): List<BookEntity>

    @Query("SELECT * FROM books WHERE id = :id")
    fun observe(id: String): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun get(id: String): BookEntity?

    @Query("SELECT COUNT(*) FROM books WHERE timesShown = 0 AND taste >= 0")
    suspend fun unseenCount(): Int

    @Update
    suspend fun update(book: BookEntity)
}

@Dao
interface ExtraDao {
    @Insert
    suspend fun insertAll(items: List<ExtraEntity>)

    @Query("SELECT * FROM extras WHERE kind = :kind AND usedDay IS NULL ORDER BY createdAt LIMIT 1")
    suspend fun nextUnused(kind: String): ExtraEntity?

    @Query("SELECT COUNT(*) FROM extras WHERE kind = :kind AND usedDay IS NULL")
    suspend fun unusedCount(kind: String): Int

    @Query("SELECT text FROM extras WHERE kind = :kind ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recentTexts(kind: String, limit: Int): List<String>

    @Query("UPDATE extras SET usedDay = :day WHERE id = :id")
    suspend fun markUsed(id: Long, day: Long)
}

@Dao
interface DayDao {
    @Query("SELECT * FROM days WHERE epochDay = :day")
    suspend fun get(day: Long): DayEntity?

    @Query("SELECT * FROM days WHERE epochDay = :day")
    fun observe(day: Long): Flow<DayEntity?>

    @Upsert
    suspend fun upsert(day: DayEntity)
}

@Dao
interface MoodDao {
    @Insert
    suspend fun insert(mood: MoodEntity): Long

    @Query("SELECT * FROM moods WHERE epochDay = :day ORDER BY createdAt DESC LIMIT 1")
    fun observeLatestFor(day: Long): Flow<MoodEntity?>

    @Query("SELECT * FROM moods WHERE epochDay = :day ORDER BY createdAt DESC LIMIT 1")
    suspend fun latestFor(day: Long): MoodEntity?

    @Query("SELECT * FROM moods WHERE epochDay BETWEEN :from AND :to ORDER BY createdAt")
    fun observeRange(from: Long, to: Long): Flow<List<MoodEntity>>

    @Query("SELECT * FROM moods ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<MoodEntity>
}

@Dao
interface CapsuleDao {
    @Insert
    suspend fun insert(capsule: CapsuleEntity): Long

    @Insert
    suspend fun insertAttachments(items: List<AttachmentEntity>)

    @Update
    suspend fun update(capsule: CapsuleEntity)

    @Query("DELETE FROM capsules WHERE id = :id")
    suspend fun delete(id: Long)

    @Transaction
    @Query("SELECT * FROM capsules ORDER BY opensAt")
    fun observeAll(): Flow<List<CapsuleWithAttachments>>

    @Transaction
    @Query("SELECT * FROM capsules WHERE id = :id")
    fun observe(id: Long): Flow<CapsuleWithAttachments?>

    @Transaction
    @Query("SELECT * FROM capsules WHERE id = :id")
    suspend fun get(id: Long): CapsuleWithAttachments?

    @Query("SELECT * FROM capsules WHERE openedAt IS NULL")
    suspend fun sealed(): List<CapsuleEntity>
}

@Dao
interface MomentDao {
    @Insert
    suspend fun insert(m: MomentEntity)

    @Query("SELECT * FROM moments ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MomentEntity>>

    @Query("SELECT DISTINCT epochDay FROM moments WHERE epochDay BETWEEN :from AND :to")
    fun observeActiveDays(from: Long, to: Long): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM moments WHERE epochDay = :day AND kind = :kind")
    suspend fun countFor(day: Long, kind: String): Int
}
