package com.kairos.app.engine

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CuratorTest {
    private fun parse(json: String, known: Set<String> = emptySet()) =
        Curator.parse(Json.parseToJsonElement(json).jsonObject, known, emptySet(), now = 1L)

    private val good = """
        {"words":[
          {"word":"apricity","partOfSpeech":"Noun","pronunciation":"uh-PRIS-ih-tee","definition":"The warmth of the sun in winter","example":"We sat on the steps in the apricity.","origin":"Latin apricus, sunny.","synonyms":["sunniness","Apricity","warmth"],"hook":"You love quiet weather words.","category":"nature","level":4},
          {"word":"sonder","partOfSpeech":"noun","pronunciation":"SON-der","definition":"Realising every stranger has a full inner life","example":"Rush hour filled her with sonder.","origin":"Coined 2012.","synonyms":[],"hook":"","category":"made-up","level":9}
        ],
        "quotes":[{"text":"Nature does not hurry, yet everything is accomplished.","author":"— Lao Tzu","theme":"calm","note":"Tao Te Ching tradition."}],
        "sparks":["Step outside and find three shades of green.","x"],
        "prompts":["What are you hoping for this year?"]}
    """.trimIndent()

    @Test fun `clean rows come out of good json`() {
        val b = parse(good)
        assertEquals(listOf("Apricity", "Sonder"), b.words.map { it.word })
        val a = b.words.first()
        assertEquals("noun", a.partOfSpeech)
        assertEquals("The warmth of the sun in winter.", a.definition)
        assertEquals("sunniness, warmth", a.synonyms)
        assertEquals(1L, a.generatedAt)
    }

    @Test fun `unknown categories and levels are clamped`() {
        val s = parse(good).words[1]
        assertEquals("wonder", s.category)
        assertEquals(5, s.level)
    }

    @Test fun `quote authors are tidied and themes kept`() {
        val q = parse(good).quotes.single()
        assertEquals("Lao Tzu", q.author)
        assertEquals("calm", q.theme)
    }

    @Test fun `too-short sparks are dropped`() {
        assertEquals(listOf("Step outside and find three shades of green."), parse(good).sparks)
    }

    @Test fun `known words and duplicates are rejected`() {
        val b = parse(good, known = setOf("apricity"))
        assertEquals(listOf("Sonder"), b.words.map { it.word })
    }

    @Test fun `definitions that use the word itself are rejected`() {
        val b = parse("""{"words":[{"word":"glow","partOfSpeech":"verb","pronunciation":"","definition":"To glow softly","example":"","origin":"","synonyms":[],"hook":"","category":"nature","level":1}],"quotes":[],"sparks":[],"prompts":[]}""")
        assertTrue(b.words.isEmpty())
    }

    @Test fun `garbage words are rejected`() {
        val b = parse("""{"words":[{"word":"x1!","partOfSpeech":"","pronunciation":"","definition":"something","example":"","origin":"","synonyms":[],"hook":"","category":"","level":1}],"quotes":[{"text":"hi","author":"","theme":"","note":""}],"sparks":[],"prompts":[]}""")
        assertTrue(b.words.isEmpty())
        assertTrue(b.quotes.isEmpty())
    }

    @Test fun `the prompt carries the person's signals`() {
        val profile = Profile(
            name = "Prashant", interests = setOf("nature"), themes = setOf("calm"), level = 4, recentMoods = listOf(Mood.Low),
            dayPart = DayPart.Evening, loved = listOf("Petrichor"), notForMe = listOf("Ebullient"), tooEasy = listOf("Happy"),
            tooHard = emptyList(), struggling = emptyList(), mastered = 3, lovedAuthors = listOf("Mary Oliver"), lovedThemes = listOf("calm"),
            avoidWords = listOf("Petrichor", "Ebullient"), avoidQuotes = emptyList(), avoidSparks = emptyList(), focus = "the ocean",
        )
        val msg = Curator.userMessage(profile, 8, 6, 4, 3)
        listOf("Prashant", "Petrichor", "Ebullient", "Mary Oliver", "the ocean", "Low", "\"level\":4").forEach { assertTrue("missing $it in $msg", msg.contains(it)) }
    }
}
