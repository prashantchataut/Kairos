package com.kairos.app.domain.progress

import com.kairos.app.data.AppState
import com.kairos.app.data.DayLog
import com.kairos.app.data.WordMemory
import com.kairos.app.domain.vocab.SrsEngine
import java.time.LocalDate

/** Streaks, weeks and counts — computed from the log, never stored as a mutable counter. */
object Progress {

    data class DayMark(val date: LocalDate, val active: Boolean, val isToday: Boolean, val mood: String?)

    data class Stats(
        val wordsMet: Int,
        val wordsOwned: Int,
        val wordsLearning: Int,
        val reviews: Int,
        val capsules: Int,
        val saved: Int,
        val sessions: Int,
        val bestStreak: Int,
    )

    fun isActive(log: DayLog?): Boolean {
        if (log == null) return false
        return log.reviewed > 0 || log.wordsMet > 0 || log.quotesRead > 0 ||
            log.mood != null || log.capsulesSealed > 0
    }

    /** Consecutive active days ending today, or ending yesterday if today is still quiet. */
    fun streak(days: Map<String, DayLog>, today: LocalDate): Int {
        var cursor = today
        if (!isActive(days[cursor.toString()])) {
            cursor = cursor.minusDays(1)
        }
        var count = 0
        while (isActive(days[cursor.toString()]) && count < 3650) {
            count++
            cursor = cursor.minusDays(1)
        }
        return count
    }

    fun bestStreak(days: Map<String, DayLog>): Int {
        val activeDays = days.values.filter { isActive(it) }
            .mapNotNull { runCatching { LocalDate.parse(it.day) }.getOrNull() }
            .sorted()
        var best = 0
        var run = 0
        var previous: LocalDate? = null
        for (day in activeDays) {
            run = if (previous != null && previous.plusDays(1) == day) run + 1 else 1
            best = maxOf(best, run)
            previous = day
        }
        return best
    }

    fun week(days: Map<String, DayLog>, today: LocalDate): List<DayMark> =
        (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val log = days[date.toString()]
            DayMark(
                date = date,
                active = isActive(log),
                isToday = offset == 0,
                mood = log?.mood,
            )
        }

    fun stats(state: AppState): Stats {
        val memories = state.memories.values
        return Stats(
            wordsMet = memories.count { it.seen > 0 },
            wordsOwned = memories.count { SrsEngine.isLearned(it) },
            wordsLearning = memories.count { it.seen > 0 && !SrsEngine.isLearned(it) },
            reviews = state.days.values.sumOf { it.reviewed },
            capsules = state.capsules.size,
            saved = memories.count { it.saved },
            sessions = state.sessionLog.size,
            bestStreak = bestStreak(state.days),
        )
    }

    fun dueCount(memories: Map<String, WordMemory>, now: Long): Int =
        memories.values.count { SrsEngine.isDue(it, now) }
}
