package com.kairos.app.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM word_progress")
    fun observeAll(): Flow<List<WordProgressEntity>>

    @Query("SELECT * FROM word_progress")
    suspend fun all(): List<WordProgressEntity>

    @Query("SELECT * FROM word_progress WHERE wordId = :id")
    suspend fun get(id: String): WordProgressEntity?

    @Query("SELECT * FROM word_progress WHERE wordId = :id")
    fun observe(id: String): Flow<WordProgressEntity?>

    @Upsert
    suspend fun upsert(entity: WordProgressEntity)

    @Query("SELECT * FROM custom_words ORDER BY addedAt DESC")
    fun observeCustom(): Flow<List<CustomWordEntity>>

    @Query("SELECT * FROM custom_words")
    suspend fun custom(): List<CustomWordEntity>

    @Upsert
    suspend fun upsertCustom(entity: CustomWordEntity)

    @Query("DELETE FROM custom_words WHERE id = :id")
    suspend fun deleteCustom(id: String)
}

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quote_state")
    fun observeAll(): Flow<List<QuoteStateEntity>>

    @Query("SELECT * FROM quote_state")
    suspend fun all(): List<QuoteStateEntity>

    @Query("SELECT * FROM quote_state WHERE quoteId = :id")
    suspend fun get(id: String): QuoteStateEntity?

    @Upsert
    suspend fun upsert(entity: QuoteStateEntity)
}

@Dao
interface DailyDao {
    @Query("SELECT * FROM daily_picks WHERE epochDay = :day")
    suspend fun get(day: Long): DailyPickEntity?

    @Query("SELECT * FROM daily_picks WHERE epochDay = :day")
    fun observe(day: Long): Flow<DailyPickEntity?>

    @Query("SELECT * FROM daily_picks WHERE epochDay >= :from")
    suspend fun since(from: Long): List<DailyPickEntity>

    @Upsert
    suspend fun upsert(entity: DailyPickEntity)
}

@Dao
interface MoodDao {
    @Insert
    suspend fun insert(entity: MoodEntity): Long

    @Query("SELECT * FROM moods WHERE epochDay >= :from ORDER BY at ASC")
    fun observeSince(from: Long): Flow<List<MoodEntity>>

    @Query("SELECT * FROM moods WHERE epochDay = :day ORDER BY at DESC LIMIT 1")
    fun observeLatest(day: Long): Flow<MoodEntity?>

    @Query("SELECT * FROM moods WHERE epochDay = :day ORDER BY at DESC LIMIT 1")
    suspend fun latest(day: Long): MoodEntity?

    @Query("SELECT COUNT(*) FROM moods")
    fun observeCount(): Flow<Int>
}

@Dao
interface CapsuleDao {
    @Query("SELECT * FROM capsules ORDER BY unlockAt ASC")
    fun observeAll(): Flow<List<CapsuleEntity>>

    @Query("SELECT * FROM capsules WHERE id = :id")
    fun observe(id: String): Flow<CapsuleEntity?>

    @Query("SELECT * FROM capsules WHERE id = :id")
    suspend fun get(id: String): CapsuleEntity?

    @Query("SELECT * FROM capsules WHERE openedAt IS NULL")
    suspend fun sealed(): List<CapsuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CapsuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(items: List<AttachmentEntity>)

    @Transaction
    suspend fun insertWithAttachments(capsule: CapsuleEntity, items: List<AttachmentEntity>) {
        insert(capsule)
        insertAttachments(items)
    }

    @Query("UPDATE capsules SET openedAt = :at WHERE id = :id AND openedAt IS NULL")
    suspend fun markOpened(id: String, at: Long)

    @Query("SELECT * FROM capsule_attachments WHERE capsuleId = :id")
    fun observeAttachments(id: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM capsule_attachments")
    fun observeAllAttachments(): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM capsule_attachments WHERE capsuleId = :id")
    suspend fun attachments(id: String): List<AttachmentEntity>

    @Delete
    suspend fun delete(entity: CapsuleEntity)
}

@Dao
interface CritterDao {
    @Query("SELECT * FROM critters ORDER BY hatchedAt ASC")
    fun observeAll(): Flow<List<CritterEntity>>

    @Query("SELECT COUNT(*) FROM critters")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: CritterEntity)

    @Query("UPDATE critters SET isBuddy = (id = :id)")
    suspend fun setBuddy(id: String)

    @Query("UPDATE critters SET name = :name WHERE id = :id")
    suspend fun rename(id: String, name: String)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM dew_ledger")
    fun observeDew(): Flow<Int>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM dew_ledger")
    suspend fun dew(): Int

    @Query("SELECT COALESCE(SUM(amount), 0) FROM dew_ledger WHERE epochDay = :day AND reason = :reason")
    suspend fun dewFor(day: Long, reason: String): Int

    @Insert
    suspend fun addDew(entity: DewEntity)

    @Query("SELECT * FROM dew_ledger WHERE epochDay >= :from ORDER BY at DESC")
    fun observeDewSince(from: Long): Flow<List<DewEntity>>
}

@Dao
interface GameDao {
    @Query("SELECT * FROM game_installs")
    fun observeInstalls(): Flow<List<GameInstallEntity>>

    @Query("SELECT * FROM game_installs WHERE gameId = :id")
    suspend fun install(id: String): GameInstallEntity?

    @Upsert
    suspend fun upsertInstall(entity: GameInstallEntity)

    @Query("DELETE FROM game_installs WHERE gameId = :id")
    suspend fun deleteInstall(id: String)

    @Query("SELECT * FROM game_scores")
    fun observeScores(): Flow<List<GameScoreEntity>>

    @Query("SELECT * FROM game_scores WHERE gameId = :id")
    suspend fun score(id: String): GameScoreEntity?

    @Upsert
    suspend fun upsertScore(entity: GameScoreEntity)
}

@Dao
interface KindnessDao {
    @Insert
    suspend fun insert(entity: KindnessEntity)

    @Query("SELECT * FROM kindness_log WHERE epochDay >= :from")
    fun observeSince(from: Long): Flow<List<KindnessEntity>>

    @Query("SELECT COUNT(*) FROM kindness_log")
    fun observeCount(): Flow<Int>
}

@Database(
    entities = [
        WordProgressEntity::class, CustomWordEntity::class, QuoteStateEntity::class, DailyPickEntity::class,
        MoodEntity::class, CapsuleEntity::class, AttachmentEntity::class, CritterEntity::class, DewEntity::class,
        GameInstallEntity::class, GameScoreEntity::class, KindnessEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class KairosDatabase : RoomDatabase() {
    abstract fun words(): WordDao
    abstract fun quotes(): QuoteDao
    abstract fun daily(): DailyDao
    abstract fun moods(): MoodDao
    abstract fun capsules(): CapsuleDao
    abstract fun critters(): CritterDao
    abstract fun games(): GameDao
    abstract fun kindness(): KindnessDao

    companion object {
        fun build(context: Context): KairosDatabase =
            Room.databaseBuilder(context, KairosDatabase::class.java, "kairos_pond.db").build()
    }
}
