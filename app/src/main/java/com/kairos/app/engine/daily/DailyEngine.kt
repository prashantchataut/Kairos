package com.kairos.app.engine.daily

import com.kairos.app.engine.content.Quote
import com.kairos.app.engine.content.Topics
import com.kairos.app.engine.content.Word
import kotlin.math.abs
import kotlin.random.Random

enum class Mood(val label: String, val line: String) {
    Stormy("Stormy", "Heavy skies. That's allowed."),
    Cloudy("Cloudy", "A bit grey around the edges."),
    Okay("Okay", "Steady water, nothing urgent."),
    Sunny("Sunny", "Light is getting in today."),
    Glowing("Glowing", "Full of sparkle — share some."),
    ;

    val isLow get() = this == Stormy || this == Cloudy
    val isHigh get() = this == Sunny || this == Glowing

    companion object {
        fun of(i: Int) = entries.getOrElse(i) { Okay }
    }
}

data class Pick<T>(val item: T, val reason: String)

/** Lightweight read model of the learner's relationship with each word or quote. */
data class WordSignal(val saved: Boolean, val timesSeen: Int, val inDeck: Boolean)
data class QuoteSignal(val saved: Boolean, val timesShown: Int)

/**
 * The recommendation engine behind "today".
 *
 * Every candidate gets an explainable score: interests you chose, the taste you've shown by
 * saving things, how well the word fits your level, and freshness. A seeded weighted draw keeps
 * the day's pick deterministic (same day + same install = same word) while still surprising.
 */
object DailyEngine {

    fun pickWord(
        words: List<Word>,
        signals: Map<String, WordSignal>,
        interests: Set<String>,
        level: Int,
        day: Long,
        salt: Long,
        recent: Set<String>,
        reroll: Int = 0,
    ): Pick<Word>? {
        val pool = words.filter { !it.custom && it.id !in recent }.ifEmpty { words.filter { !it.custom } }
        if (pool.isEmpty()) return null
        val affinity = tagAffinity(words.filter { signals[it.id]?.saved == true }.flatMap { it.tags })

        val scored = pool.map { w ->
            val s = signals[w.id]
            val interestHits = w.tags.count { it in interests }
            val interest = if (interests.isEmpty()) 0.6 else 1.2 * interestHits.coerceAtMost(2)
            val taste = w.tags.sumOf { affinity[it] ?: 0.0 }.coerceAtMost(1.2)
            val fit = 1.0 - 0.38 * abs(level - w.level)
            val fresh = when {
                s == null || s.timesSeen == 0 -> 1.0
                s.inDeck -> 0.15
                else -> 0.45
            }
            val score = (0.5 + interest + taste) * fit * fresh
            val reason = when {
                fresh >= 1.0 && interestHits > 0 -> "New to you, and it lives in ${Topics.wordLabel(w.tags.first { it in interests }).lowercase()}"
                taste >= 0.6 -> "It sits close to words you've saved"
                interestHits > 0 -> "Because you're curious about ${Topics.wordLabel(w.tags.first { it in interests }).lowercase()}"
                fit >= 1.0 -> "Right at your level — a comfortable stretch"
                else -> "A small surprise from outside your usual pond"
            }
            Triple(w, score.coerceAtLeast(0.01), reason)
        }
        val chosen = weightedDraw(scored.map { it.second }, Random(seed(day, salt, 1, reroll)))
        return scored[chosen].let { Pick(it.first, it.third) }
    }

    fun pickQuote(
        quotes: List<Quote>,
        signals: Map<String, QuoteSignal>,
        themes: Set<String>,
        mood: Mood?,
        day: Long,
        salt: Long,
        recent: Set<String>,
        reroll: Int = 0,
    ): Pick<Quote>? {
        val pool = quotes.filter { it.id !in recent }.ifEmpty { quotes }
        if (pool.isEmpty()) return null
        val affinity = tagAffinity(quotes.filter { signals[it.id]?.saved == true }.flatMap { it.tags })
        val gentle = setOf("comfort", "hope", "calm", "kindness")
        val bright = setOf("joy", "courage", "growth", "wonder")

        val scored = pool.map { q ->
            val hits = q.tags.count { it in themes }
            val theme = if (themes.isEmpty()) 0.6 else 1.2 * hits.coerceAtMost(2)
            val taste = q.tags.sumOf { affinity[it] ?: 0.0 }.coerceAtMost(1.0)
            val moodFit = when {
                mood == null -> 0.0
                mood.isLow && q.tags.any { it in gentle } -> 1.6
                mood.isHigh && q.tags.any { it in bright } -> 0.9
                mood == Mood.Okay && q.tags.any { it == "focus" || it == "growth" } -> 0.5
                else -> 0.0
            }
            val fresh = if ((signals[q.id]?.timesShown ?: 0) == 0) 1.0 else 0.5
            val score = (0.4 + theme + taste + moodFit) * fresh
            val reason = when {
                moodFit >= 1.5 -> "You said today feels ${mood!!.label.lowercase()} — here's something gentle"
                moodFit > 0.5 -> "You're ${mood!!.label.lowercase()} today — keep that light moving"
                hits > 0 -> "From ${Topics.quoteLabel(q.tags.first { it in themes }).lowercase()}, a theme you picked"
                taste >= 0.5 -> "Close to thoughts you've kept"
                else -> "A thought from a little further away"
            }
            Triple(q, score.coerceAtLeast(0.01), reason)
        }
        val chosen = weightedDraw(scored.map { it.second }, Random(seed(day, salt, 2, reroll)))
        return scored[chosen].let { Pick(it.first, it.third) }
    }

    /** Saved items teach taste: each saved tag adds a little, with diminishing returns. */
    fun tagAffinity(tags: List<String>): Map<String, Double> =
        tags.groupingBy { it }.eachCount().mapValues { (_, n) -> 0.3 * kotlin.math.ln(1.0 + n) / kotlin.math.ln(2.0) }

    fun seed(day: Long, salt: Long, channel: Int, reroll: Int): Long =
        (day * 1_000_003L) xor salt xor (channel.toLong() shl 40) xor (reroll.toLong() * 7_919L)

    fun weightedDraw(weights: List<Double>, random: Random): Int {
        val total = weights.sum()
        var r = random.nextDouble() * total
        weights.forEachIndexed { i, w ->
            r -= w
            if (r <= 0) return i
        }
        return weights.lastIndex
    }
}
