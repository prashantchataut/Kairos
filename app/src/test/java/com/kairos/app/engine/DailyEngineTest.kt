package com.kairos.app.engine

import com.kairos.app.data.content.Spark
import com.kairos.app.data.db.QuoteEntity
import com.kairos.app.data.db.WordEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyEngineTest {
    private fun word(id: String, cat: String, level: Int = 2, shown: Int = 0, last: Long? = null) =
        WordEntity(id, id, "noun", "", "def", "ex", "", "", cat, level, timesShown = shown, lastShownDay = last)

    private fun quote(id: String, theme: String) = QuoteEntity(id, "text $id", "author", theme)

    private val words = listOf(
        word("a", "nature"), word("b", "nature"), word("c", "mind"), word("d", "mind"),
        word("e", "feelings"), word("f", "craft", level = 3), word("g", "wonder", level = 1),
    )
    private val quotes = listOf(quote("q1", "hope"), quote("q2", "rest"), quote("q3", "joy"), quote("q4", "courage"), quote("q5", "calm"))
    private val taste = Taste(setOf("nature"), setOf("joy"), level = 2, salt = 42)

    @Test fun `same day and salt give the same word`() {
        val a = DailyEngine.pickWord(words, taste, 20_000)
        val b = DailyEngine.pickWord(words, taste, 20_000)
        assertEquals(a?.item?.id, b?.item?.id)
    }

    @Test fun `interests win most days`() {
        val hits = (0 until 100).count { d -> DailyEngine.pickWord(words, taste, 20_000L + d)?.item?.category == "nature" }
        assertTrue("nature picked on $hits/100 days", hits > 60)
    }

    @Test fun `recently shown words are skipped`() {
        val shown = words.map { if (it.category == "nature") it.copy(lastShownDay = 19_990, timesShown = 1) else it }
        val pick = DailyEngine.pickWord(shown, taste, 20_000)!!
        assertNotEquals("nature", pick.item.category)
    }

    @Test fun `excluded word is never picked`() {
        repeat(30) { d ->
            val pick = DailyEngine.pickWord(words, taste, 20_000L + d, exclude = setOf("a"))!!
            assertNotEquals("a", pick.item.id)
        }
    }

    @Test fun `a low mood steers quotes toward gentle themes`() {
        val gentle = Mood.Low.favouredThemes
        val hits = (0 until 60).count { d ->
            DailyEngine.pickQuote(quotes, taste.copy(quoteThemes = emptySet()), 20_000L + d, Mood.Low, DayPart.Afternoon)!!.item.theme in gentle
        }
        assertTrue("gentle picked on $hits/60 days", hits > 45)
    }

    @Test fun `every pick explains itself`() {
        val pick = DailyEngine.pickQuote(quotes, taste, 20_000, Mood.Good, DayPart.Morning)!!
        assertTrue(pick.reason.isNotBlank())
    }

    @Test fun `sparks do not repeat within a cycle`() {
        val sparks = (1..10).map { Spark("s$it", "spark $it", "kind") }
        val start = 20_000L - 20_000L % 10
        val cycle = (0 until 10).map { DailyEngine.pickSpark(sparks, start + it, 7)!!.id }
        assertEquals(10, cycle.toSet().size)
    }
}
