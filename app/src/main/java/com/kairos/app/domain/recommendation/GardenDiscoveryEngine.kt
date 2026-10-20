package com.kairos.app.domain.recommendation

/**
 * Discovery is different from recall. Protect the daily discovery from repeating
 * yesterday's item even when a due review scores highly; recall has its own queue.
 * If a small catalog is exhausted, return the least recently seen item.
 */
object GardenDiscoveryEngine {
    fun choose(ranked: List<RankedCandidate>, context: RecommendationContext): RankedCandidate? {
        if (ranked.isEmpty()) return null
        val history = context.recentSelections
            .filter { it.type == ranked.first().candidate.type }
            .groupBy { it.contentId }.mapValues { (_, visits) -> visits.maxOf { it.selectedAt } }
        return ranked.firstOrNull { it.candidate.id !in history }
            ?: ranked.minWithOrNull(compareBy<RankedCandidate> { history[it.candidate.id] ?: Long.MIN_VALUE }
                .thenByDescending { it.score }.thenBy { it.candidate.id })
    }
}
