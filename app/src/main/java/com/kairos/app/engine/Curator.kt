package com.kairos.app.engine

import com.kairos.app.data.db.QuoteEntity
import com.kairos.app.data.db.WordEntity
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/** Everything the curator knows about a person when it writes their next batch. */
data class Profile(
    val name: String,
    val interests: Set<String>,
    val themes: Set<String>,
    val level: Int,
    val recentMoods: List<Mood>,
    val dayPart: DayPart,
    val loved: List<String>,
    val notForMe: List<String>,
    val tooEasy: List<String>,
    val tooHard: List<String>,
    val struggling: List<String>,
    val mastered: Int,
    val lovedAuthors: List<String>,
    val lovedThemes: List<String>,
    val avoidWords: List<String>,
    val avoidQuotes: List<String>,
    val avoidSparks: List<String>,
    /** Optional free-text steer, e.g. "words about the ocean". */
    val focus: String? = null,
)

/** One batch of curator output after validation. */
data class Batch(
    val words: List<WordEntity>,
    val quotes: List<QuoteEntity>,
    val sparks: List<String>,
    val prompts: List<String>,
)

/**
 * The AI half of the recommendation engine. [DailyEngine] decides what to show from the pool;
 * the curator decides what goes *into* the pool, steered by everything the person has done.
 */
object Curator {
    val validCategories = Topics.words.map { it.key }.toSet()
    val validThemes = Topics.themes.map { it.key }.toSet()

    val system = """
        You are the curator inside Kairos, a calm daily app where one person learns a lovely word,
        keeps a thought worth keeping and does one small good thing each day.

        Write for a single person, using the profile you're given. Be warm, precise and grown-up:
        no cheesy self-help, no exclamation marks, no emojis.

        Words
        - Real English words (or loanwords used in English) found in standard dictionaries. Never invent words.
        - Fit the person's interests and level (1 everyday, 3 rich, 5 rare/literary). Lean towards
          words they would actually enjoy using in conversation or writing.
        - Learn from the profile: more like "loved", nothing like "notForMe"; if "tooEasy" is long go
          up a level, if "tooHard" is long come down. Never repeat anything in avoidWords.
        - definition: plain language, at most 18 words, no circular use of the word.
        - pronunciation: simple respelling with the stressed syllable in capitals, e.g. "suh-REN-dip-ih-tee".
        - example: one natural, modern sentence (at most 22 words) that makes the meaning obvious.
        - origin: at most 16 words, accurate etymology.
        - synonyms: 2 to 4 close words.
        - hook: at most 14 words, why this word suits this person right now, addressed as "you".
        - category: one of ${validCategories.joinToString()}.

        Quotes
        - Only real, well-documented quotes by real people, with their usual wording. If you are not
          sure who said it, do not use it. Proverbs are allowed with author "<Language> proverb".
        - Match the person's themes and recent moods; avoid clichés everyone has seen a hundred times
          and anything in avoidQuotes.
        - note: at most 18 words of context about the author or the line.
        - theme: one of ${validThemes.joinToString()}.

        Sparks: tiny kind or joyful actions for today, at most 14 words, doable in under ten minutes,
        never productivity chores. Prompts: gentle questions to answer in a letter to one's future self.
    """.trimIndent()

    fun userMessage(p: Profile, words: Int, quotes: Int, sparks: Int, prompts: Int): String = buildJsonObject {
        put("task", "Create $words new words, $quotes quotes, $sparks sparks and $prompts prompts for this person.")
        if (p.focus != null) put("focus", "The person asked for: ${p.focus}. Every word should relate to it.")
        putJsonObject("profile") {
            put("name", p.name.ifBlank { "friend" })
            put("interests", p.interests.joinToString())
            put("favouriteThemes", p.themes.joinToString())
            put("level", p.level)
            put("recentMoods", p.recentMoods.joinToString { it.label })
            put("timeOfDay", p.dayPart.name.lowercase())
            put("wordsRemembered", p.mastered)
            put("loved", p.loved.joinToString())
            put("notForMe", p.notForMe.joinToString())
            put("tooEasy", p.tooEasy.joinToString())
            put("tooHard", p.tooHard.joinToString())
            put("stillLearning", p.struggling.joinToString())
            put("lovedAuthors", p.lovedAuthors.joinToString())
            put("lovedThemes", p.lovedThemes.joinToString())
        }
        put("avoidWords", p.avoidWords.joinToString())
        put("avoidQuotes", p.avoidQuotes.joinToString(" | "))
        put("avoidSparks", p.avoidSparks.joinToString(" | "))
    }.toString()

