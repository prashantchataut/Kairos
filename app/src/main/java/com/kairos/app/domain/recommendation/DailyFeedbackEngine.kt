package com.kairos.app.domain.recommendation

import java.util.Locale
import kotlin.math.pow

/** Explicit family-scoped preferences. Old signals fade; repeated events cannot dominate forever. */
data class FeedbackSignal(
    val type: DailyContentType,
    val interaction: ContentInteractionType,
    val category: String,
    val source: String,
    val timestamp: Long
)
data class DailyFeedbackProfile(val categories: Map<String, Double>, val sources: Map<String, Double>, val difficultyDelta: Int)

object DailyFeedbackEngine {
    fun profile(type: DailyContentType, signals: List<FeedbackSignal>, now: Long): DailyFeedbackProfile {
        val relevant = signals.filter { it.type == type && it.timestamp <= now }
        val categories = mutableMapOf<String, Double>()
        val sources = mutableMapOf<String, Double>()
        relevant.forEach { signal ->
            val weight = when (signal.interaction) {
                ContentInteractionType.SAVED -> 3.0
                ContentInteractionType.UNSAVED -> -3.0
                ContentInteractionType.MORE_LIKE_THIS -> 5.0
                ContentInteractionType.LESS_LIKE_THIS -> -5.0
                ContentInteractionType.USED_IN_JOURNAL -> 4.0
                ContentInteractionType.COMPLETED -> 2.0
                ContentInteractionType.DISMISSED -> -2.0
                ContentInteractionType.OPENED -> .5
                else -> 0.0
            }
            val ageDays = (now - signal.timestamp).toDouble() / 86_400_000
            val decayed = weight * .5.pow(ageDays / 30)
            fun accumulate(target: MutableMap<String, Double>, key: String, value: Double) {
                val normalized = key.trim().lowercase(Locale.ROOT)
                if (normalized.isNotBlank()) target[normalized] = (target[normalized] ?: 0.0) + value
            }
            accumulate(categories, signal.category, decayed)
            accumulate(sources, signal.source, decayed * .5)
        }
        val difficulty = if (type == DailyContentType.VOCABULARY) relevant
            .filter { it.interaction == ContentInteractionType.TOO_EASY || it.interaction == ContentInteractionType.TOO_HARD }
            .sortedByDescending { it.timestamp }.take(12)
            .sumOf { if (it.interaction == ContentInteractionType.TOO_EASY) 1 else -1 }.coerceIn(-1, 1) else 0
        return DailyFeedbackProfile(categories.mapValues { it.value.coerceIn(-10.0, 10.0) }, sources.mapValues { it.value.coerceIn(-10.0, 10.0) }, difficulty)
    }
}
