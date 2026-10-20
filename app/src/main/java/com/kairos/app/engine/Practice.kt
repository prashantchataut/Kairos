package com.kairos.app.engine

import com.kairos.app.data.content.Word
import com.kairos.app.data.db.WordProgress
import kotlin.random.Random

sealed interface PracticeCard {
    val word: Word
    data class Flash(override val word: Word) : PracticeCard
    data class Choice(override val word: Word, val options: List<String>, val answer: Int) : PracticeCard
    data class Fill(override val word: Word, val sentence: String, val options: List<String>, val answer: Int) : PracticeCard
}

object Practice {
    /**
     * Builds a session: due reviews first, then saved-but-unpractised, then fresh words close to
     * your level. Mixes card types so it never feels like a worksheet.
     */
    fun buildQueue(
        words: List<Word>,
        progress: Map<String, WordProgress>,
        today: Long,
        size: Int,
        level: Int,
        interests: Set<String>,
        rnd: Random = Random(today),
    ): List<Word> {
        val due = words.filter { progress[it.id]?.let { p -> SpacedRepetition.isDue(p, today) } == true }
            .sortedBy { progress[it.id]!!.dueEpochDay }
        val savedNew = words.filter { progress[it.id]?.saved == true && (progress[it.id]?.lastReviewedEpochDay ?: -1) < 0 }
        val fresh = words.filter { (progress[it.id]?.lastReviewedEpochDay ?: -1) < 0 && it !in savedNew }
            .sortedByDescending { (if (it.theme in interests) 2 else 0) + (if (it.level == level) 1 else 0) + rnd.nextDouble() }
        return (due + savedNew.shuffled(rnd) + fresh).distinctBy { it.id }.take(size)
    }

    fun makeCard(word: Word, all: List<Word>, index: Int, rnd: Random): PracticeCard {
        val others = all.filter { it.id != word.id }
        return when (index % 3) {
            0 -> PracticeCard.Flash(word)
            1 -> {
                val distractors = (others.filter { it.pos == word.pos }.shuffled(rnd) + others.shuffled(rnd))
                    .distinctBy { it.id }.take(3).map { it.definition }
                val options = (distractors + word.definition).shuffled(rnd)
                PracticeCard.Choice(word, options, options.indexOf(word.definition))
            }
            else -> {
                val blanked = blank(word.example, word.word)
                if (blanked == null) {
                    PracticeCard.Flash(word)
                } else {
                    val distractors = (others.filter { it.pos == word.pos }.shuffled(rnd) + others.shuffled(rnd))
                        .distinctBy { it.id }.take(3).map { it.word }
                    val options = (distractors + word.word).shuffled(rnd)
                    PracticeCard.Fill(word, blanked, options, options.indexOf(word.word))
                }
            }
        }
    }

    /** Replaces the word (or a simple inflection of it) in the example with a blank. */
    fun blank(example: String, word: String): String? {
        val stem = word.lowercase().removeSuffix("e")
        val regex = Regex("\\b${Regex.escape(stem)}[a-z]*\\b", RegexOption.IGNORE_CASE)
        val match = regex.find(example) ?: return null
        return example.replaceRange(match.range, "_____")
    }
}
