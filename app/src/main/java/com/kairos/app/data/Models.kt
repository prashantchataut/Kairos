package com.kairos.app.data

import kotlinx.serialization.Serializable

/**
 * Everything Kairos knows lives in one small document.
 *
 * The app is local-first and the data is tiny (a few thousand review events over years),
 * so a single atomically-written JSON file is both simpler and safer than a database here:
 * one write, one fsync, no partial migrations, and an obvious export format.
 */
@Serializable
data class AppState(
    val schema: Int = SCHEMA,
    val profile: Profile = Profile(),
    val memories: Map<String, WordMemory> = emptyMap(),
    val quoteSignals: List<QuoteSignal> = emptyList(),
    val capsules: List<Capsule> = emptyList(),
    val days: Map<String, DayLog> = emptyMap(),
    val sessionLog: List<SessionRecord> = emptyList(),
) {
    companion object {
        const val SCHEMA = 1
    }
}

@Serializable
data class Profile(
    val name: String = "",
    val onboarded: Boolean = false,
    val interests: Set<String> = emptySet(),
    val quoteThemes: Set<String> = emptySet(),
    val dailyPace: Int = 3,
    val accent: String = "Blossom",
    val themeMode: String = "system",
    val reduceMotion: Boolean = false,
    val joinedOn: String = "",
    val reminderHour: Int? = null,
) {
    val displayName: String get() = name.ifBlank { "friend" }
}

@Serializable
data class WordMemory(
    val wordId: String,
    val ease: Double = 2.5,
    val intervalDays: Int = 0,
    val repetitions: Int = 0,
    val lapses: Int = 0,
    val dueAt: Long = 0L,
    val lastReviewedAt: Long = 0L,
    val saved: Boolean = false,
    val seen: Int = 0,
    val lastGrade: String? = null,
) {
    val started: Boolean get() = seen > 0
}

@Serializable
data class QuoteSignal(
    val quoteId: String,
    val loved: Boolean,
    val at: Long,
)

@Serializable
data class Capsule(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: Long,
    val unlockAt: Long,
    val attachments: List<Attachment> = emptyList(),
    val mood: String? = null,
    val openedAt: Long? = null,
    val tint: String = "Dusk",
    val sealed: Boolean = true,
) {
    fun isLocked(now: Long): Boolean = now < unlockAt
}

@Serializable
data class Attachment(
    val relativePath: String,
    val mimeType: String,
    val displayName: String,
    val kind: String = "image",
)

@Serializable
data class DayLog(
    val day: String,
    val reviewed: Int = 0,
    val wordsMet: Int = 0,
    val quotesRead: Int = 0,
    val mood: String? = null,
    val capsulesSealed: Int = 0,
)

@Serializable
data class SessionRecord(
    val at: Long,
    val reviewed: Int,
    val correct: Int,
    val durationMillis: Long,
)

/** A review grade in SM-2 terms, with copy that belongs to this app. */
enum class Grade(val label: String) {
    Again("again"),
    Hard("hard"),
    Good("good"),
    Easy("easy"),
}
