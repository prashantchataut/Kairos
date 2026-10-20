package com.kairos.app

import com.kairos.app.data.Grade
import com.kairos.app.data.WordMemory
import com.kairos.app.domain.vocab.SrsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SrsEngineTest {

    private val now = 1_700_000_000_000L
    private val day = SrsEngine.DAY_MS

    @Test
    fun `good grows the interval by the ease factor`() {
        val memory = WordMemory("w", ease = 2.5, intervalDays = 10, repetitions = 3, seen = 3)
        val next = SrsEngine.review(memory, Grade.Good, now)
        assertEquals(25, next.intervalDays)
        assertEquals(now + 25 * day, next.dueAt)
        assertEquals(4, next.repetitions)
        assertEquals(2.5, next.ease, 0.0001)
    }

    @Test
    fun `easy grows faster and raises ease`() {
        val memory = WordMemory("w", ease = 2.5, intervalDays = 10, repetitions = 3, seen = 3)
        val next = SrsEngine.review(memory, Grade.Easy, now)
        assertEquals(34, next.intervalDays)
        assertEquals(2.65, next.ease, 0.0001)
    }

    @Test
    fun `hard shortens growth and lowers ease`() {
        val memory = WordMemory("w", ease = 2.5, intervalDays = 10, repetitions = 3, seen = 3)
        val next = SrsEngine.review(memory, Grade.Hard, now)
        assertEquals(12, next.intervalDays)
        assertEquals(2.35, next.ease, 0.0001)
    }

    @Test
    fun `again comes back inside the sitting and records a lapse`() {
        val memory = WordMemory("w", ease = 2.5, intervalDays = 20, repetitions = 5, seen = 5)
        val next = SrsEngine.review(memory, Grade.Again, now)
        assertEquals(0, next.intervalDays)
        assertEquals(1, next.lapses)
        assertEquals(0, next.repetitions)
        assertEquals(now + SrsEngine.AGAIN_DELAY_MS, next.dueAt)
    }

    @Test
    fun `first meeting schedules sensible distances`() {
        val fresh = WordMemory("w")
        assertEquals(3, SrsEngine.review(fresh, Grade.Good, now).intervalDays)
        assertEquals(5, SrsEngine.review(fresh, Grade.Easy, now).intervalDays)
        assertEquals(1, SrsEngine.review(fresh, Grade.Hard, now).intervalDays)
    }

    @Test
    fun `ease never falls below the floor no matter how many lapses`() {
        var memory = WordMemory("w", ease = 2.5, intervalDays = 5, seen = 1)
        repeat(20) { memory = SrsEngine.review(memory, Grade.Again, now) }
        assertTrue(memory.ease >= SrsEngine.MIN_EASE)
        assertEquals(SrsEngine.MIN_EASE, memory.ease, 0.0001)
    }

    @Test
    fun `mastery rises with interval and never exceeds one`() {
        val newWord = WordMemory("w")
        assertEquals(0f, SrsEngine.mastery(newWord), 0.0001f)
        var memory = WordMemory("w", ease = 2.5, intervalDays = 1, repetitions = 1, seen = 1)
        repeat(12) { memory = SrsEngine.review(memory, Grade.Easy, now) }
        assertTrue(SrsEngine.mastery(memory) > 0.8f)
        assertTrue(SrsEngine.mastery(memory) <= 1f)
        assertTrue(SrsEngine.isLearned(memory))
    }

    @Test
    fun `due only after the scheduled moment`() {
        val memory = WordMemory("w", intervalDays = 3, dueAt = now + day, seen = 2)
        assertFalse(SrsEngine.isDue(memory, now))
        assertTrue(SrsEngine.isDue(memory, now + day))
        assertFalse(SrsEngine.isDue(WordMemory("x"), now))
    }
}
