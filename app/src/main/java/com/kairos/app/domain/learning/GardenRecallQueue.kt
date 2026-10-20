package com.kairos.app.domain.learning

/** Due memories first, saved discoveries next. Never flood a small daily ritual. */
object GardenRecallQueue {
    data class Seed(val id: Long, val saved: Boolean, val learned: Boolean, val nextReviewAt: Long?)
    fun select(seeds: List<Seed>, now: Long, limit: Int = 10): List<Long> = seeds
        .distinctBy { it.id }
        .filter { it.nextReviewAt == null || it.nextReviewAt <= now }
        .sortedWith(compareBy<Seed>(
            { if (it.nextReviewAt != null) 0 else if (it.saved) 1 else if (!it.learned) 2 else 3 },
            { it.nextReviewAt ?: Long.MAX_VALUE }, { it.id }
        )).take(limit.coerceIn(0,20)).map { it.id }
}
