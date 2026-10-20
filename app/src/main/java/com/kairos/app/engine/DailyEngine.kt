package com.kairos.app.engine

import com.kairos.app.data.content.Joy
import com.kairos.app.data.content.Quote
import com.kairos.app.data.content.Word
import com.kairos.app.data.db.QuoteState
import com.kairos.app.data.db.WordProgress
import kotlin.math.abs
import kotlin.random.Random

/** Everything the picker needs to know about you, in plain data so it is easy to test. */
data class Taste(
    val wordThemes: Set<String>,
    val quoteThemes: Set<String>,
    val level: Int,
    val words: Map<String, WordProgress>,
    val quotes: Map<String, QuoteState>,
    val recentWordIds: Set<String>,
    val recentQuoteIds: Set<String>,
    val recentJoyIds: Set<String>,
)

data class Why(val headline: String)

/**
 * Picks the day's word, thought and small joy. Deterministic for a given (day, taste), so the
 * same day always shows the same picks; weighted so saving things teaches it what you like.
 */
object DailyEngine {

    fun seed(epochDay: Long, salt: Int = 0): Long = epochDay * 1_000_003L + salt * 7_919L + 42L

    fun <T> weightedPick(items: List<T>, weight: (T) -> Double, rnd: Random): T? {
        val weights = items.map { weight(it).coerceAtLeast(0.0) }
        val total = weights.sum()
        if (total <= 0.0) return items.randomOrNull(rnd)
        var r = rnd.nextDouble() * total
        items.forEachIndexed { i, item ->
            r -= weights[i]
            if (r <= 0.0) return item
        }
        return items.last()
    }

    /** How strongly you've signalled you like a theme: one point per saved item, capped. */
    fun affinity(savedThemes: List<String>): Map<String, Int> =
        savedThemes.groupingBy { it }.eachCount().mapValues { (_, n) -> n.coerceAtMost(4) }

    fun wordWeight(w: Word, taste: Taste, savedAffinity: Map<String, Int>): Double {
        if (w.id in taste.recentWordIds) return 0.0
        val p = taste.words[w.id]
        var weight = 1.0
        if (w.theme in taste.wordThemes) weight *= 3.0
        weight *= 1.0 + 0.5 * (savedAffinity[w.theme] ?: 0)
        weight *= when (abs(w.level - taste.level)) { 0 -> 1.6; 1 -> 1.0; else -> 0.45 }
        if (p?.seen == true) weight *= 0.15
        if (p?.saved == true) weight *= 0.05
        return weight
    }

    fun quoteWeight(q: Quote, taste: Taste, savedAffinity: Map<String, Int>): Double {
        if (q.id in taste.recentQuoteIds) return 0.0
        val s = taste.quotes[q.id]
        var weight = 1.0
        if (q.theme in taste.quoteThemes) weight *= 3.0
        weight *= 1.0 + 0.5 * (savedAffinity[q.theme] ?: 0)
        if (s?.seen == true) weight *= 0.2
        if (s?.saved == true) weight *= 0.1
        return weight
    }

    fun joyWeight(j: Joy, recent: Set<String>, mood: String?): Double {
        if (j.id in recent) return 0.05
        val boost = when (mood) {
            "Low", "Sleepy", "Meh" -> if (j.kind in setOf("rest", "notice")) 3.0 else if (j.kind == "move") 0.6 else 1.0
            "Glowing", "Happy", "Proud" -> if (j.kind in setOf("connect", "create", "play")) 2.5 else 1.0
            "Curious" -> if (j.kind == "learn") 3.0 else 1.0
            "Calm" -> if (j.kind in setOf("notice", "create")) 2.0 else 1.0
            else -> 1.0
        }
        return boost
    }

    fun pickWord(words: List<Word>, taste: Taste, day: Long): Word {
        val aff = affinity(words.filter { taste.words[it.id]?.saved == true }.map { it.theme })
        return weightedPick(words, { wordWeight(it, taste, aff) }, Random(seed(day, 1))) ?: words.first()
    }

    fun pickQuote(quotes: List<Quote>, taste: Taste, day: Long): Quote {
        val aff = affinity(quotes.filter { taste.quotes[it.id]?.saved == true }.map { it.theme })
        return weightedPick(quotes, { quoteWeight(it, taste, aff) }, Random(seed(day, 2))) ?: quotes.first()
    }

    fun pickJoy(joys: List<Joy>, recent: Set<String>, mood: String?, day: Long, salt: Int = 0): Joy =
        weightedPick(joys, { joyWeight(it, recent, mood) }, Random(seed(day, 3 + salt))) ?: joys.first()

    fun whyWord(w: Word, taste: Taste, words: List<Word>): Why {
        val savedSameTheme = words.count { it.theme == w.theme && taste.words[it.id]?.saved == true }
        return when {
            savedSameTheme >= 2 -> Why("Because you saved $savedSameTheme ${w.theme} words")
            w.theme in taste.wordThemes -> Why("From ${w.theme}, a theme you picked")
            else -> Why("Something new to wander into")
        }
    }

    fun whyQuote(q: Quote, taste: Taste, quotes: List<Quote>): Why {
        val saved = quotes.count { it.theme == q.theme && taste.quotes[it.id]?.saved == true }
        return when {
            saved >= 2 -> Why("You keep saving thoughts about ${q.theme}")
            q.theme in taste.quoteThemes -> Why("A thought on ${q.theme}")
            else -> Why("A little something different")
        }
    }
}
