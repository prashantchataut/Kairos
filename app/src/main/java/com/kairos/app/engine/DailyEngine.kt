package com.kairos.app.engine

import com.kairos.app.data.db.QuoteEntity
import com.kairos.app.data.db.WordEntity
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

/** Everything the daily picker knows about the person. */
data class Taste(
    val wordInterests: Set<String>,
    val quoteThemes: Set<String>,
    val level: Int,
    val salt: Long,
    /** Themes/categories of things they saved — saving is the strongest "more like this" signal. */
    val savedWordCategories: Map<String, Int> = emptyMap(),
    val savedQuoteThemes: Map<String, Int> = emptyMap(),
)

enum class DayPart { Morning, Afternoon, Evening, Night;
    companion object {
        fun of(hour: Int) = when (hour) {
            in 5..11 -> Morning
            in 12..16 -> Afternoon
            in 17..21 -> Evening
            else -> Night
        }
    }
}

/** A pick plus a short, honest sentence about why it was chosen. */
data class Pick<T>(val item: T, val reason: String)

/**
 * The quote and vocabulary engine.
 *
 * Scoring is transparent and deterministic: the same day, salt and history always produce the same
 * pick, and every pick explains itself. Randomness only breaks ties via a day-seeded jitter.
 */
object DailyEngine {
    private const val WORD_COOLDOWN_DAYS = 120
    private const val QUOTE_COOLDOWN_DAYS = 60

    private fun rng(day: Long, salt: Long, stream: Int) = Random(day * 1_000_003L + salt + stream * 7919L)

    fun pickWord(words: List<WordEntity>, taste: Taste, day: Long, exclude: Set<String> = emptySet()): Pick<WordEntity>? {
        if (words.isEmpty()) return null
        val random = rng(day, taste.salt, 1)
        val pool = words.filter { it.id !in exclude && !it.mastered && it.taste >= 0 }
            .filter { w -> w.lastShownDay?.let { day - it >= WORD_COOLDOWN_DAYS } ?: true }
            .ifEmpty { words.filter { it.id !in exclude } }
            .ifEmpty { words }
        val scored = pool.map { w ->
            var score = 0.0
            val interested = w.category in taste.wordInterests
            if (interested) score += 2.0
            val savedAffinity = min(3, taste.savedWordCategories[w.category] ?: 0)
            score += savedAffinity * 0.6
            score -= abs(w.level - taste.level) * 0.9
            if (w.timesShown == 0) score += 2.5
            // Fresher curator output reflects the latest feedback, so it gets a gentle edge.
            score += (w.generatedAt / 86_400_000L - day).coerceIn(-30, 0) * 0.03 + 0.9
            score += random.nextDouble() * 1.6
            val reason = when {
                w.hook.isNotBlank() -> w.hook
                savedAffinity >= 2 -> "Because you've been saving ${w.category} words"
                interested -> "From your ${w.category} interests"
                else -> "Something new for you"
            }
            Triple(w, score, reason)
        }
        val best = scored.maxBy { it.second }
        return Pick(best.first, best.third)
    }

    fun pickQuote(
        quotes: List<QuoteEntity>,
        taste: Taste,
        day: Long,
        mood: Mood?,
        dayPart: DayPart,
        exclude: Set<String> = emptySet(),
        shuffle: Int = 0,
    ): Pick<QuoteEntity>? {
        if (quotes.isEmpty()) return null
        val random = rng(day, taste.salt, 2 + shuffle)
        val pool = quotes.filter { it.id !in exclude && it.taste >= 0 }
            .filter { q -> q.lastShownDay?.let { day - it >= QUOTE_COOLDOWN_DAYS } ?: true }
            .ifEmpty { quotes.filter { it.id !in exclude } }
            .ifEmpty { quotes }
        val partThemes = when (dayPart) {
            DayPart.Morning -> setOf("courage", "hope", "growth")
            DayPart.Afternoon -> setOf("growth", "joy", "kindness")
            DayPart.Evening -> setOf("calm", "rest", "wonder")
            DayPart.Night -> setOf("rest", "calm", "hope")
        }
        val scored = pool.map { q ->
            var score = 0.0
            val moodFit = mood != null && q.theme in mood.favouredThemes
            val chosen = q.theme in taste.quoteThemes
            val savedAffinity = min(3, taste.savedQuoteThemes[q.theme] ?: 0)
            if (moodFit) score += 3.2
            if (chosen) score += 2.4
            score += savedAffinity * 0.5
            if (q.theme in partThemes) score += 1.0
            if (q.timesShown == 0) score += 0.8
            score += random.nextDouble() * 2.0
            val reason = when {
                moodFit && mood != null -> "For a ${mood.label.lowercase()} day"
                savedAffinity >= 2 -> "You keep saving ${q.theme} quotes"
                chosen -> "From your ${q.theme} theme"
                else -> "A ${dayPart.name.lowercase()} thought"
            }
            Triple(q, score, reason)
        }
        val best = scored.maxBy { it.second }
        return Pick(best.first, best.third)
    }

}
