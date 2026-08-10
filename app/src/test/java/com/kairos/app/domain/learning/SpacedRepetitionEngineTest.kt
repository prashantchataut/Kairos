package com.kairos.app.domain.learning

import com.kairos.app.data.local.entity.VocabularyLearningEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Functional tests for the spaced-repetition engine powering the flashcard
 * practice flow: grade -> interval/ease/box/mastery semantics.
 */
class SpacedRepetitionEngineTest {

    private val engine = SpacedRepetitionEngine()

    private fun freshEntity() = VocabularyLearningEntity(wordId = 1)

    @Test
    fun `again resets repetitions and moves down a box`() {
        val reviewed = VocabularyLearningEntity(
            wordId = 1,
            repetitions = 3,
            boxLevel = 3,
            easeFactor = 2.5f,
            interval = 4
        )
        val result = engine.calculateNextReview(quality = 1, currentLearning = reviewed)

        assertEquals(0, result.newRepetitions)
        assertEquals(0, result.newCorrectStreak)
        assertEquals(1, result.newBoxLevel) // incorrect answers reset to box 1
        assertEquals(1, result.newInterval)
        assertFalse(result.isMastered)
        assertTrue(result.nextReviewDate > System.currentTimeMillis())
    }

    @Test
    fun `good keeps the word moving through boxes`() {
        val result = engine.calculateNextReview(quality = 4, currentLearning = freshEntity())

        assertEquals(1, result.newRepetitions)
        assertEquals(1, result.newCorrectStreak)
        assertEquals(2, result.newBoxLevel) // box 1 -> box 2
        assertEquals(1, result.newInterval) // first successful recall: 1 day
        assertFalse(result.isMastered)
    }

    @Test
    fun `easy raises ease factor more than good`() {
        val good = engine.calculateNextReview(quality = 4, currentLearning = freshEntity())
        val easy = engine.calculateNextReview(quality = 5, currentLearning = freshEntity())

        assertEquals(2, easy.newBoxLevel)
        assertTrue("easy should raise ease factor more", easy.newEaseFactor > good.newEaseFactor)
        // First successful recall is always 1 day for both.
        assertEquals(1, good.newInterval)
        assertEquals(1, easy.newInterval)
    }

    @Test
    fun `consistent correct reviews reach mastery at box five`() {
        var entity = freshEntity()
        repeat(4) {
            val result = engine.calculateNextReview(quality = 5, currentLearning = entity)
            entity = entity.copy(
                repetitions = result.newRepetitions,
                boxLevel = result.newBoxLevel,
                interval = result.newInterval,
                easeFactor = result.newEaseFactor
            )
        }
        val final = engine.calculateNextReview(quality = 5, currentLearning = entity)
        assertTrue("box should reach 5", final.newBoxLevel >= 5)
        assertTrue("should be mastered after 3+ reps at box 5", final.isMastered)
    }

    @Test
    fun `ease factor never drops below minimum`() {
        val entity = VocabularyLearningEntity(wordId = 1, easeFactor = 1.3f)
        val result = engine.calculateNextReview(quality = 1, currentLearning = entity)
        assertTrue(result.newEaseFactor >= 1.3f)
    }

    @Test
    fun `invalid quality is rejected`() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            engine.calculateNextReview(quality = 6, currentLearning = freshEntity())
        }
    }
}
