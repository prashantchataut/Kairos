package com.kairos.app.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kairos.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.random.Random

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("kairos_settings")

data class Settings(
    val onboarded: Boolean = false,
    val name: String = "",
    val theme: ThemeMode = ThemeMode.System,
    val reduceMotion: Boolean = false,
    val calmBackground: Boolean = false,
    val wordInterests: Set<String> = emptySet(),
    val quoteThemes: Set<String> = emptySet(),
    /** 1 = everyday, 2 = rich, 3 = rare and delicious. */
    val level: Int = 2,
    val remindersOn: Boolean = false,
    val reminderMinutes: Int = 9 * 60,
    val salt: Long = 0,
    val ttsOn: Boolean = true,
)

class SettingsStore(private val context: Context) {
    private object K {
        val onboarded = booleanPreferencesKey("onboarded")
        val name = stringPreferencesKey("name")
        val theme = stringPreferencesKey("theme")
        val reduceMotion = booleanPreferencesKey("reduce_motion")
        val calm = booleanPreferencesKey("calm_background")
        val wordInterests = stringSetPreferencesKey("word_interests")
        val quoteThemes = stringSetPreferencesKey("quote_themes")
        val level = intPreferencesKey("level")
        val reminders = booleanPreferencesKey("reminders_on")
        val reminderMinutes = intPreferencesKey("reminder_minutes")
        val salt = longPreferencesKey("salt")
        val tts = booleanPreferencesKey("tts_on")
    }

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            onboarded = p[K.onboarded] ?: false,
            name = p[K.name] ?: "",
            theme = p[K.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.System,
            reduceMotion = p[K.reduceMotion] ?: false,
            calmBackground = p[K.calm] ?: false,
            wordInterests = p[K.wordInterests] ?: emptySet(),
            quoteThemes = p[K.quoteThemes] ?: emptySet(),
            level = p[K.level] ?: 2,
            remindersOn = p[K.reminders] ?: false,
            reminderMinutes = p[K.reminderMinutes] ?: (9 * 60),
            salt = p[K.salt] ?: 0L,
            ttsOn = p[K.tts] ?: true,
        )
    }

    suspend fun current(): Settings = settings.first()

    /** A per-install salt keeps everyone's daily word different while staying deterministic. */
    suspend fun ensureSalt(): Long {
        val existing = current().salt
        if (existing != 0L) return existing
        val salt = Random.nextLong().let { if (it == 0L) 1L else it }
        context.dataStore.edit { it[K.salt] = salt }
        return salt
    }

    suspend fun completeOnboarding(name: String, words: Set<String>, quotes: Set<String>, level: Int) {
        context.dataStore.edit {
            it[K.name] = name.trim()
            it[K.wordInterests] = words
            it[K.quoteThemes] = quotes
            it[K.level] = level
            it[K.onboarded] = true
        }
    }

    suspend fun setName(v: String) = context.dataStore.edit { it[K.name] = v.trim() }
    suspend fun setTheme(v: ThemeMode) = context.dataStore.edit { it[K.theme] = v.name }
    suspend fun setReduceMotion(v: Boolean) = context.dataStore.edit { it[K.reduceMotion] = v }
    suspend fun setCalmBackground(v: Boolean) = context.dataStore.edit { it[K.calm] = v }
    suspend fun setWordInterests(v: Set<String>) = context.dataStore.edit { it[K.wordInterests] = v }
    suspend fun setQuoteThemes(v: Set<String>) = context.dataStore.edit { it[K.quoteThemes] = v }
    suspend fun setLevel(v: Int) = context.dataStore.edit { it[K.level] = v.coerceIn(1, 3) }
    suspend fun setReminders(on: Boolean, minutes: Int) = context.dataStore.edit {
        it[K.reminders] = on
        it[K.reminderMinutes] = minutes
    }
    suspend fun setTts(v: Boolean) = context.dataStore.edit { it[K.tts] = v }
}
