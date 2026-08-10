package com.kairos.app.data.repository

import com.kairos.app.data.local.dao.DailyContentDao
import com.kairos.app.data.local.dao.QuoteDao
import com.kairos.app.data.local.dao.VocabularyDao
import com.kairos.app.data.local.dao.VocabularyLearningDao
import com.kairos.app.data.local.entity.ContentInteractionEntity
import com.kairos.app.data.local.entity.DailyContentSelectionEntity
import com.kairos.app.data.local.entity.QuoteEntity
import com.kairos.app.data.local.entity.VocabularyEntity
import com.kairos.app.data.local.entity.VocabularyLearningEntity
import com.kairos.app.data.local.preferences.PreferencesManager
import com.kairos.app.domain.model.DailyPlan
import com.kairos.app.domain.model.DailyQuote
import com.kairos.app.domain.model.DailyWord
import com.kairos.app.domain.model.RecommendedDailyItem
import com.kairos.app.domain.recommendation.CandidateHistory
import com.kairos.app.domain.recommendation.ContentInteractionType
import com.kairos.app.domain.recommendation.DailyContentType
import com.kairos.app.domain.recommendation.ExplainableRecommendationRanker
import com.kairos.app.domain.recommendation.InteractionSignal
import com.kairos.app.domain.recommendation.RecommendationCandidate
import com.kairos.app.domain.recommendation.RecommendationContext
import com.kairos.app.domain.repository.DailyPlanRepository
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyPlanRepositoryImpl @Inject constructor(
    private val vocabularyDao: VocabularyDao,
    private val vocabularyLearningDao: VocabularyLearningDao,
    private val quoteDao: QuoteDao,
    private val dailyContentDao: DailyContentDao,
    private val preferencesManager: PreferencesManager,
    private val ranker: ExplainableRecommendationRanker
) : DailyPlanRepository {

    override suspend fun getOrCreateDailyPlan(
        userId: String,
        localDate: LocalDate,
        now: Long
    ): DailyPlan {
        val dateKey = localDate.toString()
        var existing = dailyContentDao.getSelectionsForDate(userId, dateKey)
        val existingPlan = hydratePlan(localDate, existing)
        if (existingPlan.word != null && existingPlan.quote != null) return existingPlan

        // A catalog update may remove an item selected earlier. Remove only the
        // broken pointer and rebuild that part of the plan; keep valid decisions stable.
        if (existingPlan.word == null && existing.any { it.contentType == DailyContentType.VOCABULARY.name }) {
            dailyContentDao.deleteSelection(userId, dateKey, DailyContentType.VOCABULARY.name)
        }
        if (existingPlan.quote == null && existing.any { it.contentType == DailyContentType.QUOTE.name }) {
            dailyContentDao.deleteSelection(userId, dateKey, DailyContentType.QUOTE.name)
        }
        existing = dailyContentDao.getSelectionsForDate(userId, dateKey)

        val vocabulary = vocabularyDao.getAllCuratedVocabularySync()
        val quotes = quoteDao.getAllQuotesSync()
        val learning = vocabularyLearningDao.getAllLearningEntriesForUserSync(userId)
            .associateBy { it.wordId }
        val recentSelections = dailyContentDao.getRecentSelections(userId, now - SELECTION_LOOKBACK_MS)
        val recentInteractions = dailyContentDao.getRecentInteractions(userId, now - INTERACTION_LOOKBACK_MS)
        // Word and quote interests are chosen separately during setup, so the
        // daily word and the daily quote each rank against their own themes.
        val quoteCategories = preferencesManager.selectedWisdomCategories.first()
        val wordCategories = preferencesManager.preferredWordCategories.first()
        val targetDifficulty = preferencesManager.vocabularyDifficulty.first().coerceIn(1, 5)
        val feedback = FeedbackProfile.from(recentInteractions)

        val quoteContext = RecommendationContext(
            userId = userId,
            epochDay = localDate.toEpochDay(),
            now = now,
            targetDifficulty = (targetDifficulty + feedback.difficultyDelta).coerceIn(1, 5),
            preferredCategories = quoteCategories.associate { it.normalized() to 1.0 },
            recentSelections = recentSelections.mapNotNull { it.toCandidateHistoryOrNull() },
            categoryFeedback = feedback.categoryScores,
            sourceFeedback = feedback.sourceScores,
            temporalCategories = temporalCategories(localDate.dayOfWeek)
        )

        val wordContext = quoteContext.copy(
            preferredCategories = wordCategories.associate { it.normalized() to 1.0 }
        )

        val selectionsToInsert = buildList {
            if (existing.none { it.contentType == DailyContentType.VOCABULARY.name }) {
                rankVocabulary(vocabulary, learning, wordContext)?.let { ranked ->
                    add(ranked.toSelection(userId, dateKey, now))
                }
            }
            if (existing.none { it.contentType == DailyContentType.QUOTE.name }) {
                rankQuotes(quotes, quoteContext)?.let { ranked ->
                    add(ranked.toSelection(userId, dateKey, now))
                }
            }
        }

        dailyContentDao.insertSelectionsIfAbsent(selectionsToInsert)
        return hydratePlan(localDate, dailyContentDao.getSelectionsForDate(userId, dateKey))
    }

    override suspend fun recordImpression(
        userId: String,
        localDate: LocalDate,
        type: DailyContentType,
        contentId: Long,
        now: Long
    ) {
        val changed = dailyContentDao.markImpressed(
            userId = userId,
            localDate = localDate.toString(),
            contentType = type.name,
            contentId = contentId,
            timestamp = now
        )
        if (changed > 0) {
            recordInteraction(
                userId = userId,
                localDate = localDate,
                type = type,
                contentId = contentId,
                interaction = ContentInteractionType.IMPRESSION,
                now = now
            )
        }
    }

    override suspend fun recordInteraction(
        userId: String,
        localDate: LocalDate,
        type: DailyContentType,
        contentId: Long,
        interaction: ContentInteractionType,
        category: String?,
        sourceKey: String?,
        difficulty: Int?,
        now: Long
    ) {
        val selection = dailyContentDao.getSelection(userId, localDate.toString(), type.name)
        dailyContentDao.insertInteraction(
            ContentInteractionEntity(
                userId = userId,
                localDate = localDate.toString(),
                contentType = type.name,
                contentId = contentId,
                category = category ?: selection?.category.orEmpty(),
                sourceKey = sourceKey ?: selection?.sourceKey.orEmpty(),
                difficulty = difficulty,
                interactionType = interaction.name,
                createdAt = now
            )
        )
        if (interaction in COMPLETION_SIGNALS) {
            dailyContentDao.markCompleted(
                userId = userId,
                localDate = localDate.toString(),
                contentType = type.name,
                contentId = contentId,
                timestamp = now
            )
        }
    }

    override suspend fun recentInteractionSignals(
        userId: String,
        sinceMillis: Long
    ): List<InteractionSignal> {
        return dailyContentDao.getRecentInteractions(userId, sinceMillis)
            .mapNotNull { it.toInteractionSignalOrNull() }
    }

    private fun ContentInteractionEntity.toInteractionSignalOrNull(): InteractionSignal? {
        val interaction = runCatching { ContentInteractionType.valueOf(interactionType) }.getOrNull() ?: return null
        return InteractionSignal(
            interaction = interaction,
            category = category.takeIf { it.isNotBlank() },
            sourceKey = sourceKey.takeIf { it.isNotBlank() },
            difficulty = difficulty,
            createdAt = createdAt
        )
    }

    private fun rankVocabulary(
        words: List<VocabularyEntity>,
        learning: Map<Long, VocabularyLearningEntity>,
        context: RecommendationContext
    ): RankedContent<VocabularyEntity>? {
        val byId = words.associateBy { it.id }
        val candidates = words.map { word ->
            val progress = learning[word.id]
            RecommendationCandidate(
                id = word.id,
                type = DailyContentType.VOCABULARY,
                category = word.category,
                sourceKey = word.partOfSpeech.ifBlank { "vocabulary" },
                difficulty = word.difficulty,
                quality = vocabularyQuality(word),
                reviewDueAt = progress?.nextReviewDate,
                isIntroduced = progress?.isIntroduced == true,
                isMastered = progress?.isMastered == true
            )
        }
        return ranker.rank(candidates, context).firstOrNull()?.let { ranked ->
            byId[ranked.candidate.id]?.let { RankedContent(it, ranked.candidate, ranked.score, ranked.reason) }
        }
    }

    private fun rankQuotes(
        quotes: List<QuoteEntity>,
        context: RecommendationContext
    ): RankedContent<QuoteEntity>? {
        val byId = quotes.associateBy { it.id }
        val candidates = quotes.map { quote ->
            RecommendationCandidate(
                id = quote.id,
                type = DailyContentType.QUOTE,
                category = quote.category,
                sourceKey = quote.author.ifBlank { "unknown" },
                quality = quoteQuality(quote)
            )
        }
        return ranker.rank(candidates, context).firstOrNull()?.let { ranked ->
            byId[ranked.candidate.id]?.let { RankedContent(it, ranked.candidate, ranked.score, ranked.reason) }
        }
    }

    private suspend fun hydratePlan(
        localDate: LocalDate,
        selections: List<DailyContentSelectionEntity>
    ): DailyPlan {
        val wordSelection = selections.firstOrNull { it.contentType == DailyContentType.VOCABULARY.name }
        val quoteSelection = selections.firstOrNull { it.contentType == DailyContentType.QUOTE.name }
        val word = wordSelection?.let { selection ->
            vocabularyDao.getWordById(selection.contentId)?.let { entity ->
                RecommendedDailyItem(
                    entity.toDailyWord(),
                    DailyContentType.VOCABULARY,
                    selection.score,
                    selection.reason,
                    selection.impressedAt,
                    selection.completedAt
                )
            }
        }
        val quote = quoteSelection?.let { selection ->
            quoteDao.getQuoteById(selection.contentId)?.let { entity ->
                RecommendedDailyItem(
                    entity.toDailyQuote(),
                    DailyContentType.QUOTE,
                    selection.score,
                    selection.reason,
                    selection.impressedAt,
                    selection.completedAt
                )
            }
        }
        return DailyPlan(
            localDate = localDate,
            word = word,
            quote = quote,
            algorithmVersion = selections.maxOfOrNull { it.algorithmVersion } ?: ALGORITHM_VERSION
        )
    }

    private data class RankedContent<T>(
        val value: T,
        val candidate: RecommendationCandidate,
        val score: Double,
        val reason: String
    ) {
        fun toSelection(userId: String, dateKey: String, now: Long) = DailyContentSelectionEntity(
            userId = userId,
            localDate = dateKey,
            contentType = candidate.type.name,
            contentId = candidate.id,
            category = candidate.category,
            sourceKey = candidate.sourceKey,
            algorithmVersion = ALGORITHM_VERSION,
            score = score,
            reason = reason,
            selectedAt = now
        )
    }

    private data class FeedbackProfile(
        val categoryScores: Map<String, Double>,
        val sourceScores: Map<String, Double>,
        val difficultyDelta: Int
    ) {
        companion object {
            fun from(interactions: List<ContentInteractionEntity>): FeedbackProfile {
                val category = mutableMapOf<String, Double>()
                val source = mutableMapOf<String, Double>()
                interactions.forEach { interaction ->
                    val weight = INTERACTION_WEIGHTS[interaction.interactionType] ?: 0.0
                    if (weight == 0.0) return@forEach
                    interaction.category.trim().lowercase().takeIf { it.isNotEmpty() }?.let { key ->
                        category[key] = (category[key] ?: 0.0) + weight
                    }
                    interaction.sourceKey.trim().lowercase().takeIf { it.isNotEmpty() }?.let { key ->
                        source[key] = (source[key] ?: 0.0) + weight * 0.5
                    }
                }
                val difficultyDelta = interactions
                    .asSequence()
                    .filter { it.contentType == DailyContentType.VOCABULARY.name }
                    .take(12)
                    .map { interaction ->
                        when (interaction.interactionType) {
                            ContentInteractionType.TOO_EASY.name -> 1
                            ContentInteractionType.TOO_HARD.name -> -1
                            else -> 0
                        }
                    }
                    .sum()
                    .coerceIn(-1, 1)
                return FeedbackProfile(category, source, difficultyDelta)
            }
        }
    }

    private fun DailyContentSelectionEntity.toCandidateHistoryOrNull(): CandidateHistory? {
        val type = runCatching { DailyContentType.valueOf(contentType) }.getOrNull() ?: return null
        return CandidateHistory(selectedAt, contentId, type, category, sourceKey)
    }

    private fun VocabularyEntity.toDailyWord() = DailyWord(
        id = id,
        word = word,
        definition = definition,
        pronunciation = pronunciation,
        partOfSpeech = partOfSpeech,
        exampleSentence = exampleSentence,
        difficulty = difficulty,
        category = category
    )

    private fun QuoteEntity.toDailyQuote() = DailyQuote(
        id = id,
        content = content,
        author = author,
        source = source,
        category = category,
        reflectionPrompt = reflectionPrompt
    )

    private fun vocabularyQuality(word: VocabularyEntity): Double {
        var score = 0.35
        if (word.definition.length >= 24) score += 0.20
        if (word.exampleSentence.length >= 24) score += 0.20
        if (word.pronunciation.isNotBlank()) score += 0.10
        if (word.partOfSpeech.isNotBlank()) score += 0.05
        if (word.synonyms.isNotBlank()) score += 0.05
        if (word.origin.isNotBlank()) score += 0.05
        return score.coerceIn(0.0, 1.0)
    }

    private fun quoteQuality(quote: QuoteEntity): Double {
        var score = 0.45
        if (quote.author.isNotBlank() && !quote.author.equals("unknown", true)) score += 0.15
        if (quote.source.isNotBlank()) score += 0.10
        if (quote.reflectionPrompt.length >= 16) score += 0.20
        if (quote.tags.isNotBlank()) score += 0.10
        return score.coerceIn(0.0, 1.0)
    }

    private fun temporalCategories(day: DayOfWeek): Set<String> = when (day) {
        DayOfWeek.MONDAY -> setOf("focus", "discipline", "productivity", "motivation")
        DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY -> setOf("learning", "growth", "wisdom", "business")
        DayOfWeek.THURSDAY -> setOf("resilience", "courage", "stoic", "growth")
        DayOfWeek.FRIDAY -> setOf("gratitude", "success", "life", "reflection")
        DayOfWeek.SATURDAY -> setOf("creativity", "literary", "curiosity", "life")
        DayOfWeek.SUNDAY -> setOf("reflection", "wisdom", "peace", "gratitude")
    }

    private fun String.normalized(): String = trim().lowercase()

    private companion object {
        const val ALGORITHM_VERSION = 1
        const val DAY_MS = 24L * 60L * 60L * 1_000L
        const val SELECTION_LOOKBACK_MS = 60L * DAY_MS
        const val INTERACTION_LOOKBACK_MS = 90L * DAY_MS

        val COMPLETION_SIGNALS = setOf(
            ContentInteractionType.COMPLETED,
            ContentInteractionType.USED_IN_JOURNAL
        )

        val INTERACTION_WEIGHTS = mapOf(
            ContentInteractionType.OPENED.name to 0.5,
            ContentInteractionType.SAVED.name to 3.0,
            ContentInteractionType.DISMISSED.name to -2.0,
            ContentInteractionType.COMPLETED.name to 2.0,
            ContentInteractionType.USED_IN_JOURNAL.name to 4.0,
            ContentInteractionType.MORE_LIKE_THIS.name to 5.0,
            ContentInteractionType.LESS_LIKE_THIS.name to -5.0
        )
    }
}
