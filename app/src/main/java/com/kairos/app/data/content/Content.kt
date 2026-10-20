package com.kairos.app.data.content

import android.content.Context
import com.kairos.app.data.db.QuoteEntity
import com.kairos.app.data.db.WordEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WordSeed(
    val word: String,
    val pos: String,
    val say: String,
    val def: String,
    val ex: String,
    val origin: String = "",
    val syn: List<String> = emptyList(),
    val cat: String,
    val level: Int = 2,
) {
    val id get() = word.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
    fun toEntity() = WordEntity(
        id = id, word = word, partOfSpeech = pos, pronunciation = say, definition = def,
        example = ex, origin = origin, synonyms = syn.joinToString(", "), category = cat, level = level,
    )
}

@Serializable
data class QuoteSeed(val text: String, val by: String, val theme: String) {
    /** Stable id derived from the text, so content updates never duplicate a quote. */
    val id get() = "q" + text.lowercase().filter { it.isLetter() }.take(48).hashCode().toUInt().toString(36)
    fun toEntity() = QuoteEntity(id = id, text = text, author = by, theme = theme)
}

/** A tiny good thing to do today. Deliberately not a to-do: one per day, optional, kind. */
@Serializable
data class Spark(val id: String, val text: String, val kind: String)

/** The interest taxonomy shown in onboarding and settings. */
object Taxonomy {
    data class Topic(val key: String, val label: String, val blurb: String)

    val wordTopics = listOf(
        Topic("feelings", "Feelings", "Words for what's going on inside"),
        Topic("mind", "Mind", "Thinking, focus and clarity"),
        Topic("nature", "Nature", "Light, weather, growing things"),
        Topic("connection", "People", "Friendship, love, belonging"),
        Topic("craft", "Making", "Work, creativity and skill"),
        Topic("wonder", "Wonder", "Rare, delightful, curious words"),
        Topic("courage", "Growth", "Resilience and becoming"),
    )

    val quoteThemes = listOf(
        Topic("hope", "Hope", ""),
        Topic("courage", "Courage", ""),
        Topic("kindness", "Kindness", ""),
        Topic("growth", "Growth", ""),
        Topic("calm", "Calm", ""),
        Topic("joy", "Joy", ""),
        Topic("wonder", "Wonder", ""),
        Topic("rest", "Rest", ""),
    )

    fun wordLabel(key: String) = wordTopics.firstOrNull { it.key == key }?.label ?: key.replaceFirstChar { it.uppercase() }
    fun themeLabel(key: String) = quoteThemes.firstOrNull { it.key == key }?.label ?: key.replaceFirstChar { it.uppercase() }
}

class ContentLoader(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    private inline fun <reified T> read(name: String): T =
        context.assets.open(name).bufferedReader().use { json.decodeFromString(it.readText()) }

    fun words(): List<WordSeed> = read("content/words.json")
    fun quotes(): List<QuoteSeed> = read("content/quotes.json")
    val sparks: List<Spark> by lazy { read<List<Spark>>("content/sparks.json") }
    val prompts: List<String> by lazy { read<List<String>>("content/capsule_prompts.json") }

    companion object {
        /** Bump when the bundled JSON changes; new rows are inserted, existing state is kept. */
        const val VERSION = 1
    }
}
