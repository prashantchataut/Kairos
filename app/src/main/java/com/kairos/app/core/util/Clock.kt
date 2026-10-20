package com.kairos.app.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

/** A clock seam so every engine can be tested without waiting for tomorrow. */
interface Clock {
    fun now(): Instant
    fun zone(): ZoneId
    fun today(): LocalDate = LocalDate.ofInstant(now(), zone())
    fun millis(): Long = now().toEpochMilli()
}

class SystemClock(private val zoneId: ZoneId = ZoneId.systemDefault()) : Clock {
    override fun now(): Instant = Instant.now()
    override fun zone(): ZoneId = zoneId
}

object KairosDates {
    private val monthDay = DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH)
    private val monthDayYear = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
    private val weekday = DateTimeFormatter.ofPattern("EEEE", Locale.ENGLISH)
    private val time = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

    fun monthDay(date: LocalDate): String = date.format(monthDay)
    fun monthDayYear(date: LocalDate): String = date.format(monthDayYear)
    fun weekdayName(date: LocalDate): String = date.format(weekday)
    fun timeOfDay(dateTime: LocalDateTime): String = dateTime.format(time)

    fun relativeDay(date: LocalDate, today: LocalDate): String {
        val days = ChronoUnit.DAYS.between(today, date)
        return when {
            days == 0L -> "today"
            days == 1L -> "tomorrow"
            days == -1L -> "yesterday"
            days > 1 -> "in $days days"
            else -> "${abs(days)} days ago"
        }
    }

    /** "3 months" — coarse, human, never "in 92 days" once it is far away. */
    fun coarseDistance(from: Instant, to: Instant): String {
        val days = ChronoUnit.DAYS.between(
            LocalDate.ofInstant(from, ZoneId.systemDefault()),
            LocalDate.ofInstant(to, ZoneId.systemDefault()),
        )
        return when {
            days < 0 -> "ready"
            days == 0L -> "today"
            days == 1L -> "tomorrow"
            days < 30 -> "$days days"
            days < 365 -> "${days / 30} months"
            else -> {
                val years = days / 365
                val months = (days % 365) / 30
                if (months == 0L) "$years ${if (years == 1L) "year" else "years"}"
                else "$years ${if (years == 1L) "year" else "years"}, $months mo"
            }
        }
    }

    fun countdown(target: Instant, now: Instant): Countdown {
        val millis = target.toEpochMilli() - now.toEpochMilli()
        if (millis <= 0) return Countdown(0, 0, 0, 0, ready = true)
        val totalSeconds = millis / 1000
        val days = totalSeconds / 86_400
        val hours = (totalSeconds % 86_400) / 3_600
        val minutes = (totalSeconds % 3_600) / 60
        val seconds = totalSeconds % 60
        return Countdown(days, hours, minutes, seconds, ready = false)
    }

    fun dayKey(date: LocalDate): String = date.toString()

    fun greeting(hour: Int): String = when {
        hour < 5 -> "Still awake"
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        hour < 22 -> "Good evening"
        else -> "Winding down"
    }
}

data class Countdown(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val ready: Boolean,
) {
    fun compact(): String = when {
        ready -> "Ready to open"
        days > 0 -> "$days ${if (days == 1L) "day" else "days"}"
        hours > 0 -> "$hours ${if (hours == 1L) "hour" else "hours"}"
        else -> "$minutes min"
    }
}
