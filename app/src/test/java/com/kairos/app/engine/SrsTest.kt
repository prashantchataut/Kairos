package com.kairos.app.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SrsTest {
    private val fresh = SrsState(reps = 0, ease = 2.5, intervalDays = 0, lapses = 0)

    @Test fun `first good review comes back tomorrow`() {
        assertEquals(1, Srs.review(fresh, Recall.Good).intervalDays)
    }

    @Test fun `first easy review skips ahead`() {
        assertEquals(3, Srs.review(fresh, Recall.Easy).intervalDays)
    }

    @Test fun `intervals grow with successive good reviews`() {
        var s = fresh
        val seen = mutableListOf<Int>()
        repeat(5) { s = Srs.review(s, Recall.Good); seen += s.intervalDays }
        assertEquals(listOf(1, 6), seen.take(2))
        assertTrue(seen.zipWithNext().all { (a, b) -> b > a })
    }

    @Test fun `again resets reps and counts a lapse`() {
        val learned = SrsState(reps = 4, ease = 2.5, intervalDays = 30, lapses = 0)
        val after = Srs.review(learned, Recall.Again)
        assertEquals(0, after.reps)
        assertEquals(1, after.intervalDays)
        assertEquals(1, after.lapses)
        assertTrue(after.ease < learned.ease)
    }

    @Test fun `ease never drops below the floor`() {
        var s = fresh
        repeat(20) { s = Srs.review(s, Recall.Again) }
        assertTrue(s.ease >= Srs.MIN_EASE)
    }

    @Test fun `hard still moves forward`() {
        val s = SrsState(reps = 3, ease = 2.5, intervalDays = 10, lapses = 0)
        assertTrue(Srs.review(s, Recall.Hard).intervalDays > 10)
    }

    @Test fun `interval labels are short`() {
        assertEquals("6d", Srs.formatInterval(6))
        assertEquals("2mo", Srs.formatInterval(60))
        assertEquals("1y", Srs.formatInterval(400))
    }
}
