package com.kairos.app.engine

import com.kairos.app.data.db.BookEntity
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
    val books: List<BookEntity> = emptyList(),
)

/**
 * The AI half of the recommendation engine. [DailyEngine] decides what to show from the pool;
 * the curator decides what goes *into* the pool, steered by everything the person has done.
 *
 * Writing is split into small tasks (words, thoughts, books) that run in parallel, which is much
 * faster than one big batch and lets each task use the provider best suited to it.
 */
object Curator {
    val validCategories = Topics.words.map { it.key }.toSet()
    val validThemes = Topics.themes.map { it.key }.toSet()
    val validBookCategories = Topics.books.map { it.key }.toSet()

    private val voice = """
        You are Kai, the curator inside Kairos: a small daily app that gives one person a word worth
        knowing, a thought worth keeping, a small good thing to do and books worth knowing about.
        You write for exactly one person, using the profile you're given.

        Your voice: modern, warm, honest and specific, like a thoughtful friend with good taste in books
        and music. Plain words, concrete images, a little wit. Never preachy, never corporate.
        No exclamation marks, no emojis, no hashtags.
        Banned, because they are what makes writing feel like AI slop: "journey", "unlock", "potential",
        "embrace", "empower", "manifest", "hustle", "grind", "level up", "game-changer", "tapestry",
        "testament", "delve", "you've got this", "believe in yourself", "every day is a new beginning",
        "everything happens for a reason", "the universe", "vibes", "self-care Sunday".
    """.trimIndent()

    val wordsSystem = voice + "\n\n" + """
        Words
        - Mix three kinds in every batch:
          1. beautiful English words people half-know and would love to use (luminous, ineffable, halcyon);
          2. precise, rich English words for exact feelings and things (liminal, petrichor, apophenia);
          3. borrowed concept words that name something English struggles to name, from any language
             or tradition: Greek and Latin above all (akrasia, eudaimonia, ataraxia, meraki, kairos,
             memento mori, amor fati, sprezzatura), and also Japanese, German, Portuguese, Sanskrit,
             Nepali, Arabic, Welsh, Danish and others (wabi-sabi, Sehnsucht, saudade, hiraeth, hygge).
             Put the source language in "language"; leave it empty for ordinary English words.
        - Every word must be real and appear in reputable dictionaries or reference works. Never invent
          words. Avoid words coined recently on the internet.
        - Fit the person's interests and level (1 everyday, 3 rich, 5 rare or literary). Learn from the
          profile: more like "loved", nothing like "notForMe"; harder if "tooEasy" is long, easier if
          "tooHard" is. Never repeat anything in avoidWords.
        - definition: plain language, at most 18 words, never uses the word itself.
        - pronunciation: simple respelling, stressed syllable in capitals, e.g. "uh-KRAY-zee-uh".
        - example: one natural, modern sentence (at most 22 words) where the meaning is obvious and the
          scene is specific (a kitchen at 2am, a missed train), not abstract.
        - origin: at most 18 words, accurate etymology with the original word and its meaning.
        - synonyms: 2 to 4 close words or phrases.
        - hook: at most 14 words, why this word suits this person right now, addressed as "you".
        - category: one of ${validCategories.joinToString()}. Use "wisdom" for philosophical concept words.
    """.trimIndent()

    val thoughtsSystem = voice + "\n\n" + """
        Thoughts
        - A thought is something a person screenshots and sends to a friend. It should make them feel
          seen, lighter, braver or softer, not lectured. Range widely: comfort, permission to rest,
          tenderness, wonder, love, grief, humour, quiet hope, courage. At most a third may be about
          doing more or trying harder.
        - The tone of the lines this person loves: "Maybe it will all work out so well that you'll laugh
          at how much you worried." / "Don't forget to imagine the best case scenario, too." /
          "Actually, my grass is green enough." / "What leaves creates space for what's meant to stay."
          Never copy these; write in their spirit.
        - Mix kinds in every batch:
          - "quote": real words by real people: novelists, poets, songwriters, scientists, philosophers,
            filmmakers. Prefer the less obvious: Mary Oliver, Toni Morrison, Ocean Vuong, Ursula K. Le
            Guin, James Baldwin, Rilke, Seneca, Wisława Szymborska, Nora Ephron, Fred Rogers, Kae Tempest.
            Exact wording only, with "source" (book, poem, song or speech). If you are not certain who
            said it, do not use it. No misattributed Einstein, Buddha, Rumi or Marilyn Monroe quotes.
          - "line": an original line you write for this person (leave author empty). Short, modern,
            lowercase-friendly, the kind of thing that ends up on a poster.
          - "proverb": a real proverb, author "<Language> proverb" (Nepali, Japanese, Yoruba, Irish…).
          - "affirmation": a short first-person line the person can say to themselves ("I'm allowed to
            go slowly.", "I don't have to earn rest."), gentle and true, never grandiose. Leave author empty.
        - At most 30 words each. No clichés everyone has seen, nothing in avoidQuotes.
        - note: at most 18 words of context about the author, the source or the idea.
        - theme: one of ${validThemes.joinToString()}.
        Sparks: tiny kind, curious or joyful things to do today, at most 14 words, doable in under ten
        minutes, never chores or productivity. Prompts: gentle, specific questions to answer in a letter
        to one's future self.
    """.trimIndent()

