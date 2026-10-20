package com.kairos.app.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Spaced-repetition + taste state for one vocabulary word. */
@Entity(tableName = "word_progress")
data class WordProgress(
    @PrimaryKey val wordId: String,
    val saved: Boolean = false,
    val seen: Boolean = false,
    val repetitions: Int = 0,
    val ease: Double = 2.5,
    val intervalDays: Int = 0,
    val dueEpochDay: Long = 0,
    val lapses: Int = 0,
    val lastReviewedEpochDay: Long = -1,
)

@Entity(tableName = "quote_state")
data class QuoteState(
    @PrimaryKey val quoteId: String,
    val saved: Boolean = false,
    val seen: Boolean = false,
)

/** What Today showed on a given local date — so the day's picks never shuffle under you. */
@Entity(tableName = "daily_pick")
data class DailyPick(
    @PrimaryKey val epochDay: Long,
    val wordId: String,
    val quoteId: String,
    val joyId: String,
)

@Entity(tableName = "mood_log")
data class MoodLog(
    @PrimaryKey val epochDay: Long,
    val mood: String,
    val at: Long,
)

/** A "petal" in the joy jar: a small good thing you did. */
@Entity(tableName = "joy_log", indices = [Index("epochDay")])
data class JoyLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val joyId: String,
    val epochDay: Long,
    val at: Long,
)

@Entity(tableName = "capsule")
data class Capsule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val createdAt: Long,
    val unlockAt: Long,
    val openedAt: Long? = null,
    val moodAtWriting: String? = null,
    val paper: Int = 0,
)

@Entity(tableName = "capsule_attachment", indices = [Index("capsuleId")])
data class CapsuleAttachment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val capsuleId: Long,
    val kind: String, // image | audio | file
    val path: String,
    val name: String,
    val mime: String,
    val bytes: Long,
)

@Entity(tableName = "game_shelf")
data class ShelfEntry(
    @PrimaryKey val gameId: String,
    val addedAt: Long,
    val filePath: String? = null,
    val bytes: Long = 0,
    val lastPlayedAt: Long? = null,
)

@Entity(tableName = "game_score")
data class GameScore(
    @PrimaryKey val gameId: String,
    val best: Int = 0,
    val plays: Int = 0,
)

@Dao
interface WordDao {
    @Query("SELECT * FROM word_progress") fun observeAll(): Flow<List<WordProgress>>
    @Query("SELECT * FROM word_progress") suspend fun all(): List<WordProgress>
    @Query("SELECT * FROM word_progress WHERE wordId = :id") suspend fun get(id: String): WordProgress?
    @Query("SELECT * FROM word_progress WHERE wordId = :id") fun observe(id: String): Flow<WordProgress?>
    @Upsert suspend fun upsert(p: WordProgress)
}

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quote_state") fun observeAll(): Flow<List<QuoteState>>
    @Query("SELECT * FROM quote_state") suspend fun all(): List<QuoteState>
    @Query("SELECT * FROM quote_state WHERE quoteId = :id") suspend fun get(id: String): QuoteState?
    @Upsert suspend fun upsert(s: QuoteState)
}

@Dao
interface DayDao {
    @Query("SELECT * FROM daily_pick WHERE epochDay = :day") suspend fun pick(day: Long): DailyPick?
    @Query("SELECT * FROM daily_pick ORDER BY epochDay DESC LIMIT :n") suspend fun recentPicks(n: Int): List<DailyPick>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertPick(p: DailyPick)
    @Upsert suspend fun upsertPick(p: DailyPick)

    @Upsert suspend fun setMood(m: MoodLog)
    @Query("SELECT * FROM mood_log WHERE epochDay = :day") fun observeMood(day: Long): Flow<MoodLog?>
    @Query("SELECT * FROM mood_log WHERE epochDay BETWEEN :from AND :to") fun observeMoods(from: Long, to: Long): Flow<List<MoodLog>>

    @Insert suspend fun logJoy(j: JoyLog)
    @Query("SELECT COUNT(*) FROM joy_log") fun observeJoyCount(): Flow<Int>
    @Query("SELECT * FROM joy_log WHERE epochDay = :day") fun observeJoysOn(day: Long): Flow<List<JoyLog>>
    @Query("SELECT DISTINCT epochDay FROM joy_log UNION SELECT epochDay FROM mood_log") suspend fun activeDays(): List<Long>
}

@Dao
interface CapsuleDao {
    @Query("SELECT * FROM capsule ORDER BY unlockAt ASC") fun observeAll(): Flow<List<Capsule>>
    @Query("SELECT * FROM capsule WHERE id = :id") fun observe(id: Long): Flow<Capsule?>
    @Query("SELECT * FROM capsule WHERE id = :id") suspend fun get(id: Long): Capsule?
    @Query("SELECT * FROM capsule_attachment WHERE capsuleId = :id") fun observeAttachments(id: Long): Flow<List<CapsuleAttachment>>
    @Query("SELECT * FROM capsule_attachment WHERE capsuleId = :id") suspend fun attachments(id: Long): List<CapsuleAttachment>
    @Query("SELECT capsuleId, COUNT(*) AS n FROM capsule_attachment GROUP BY capsuleId") fun observeAttachmentCounts(): Flow<List<AttachmentCount>>
    @Insert suspend fun insert(c: Capsule): Long
    @Insert suspend fun insertAttachments(a: List<CapsuleAttachment>)
    @Query("UPDATE capsule SET openedAt = :at WHERE id = :id AND openedAt IS NULL") suspend fun markOpened(id: Long, at: Long)
    @Query("DELETE FROM capsule WHERE id = :id") suspend fun delete(id: Long)
    @Query("DELETE FROM capsule_attachment WHERE capsuleId = :id") suspend fun deleteAttachments(id: Long)
    @Query("SELECT * FROM capsule WHERE openedAt IS NULL AND unlockAt > :now") suspend fun pendingAfter(now: Long): List<Capsule>

    @Transaction
    suspend fun insertWithAttachments(c: Capsule, a: List<CapsuleAttachment>): Long {
        val id = insert(c)
        if (a.isNotEmpty()) insertAttachments(a.map { it.copy(capsuleId = id) })
        return id
    }
}

data class AttachmentCount(val capsuleId: Long, val n: Int)

@Dao
interface GameDao {
    @Query("SELECT * FROM game_shelf") fun observeShelf(): Flow<List<ShelfEntry>>
    @Query("SELECT * FROM game_shelf WHERE gameId = :id") suspend fun shelf(id: String): ShelfEntry?
    @Upsert suspend fun upsertShelf(e: ShelfEntry)
    @Query("DELETE FROM game_shelf WHERE gameId = :id") suspend fun removeShelf(id: String)
    @Query("SELECT * FROM game_score") fun observeScores(): Flow<List<GameScore>>
    @Query("SELECT * FROM game_score WHERE gameId = :id") suspend fun score(id: String): GameScore?
    @Upsert suspend fun upsertScore(s: GameScore)
}

@Database(
    entities = [
        WordProgress::class, QuoteState::class, DailyPick::class, MoodLog::class, JoyLog::class,
        Capsule::class, CapsuleAttachment::class, ShelfEntry::class, GameScore::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class KairosDatabase : RoomDatabase() {
    abstract fun words(): WordDao
    abstract fun quotes(): QuoteDao
    abstract fun days(): DayDao
    abstract fun capsules(): CapsuleDao
    abstract fun games(): GameDao

    companion object {
        fun build(context: Context): KairosDatabase =
            Room.databaseBuilder(context, KairosDatabase::class.java, "kairos2.db").build()
    }
}