    private fun str() = buildJsonObject { put("type", "string") }
    private fun obj(fields: List<String>, ints: List<String> = emptyList(), arrays: List<String> = emptyList()) = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            fields.forEach { put(it, str()) }
            ints.forEach { putJsonObject(it) { put("type", "integer") } }
            arrays.forEach { putJsonObject(it) { put("type", "array"); put("items", str()) } }
        }
        putJsonArray("required") { (fields + ints + arrays).forEach { add(JsonPrimitive(it)) } }
        put("additionalProperties", false)
    }
    private fun arrayOf(item: JsonObject) = buildJsonObject { put("type", "array"); put("items", item) }

    val schema: JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") {
            put("words", arrayOf(obj(listOf("word", "partOfSpeech", "pronunciation", "definition", "example", "origin", "hook", "category"), ints = listOf("level"), arrays = listOf("synonyms"))))
            put("quotes", arrayOf(obj(listOf("text", "author", "theme", "note"))))
            put("sparks", arrayOf(str()))
            put("prompts", arrayOf(str()))
        }
        putJsonArray("required") { listOf("words", "quotes", "sparks", "prompts").forEach { add(JsonPrimitive(it)) } }
        put("additionalProperties", false)
    }

    fun wordId(word: String) = word.lowercase().trim().replace(Regex("[^a-z0-9]+"), "-").trim('-')
    fun quoteId(text: String) = "q" + text.lowercase().filter { it.isLetter() }.take(60).hashCode().toUInt().toString(36)

    private fun JsonObject.s(key: String) = (this[key] as? JsonPrimitive)?.contentOrNull?.trim().orEmpty()

    /**
     * Turns raw model JSON into clean rows: drops anything incomplete, invented-looking, too long,
     * or already known ([knownWords] lowercase, [knownQuoteIds]). Never trusts the model blindly.
     */
    fun parse(root: JsonObject, knownWords: Set<String>, knownQuoteIds: Set<String>, now: Long = System.currentTimeMillis()): Batch {
        val seen = knownWords.toMutableSet()
        val words = (root["words"] as? JsonArray).orEmpty().mapNotNull { el ->
            val o = el as? JsonObject ?: return@mapNotNull null
            val word = o.s("word").trim('.', ' ')
            val def = o.s("definition")
            val key = word.lowercase()
            val looksReal = word.length in 2..32 && word.all { it.isLetter() || it == '-' || it == ' ' || it == '\'' } && word.count { it == ' ' } <= 2
            if (!looksReal || def.isBlank() || key in seen || def.lowercase().contains(key)) return@mapNotNull null
            seen += key
            val category = o.s("category").lowercase().takeIf { it in validCategories } ?: "wonder"
            val level = ((o["level"] as? JsonPrimitive)?.intOrNull ?: 3).coerceIn(1, 5)
            WordEntity(
                id = wordId(word),
                word = word.replaceFirstChar { it.uppercase() },
                partOfSpeech = o.s("partOfSpeech").lowercase().ifBlank { "noun" },
                pronunciation = o.s("pronunciation"),
                definition = def.trimEnd('.') + ".",
                example = o.s("example"),
                origin = o.s("origin"),
                synonyms = (o["synonyms"] as? JsonArray).orEmpty().mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.trim() }
                    .filter { it.isNotBlank() && it.lowercase() != key }.take(4).joinToString(", "),
                category = category,
                level = level,
                hook = o.s("hook"),
                generatedAt = now,
            )
        }
        val quoteIds = knownQuoteIds.toMutableSet()
        val quotes = (root["quotes"] as? JsonArray).orEmpty().mapNotNull { el ->
            val o = el as? JsonObject ?: return@mapNotNull null
            val text = o.s("text").trim('"', '“', '”', ' ')
            val author = o.s("author").removePrefix("—").removePrefix("-").trim()
            if (text.length !in 12..320 || author.isBlank() || author.length > 80) return@mapNotNull null
            val id = quoteId(text)
            if (id in quoteIds) return@mapNotNull null
            quoteIds += id
            QuoteEntity(
                id = id, text = text, author = author,
                theme = o.s("theme").lowercase().takeIf { it in validThemes } ?: "hope",
                note = o.s("note"), generatedAt = now,
            )
        }
        fun strings(key: String, max: Int) = (root[key] as? JsonArray).orEmpty()
            .mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.trim() }
            .filter { it.length in 8..max }.distinct()
        return Batch(words, quotes, strings("sparks", 120), strings("prompts", 160))
    }

}
