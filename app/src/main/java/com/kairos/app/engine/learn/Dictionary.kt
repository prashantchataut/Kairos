package com.kairos.app.engine.learn

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.Locale

data class LookupResult(
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val definition: String,
    val example: String,
    val synonyms: List<String>,
)

sealed interface Lookup {
    data class Found(val results: List<LookupResult>) : Lookup
    data object NotFound : Lookup
    data class Offline(val message: String) : Lookup
}

/** Free Dictionary API (dictionaryapi.dev) — no key, no account, nothing about you is sent but the word. */
class DictionaryClient(private val http: OkHttpClient) {
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable private data class Entry(val word: String, val phonetic: String? = null, val phonetics: List<Phon> = emptyList(), val meanings: List<Meaning> = emptyList())
    @Serializable private data class Phon(val text: String? = null)
    @Serializable private data class Meaning(val partOfSpeech: String = "", val definitions: List<Def> = emptyList(), val synonyms: List<String> = emptyList())
    @Serializable private data class Def(val definition: String = "", val example: String? = null, val synonyms: List<String> = emptyList())

    suspend fun lookup(raw: String): Lookup = withContext(Dispatchers.IO) {
        val word = raw.trim().lowercase(Locale.ROOT)
        if (word.isEmpty()) return@withContext Lookup.NotFound
        val url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + URLEncoder.encode(word, "UTF-8").replace("+", "%20")
        try {
            http.newCall(Request.Builder().url(url).build()).execute().use { res ->
                if (res.code == 404) return@withContext Lookup.NotFound
                if (!res.isSuccessful) return@withContext Lookup.Offline("The dictionary is resting (${res.code}). Try again soon.")
                val entries = json.decodeFromString<List<Entry>>(res.body?.string().orEmpty())
                val results = entries.flatMap { e ->
                    val ph = e.phonetic ?: e.phonetics.firstNotNullOfOrNull { it.text }.orEmpty()
                    e.meanings.flatMap { m ->
                        m.definitions.take(2).map { d ->
                            LookupResult(e.word, ph, m.partOfSpeech, d.definition, d.example.orEmpty(), (d.synonyms + m.synonyms).distinct().take(5))
                        }
                    }
                }.take(6)
                if (results.isEmpty()) Lookup.NotFound else Lookup.Found(results)
            }
        } catch (e: Exception) {
            Lookup.Offline("You seem to be offline. Your saved words still work without internet.")
        }
    }
}

/** Says words out loud with the device's own voice. Lazily created, shared app-wide. */
class Speaker(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: String? = null
    @Volatile var enabled = true

    fun say(text: String) {
        if (!enabled) return
        val engine = tts ?: TextToSpeech(context.applicationContext, this).also { tts = it }
        if (ready) {
            engine.setSpeechRate(0.92f)
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kairos")
        } else {
            pending = text
        }
    }

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (ready) {
            tts?.language = Locale.US
            pending?.let { say(it) }
        }
        pending = null
    }
}
