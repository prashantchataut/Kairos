package com.kairos.app.engine

import kotlin.math.max
import kotlin.math.roundToInt

/** How well the person remembered a card. Maps to SM-2 quality scores. */
enum class Recall(val quality: Int, val label: String) {
    Again(1, "Again"), Hard(3, "Hard"), Good(4, "Good"), Easy(5, "Easy")
}

data class SrsState(val reps: Int, val ease: Double, val intervalDays: Int, val lapses: Int)

/**
 * SuperMemo-2 with two small kindnesses: "Hard" still moves forward (just slowly), and a lapse
 * keeps half of the old interval knowledge in the ease rather than resetting it to the floor.
 */
object Srs {
    const val MIN_EASE = 1.3

    fun review(state: SrsState, recall: Recall): SrsState {
        val q = recall.quality
        val ease = max(MIN_EASE, state.ease + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)))
        if (recall == Recall.Again) {
            return SrsState(reps = 0, ease = max(MIN_EASE, state.ease - 0.2), intervalDays = 1, lapses = state.lapses + 1)
        }
        val interval = when (state.reps) {
            0 -> if (recall == Recall.Easy) 3 else 1
            1 -> if (recall == Recall.Hard) 3 else 6
            else -> {
                val factor = when (recall) {
                    Recall.Hard -> 1.2
                    Recall.Easy -> ease * 1.3
                    else -> ease
                }
                max(state.intervalDays + 1, (state.intervalDays * factor).roundToInt())
            }
        }
        return SrsState(reps = state.reps + 1, ease = ease, intervalDays = interval, lapses = state.lapses)
    }

    /** Short preview for grade buttons: "1d", "6d", "2mo". */
    fun preview(state: SrsState, recall: Recall): String = formatInterval(review(state, recall).intervalDays)

    fun formatInterval(days: Int): String = when {
        days < 1 -> "now"
        days < 30 -> "${days}d"
        days < 365 -> "${(days / 30.0).roundToInt()}mo"
        else -> "${(days / 365.0).roundToInt()}y"
    }
}
