package com.kairos.app

import com.kairos.app.data.Profile
import com.kairos.app.data.QuoteSignal
import com.kairos.app.data.catalog.QuoteCatalog
import com.kairos.app.domain.quote.QuoteEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class QuoteEngineTest {

    private val day = LocalDate.of(2026, 10, 20)

    private val taste = QuoteEngine.taste(
        profile = Profile(quoteThemes = setOf(QuoteCatalog.THEME_STILLNESS)),
        signals = listOf(
            QuoteSignal("co-01", loved = true, at = 1L),
            QuoteSignal("pl-04", loved = false, at = 2L),
        ),
    )

    @Test
    fun `the day's quote is deterministic`() {
        val first = QuoteEngine.forDay(day, taste)
        val second = QuoteEngine.forDay(day, taste)
        assertEquals(first.quote.id, second.quote.id)
        assertEquals(first.score, second.score, 0.0001)
    }

    @Test
    fun `a dismissed quote is never the pick while alternatives exist`() {
        val result = QuoteEngine.forDay(day, taste)
        assertNotEquals("pl-04", result.quote.id)
        assertFalse(result.quote.id == "pl-04")
    }

    @Test
    fun `theme preference lifts the score of that theme`() {
        val stillness = QuoteCatalog.byId.getValue("st-01")
        val play = QuoteCatalog.byId.getValue("pl-01")
        val stillScore = QuoteEngine.score(stillness, taste, day.toEpochDay())
        val playScore = QuoteEngine.score(play, taste, day.toEpochDay())
        assertTrue(stillScore > playScore)
    }

    @Test
    fun `shortlist is the requested size and contains no duplicates`() {
        val shortlist = QuoteEngine.shortlist(day, taste, 6)
        assertEquals(6, shortlist.size)
        assertEquals(6, shortlist.map { it.id }.toSet().size)
    }

    @Test
    fun `every quote has a unique id and a known theme`() {
        val ids = QuoteCatalog.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        val themes = QuoteCatalog.themes.map { it.first }.toSet()
        assertTrue(QuoteCatalog.all.all { it.theme in themes })
        assertTrue(QuoteCatalog.all.all { it.text.isNotBlank() && it.author.isNotBlank() })
    }

    @Test
    fun `day number counts from joining`() {
        assertEquals(1, QuoteEngine.dayNumber("", day))
        assertEquals(11, QuoteEngine.dayNumber("2026-10-10", day))
    }

    @Test
    fun `mix is stable for a given input`() {
        assertEquals(QuoteEngine.mix(42L), QuoteEngine.mix(42L))
        assertNotEquals(QuoteEngine.mix(42L), QuoteEngine.mix(43L))
    }
}
