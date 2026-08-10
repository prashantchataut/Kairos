package com.kairos.app.data.content

import android.util.Log
import com.kairos.app.data.ai.GeminiResult
import com.kairos.app.data.ai.GeminiService
import com.kairos.app.data.local.dao.QuoteDao
import com.kairos.app.data.local.dao.VocabularyDao
import com.kairos.app.data.local.entity.QuoteEntity
import com.kairos.app.data.local.entity.VocabularyEntity
import com.kairos.app.data.local.preferences.PreferencesManager
import org.json.JSONArray
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Online content refresh: pulls fresh curated words and quotes from the
 * configured AI provider, validated and inserted only when genuinely missing.
 *
 * This is the "the app is online" path: when a provider key is configured and
 * the device is online, Kairos enriches its local catalog with new hand-quality
 * material matched to the user's saved interests. Everything is optional —
 * without a key, or offline, the app keeps working on the curated local catalog.
 */
@Singleton
class OnlineContentRefresher @Inject constructor(
    private val geminiService: GeminiService,
    private val vocabularyDao: VocabularyDao,
    private val quoteDao: QuoteDao,
    private val preferencesManager: PreferencesManager
) {

    data class RefreshResult(
        val wordsAdded: Int,
        val quotesAdded: Int,
        val skipped: Boolean = false
    ) {
        val nothingNew: Boolean get() = wordsAdded == 0 && quotesAdded == 0
    }

    data class OnlineWord(
        val word: String,
        val definition: String,
        val partOfSpeech: String,
        val pronunciation: String,
        val exampleSentence: String,
        val synonyms: String,
        val category: String,
        val difficulty: Int
    )

    data class OnlineQuote(
        val content: String,
        val author: String,
        val category: String,
        val reflectionPrompt: String
    )

    companion object {
        private const val TAG = "OnlineContentRefresher"
        private const val WORDS_PER_REFRESH = 5
        private const val QUOTES_PER_REFRESH = 4
        private val REFRESH_MIN_INTERVAL_MS = TimeUnit.HOURS.toMillis(18)

        internal const val WORD_ORIGIN_MARKER = "kairos:online"
        private const val WORD_ORIGIN = WORD_ORIGIN_MARKER
        internal const val FAMILY_ORIGIN_PREFIX = "kairos:family:"
    }

    /**
     * Day-gated refresh. Returns [RefreshResult.skipped] when the provider is
     * not configured or the last refresh is too recent.
     */
    suspend fun refreshIfDue(force: Boolean = false): RefreshResult {
        if (!geminiService.isConfigured()) {
            Log.i(TAG, "AI provider not configured; keeping the curated local catalog")
            return RefreshResult(0, 0, skipped = true)
        }
        if (!force) {
            val last = preferencesManager.lastOnlineRefreshDate.first()
            if (last > 0 && System.currentTimeMillis() - last < REFRESH_MIN_INTERVAL_MS) {
                return RefreshResult(0, 0, skipped = true)
            }
        }
        return refreshNow()
    }

    suspend fun refreshNow(): RefreshResult {
        val wordCategories = preferencesManager.preferredWordCategories.first().takeIf { it.isNotEmpty() }
            ?: setOf("self-improvement", "communication", "mindfulness", "reflection")
        val quoteCategories = preferencesManager.selectedWisdomCategories.first().takeIf { it.isNotEmpty() }
            ?: setOf("wisdom", "life", "motivation")

        var wordsAdded = 0
        var quotesAdded = 0

        // Words — matched to the user's preferred learning areas.
        runCatching {
            val wordsPrompt = buildString {
                append("You are curating vocabulary for a premium learning app. ")
                append("Return STRICT JSON only, no markdown: ")
                append("{\"words\":[{\"word\":\"...\",\"definition\":\"...\",\"partOfSpeech\":\"...\",")
                append("\"pronunciation\":\"...\",\"exampleSentence\":\"...\",\"synonyms\":\"...\",")
                append("\"category\":\"...\",\"difficulty\":1}]} ")
                append("Give $WORDS_PER_REFRESH sophisticated, genuinely useful English words ")
                append("a thoughtful adult learner would want to know. Prefer uncommon but useful words; ")
                append("avoid simple dictionary filler and avoid repeating a word's derived forms. ")
                append("Choose words in these interest areas: ${wordCategories.joinToString(", ")}. ")
                append("Definitions must be accurate and concise; examples must be natural and reflective.")
            }
            val result = geminiService.generateCuratedContent(wordsPrompt)
            if (result is GeminiResult.Success) {
                wordsAdded = insertMissingWords(parseWords(result.data))
            }
        }.onFailure { Log.w(TAG, "Word refresh failed", it) }

        // Quotes — matched to the user's chosen themes.
        runCatching {
            val quotesPrompt = buildString {
                append("You are curating quotes for a premium reflection app. ")
                append("Return STRICT JSON only, no markdown: ")
                append("{\"quotes\":[{\"content\":\"...\",\"author\":\"...\",\"category\":\"...\",\"reflectionPrompt\":\"...\"}]} ")
                append("Give $QUOTES_PER_REFRESH short, memorable, attributed quotes ")
                append("a thoughtful adult would want to reread. Prefer lesser-known lines over overused ones; ")
                append("never fabricate an author or attribution. Themes: ${quoteCategories.joinToString(", ")}. ")
                append("Each reflectionPrompt must be one gentle question.")
            }
            val result = geminiService.generateCuratedContent(quotesPrompt)
            if (result is GeminiResult.Success) {
                quotesAdded = insertMissingQuotes(parseQuotes(result.data))
            }
        }.onFailure { Log.w(TAG, "Quote refresh failed", it) }

        preferencesManager.setLastOnlineRefreshDate(System.currentTimeMillis())
        Log.i(TAG, "Refresh complete: $wordsAdded words, $quotesAdded quotes")
        return RefreshResult(wordsAdded, quotesAdded)
    }

    // ------------------------------------------------------------------ parse

    internal fun parseWords(payload: String): List<OnlineWord> = runCatching {
        val root = JSONObject(payload)
        val arr = root.optJSONArray("words") ?: JSONArray()
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                add(
                    OnlineWord(
                        word = o.optString("word").trim(),
                        definition = o.optString("definition").trim(),
                        partOfSpeech = o.optString("partOfSpeech").trim(),
                        pronunciation = o.optString("pronunciation").trim(),
                        exampleSentence = o.optString("exampleSentence").trim(),
                        synonyms = o.optString("synonyms").trim(),
                        category = o.optString("category").trim().ifBlank { "general" },
                        difficulty = o.optInt("difficulty", 3).coerceIn(1, 5)
                    )
                )
            }
        }
    }.getOrElse {
        Log.w(TAG, "Could not parse word payload", it)
        emptyList()
    }

    internal fun parseQuotes(payload: String): List<OnlineQuote> = runCatching {
        val root = JSONObject(payload)
        val arr = root.optJSONArray("quotes") ?: JSONArray()
        buildList {
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                add(
                    OnlineQuote(
                        content = o.optString("content").trim(),
                        author = o.optString("author").trim(),
                        category = o.optString("category").trim().ifBlank { "wisdom" },
                        reflectionPrompt = o.optString("reflectionPrompt").trim()
                    )
                )
            }
        }
    }.getOrElse {
        Log.w(TAG, "Could not parse quote payload", it)
        emptyList()
    }

    // ------------------------------------------------------------------ validate + dedupe + insert

    internal fun validateWord(candidate: OnlineWord): Boolean =
        candidate.word.length in 2..30 &&
            candidate.definition.length >= 8 &&
            !candidate.word.any { !it.isLetter() && it != '-' && it != ' ' } &&
            candidate.category.length <= 40

    internal fun validateQuote(candidate: OnlineQuote): Boolean =
        candidate.content.length in 12..500 &&
            candidate.author.length in 2..60 &&
            candidate.category.length <= 40

    private suspend fun insertMissingWords(candidates: List<OnlineWord>): Int {
        if (candidates.isEmpty()) return 0
        val existing = vocabularyDao.getAllVocabularySync()
            .map { it.word.trim().lowercase() }
            .toHashSet()
        val seen = HashSet<String>()
        val toInsert = candidates
            .filter(::validateWord)
            .filter { candidate ->
                val key = candidate.word.trim().lowercase()
                key !in existing && seen.add(key)
            }
            .take(WORDS_PER_REFRESH)
            .map { candidate ->
                VocabularyEntity(
                    word = candidate.word,
                    definition = candidate.definition,
                    pronunciation = candidate.pronunciation,
                    partOfSpeech = candidate.partOfSpeech.ifBlank { "word" },
                    exampleSentence = candidate.exampleSentence,
                    synonyms = candidate.synonyms,
                    antonyms = "",
                    origin = WORD_ORIGIN,
                    difficulty = candidate.difficulty.coerceIn(1, 5),
                    category = candidate.category.ifBlank { "general" }
                )
            }
        if (toInsert.isEmpty()) return 0
        vocabularyDao.insertWords(toInsert)
        return toInsert.size
    }

    private suspend fun insertMissingQuotes(candidates: List<OnlineQuote>): Int {
        if (candidates.isEmpty()) return 0
        val existing = quoteDao.getAllQuotesSync()
            .map { it.content.trim().lowercase() }
            .toHashSet()
        val seen = HashSet<String>()
        val toInsert = candidates
            .filter(::validateQuote)
            .filter { candidate ->
                val key = candidate.content.trim().lowercase()
                key !in existing && seen.add(key)
            }
            .take(QUOTES_PER_REFRESH)
            .map { candidate ->
                QuoteEntity(
                    content = candidate.content,
                    author = candidate.author,
                    category = candidate.category.ifBlank { "wisdom" },
                    reflectionPrompt = candidate.reflectionPrompt
                )
            }
        if (toInsert.isEmpty()) return 0
        quoteDao.insertQuotes(toInsert)
        return toInsert.size
    }

    /** Family-derived rows are never part of the promoted experience. */
    internal fun isPromotedWord(origin: String?): Boolean =
        origin == null || !origin.startsWith(FAMILY_ORIGIN_PREFIX)
}
