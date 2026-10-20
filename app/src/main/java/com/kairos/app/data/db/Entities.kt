package com.kairos.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A vocabulary word plus the person's relationship with it (saved, spaced-repetition state). */
@Entity(tableName = "words", indices = [Index("category"), Index("dueDay")])
data class WordEntity(
    @PrimaryKey val id: String,
    val word: String,
    val partOfSpeech: String,
    val pronunciation: String,
    val definition: String,
    val example: String,
    val origin: String,
    val synonyms: String,
    val category: String,
    val level: Int,
    val saved: Boolean = false,
    val savedAt: Long? = null,
    val timesShown: Int = 0,
    val lastShownDay: Long? = null,
    /** SM-2 state. [dueDay] is null until the word enters practice. */
    val reps: Int = 0,
    val ease: Double = 2.5,
    val intervalDays: Int = 0,
    val dueDay: Long? = null,
    val lapses: Int = 0,
) {
    val inPractice get() = dueDay != null
    val mastered get() = intervalDays >= 21
    val synonymList get() = synonyms.split(',').map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "quotes", indices = [Index("theme")])
data class QuoteEntity(
    @PrimaryKey val id: String,
    val text: String,
    val author: String,
    val theme: String,
    val saved: Boolean = false,
    val savedAt: Long? = null,
    val timesShown: Int = 0,
    val lastShownDay: Long? = null,
)

/** What Kairos chose for one local date. Written once, so the day stays stable. */
@Entity(tableName = "days")
data class DayEntity(
    @PrimaryKey val epochDay: Long,
    val wordId: String,
    val quoteId: String,
    val sparkId: String,
    val sparkDone: Boolean = false,
    val quoteShuffles: Int = 0,
    val wordReason: String = "",
    val quoteReason: String = "",
)

@Entity(tableName = "moods", indices = [Index("epochDay")])
data class MoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val mood: String,
    val note: String = "",
    val createdAt: Long,
)

@Entity(tableName = "capsules")
data class CapsuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val letter: String,
    val createdAt: Long,
    val opensAt: Long,
    val openedAt: Long? = null,
    val moodAtSeal: String? = null,
    val wash: String,
    val reply: String = "",
)

@Entity(
    tableName = "attachments",
    foreignKeys = [ForeignKey(
        entity = CapsuleEntity::class,
        parentColumns = ["id"],
        childColumns = ["capsuleId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("capsuleId")],
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val capsuleId: Long,
    val kind: String,
    /** Absolute path inside the app's private files dir. */
    val path: String,
    val displayName: String,
    val mime: String,
    val sizeBytes: Long,
)

/** One paint drop in the joy jar — a small record of something good the person did. */
@Entity(tableName = "joy_drops")
data class JoyDropEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val epochDay: Long,
    val source: String,
    val wash: String,
    val label: String,
)

data class CapsuleWithAttachments(
    @androidx.room.Embedded val capsule: CapsuleEntity,
    @androidx.room.Relation(parentColumn = "id", entityColumn = "capsuleId")
    val attachments: List<AttachmentEntity>,
)
