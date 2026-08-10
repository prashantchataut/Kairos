package com.kairos.app.ui.vocabulary

import com.kairos.app.ui.screens.vocabulary.SessionGrade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Grade semantics for the flashcard flow: Again/Hard/Good/Easy map to SM-2
 * qualities, and passing grades advance the card.
 */
class SessionGradeTest {

    @Test
    fun `grade quality values match SM-2 scale`() {
        assertEquals(1, SessionGrade.AGAIN.quality)
        assertEquals(3, SessionGrade.HARD.quality)
        assertEquals(4, SessionGrade.GOOD.quality)
        assertEquals(5, SessionGrade.EASY.quality)
    }

    @Test
    fun `only again is a failing grade`() {
        assertFalse(SessionGrade.AGAIN.isPassing)
        assertTrue(SessionGrade.HARD.isPassing)
        assertTrue(SessionGrade.GOOD.isPassing)
        assertTrue(SessionGrade.EASY.isPassing)
    }

    @Test
    fun `labels are clear and consumer-facing`() {
        assertEquals("Again", SessionGrade.AGAIN.label)
        assertEquals("Hard", SessionGrade.HARD.label)
        assertEquals("Good", SessionGrade.GOOD.label)
        assertEquals("Easy", SessionGrade.EASY.label)
    }
}
