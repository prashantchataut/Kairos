package com.kairos.app

import com.kairos.app.data.DayLog
import com.kairos.app.domain.progress.Progress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProgressTest {

    private val today = LocalDate.of(2026, 10, 20)

    @Test
    fun `streak counts back from today`() {
        val days = mapOf(
            today.toString() to DayLog(today.toString(), reviewed = 2),
            today.minusDays(1).toString() to DayLog(today.minusDays(1).toString(), mood = "good"),
            today.minusDays(2).toString() to DayLog(today.minusDays(2).toString(), quotesRead = 1),
        )
        assertEquals(3, Progress.streak(days, today))
    }

    @Test
    fun `a quiet today does not break yesterday's thread`() {
        val days = mapOf(
            today.minusDays(1).toString() to DayLog(today.minusDays(1).toString(), reviewed = 1),
            today.minusDays(2).toString() to DayLog(today.minusDays(2).toString(), reviewed = 1),
        )
        assertEquals(2, Progress.streak(days, today))
    }

    @Test
    fun `a gap breaks the streak`() {
        val days = mapOf(
            today.toString() to DayLog(today.toString(), reviewed = 1),
            today.minusDays(2).toString() to DayLog(today.minusDays(2).toString(), reviewed = 1),
        )
        assertEquals(1, Progress.streak(days, today))
        assertEquals(0, Progress.streak(emptyMap(), today))
    }

    @Test
    fun `best streak survives old runs`() {
        val days = (0..4).associate { offset ->
            val date = today.minusDays(offset.toLong())
            date.toString() to DayLog(date.toString(), reviewed = 1)
        } + (10..15).associate { offset ->
            val date = today.minusDays(offset.toLong())
            date.toString() to DayLog(date.toString(), reviewed = 1)
        }
        assertEquals(6, Progress.bestStreak(days))
    }

    @Test
    fun `week always has seven marks ending today`() {
        val week = Progress.week(emptyMap(), today)
        assertEquals(7, week.size)
        assertTrue(week.last().isToday)
        assertEquals(today.minusDays(6), week.first().date)
        assertFalse(week.any { it.active })
    }
}
