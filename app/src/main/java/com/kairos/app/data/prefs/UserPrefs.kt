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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.random.Random

enum class ThemeMode { System, Daybreak, Dusk }

data class Settings(
    val onboarded: Boolean = false,
    val name: String = "",
    val wordInterests: Set<String> = emptySet(),
    val quoteThemes: Set<String> = emptySet(),
    /** 1 = gentle, 2 = curious, 3 = ambitious. Biases word difficulty. */
    val level: Int = 2,
    val remindersOn: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 30,
    val themeMode: ThemeMode = ThemeMode.System,
    val reducedMotion: Boolean = false,
    val haptics: Boolean = true,
    /** Random per install so two people never get the identical sequence. */
    val salt: Long = 0,
    val contentVersion: Int = 0,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("kairos_settings")

class UserPrefs(private val context: Context) {
    private object K {
        val onboarded = booleanPreferencesKey("onboarded")
        val name = stringPreferencesKey("name")
        val interests = stringSetPreferencesKey("word_interests")
        val themes = stringSetPreferencesKey("quote_themes")
        val level = intPreferencesKey("level")
        val remindersOn = booleanPreferencesKey("reminders_on")
        val hour = intPreferencesKey("reminder_hour")
        val minute = intPreferencesKey("reminder_minute")
        val theme = stringPreferencesKey("theme_mode")
        val reducedMotion = booleanPreferencesKey("reduced_motion")
        val haptics = booleanPreferencesKey("haptics")
        val salt = longPreferencesKey("salt")
        val contentVersion = intPreferencesKey("content_version")
    }

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            onboarded = p[K.onboarded] ?: false,
            name = p[K.name] ?: "",
            wordInterests = p[K.interests] ?: emptySet(),
            quoteThemes = p[K.themes] ?: emptySet(),
            level = p[K.level] ?: 2,
            remindersOn = p[K.remindersOn] ?: false,
            reminderHour = p[K.hour] ?: 8,
            reminderMinute = p[K.minute] ?: 30,
            themeMode = p[K.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.System,
            reducedMotion = p[K.reducedMotion] ?: false,
            haptics = p[K.haptics] ?: true,
            salt = p[K.salt] ?: 0,
            contentVersion = p[K.contentVersion] ?: 0,
        )
    }

    suspend fun current(): Settings = settings.first()

    /** Creates the per-install salt once and returns it. */
    suspend fun ensureSalt(): Long {
        var salt = 0L
        context.dataStore.edit { p ->
            salt = p[K.salt] ?: Random.nextLong().also { p[K.salt] = it }
        }
        return salt
    }

    suspend fun completeOnboarding(name: String, interests: Set<String>, themes: Set<String>, level: Int) =
        context.dataStore.edit {
            it[K.name] = name.trim()
            it[K.interests] = interests
            it[K.themes] = themes
            it[K.level] = level
            it[K.onboarded] = true
        }

    suspend fun setName(name: String) = context.dataStore.edit { it[K.name] = name.trim() }
    suspend fun setInterests(interests: Set<String>, themes: Set<String>, level: Int) = context.dataStore.edit {
        it[K.interests] = interests; it[K.themes] = themes; it[K.level] = level
    }
    suspend fun setReminder(on: Boolean, hour: Int, minute: Int) = context.dataStore.edit {
        it[K.remindersOn] = on; it[K.hour] = hour; it[K.minute] = minute
    }
    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[K.theme] = mode.name }
    suspend fun setReducedMotion(on: Boolean) = context.dataStore.edit { it[K.reducedMotion] = on }
    suspend fun setHaptics(on: Boolean) = context.dataStore.edit { it[K.haptics] = on }
    suspend fun setContentVersion(v: Int) = context.dataStore.edit { it[K.contentVersion] = v }
}
