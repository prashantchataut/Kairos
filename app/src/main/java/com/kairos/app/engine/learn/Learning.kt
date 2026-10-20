package com.kairos.app.engine.learn

import com.kairos.app.data.db.WordProgressEntity
import com.kairos.app.engine.content.Word
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

enum class Grade(val quality: Int, val label: String) {
    Again(1, "Again"), Hard(3, "Hard"), Good(4, "Good"), Easy(5, "Easy"),
}

enum class Stage(val label: String, val blurb: String) {
    Seed("Seed", "Just planted"),
    Sprout("Sprout", "Coming back to you"),
    Bloom("Bloom", "Mostly yours"),
    Rooted("Rooted", "Part of you now"),
}

/** SM-2, tuned slightly gentler for a daily app: a lapse costs ease, never progress shame. */
object Srs {
    fun review(p: WordProgressEntity, grade: Grade, today: Long, now: Long): WordProgressEntity {
        val q = grade.quality
        val ease = max(1.3, p.ease + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)))
        return if (q < 3) {
            p.copy(ease = ease, reps = 0, lapses = p.lapses + 1, intervalDays = 0, dueEpochDay = today, lastReviewedAt = now, inDeck = true)
        } else {
            val reps = p.reps + 1
            val base = when (reps) {
                1 -> 1
                2 -> 3
                else -> (max(1, p.intervalDays) * ease).roundToInt()
            }
            val interval = when (grade) {
                Grade.Easy -> (base * 1.35).roundToInt().coerceAtLeast(base + 1)
                Grade.Hard -> max(1, (base * 0.7).roundToInt())
                else -> base
            }.coerceAtMost(365)
            p.copy(ease = ease, reps = reps, intervalDays = interval, dueEpochDay = today + interval, lastReviewedAt = now, inDeck = true)
        }
    }

    fun stage(p: WordProgressEntity?): Stage = when {
        p == null || !p.inDeck || p.reps == 0 -> Stage.Seed
        p.intervalDays < 7 -> Stage.Sprout
        p.intervalDays < 21 -> Stage.Bloom
        else -> Stage.Rooted
    }

    fun isDue(p: WordProgressEntity, today: Long) = p.inDeck && p.dueEpochDay <= today
}

sealed interface Exercise {
    val word: Word

    data class Flashcard(override val word: Word) : Exercise
    data class Meaning(override val word: Word, val options: List<Word>) : Exercise
    data class Cloze(override val word: Word, val sentence: String, val options: List<Word>) : Exercise
    data class Unscramble(override val word: Word, val letters: List<Char>) : Exercise
}

/**
 * Builds a short, varied session: due reviews first, then a few new words. Each word appears
 * once, as the exercise that best tests it — recognition for new words, recall for familiar ones.
 */
object SessionBuilder {
    fun build(
        due: List<Word>,
        fresh: List<Word>,
        pool: List<Word>,
        progress: Map<String, WordProgressEntity>,
        size: Int = 8,
        random: Random = Random.Default,
    ): List<Exercise> {
        val chosen = (due.shuffled(random).take(size) + fresh.shuffled(random)).distinctBy { it.id }.take(size)
        return chosen.mapIndexed { i, w ->
            val reps = progress[w.id]?.reps ?: 0
            val distractors = distractorsFor(w, pool, random)
            val cloze = clozeOf(w)
            when {
                reps == 0 && i % 2 == 0 -> Exercise.Flashcard(w)
                reps == 0 -> Exercise.Meaning(w, (distractors + w).shuffled(random))
                cloze != null && reps % 2 == 1 -> Exercise.Cloze(w, cloze, (distractors + w).shuffled(random))
                w.word.length in 4..11 && !w.word.contains(' ') && reps >= 2 -> Exercise.Unscramble(w, scramble(w.word, random))
                else -> Exercise.Meaning(w, (distractors + w).shuffled(random))
            }
        }
    }

    fun distractorsFor(word: Word, pool: List<Word>, random: Random): List<Word> {
        val samePos = pool.filter { it.id != word.id && it.pos == word.pos && it.word != word.word }
        val source = if (samePos.size >= 3) samePos else pool.filter { it.id != word.id }
        return source.shuffled(random).take(3)
    }

    /** Blanks the word (or an inflection of it) out of its example sentence. */
    fun clozeOf(word: Word): String? {
        val stem = word.word.lowercase().let { if (it.length > 5) it.dropLast(2) else it }
        val regex = Regex("\\b(${Regex.escape(stem)}[a-z]*)\\b", RegexOption.IGNORE_CASE)
        val match = regex.find(word.example) ?: return null
        return word.example.replaceRange(match.range, "_____")
    }

    fun scramble(text: String, random: Random): List<Char> {
        val chars = text.lowercase().toList()
        if (chars.size < 2) return chars
        var s: List<Char>
        var guard = 0
        do {
            s = chars.shuffled(random)
            guard++
        } while (s == chars && guard < 10)
        return s
    }
}
