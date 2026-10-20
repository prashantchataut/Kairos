package com.kairos.app.domain.quote

import com.kairos.app.data.Profile
import com.kairos.app.data.QuoteSignal
import com.kairos.app.data.catalog.Quote
import com.kairos.app.data.catalog.QuoteCatalog
import java.time.LocalDate
import kotlin.math.abs

/**
 * The quote engine.
 *
 * The day's quote is deterministic: the same date and the same taste produce the same line,
 * so the app can be reopened all day without the thought changing under you. Taste comes from
 * three places — what you said you like at onboarding, which quotes you have loved, and which
 * ones you have waved away. Novelty keeps it from becoming a greatest-hits loop.
 *
 * No network, no model, no randomness: a small, explainable ranking you can read.
 */
object QuoteEngine {

    data class Selection(
        val quote: Quote,
        val reason: String,
        val score: Double,
    )

    data class Taste(
        val themes: Set<String>,
        val loved: Set<String>,
        val dismissed: Set<String>,
        val lovedThemes: Map<String, Int>,
    )

    fun taste(profile: Profile, signals: List<QuoteSignal>): Taste {
        val lovedIds = signals.filter { it.loved }.map { it.quoteId }.toSet()
        val dismissedIds = signals.filterNot { it.loved }.map { it.quoteId }.toSet()
        val lovedThemes = lovedIds.mapNotNull { QuoteCatalog.byId[it]?.theme }
            .groupingBy { it }
            .eachCount()
        return Taste(
            themes = profile.quoteThemes,
            loved = lovedIds,
            dismissed = dismissedIds,
            lovedThemes = lovedThemes,
        )
    }

    /**
     * Rank every quote for [date] and return the best. Ties break on id so the result is stable.
     */
    fun forDay(
        date: LocalDate,
        taste: Taste,
        exclude: Set<String> = emptySet(),
        catalog: List<Quote> = QuoteCatalog.all,
    ): Selection {
        val seed = date.toEpochDay()
        val ranked = catalog
            .filterNot { it.id in exclude }
            .map { quote -> quote to score(quote, taste, seed) }
            .sortedWith(compareByDescending<Pair<Quote, Double>> { it.second }.thenBy { it.first.id })

        val winner = ranked.firstOrNull() ?: (catalog.first() to 0.0)
        return Selection(
            quote = winner.first,
            reason = explain(winner.first, taste),
            score = winner.second,
        )
    }

    /** A few good options for the day, used by the swipe/again affordance. */
    fun shortlist(
        date: LocalDate,
        taste: Taste,
        count: Int = 5,
    ): List<Quote> {
        val seed = date.toEpochDay()
        return QuoteCatalog.all
            .map { it to score(it, taste, seed) }
            .sortedWith(compareByDescending<Pair<Quote, Double>> { it.second }.thenBy { it.first.id })
            .take(count)
            .map { it.first }
    }

    /**
     * Score in roughly 0..10.
     *
     *  - theme affinity from onboarding: +2.2
     *  - themes you have loved before: +0.9 each, capped at +2.7
     *  - novelty: unseen quotes get +1.1, dismissed ones lose 2.4
     *  - serendipity: a stable per-day jitter so the same taste does not produce a rut
     */
    internal fun score(quote: Quote, taste: Taste, seed: Long): Double {
        var score = 1.0
        if (quote.theme in taste.themes) score += 2.2
        score += (taste.lovedThemes[quote.theme] ?: 0).coerceAtMost(3) * 0.9
        if (quote.id in taste.loved) score -= 1.2 // don't repeat a line you just loved
        if (quote.id in taste.dismissed) score -= 2.4
        if (quote.id !in taste.loved && quote.id !in taste.dismissed) score += 1.1

        // Stable pseudo-random jitter in [-0.75, 0.75].
        val hash = mix(seed * 1_000_003L + quote.id.hashCode().toLong())
        val jitter = ((hash % 1500L) / 1000.0) - 0.75
        score += jitter

        // Slight preference for lines that fit a phone screen without a scroll.
        if (quote.text.length in 60..170) score += 0.25
        return score
    }

    private fun explain(quote: Quote, taste: Taste): String = when {
        quote.id in taste.loved -> "one you loved, back for a second look"
        quote.theme in taste.lovedThemes && taste.themes.contains(quote.theme) ->
            "because you keep returning to ${quote.theme}"
        quote.theme in taste.themes -> "picked for your ${quote.theme} shelf"
        taste.themes.isEmpty() -> "today's pick from the reading room"
        else -> "a small change of air"
    }

    /** 64-bit avalanche so adjacent days produce unrelated jitter. */
    internal fun mix(input: Long): Long {
        var z = input + -0x61c8864680b583ebL
        z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L
        z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L
        return z xor (z ushr 31)
    }

    /** Days since the user joined, for the "day N" line. */
    fun dayNumber(joinedOn: String, today: LocalDate): Int {
        if (joinedOn.isBlank()) return 1
        return runCatching {
            abs(today.toEpochDay() - LocalDate.parse(joinedOn).toEpochDay()).toInt() + 1
        }.getOrDefault(1)
    }
}
