package com.kairos.app.data.content

import com.kairos.app.data.ai.GeminiService
import com.kairos.app.data.local.dao.QuoteDao
import com.kairos.app.data.local.dao.VocabularyDao
import com.kairos.app.data.local.preferences.PreferencesManager
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Validation, dedupe, and curation rules for the online content refresh path.
 * (JSON parsing itself is exercised on-device; these cover the pure rules.)
 */
class OnlineContentRefresherLogicTest {

    private val refresher = OnlineContentRefresher(
        geminiService = mockk<GeminiService>(relaxed = true),
        vocabularyDao = mockk<VocabularyDao>(relaxed = true),
        quoteDao = mockk<QuoteDao>(relaxed = true),
        preferencesManager = mockk<PreferencesManager>(relaxed = true)
    )

    @Test
    fun `valid words pass validation`() {
        assertTrue(
            refresher.validateWord(
                OnlineContentRefresher.OnlineWord(
                    word = "Sonder",
                    definition = "The realization that each passerby has a life as vivid as your own",
                    partOfSpeech = "noun",
                    pronunciation = "SON-der",
                    exampleSentence = "A wave of sonder washed over her on the crowded platform.",
                    synonyms = "—",
                    category = "reflection",
                    difficulty = 3
                )
            )
        )
    }

    @Test
    fun `garbage words are rejected`() {
        assertFalse(
            refresher.validateWord(
                OnlineContentRefresher.OnlineWord(
                    word = "x",
                    definition = "short",
                    partOfSpeech = "",
                    pronunciation = "",
                    exampleSentence = "",
                    synonyms = "",
                    category = "",
                    difficulty = 3
                )
            )
        )
        assertFalse(
            refresher.validateWord(
                OnlineContentRefresher.OnlineWord(
                    word = "hello123!!",
                    definition = "A sufficiently long definition to pass the length gate",
                    partOfSpeech = "noun",
                    pronunciation = "",
                    exampleSentence = "",
                    synonyms = "",
                    category = "",
                    difficulty = 3
                )
            )
        )
    }

    @Test
    fun `quotes require real content and attribution`() {
        assertTrue(
            refresher.validateQuote(
                OnlineContentRefresher.OnlineQuote(
                    content = "Attention is the beginning of devotion.",
                    author = "Mary Oliver",
                    category = "wisdom",
                    reflectionPrompt = "What received your full attention today?"
                )
            )
        )
        assertFalse(
            refresher.validateQuote(
                OnlineContentRefresher.OnlineQuote("tiny", "", "wisdom", "")
            )
        )
    }

    @Test
    fun `derived family rows are hidden from the promoted surface`() {
        assertTrue(refresher.isPromotedWord(null))
        assertTrue(refresher.isPromotedWord("curated"))
        assertTrue(refresher.isPromotedWord("kairos:online"))
        assertFalse(refresher.isPromotedWord("kairos:family:distill"))
    }

    @Test
    fun `online words keep their curated origin marker`() {
        assertEquals("kairos:online", OnlineContentRefresher.WORD_ORIGIN_MARKER)
    }
}
