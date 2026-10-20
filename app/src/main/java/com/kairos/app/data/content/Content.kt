package com.kairos.app.data.content

import android.content.Context
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
    val phonetic: String,
    val pos: String,
    val definition: String,
    val example: String,
    val theme: String,
    val level: Int,
)

@Serializable
data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val theme: String,
)

@Serializable
data class Joy(
    val id: String,
    val title: String,
    val detail: String,
    val kind: String,
    val minutes: Int,
)

object Themes {
    val wordThemes = listOf("feelings", "nature", "mind", "kindness", "courage", "wonder", "time", "words")
    val quoteThemes = listOf("hope", "calm", "growth", "courage", "kindness", "time", "wonder", "learning", "play")
    fun label(theme: String) = theme.replaceFirstChar { it.uppercase() }
}

val ContentJson = Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false }

/** Curated, bundled content. Loaded once from assets and kept in memory (it is small). */
class ContentRepository(private val context: Context) {
    private val mutex = Mutex()
    private var words: List<Word>? = null
    private var quotes: List<Quote>? = null
    private var joys: List<Joy>? = null

    private suspend inline fun <reified T> load(name: String): List<T> = withContext(Dispatchers.IO) {
        context.assets.open(name).bufferedReader().use { ContentJson.decodeFromString<List<T>>(it.readText()) }
    }

    suspend fun words(): List<Word> = mutex.withLock { words ?: load<Word>("words.json").also { words = it } }
    suspend fun quotes(): List<Quote> = mutex.withLock { quotes ?: load<Quote>("quotes.json").also { quotes = it } }
    suspend fun joys(): List<Joy> = mutex.withLock { joys ?: load<Joy>("joys.json").also { joys = it } }

    suspend fun word(id: String) = words().firstOrNull { it.id == id }
    suspend fun quote(id: String) = quotes().firstOrNull { it.id == id }
    suspend fun joy(id: String) = joys().firstOrNull { it.id == id }
}
