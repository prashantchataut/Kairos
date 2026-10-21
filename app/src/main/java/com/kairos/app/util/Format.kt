package com.kairos.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object Fmt {
    private val zone get() = ZoneId.systemDefault()
    private val long = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
    private val short = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    private val monthDay = DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault())

    fun date(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().format(long)
    fun shortDate(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().format(short)
    fun localDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    /** "October 20th" — big, friendly dates for headers. */
    fun ordinalDate(date: LocalDate): String {
        val d = date.dayOfMonth
        val suffix = if (d in 11..13) "th" else when (d % 10) { 1 -> "st"; 2 -> "nd"; 3 -> "rd"; else -> "th" }
        return date.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())) + " $d$suffix"
    }

    fun monthDay(date: LocalDate): String = date.format(monthDay)

    /** "in 3 days", "in 2 months", "tomorrow", "today". */
    fun until(millis: Long, now: Long = System.currentTimeMillis()): String {
        val from = localDate(now)
        val to = localDate(millis)
        val days = ChronoUnit.DAYS.between(from, to)
        return when {
            millis <= now -> "ready now"
            days == 0L -> "later today"
            days == 1L -> "tomorrow"
            days < 14 -> "in $days days"
            days < 60 -> "in ${days / 7} weeks"
            days < 365 * 2 -> "in ${ChronoUnit.MONTHS.between(from, to).coerceAtLeast(1)} months"
            else -> "in ${ChronoUnit.YEARS.between(from, to)} years"
        }
    }

    fun ago(millis: Long, now: Long = System.currentTimeMillis()): String {
        val days = ChronoUnit.DAYS.between(localDate(millis), localDate(now))
        return when {
            days <= 0 -> "today"
            days == 1L -> "yesterday"
            days < 14 -> "$days days ago"
            days < 60 -> "${days / 7} weeks ago"
            days < 730 -> "${ChronoUnit.MONTHS.between(localDate(millis), localDate(now)).coerceAtLeast(1)} months ago"
            else -> "${days / 365} years ago"
        }
    }

    fun greeting(hour: Int) = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Hello, night owl"
    }

    fun size(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
    }
}
