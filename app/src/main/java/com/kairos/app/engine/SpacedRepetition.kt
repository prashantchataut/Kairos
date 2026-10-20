package com.kairos.app.engine

import com.kairos.app.data.db.WordProgress
import kotlin.math.max
import kotlin.math.roundToInt

enum class Grade(val label: String, val q: Int) {
    Again("Again", 1),
    Hard("Hard", 3),
    Good("Good", 4),
    Easy("Easy", 5),
}

/**
 * Words grow like plants instead of being "completed": a seed you've never practised, a sprout
 * you're learning, a bud that's sticking, and a bloom you'll remember for weeks.
 */
enum class Growth(val label: String) { Seed("Seed"), Sprout("Sprout"), Bud("Bud"), Bloom("Bloom") }

object SpacedRepetition {
    const val MIN_EASE = 1.3

    /** SM-2, adapted to four buttons and a gentler first few steps. */
    fun review(p: WordProgress, grade: Grade, today: Long): WordProgress {
        val q = grade.q
        val ease = max(MIN_EASE, p.ease + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)))
        if (grade == Grade.Again) {
            return p.copy(
                seen = true,
                repetitions = 0,
                ease = ease,
                intervalDays = 0,
                dueEpochDay = today, // comes back in the same session / today
                lapses = p.lapses + 1,
                lastReviewedEpochDay = today,
            )
        }
        val reps = p.repetitions + 1
        val interval = when (reps) {
            1 -> when (grade) { Grade.Easy -> 3; else -> 1 }
            2 -> when (grade) { Grade.Hard -> 3; Grade.Good -> 4; else -> 6 }
            else -> {
                val mult = when (grade) { Grade.Hard -> 0.8; Grade.Easy -> 1.3; else -> 1.0 }
                max(p.intervalDays + 1, (p.intervalDays * ease * mult).roundToInt())
            }
        }
        return p.copy(
            seen = true,
            repetitions = reps,
            ease = ease,
            intervalDays = interval,
            dueEpochDay = today + interval,
            lastReviewedEpochDay = today,
        )
    }

    fun growth(p: WordProgress?): Growth = when {
        p == null || p.lastReviewedEpochDay < 0 -> Growth.Seed
        p.intervalDays < 4 -> Growth.Sprout
        p.intervalDays < 21 -> Growth.Bud
        else -> Growth.Bloom
    }

    fun isDue(p: WordProgress, today: Long) = p.lastReviewedEpochDay >= 0 && p.dueEpochDay <= today

    /** Human preview of when a grade would bring the card back. */
    fun preview(p: WordProgress, grade: Grade, today: Long): String {
        val next = review(p, grade, today)
        return when (val d = next.dueEpochDay - today) {
            0L -> "now"
            1L -> "1 day"
            in 2..29 -> "$d days"
            else -> "${d / 30} mo"
        }
    }
}
