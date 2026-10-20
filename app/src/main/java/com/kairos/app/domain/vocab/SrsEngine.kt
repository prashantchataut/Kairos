package com.kairos.app.domain.vocab

import com.kairos.app.data.Grade
import com.kairos.app.data.WordMemory
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Spaced repetition, SM-2 with two changes that matter on a phone:
 *
 *  - **Again** comes back in ten minutes, not tomorrow. A wrong card should be re-met inside
 *    the same sitting, while the correction is still warm.
 *  - **Easy** grows faster than **Good**, so a word you already own stops wasting your time.
 *
 * The scheduler is a pure function of (memory, grade, now). That makes it testable, and it means
 * the UI can show exactly what will happen next before you tap.
 */
object SrsEngine {

    const val DAY_MS = 86_400_000L
    const val AGAIN_DELAY_MS = 10 * 60 * 1000L
    const val MIN_EASE = 1.3
    const val MAX_EASE = 3.1
    const val LEARNED_INTERVAL_DAYS = 21

    fun review(memory: WordMemory, grade: Grade, now: Long): WordMemory {
        val ease = memory.ease.coerceIn(MIN_EASE, MAX_EASE)
        return when (grade) {
            Grade.Again -> memory.copy(
                ease = max(MIN_EASE, ease - 0.20),
                intervalDays = 0,
                repetitions = 0,
                lapses = memory.lapses + 1,
                dueAt = now + AGAIN_DELAY_MS,
                lastReviewedAt = now,
                seen = memory.seen + 1,
                lastGrade = grade.name,
            )

            Grade.Hard -> {
                val nextInterval = if (memory.intervalDays <= 0) 1
                else max(1, (memory.intervalDays * 1.2).roundToInt())
                memory.copy(
                    ease = max(MIN_EASE, ease - 0.15),
                    intervalDays = nextInterval,
                    repetitions = memory.repetitions + 1,
                    dueAt = now + nextInterval * DAY_MS,
                    lastReviewedAt = now,
                    seen = memory.seen + 1,
                    lastGrade = grade.name,
                )
            }

            Grade.Good -> {
                val nextInterval = when {
                    memory.intervalDays <= 0 -> 3
                    else -> max(1, (memory.intervalDays * ease).roundToInt())
                }
                memory.copy(
                    ease = ease,
                    intervalDays = nextInterval,
                    repetitions = memory.repetitions + 1,
                    dueAt = now + nextInterval * DAY_MS,
                    lastReviewedAt = now,
                    seen = memory.seen + 1,
                    lastGrade = grade.name,
                )
            }

            Grade.Easy -> {
                val nextInterval = when {
                    memory.intervalDays <= 0 -> 5
                    else -> max(2, (memory.intervalDays * ease * 1.35).roundToInt())
                }
                memory.copy(
                    ease = min(MAX_EASE, ease + 0.15),
                    intervalDays = nextInterval,
                    repetitions = memory.repetitions + 1,
                    dueAt = now + nextInterval * DAY_MS,
                    lastReviewedAt = now,
                    seen = memory.seen + 1,
                    lastGrade = grade.name,
                )
            }
        }
    }

    /** 0..1, used by the ring on a word card and by the "words I own" count. */
    fun mastery(memory: WordMemory): Float {
        if (memory.seen == 0) return 0f
        val intervalScore = (memory.intervalDays / 30f).coerceIn(0f, 1f)
        val easeScore = ((memory.ease - MIN_EASE) / (MAX_EASE - MIN_EASE)).toFloat().coerceIn(0f, 1f)
        val repetitionScore = (memory.repetitions / 6f).coerceIn(0f, 1f)
        val lapsePenalty = (memory.lapses * 0.05f).coerceAtMost(0.25f)
        return ((intervalScore * 0.5f + easeScore * 0.25f + repetitionScore * 0.25f) - lapsePenalty)
            .coerceIn(0f, 1f)
    }

    fun isLearned(memory: WordMemory): Boolean = memory.intervalDays >= LEARNED_INTERVAL_DAYS

    fun isDue(memory: WordMemory, now: Long): Boolean = memory.seen > 0 && memory.dueAt <= now

    /** Human copy for what a grade will do, shown under the grade buttons. */
    fun preview(memory: WordMemory, grade: Grade, now: Long): String {
        val next = review(memory, grade, now)
        val delta = next.dueAt - now
        return when {
            delta <= 0 -> "now"
            delta < 3_600_000L -> "${delta / 60_000L} min"
            delta < 2 * DAY_MS -> "${delta / DAY_MS} day"
            delta < 30 * DAY_MS -> "${delta / DAY_MS} days"
            delta < 365 * DAY_MS -> "${delta / (30 * DAY_MS)} mo"
            else -> "${delta / (365 * DAY_MS)} yr"
        }
    }

    /** The date a word becomes "learned", used for the little milestone toast. */
    fun learnedOn(memory: WordMemory): Long? =
        if (isLearned(memory)) memory.lastReviewedAt else null
}