    val booksSystem = voice + "\n\n" + """
        Books
        - Real, published books only (fiction and non-fiction), from classics to the last few years.
          Choose books this person would actually love from their interests, loved authors and moods.
          Never repeat anything in avoidBooks.
        - summary: a micro-summary in 3 or 4 short sentences: what the book is, what it argues or what
          happens, and why it stays with people. No spoilers for novels' endings.
        - ideas: 3 key ideas or takeaways, each at most 16 words, one per array item.
        - line: one short memorable sentence. Only quote the book if you are certain of the wording;
          otherwise write a one-line essence of it in your own words.
        - hook: at most 14 words, why it suits this person, addressed as "you".
        - minutes: reading time of the summary in minutes (2 to 5).
        - category: one of ${validBookCategories.joinToString()}.
    """.trimIndent()

    /** The single-batch prompt, kept for tests and as documentation of the full voice. */
    val system = wordsSystem + "\n\n" + thoughtsSystem.removePrefix(voice)

    fun profileJson(p: Profile) = buildJsonObject {
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

    fun userMessage(p: Profile, words: Int, quotes: Int, sparks: Int, prompts: Int): String = buildJsonObject {
        put("task", "Create $words new words, $quotes thoughts, $sparks sparks and $prompts prompts for this person.")
        if (p.focus != null) put("focus", "The person asked for: ${p.focus}. Every word should relate to it.")
        put("profile", profileJson(p))
        put("avoidWords", p.avoidWords.joinToString())
        put("avoidQuotes", p.avoidQuotes.joinToString(" | "))
        put("avoidSparks", p.avoidSparks.joinToString(" | "))
    }.toString()

    fun wordsMessage(p: Profile, words: Int): String = buildJsonObject {
        put("task", "Create $words new words for this person, mixing the three kinds.")
        if (p.focus != null) put("focus", "The person asked for: ${p.focus}. Every word should relate to it.")
        put("profile", profileJson(p))
        put("avoidWords", p.avoidWords.joinToString())
    }.toString()

    fun thoughtsMessage(p: Profile, thoughts: Int, sparks: Int, prompts: Int): String = buildJsonObject {
        put("task", "Create $thoughts thoughts (mixing quotes, lines and proverbs), $sparks sparks and $prompts prompts for this person.")
        put("profile", profileJson(p))
        put("avoidQuotes", p.avoidQuotes.joinToString(" | "))
        put("avoidSparks", p.avoidSparks.joinToString(" | "))
    }.toString()

    /** A compact request for the on-device model (it accepts under 4000 tokens). */
    fun localThoughtsMessage(p: Profile, thoughts: Int, sparks: Int, prompts: Int): String = buildJsonObject {
        put("task", "Write $thoughts lines and affirmations, $sparks sparks (tiny kind things to do today) and $prompts gentle questions for a letter to one's future self.")
        put("moods", p.recentMoods.joinToString { it.label })
        put("themes", p.themes.joinToString())
        put("allowedThemes", validThemes.joinToString())
    }.toString()

    fun booksMessage(p: Profile, books: Int, avoidBooks: List<String>): String = buildJsonObject {
        put("task", "Recommend $books books for this person, each with a micro-summary.")
        put("profile", profileJson(p))
        put("avoidBooks", avoidBooks.joinToString(" | "))
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
    private fun schemaOf(vararg parts: Pair<String, JsonObject>) = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") { parts.forEach { (k, v) -> put(k, v) } }
        putJsonArray("required") { parts.forEach { add(JsonPrimitive(it.first)) } }
        put("additionalProperties", false)
    }

    private val wordItem = obj(listOf("word", "partOfSpeech", "pronunciation", "definition", "example", "origin", "hook", "category", "language"), ints = listOf("level"), arrays = listOf("synonyms"))
    private val quoteItem = obj(listOf("text", "author", "kind", "source", "theme", "note"))
    private val bookItem = obj(listOf("title", "author", "summary", "line", "hook", "category"), ints = listOf("year", "minutes"), arrays = listOf("ideas"))

    val wordsSchema = schemaOf("words" to arrayOf(wordItem))
    val thoughtsSchema = schemaOf("quotes" to arrayOf(quoteItem), "sparks" to arrayOf(str()), "prompts" to arrayOf(str()))
    val booksSchema = schemaOf("books" to arrayOf(bookItem))
    val schema: JsonObject = schemaOf("words" to arrayOf(wordItem), "quotes" to arrayOf(quoteItem), "sparks" to arrayOf(str()), "prompts" to arrayOf(str()))

    fun wordId(word: String) = word.lowercase().trim().replace(Regex("[^a-z0-9]+"), "-").trim('-')
    fun bookId(title: String, author: String) = "b" + (title.lowercase().filter { it.isLetter() } + author.lowercase().filter { it.isLetter() }).hashCode().toUInt().toString(36)
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
                language = o.s("language").takeIf { it.length in 2..30 && !it.equals("english", true) }.orEmpty(),
            )
        }
        val quoteIds = knownQuoteIds.toMutableSet()
        val quotes = (root["quotes"] as? JsonArray).orEmpty().mapNotNull { el ->
            val o = el as? JsonObject ?: return@mapNotNull null
            val text = o.s("text").trim('"', '“', '”', ' ')
            val author = o.s("author").removePrefix("—").removePrefix("-").trim()
            val kind = o.s("kind").lowercase().takeIf { it in setOf("quote", "line", "proverb", "affirmation") } ?: if (author.isBlank()) "line" else "quote"
            if (text.length !in 12..320 || author.length > 80) return@mapNotNull null
            if (author.isBlank() && kind != "line" && kind != "affirmation") return@mapNotNull null
            if (Slop.matches(text)) return@mapNotNull null
            val id = quoteId(text)
            if (id in quoteIds) return@mapNotNull null
            quoteIds += id
            QuoteEntity(
                id = id, text = text, author = author,
                theme = o.s("theme").lowercase().takeIf { it in validThemes } ?: "hope",
                note = o.s("note"), generatedAt = now,
                kind = kind, source = o.s("source").take(80),
            )
        }
        fun strings(key: String, max: Int) = (root[key] as? JsonArray).orEmpty()
            .mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.trim() }
            .filter { it.length in 8..max }.distinct()
        val books = (root["books"] as? JsonArray).orEmpty().mapNotNull { el ->
            val o = el as? JsonObject ?: return@mapNotNull null
            val title = o.s("title").trim('"', '“', '”', ' ')
            val author = o.s("author")
            val summary = o.s("summary")
            if (title.length !in 1..120 || author.isBlank() || summary.length < 40) return@mapNotNull null
            val ideas = (o["ideas"] as? JsonArray).orEmpty().mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.trim() }.filter { it.length in 4..160 }.take(4)
            BookEntity(
                id = bookId(title, author), title = title, author = author,
                year = ((o["year"] as? JsonPrimitive)?.intOrNull ?: 0).coerceIn(0, 2100),
                summary = summary, ideas = ideas.joinToString("\n"), line = o.s("line"), hook = o.s("hook"),
                category = o.s("category").lowercase().takeIf { it in validBookCategories } ?: "living",
                minutes = ((o["minutes"] as? JsonPrimitive)?.intOrNull ?: 3).coerceIn(1, 8), generatedAt = now,
            )
        }.distinctBy { it.id }
        return Batch(words, quotes, strings("sparks", 120).filterNot { Slop.matches(it) }, strings("prompts", 160), books)
    }

}

/** Phrases that make a line read like generic AI motivation. Anything containing them is dropped. */
object Slop {
    private val phrases = listOf(
        "journey", "unlock", "your potential", "embrace", "empower", "manifest", "hustle", "grind", "level up",
        "game-changer", "game changer", "tapestry", "testament to", "delve", "you've got this", "you got this",
        "believe in yourself", "new beginning", "happens for a reason", "the universe", "vibes", "self-care",
        "rise and shine", "never give up", "dream big", "be the best version",
    )
    fun matches(text: String): Boolean { val t = text.lowercase(); return phrases.any { it in t } }
}
