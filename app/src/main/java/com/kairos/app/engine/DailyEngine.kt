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
    /** Languages of borrowed words they saved (Greek, Japanese…). */
    val savedLanguages: Map<String, Int> = emptyMap(),
    /** Authors whose lines they kept. */
    val lovedAuthors: Set<String> = emptySet(),
    /** Categories and themes shown in the last few days, so picks keep varying. */
    val recentCategories: List<String> = emptyList(),
    val recentThemes: List<String> = emptyList(),
    /** Kinds of thought shown recently ("quote", "line", "proverb"), to keep a balance. */
    val recentKinds: List<String> = emptyList(),
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

    /** One signal and how much it contributed, so the strongest one can explain the pick. */
    private class Scored<T>(val item: T, var score: Double = 0.0, var reason: String = "", var best: Double = 0.0) {
        fun add(points: Double, why: String? = null) {
            score += points
            if (why != null && points > best) { best = points; reason = why }
        }
    }

    /**
     * Scores every candidate word with several independent strategies:
     * interests, what they save (category and language), level fit with a weekly stretch, novelty,
     * freshness, variety against the last few days, a weekday rhythm, and a little serendipity.
     */
    fun scoreWords(words: List<WordEntity>, taste: Taste, day: Long, exclude: Set<String> = emptySet(), stream: Int = 1): List<Pick<WordEntity>> {
        if (words.isEmpty()) return emptyList()
        val random = rng(day, taste.salt, stream)
        val pool = words.filter { it.id !in exclude && !it.mastered && it.taste >= 0 }
            .filter { w -> w.lastShownDay?.let { day - it >= WORD_COOLDOWN_DAYS } ?: true }
            .ifEmpty { words.filter { it.id !in exclude && it.taste >= 0 } }
            .ifEmpty { words.filter { it.id !in exclude } }
            .ifEmpty { words }
        val weekday = java.time.LocalDate.ofEpochDay(day).dayOfWeek.value
        val stretchDay = (day + taste.salt).mod(4L) == 0L
        val weekendCats = setOf("wisdom", "wonder", "culture")
        val serendipity = Random(day * 31 + taste.salt).nextInt(Topics.words.size).let { Topics.words[it].key }
        return pool.map { w ->
            val s = Scored(w)
            if (w.category in taste.wordInterests) s.add(2.0, "From your ${Topics.wordLabel(w.category).lowercase()} interests")
            val saved = min(3, taste.savedWordCategories[w.category] ?: 0)
            s.add(saved * 0.6, if (saved >= 2) "Because you keep saving ${Topics.wordLabel(w.category).lowercase()} words" else null)
            if (w.language.isNotBlank()) {
                val lang = min(3, taste.savedLanguages[w.language] ?: 0)
                s.add(0.8 + lang * 0.5, if (lang >= 1) "You liked ${w.language} words before" else "A ${w.language} word for something English can't quite say")
            }
            val target = if (stretchDay) taste.level + 1 else taste.level
            s.add(-abs(w.level - target) * 0.9)
            if (stretchDay && w.level == taste.level + 1) s.add(0.4, "A little stretch today")
            if (w.timesShown == 0) s.add(2.5)
            // Fresher curator output reflects the latest feedback, so it gets a gentle edge.
            s.add((w.generatedAt / 86_400_000L - day).coerceIn(-30, 0) * 0.03 + 0.9)
            val recentHits = taste.recentCategories.take(3).count { it == w.category }
            s.add(-recentHits * 1.1)
            if (weekday >= 6 && w.category in weekendCats) s.add(0.7, "A slow weekend word")
            if (w.category == serendipity && w.category !in taste.wordInterests) s.add(0.9, "Something a little outside your usual")
            s.add(random.nextDouble() * 1.6)
            Pick(w, w.hook.ifBlank { s.reason.ifBlank { "Something new for you" } }) to s.score
        }.sortedByDescending { it.second }.map { it.first }
    }

    fun pickWord(words: List<WordEntity>, taste: Taste, day: Long, exclude: Set<String> = emptySet()): Pick<WordEntity>? =
        scoreWords(words, taste, day, exclude).firstOrNull()

    /**
     * Thoughts: mood, chosen themes, what they keep, authors they love, the time of day, a balance of
     * real quotes, original lines and proverbs, variety against recent themes, and novelty.
     */
    fun scoreQuotes(
        quotes: List<QuoteEntity>,
        taste: Taste,
        day: Long,
        mood: Mood?,
        dayPart: DayPart,
        exclude: Set<String> = emptySet(),
        shuffle: Int = 0,
    ): List<Pick<QuoteEntity>> {
        if (quotes.isEmpty()) return emptyList()
        val random = rng(day, taste.salt, 2 + shuffle)
        val pool = quotes.filter { it.id !in exclude && it.taste >= 0 }
            .filter { q -> q.lastShownDay?.let { day - it >= QUOTE_COOLDOWN_DAYS } ?: true }
            .ifEmpty { quotes.filter { it.id !in exclude } }
            .ifEmpty { quotes }
        val partThemes = when (dayPart) {
            DayPart.Morning -> setOf("courage", "hope", "growth", "lightness")
            DayPart.Afternoon -> setOf("growth", "joy", "kindness", "lightness")
            DayPart.Evening -> setOf("calm", "rest", "wonder", "love")
            DayPart.Night -> setOf("rest", "calm", "comfort", "love")
        }
        val lastKind = taste.recentKinds.firstOrNull()
        return pool.map { q ->
            val s = Scored(q)
            if (mood != null && q.theme in mood.favouredThemes) s.add(3.2, "For a ${mood.label.lowercase()} day")
            if (q.theme in taste.quoteThemes) s.add(2.4, "From your ${Topics.themeLabel(q.theme).lowercase()} theme")
            val saved = min(3, taste.savedQuoteThemes[q.theme] ?: 0)
            s.add(saved * 0.5, if (saved >= 2) "You keep saving ${Topics.themeLabel(q.theme).lowercase()} thoughts" else null)
            if (q.author in taste.lovedAuthors) s.add(1.4, "More from ${q.author}, whom you kept before")
            if (q.theme in partThemes) s.add(1.0, "A ${dayPart.name.lowercase()} thought")
            if (lastKind != null && q.kind != lastKind) s.add(0.6)
            if (q.isLine) s.add(0.2)
            s.add(-taste.recentThemes.take(3).count { it == q.theme } * 0.9)
            if (q.timesShown == 0) s.add(0.8)
            s.add(random.nextDouble() * 2.0)
            Pick(q, s.reason.ifBlank { "A ${dayPart.name.lowercase()} thought" }) to s.score
        }.sortedByDescending { it.second }.map { it.first }
    }

    fun pickQuote(
        quotes: List<QuoteEntity>,
        taste: Taste,
        day: Long,
        mood: Mood?,
        dayPart: DayPart,
        exclude: Set<String> = emptySet(),
        shuffle: Int = 0,
    ): Pick<QuoteEntity>? = scoreQuotes(quotes, taste, day, mood, dayPart, exclude, shuffle).firstOrNull()

    /**
     * Turns a ranked list into a feed that doesn't repeat itself: walks the ranking but holds back an
     * item whose group (category, theme) matches either of the last two picked (maximal marginal
     * relevance, simply). There is no fixed limit: the feed is as long as the pool.
     */
    fun <T> diversify(ranked: List<T>, group: (T) -> String): List<T> {
        val rest = ranked.toMutableList()
        val out = ArrayList<T>(ranked.size)
        while (rest.isNotEmpty()) {
            val recent = out.takeLast(2).map(group)
            val last = recent.lastOrNull()
            val i = rest.indexOfFirst { group(it) !in recent }.takeIf { it >= 0 }
                ?: rest.indexOfFirst { group(it) != last }.takeIf { it >= 0 }
                ?: 0
            out += rest.removeAt(i)
        }
        return out
    }
}
