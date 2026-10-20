package com.kairos.app

import com.kairos.app.data.Profile
import com.kairos.app.data.WordMemory
import com.kairos.app.data.catalog.VocabCatalog
import com.kairos.app.domain.vocab.SessionBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SessionBuilderTest {

    private val today = LocalDate.of(2026, 10, 20)
    private val now = 1_700_000_000_000L

    @Test
    fun `due reviews come before new words`() {
        val due = WordMemory("wd-001", intervalDays = 5, dueAt = now - 1000, seen = 2, repetitions = 2)
        val session = SessionBuilder.build(
            date = today,
            memories = mapOf("wd-001" to due),
            profile = Profile(dailyPace = 5),
            now = now,
            size = 5,
        )
        assertEquals("wd-001", session.cards.first().word.id)
        assertTrue(session.cards.first().isNew.not())
        assertTrue(session.cards.drop(1).all { it.isNew })
    }

    @Test
    fun `size is respected and never exceeds the catalog`() {
        val session = SessionBuilder.build(
            date = today,
            memories = emptyMap(),
            profile = Profile(dailyPace = 8),
            now = now,
            size = 8,
        )
        assertEquals(8, session.cards.size)
        assertEquals(8, session.cards.map { it.word.id }.toSet().size)
    }

    @Test
    fun `the queue is deterministic for the same day`() {
        val first = SessionBuilder.build(today, emptyMap(), Profile(dailyPace = 5), now, size = 5)
        val second = SessionBuilder.build(today, emptyMap(), Profile(dailyPace = 5), now, size = 5)
        assertEquals(first.cards.map { it.word.id }, second.cards.map { it.word.id })
    }

    @Test
    fun `interest themes shape the new words`() {
        val session = SessionBuilder.build(
            date = today,
            memories = emptyMap(),
            profile = Profile(dailyPace = 6, interests = setOf(VocabCatalog.NATURE)),
            now = now,
            size = 6,
        )
        assertTrue(session.cards.all { card -> VocabCatalog.NATURE in card.word.themes })
    }

    @Test
    fun `quiz offers four options including the answer`() {
        val session = SessionBuilder.build(today, emptyMap(), Profile(dailyPace = 3), now, size = 3)
        val quiz = SessionBuilder.quiz(session.cards.first(), seed = 3)
        assertEquals(4, quiz.options.size)
        assertTrue(quiz.answer in quiz.options)
        assertTrue(quiz.correctIndex >= 0)
    }

    @Test
    fun `catalog entries are complete and unique`() {
        val ids = VocabCatalog.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(VocabCatalog.all.all { it.ipa.startsWith("/") && it.ipa.endsWith("/") })
        assertTrue(VocabCatalog.all.all { it.definition.length > 20 && it.example.isNotBlank() })
        assertTrue(VocabCatalog.all.all { it.themes.isNotEmpty() })
    }
}
