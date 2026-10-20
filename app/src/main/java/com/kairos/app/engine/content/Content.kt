package com.kairos.app.engine.content

import android.content.Context
import com.kairos.app.data.db.CustomWordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Word(
    val id: String,
    val word: String,
    val phonetic: String = "",
    val pos: String,
    val definition: String,
    val example: String,
    val synonyms: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val level: Int = 2,
    val origin: String = "",
    val custom: Boolean = false,
)

@Serializable
data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val tags: List<String> = emptyList(),
)

/** A small, kind thing to do today. Not a task — an invitation. */
@Serializable
data class Kindness(
    val id: String,
    val title: String,
    val detail: String,
    val minutes: Int,
    val kind: String,
)

@Serializable
data class Prompt(val id: String, val text: String, val kind: String)

@Serializable
private data class Bundle<T>(val version: Int = 1, val items: List<T>)

data class Topic(val id: String, val label: String)

object Topics {
    val wordInterests = listOf(
        Topic("feelings", "Feelings"), Topic("nature", "Nature"), Topic("mind", "Mind"),
        Topic("people", "People"), Topic("creativity", "Making things"), Topic("work", "Work & focus"),
        Topic("wonder", "Wonder"), Topic("delight", "Delicious rare words"),
    )
    val quoteThemes = listOf(
        Topic("hope", "Hope"), Topic("courage", "Courage"), Topic("calm", "Calm"), Topic("growth", "Growth"),
        Topic("joy", "Joy"), Topic("kindness", "Kindness"), Topic("stoic", "Stoic"), Topic("wonder", "Wonder"),
        Topic("comfort", "Comfort"), Topic("focus", "Focus"),
    )
    fun wordLabel(id: String) = wordInterests.firstOrNull { it.id == id }?.label ?: id
    fun quoteLabel(id: String) = quoteThemes.firstOrNull { it.id == id }?.label ?: id
}

/** Offline content shipped in assets. Loaded once, kept in memory (a few hundred KB). */
class ContentRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()
    private var words: List<Word>? = null
    private var quotes: List<Quote>? = null
    private var kindness: List<Kindness>? = null
    private var prompts: List<Prompt>? = null

    private suspend fun read(name: String): String = withContext(Dispatchers.IO) {
        context.assets.open("content/$name").bufferedReader().use { it.readText() }
    }

    suspend fun words(): List<Word> = mutex.withLock {
        words ?: json.decodeFromString<Bundle<Word>>(read("words.json")).items.also { words = it }
    }

    suspend fun quotes(): List<Quote> = mutex.withLock {
        quotes ?: json.decodeFromString<Bundle<Quote>>(read("quotes.json")).items.also { quotes = it }
    }

    suspend fun kindness(): List<Kindness> = mutex.withLock {
        kindness ?: json.decodeFromString<Bundle<Kindness>>(read("kindness.json")).items.also { kindness = it }
    }

    suspend fun prompts(): List<Prompt> = mutex.withLock {
        prompts ?: json.decodeFromString<Bundle<Prompt>>(read("prompts.json")).items.also { prompts = it }
    }
}

fun CustomWordEntity.toWord() = Word(
    id = id, word = word, phonetic = phonetic, pos = partOfSpeech, definition = definition,
    example = example, tags = listOf("mine"), level = 2, custom = true,
)
