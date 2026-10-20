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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(words: List<WordEntity>)

    @Query("SELECT * FROM words ORDER BY word COLLATE NOCASE")
    fun observeAll(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words")
    suspend fun all(): List<WordEntity>

    @Query("SELECT * FROM words WHERE id = :id")
    fun observe(id: String): Flow<WordEntity?>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun get(id: String): WordEntity?

    @Query("SELECT COUNT(*) FROM words")
    suspend fun count(): Int

    @Update
    suspend fun update(word: WordEntity)

    @Query("UPDATE words SET saved = :saved, savedAt = :at WHERE id = :id")
    suspend fun setSaved(id: String, saved: Boolean, at: Long?)

    @Query("SELECT * FROM words WHERE dueDay IS NOT NULL AND dueDay <= :today ORDER BY dueDay")
    suspend fun due(today: Long): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words WHERE dueDay IS NOT NULL AND dueDay <= :today")
    fun observeDueCount(today: Long): Flow<Int>

    @Query("SELECT * FROM words WHERE dueDay IS NULL AND (saved = 1 OR timesShown > 0) ORDER BY saved DESC, savedAt DESC, lastShownDay DESC LIMIT :limit")
    suspend fun freshForPractice(limit: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE dueDay IS NULL AND saved = 0 AND timesShown = 0 ORDER BY RANDOM() LIMIT :limit")
    suspend fun unseen(limit: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE partOfSpeech = :pos AND id != :exclude ORDER BY RANDOM() LIMIT :limit")
    suspend fun distractors(pos: String, exclude: String, limit: Int): List<WordEntity>

    @Query("SELECT * FROM words WHERE id != :exclude ORDER BY RANDOM() LIMIT :limit")
    suspend fun anyDistractors(exclude: String, limit: Int): List<WordEntity>
}

@Dao
interface QuoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(quotes: List<QuoteEntity>)

    @Query("SELECT * FROM quotes ORDER BY author COLLATE NOCASE")
    fun observeAll(): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes")
    suspend fun all(): List<QuoteEntity>

    @Query("SELECT * FROM quotes WHERE id = :id")
    fun observe(id: String): Flow<QuoteEntity?>

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun get(id: String): QuoteEntity?

    @Update
    suspend fun update(quote: QuoteEntity)

    @Query("UPDATE quotes SET saved = :saved, savedAt = :at WHERE id = :id")
    suspend fun setSaved(id: String, saved: Boolean, at: Long?)
}

@Dao
interface DayDao {
    @Query("SELECT * FROM days WHERE epochDay = :day")
    suspend fun get(day: Long): DayEntity?

    @Query("SELECT * FROM days WHERE epochDay = :day")
    fun observe(day: Long): Flow<DayEntity?>

    @Upsert
    suspend fun upsert(day: DayEntity)

    @Query("SELECT * FROM days WHERE epochDay BETWEEN :from AND :to")
    fun observeRange(from: Long, to: Long): Flow<List<DayEntity>>
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
interface JoyDao {
    @Insert
    suspend fun insert(drop: JoyDropEntity)

    @Query("SELECT * FROM joy_drops ORDER BY createdAt")
    fun observeAll(): Flow<List<JoyDropEntity>>

    @Query("SELECT COUNT(DISTINCT epochDay) FROM joy_drops")
    fun observeDaysShownUp(): Flow<Int>

    @Query("SELECT DISTINCT epochDay FROM joy_drops WHERE epochDay BETWEEN :from AND :to")
    fun observeActiveDays(from: Long, to: Long): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM joy_drops WHERE epochDay = :day AND source = :source")
    suspend fun countFor(day: Long, source: String): Int
}
