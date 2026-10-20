package com.kairos.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Learning state for a catalog or custom word. Spaced repetition uses SM-2 fields. */
@Entity(tableName = "word_progress")
data class WordProgressEntity(
    @PrimaryKey val wordId: String,
    val saved: Boolean = false,
    val ease: Double = 2.5,
    val intervalDays: Int = 0,
    val reps: Int = 0,
    val lapses: Int = 0,
    val dueEpochDay: Long = 0,
    val lastReviewedAt: Long = 0,
    val timesSeen: Int = 0,
    val inDeck: Boolean = false,
)

/** Words the user looked up and adopted from the online dictionary. */
@Entity(tableName = "custom_words")
data class CustomWordEntity(
    @PrimaryKey val id: String,
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val definition: String,
    val example: String,
    val addedAt: Long,
)

@Entity(tableName = "quote_state")
data class QuoteStateEntity(
    @PrimaryKey val quoteId: String,
    val saved: Boolean = false,
    val timesShown: Int = 0,
    val lastShownDay: Long = 0,
)

/** The day's picks are frozen once chosen so the app feels stable across launches. */
@Entity(tableName = "daily_picks")
data class DailyPickEntity(
    @PrimaryKey val epochDay: Long,
    val wordId: String,
    val quoteId: String,
    val wordReason: String,
    val quoteReason: String,
    val rerolls: Int = 0,
)

@Entity(tableName = "moods", indices = [Index("epochDay")])
data class MoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val at: Long,
    val mood: Int,
    val note: String = "",
)

@Entity(tableName = "capsules")
data class CapsuleEntity(
    @PrimaryKey val id: String,
    val title: String,
    /** AES-GCM sealed letter body (Base64). Never stored in plain text. */
    val cipher: String,
    val iv: String,
    val createdAt: Long,
    val unlockAt: Long,
    val openedAt: Long? = null,
    val mood: Int = 2,
    val stamp: Int = 0,
    val prompt: String = "",
)

@Entity(
    tableName = "capsule_attachments",
    foreignKeys = [ForeignKey(entity = CapsuleEntity::class, parentColumns = ["id"], childColumns = ["capsuleId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("capsuleId")],
)
data class AttachmentEntity(
    @PrimaryKey val id: String,
    val capsuleId: String,
    val path: String,
    val mime: String,
    val name: String,
    val bytes: Long,
    val kind: String,
    val durationMs: Long = 0,
)

@Entity(tableName = "critters")
data class CritterEntity(
    @PrimaryKey val id: String,
    val seed: Long,
    val name: String,
    val hatchedAt: Long,
    val isBuddy: Boolean = false,
    val origin: String = "",
)

@Entity(tableName = "dew_ledger")
data class DewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Int,
    val reason: String,
    val at: Long,
    val epochDay: Long,
)

@Entity(tableName = "game_installs")
data class GameInstallEntity(
    @PrimaryKey val gameId: String,
    val state: String,
    val filePath: String? = null,
    val fileName: String? = null,
    val version: String? = null,
    val bytes: Long = 0,
    val playablePath: String? = null,
    val message: String? = null,
    val updatedAt: Long = 0,
)

@Entity(tableName = "game_scores")
data class GameScoreEntity(
    @PrimaryKey val gameId: String,
    val best: Int = 0,
    val plays: Int = 0,
    val lastPlayedAt: Long = 0,
)

@Entity(tableName = "kindness_log", indices = [Index("epochDay")])
data class KindnessEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityId: String,
    val epochDay: Long,
    val at: Long,
)
