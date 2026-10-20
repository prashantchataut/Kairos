package com.kairos.app.domain.capsule

import com.kairos.app.data.Capsule
import com.kairos.app.core.util.Countdown
import com.kairos.app.core.util.KairosDates
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Time capsules: a letter with a date on the front you are not allowed to open yet.
 *
 * The rules are deliberately simple and enforced in one place:
 *  - a capsule can be opened at or after [Capsule.unlockAt];
 *  - opening is recorded once, the first time, and never un-records;
 *  - a capsule can be deleted, but never edited after sealing — the whole point is that the
 *    person who wrote it is a different person.
 */
object CapsuleEngine {

    data class Draft(
        val title: String = "",
        val body: String = "",
        val unlockAt: Long = 0L,
        val mood: String? = null,
    )

    data class Validation(val ok: Boolean, val message: String? = null)

    const val MIN_LEAD_MS = 60 * 60 * 1000L // one hour: enough to mean "later"

    fun validate(draft: Draft, now: Long): Validation = when {
        draft.title.isBlank() && draft.body.isBlank() ->
            Validation(false, "Write something for future you first.")
        draft.unlockAt <= now + MIN_LEAD_MS ->
            Validation(false, "Choose a date at least an hour from now.")
        else -> Validation(true)
    }

    fun canOpen(capsule: Capsule, now: Long): Boolean = now >= capsule.unlockAt

    fun countdown(capsule: Capsule, now: Long): Countdown =
        KairosDates.countdown(Instant.ofEpochMilli(capsule.unlockAt), Instant.ofEpochMilli(now))

    fun isUnopened(capsule: Capsule): Boolean = capsule.openedAt == null

    fun sealAt(date: LocalDate, hour: Int, zone: ZoneId): Long =
        LocalDateTime.of(date, LocalTime.of(hour, 0)).atZone(zone).toInstant().toEpochMilli()

    /** Quick choices offered in the composer, in the user's own timezone. */
    fun presets(now: Long, zone: ZoneId): List<Preset> {
        val today = LocalDate.ofInstant(Instant.ofEpochMilli(now), zone)
        return listOf(
            Preset("tonight", "Tonight, 9 pm", sealAt(today, 21, zone).takeIf { it > now + MIN_LEAD_MS }
                ?: sealAt(today.plusDays(1), 21, zone)),
            Preset("week", "In a week", now + 7 * 86_400_000L),
            Preset("month", "In a month", now + 30 * 86_400_000L),
            Preset("season", "In three months", now + 90 * 86_400_000L),
            Preset("year", "In a year", now + 365 * 86_400_000L),
        )
    }

    data class Preset(val id: String, val label: String, val at: Long)

    /** Groups capsules into "opening soon", "sealed", "opened" for the vault. */
    data class Vault(
        val ready: List<Capsule>,
        val sealed: List<Capsule>,
        val opened: List<Capsule>,
    ) {
        val all: List<Capsule> get() = ready + sealed + opened
    }

    fun vault(capsules: List<Capsule>, now: Long): Vault {
        val sorted = capsules.sortedBy { it.unlockAt }
        return Vault(
            ready = sorted.filter { it.openedAt == null && it.unlockAt <= now },
            sealed = sorted.filter { it.openedAt == null && it.unlockAt > now },
            opened = sorted.filter { it.openedAt != null }.sortedByDescending { it.openedAt },
        )
    }

    fun nextUnlock(capsules: List<Capsule>, now: Long): Capsule? =
        capsules.filter { it.openedAt == null && it.unlockAt > now }.minByOrNull { it.unlockAt }

    val moods = listOf("hopeful", "tender", "proud", "tired", "curious", "determined")
}
