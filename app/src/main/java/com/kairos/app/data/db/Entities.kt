package com.kairos.app.data.db

import androidx.room.Embedded
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * A word the AI curator generated for this person, plus everything we learn from how they
 * react to it. Nothing here is bundled: every row was written for one person.
 */
@Entity(tableName = "words", indices = [Index("category"), Index("dueDay"), Index(value = ["word"], unique = true)])
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
    /** 1 (everyday) … 5 (rare). */
    val level: Int,
    /** One line from the curator about why this word suits the person. */
    val hook: String = "",
    val generatedAt: Long = System.currentTimeMillis(),
    val saved: Boolean = false,
    val savedAt: Long? = null,
    val timesShown: Int = 0,
    val lastShownDay: Long? = null,
    /** +1 "more like this", -1 "not for me". */
    val taste: Int = 0,
    /** -1 "too easy", +1 "too hard". */
    val difficultyVote: Int = 0,
    val reps: Int = 0,
    val ease: Double = 2.5,
    val intervalDays: Int = 0,
    val dueDay: Long? = null,
    val lapses: Int = 0,
    /** The language the word comes from when it's a borrowed concept word (Greek, Japanese…), else blank. */
    @ColumnInfo(defaultValue = "") val language: String = "",
    /** Total time spent on it in the Feed, and how often it was shared: the quiet signals. */
    @ColumnInfo(defaultValue = "0") val dwellMs: Long = 0,
    @ColumnInfo(defaultValue = "0") val shares: Int = 0,
) {
    val inPractice get() = dueDay != null
    /** Saved, liked, shared or read for a good while. */
    val engaged get() = saved || taste > 0 || shares > 0 || dwellMs >= 12_000
    val mastered get() = intervalDays >= 21
    val synonymList get() = synonyms.split(',').map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "quotes", indices = [Index("theme")])
data class QuoteEntity(
    @PrimaryKey val id: String,
    val text: String,
    val author: String,
    val theme: String,
    /** A sentence of context about the author or the line. */
    val note: String = "",
    val generatedAt: Long = System.currentTimeMillis(),
    val saved: Boolean = false,
    val savedAt: Long? = null,
    val timesShown: Int = 0,
    val lastShownDay: Long? = null,
    val taste: Int = 0,
    /** "quote" (a real person's words), "line" (written by Kai), "affirmation" (first person) or "proverb". */
    @ColumnInfo(defaultValue = "quote") val kind: String = "quote",
    /** Where a quote comes from (a book, song or film), when known. */
    @ColumnInfo(defaultValue = "") val source: String = "",
    @ColumnInfo(defaultValue = "0") val dwellMs: Long = 0,
    @ColumnInfo(defaultValue = "0") val shares: Int = 0,
) {
    /** Kai's own writing: no author to show. */
    val isLine get() = kind == "line" || kind == "affirmation"
    val engaged get() = saved || taste > 0 || shares > 0 || dwellMs >= 10_000
}

/** A micro-summary of a book worth knowing, written for one person. */
@Entity(tableName = "books", indices = [Index("category")])
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val year: Int = 0,
    /** Three or four sentences: what the book is and why it matters. */
    val summary: String,
    /** Key ideas, one per line. */
    val ideas: String,
    /** One short, memorable line from or about the book. */
    val line: String = "",
    /** Why it suits this person, addressed as "you". */
    val hook: String = "",
    val category: String = "",
    val minutes: Int = 3,
    val generatedAt: Long = System.currentTimeMillis(),
    val saved: Boolean = false,
    val savedAt: Long? = null,
    val timesShown: Int = 0,
    val lastShownDay: Long? = null,
    val taste: Int = 0,
    @ColumnInfo(defaultValue = "0") val dwellMs: Long = 0,
) {
    val ideaList get() = ideas.split('\n').map { it.trim().trimStart('-', '•', ' ') }.filter { it.isNotEmpty() }
}

/** Small generated extras: daily "tiny good things" and capsule writing prompts. */
@Entity(tableName = "extras", indices = [Index("kind")])
data class ExtraEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val usedDay: Long? = null,
)

/** What Kairos chose for one local date. Written once, so the day stays stable. */
@Entity(tableName = "days")
data class DayEntity(
    @PrimaryKey val epochDay: Long,
    val wordId: String,
    val quoteId: String,
    val spark: String = "",
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
    val tint: String,
    val reply: String = "",
)

@Entity(
    tableName = "attachments",
    foreignKeys = [ForeignKey(entity = CapsuleEntity::class, parentColumns = ["id"], childColumns = ["capsuleId"], onDelete = ForeignKey.CASCADE)],
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

/** A record of a good moment: check-ins, practice, saves, letters. Powers the activity view. */
@Entity(tableName = "moments", indices = [Index("epochDay")])
data class MomentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long,
    val epochDay: Long,
    val kind: String,
    val label: String,
)

data class CapsuleWithAttachments(
    @Embedded val capsule: CapsuleEntity,
    @Relation(parentColumn = "id", entityColumn = "capsuleId") val attachments: List<AttachmentEntity>,
)
