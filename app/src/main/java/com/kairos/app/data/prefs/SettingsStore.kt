package com.kairos.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kairos.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore("kairos_settings")

const val DEFAULT_CATALOG_URL = "https://raw.githubusercontent.com/prashantchataut/Kairos/main/catalog/games.json"

data class Settings(
    val onboarded: Boolean = false,
    val name: String = "",
    val theme: ThemeMode = ThemeMode.System,
    val reduceMotion: Boolean = false,
    val wordThemes: Set<String> = emptySet(),
    val quoteThemes: Set<String> = emptySet(),
    val level: Int = 2,
    val reminders: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val catalogUrl: String = DEFAULT_CATALOG_URL,
    val sessionSize: Int = 8,
)

class SettingsStore(private val context: Context) {
    private object K {
        val onboarded = booleanPreferencesKey("onboarded")
        val name = stringPreferencesKey("name")
        val theme = stringPreferencesKey("theme")
        val reduceMotion = booleanPreferencesKey("reduce_motion")
        val wordThemes = stringSetPreferencesKey("word_themes")
        val quoteThemes = stringSetPreferencesKey("quote_themes")
        val level = intPreferencesKey("level")
        val reminders = booleanPreferencesKey("reminders")
        val reminderHour = intPreferencesKey("reminder_hour")
        val reminderMinute = intPreferencesKey("reminder_minute")
        val catalogUrl = stringPreferencesKey("catalog_url")
        val sessionSize = intPreferencesKey("session_size")
    }

    val settings: Flow<Settings> = context.store.data.map { it.toSettings() }

    suspend fun current(): Settings = settings.first()

    private fun Preferences.toSettings() = Settings(
        onboarded = this[K.onboarded] ?: false,
        name = this[K.name] ?: "",
        theme = runCatching { ThemeMode.valueOf(this[K.theme] ?: "System") }.getOrDefault(ThemeMode.System),
        reduceMotion = this[K.reduceMotion] ?: false,
        wordThemes = this[K.wordThemes] ?: emptySet(),
        quoteThemes = this[K.quoteThemes] ?: emptySet(),
        level = this[K.level] ?: 2,
        reminders = this[K.reminders] ?: false,
        reminderHour = this[K.reminderHour] ?: 9,
        reminderMinute = this[K.reminderMinute] ?: 0,
        catalogUrl = this[K.catalogUrl] ?: DEFAULT_CATALOG_URL,
        sessionSize = this[K.sessionSize] ?: 8,
    )

    suspend fun completeOnboarding(name: String, wordThemes: Set<String>, quoteThemes: Set<String>, level: Int) {
        context.store.edit {
            it[K.name] = name.trim()
            it[K.wordThemes] = wordThemes
            it[K.quoteThemes] = quoteThemes
            it[K.level] = level
            it[K.onboarded] = true
        }
    }

    suspend fun setName(v: String) = context.store.edit { it[K.name] = v.trim() }
    suspend fun setTheme(v: ThemeMode) = context.store.edit { it[K.theme] = v.name }
    suspend fun setReduceMotion(v: Boolean) = context.store.edit { it[K.reduceMotion] = v }
    suspend fun setWordThemes(v: Set<String>) = context.store.edit { it[K.wordThemes] = v }
    suspend fun setQuoteThemes(v: Set<String>) = context.store.edit { it[K.quoteThemes] = v }
    suspend fun setLevel(v: Int) = context.store.edit { it[K.level] = v }
    suspend fun setSessionSize(v: Int) = context.store.edit { it[K.sessionSize] = v }
    suspend fun setCatalogUrl(v: String) = context.store.edit { it[K.catalogUrl] = v.trim() }
    suspend fun setReminders(enabled: Boolean, hour: Int, minute: Int) = context.store.edit {
        it[K.reminders] = enabled
        it[K.reminderHour] = hour
        it[K.reminderMinute] = minute
    }
}
