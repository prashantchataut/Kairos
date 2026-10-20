package com.kairos.app.domain.play

import org.junit.Assert.*
import org.junit.Test

class WordGardenTest {
    @Test fun everyQuestionHasExactlyOneValidAnswer() {
        WordGarden.words.forEachIndexed { index, word ->
            assertEquals(1, word.choices.count { WordGarden.isCorrect(index, it) })
            assertEquals(3, word.choices.distinct().size)
        }
    }
    @Test fun completedGardenCyclesWithoutCrashing() {
        assertEquals(WordGarden.question(0), WordGarden.question(WordGarden.words.size))
        assertNotNull(WordGarden.question(Int.MAX_VALUE))
        assertNotNull(WordGarden.question(-1))
    }
    @Test fun incorrectAnswerDoesNotGrowFlower() {
        assertFalse(WordGarden.isCorrect(0, "A carefully planned journey"))
        assertTrue(WordGarden.isCorrect(0, "A happy discovery made by chance"))
    }
}
