package com.kairos.app.domain.vocab

import com.kairos.app.data.Profile
import com.kairos.app.data.WordMemory
import com.kairos.app.data.catalog.VocabCatalog
import com.kairos.app.data.catalog.Word
import java.time.LocalDate
import kotlin.random.Random

/**
 * Builds the sitting.
 *
 * Order matters more than volume: overdue reviews first (they are already half-forgotten),
 * then words you have saved, then fresh words matched to the themes you chose. The queue is
 * deterministic for a given day, so leaving and returning does not reshuffle your sitting.
 */
object SessionBuilder {

    data class Card(
        val word: Word,
        val memory: WordMemory,
        val reason: String,
        val isNew: Boolean,
    )

    data class Session(
        val cards: List<Card>,
        val dueCount: Int,
        val newCount: Int,
    )

    fun build(
        date: LocalDate,
        memories: Map<String, WordMemory>,
        profile: Profile,
        now: Long,
        size: Int = profile.dailyPace.coerceIn(3, 12),
        catalog: List<Word> = VocabCatalog.all,
    ): Session {
        val byId = catalog.associateBy { it.id }
        val known = catalog.mapNotNull { word ->
            memories[word.id]?.takeIf { it.seen > 0 }?.let { word to it }
        }

        val due = known
            .filter { (_, memory) -> SrsEngine.isDue(memory, now) }
            .sortedWith(compareBy<Pair<Word, WordMemory>> { it.second.dueAt }.thenBy { it.first.term })
            .map { (word, memory) ->
                Card(
                    word = word,
                    memory = memory,
                    reason = if (memory.lapses > 0) "a wobbly one — worth another look" else "due today",
                    isNew = false,
                )
            }

        val saved = known
            .filter { (_, memory) -> memory.saved && !SrsEngine.isDue(memory, now) }
            .sortedBy { it.second.lastReviewedAt }
            .map { (word, memory) ->
                Card(word, memory, "you saved this one", isNew = false)
            }

        val newPool = catalog
            .filter { (memories[it.id]?.seen ?: 0) == 0 }
            .filter { word -> profile.interests.isEmpty() || word.themes.any { it in profile.interests } }
            .shuffled(Random(date.toEpochDay() * 31 + 7))
            .map { word ->
                Card(
                    word = word,
                    memory = memories[word.id] ?: WordMemory(wordId = word.id),
                    reason = "new for you",
                    isNew = true,
                )
            }

        val queue = LinkedHashMap<String, Card>()
        (due + saved + newPool).forEach { card ->
            if (queue.size < size) queue.putIfAbsent(card.word.id, card)
        }

        // If the catalog is exhausted of new words, top up with the least recently seen.
        if (queue.size < size) {
            known.sortedBy { it.second.lastReviewedAt }
                .forEach { (word, memory) ->
                    if (queue.size < size) {
                        queue.putIfAbsent(
                            word.id,
                            Card(word, memory, "a gentle revisit", isNew = false),
                        )
                    }
                }
        }

        return Session(
            cards = queue.values.toList(),
            dueCount = due.size,
            newCount = queue.values.count { it.isNew },
        )
    }

    /** Three definitions, one of them right. Distractors share a part of speech where possible. */
    fun quiz(card: Card, catalog: List<Word> = VocabCatalog.all, seed: Long = 0L): Quiz {
        val pool = catalog.filter { it.id != card.word.id }
        val sameKind = pool.filter { it.partOfSpeech == card.word.partOfSpeech }
        val distractors = (if (sameKind.size >= 3) sameKind else pool)
            .shuffled(Random(seed * 17 + card.word.id.hashCode()))
            .take(3)
        val options = (distractors.map { it.definition } + card.word.definition)
            .shuffled(Random(seed * 29 + 3))
        return Quiz(word = card.word, options = options, answer = card.word.definition)
    }

    data class Quiz(
        val word: Word,
        val options: List<String>,
        val answer: String,
    ) {
        val correctIndex: Int get() = options.indexOf(answer)
    }

    /** Summary numbers for the review-complete screen. */
    data class Outcome(
        val reviewed: Int,
        val newWords: Int,
        val again: Int,
        val minutes: Int,
    )
}
