package com.kairos.app.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Kairos_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("UnencryptedSharedPreferences") private val sharedPreferences: SharedPreferences,
    @Named("EncryptedSharedPreferences") private val encryptedSharedPreferences: SharedPreferences
) {
    private val dataStore = context.dataStore
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val facadeSupport = PreferenceFacadeSupport(dataStore)

    private val _geminiApiKey = MutableStateFlow("")
    val geminiApiKey: Flow<String> = _geminiApiKey.asStateFlow()

    private val _therapistApiKey = MutableStateFlow("")
    val therapistApiKey: Flow<String> = _therapistApiKey.asStateFlow()

    private val notificationPreferences = NotificationPreferences(
        support = facadeSupport,
        notificationsEnabledKey = PreferencesKeys.NOTIFICATIONS_ENABLED,
        dailyReminderHourKey = PreferencesKeys.DAILY_REMINDER_HOUR,
        dailyReminderMinuteKey = PreferencesKeys.DAILY_REMINDER_MINUTE,
        wisdomNotificationEnabledKey = PreferencesKeys.WISDOM_NOTIFICATION_ENABLED,
        journalReminderEnabledKey = PreferencesKeys.JOURNAL_REMINDER_ENABLED,
        eveningReminderHourKey = PreferencesKeys.EVENING_REMINDER_HOUR,
        eveningReminderMinuteKey = PreferencesKeys.EVENING_REMINDER_MINUTE
    )

    private val privacyPreferences = PrivacyPreferences(
        support = facadeSupport,
        privacyLockJournalKey = PreferencesKeys.PRIVACY_LOCK_JOURNAL,
        privacyLockFutureMessagesKey = PreferencesKeys.PRIVACY_LOCK_FUTURE_MESSAGES,
        privacyLockOnBackgroundKey = PreferencesKeys.PRIVACY_LOCK_ON_BACKGROUND,
        privacyLastUnlockedAtKey = PreferencesKeys.PRIVACY_LAST_UNLOCKED_AT
    )

    private val ritualPreferences = RitualPreferences(
        support = facadeSupport,
        morningRitualEnabledKey = PreferencesKeys.MORNING_RITUAL_ENABLED,
        eveningRitualEnabledKey = PreferencesKeys.EVENING_RITUAL_ENABLED,
        morningRitualStartHourKey = PreferencesKeys.MORNING_RITUAL_START_HOUR,
        morningRitualEndHourKey = PreferencesKeys.MORNING_RITUAL_END_HOUR,
        eveningRitualStartHourKey = PreferencesKeys.EVENING_RITUAL_START_HOUR,
        ritualReminderEnabledKey = PreferencesKeys.RITUAL_REMINDER_ENABLED
    )

    init {
        coroutineScope.launch {
            migrateSensitiveString(PreferencesKeys.GEMINI_API_KEY, PreferencesKeys.GEMINI_API_KEY.name) {
                _geminiApiKey.value = it
            }
        }
        coroutineScope.launch {
            migrateSensitiveString(PreferencesKeys.THERAPIST_API_KEY, PreferencesKeys.THERAPIST_API_KEY.name) {
                _therapistApiKey.value = it
            }
        }
    }

    /**
     * Security: Performs a one-time migration for a sensitive string preference
     * from the unencrypted DataStore to EncryptedSharedPreferences.
     * This is crucial for securing data like API keys for existing users.
     */
    private suspend fun migrateSensitiveString(
        dataStoreKey: Preferences.Key<String>,
        encryptedPrefsKey: String,
        onValueMigrated: (String) -> Unit
    ) {
        val initialValue = encryptedSharedPreferences.getString(encryptedPrefsKey, null)
        if (initialValue != null) {
            onValueMigrated(initialValue)
            return
        }

        val valueFromDataStore = dataStore.data.map { it[dataStoreKey] }.first()

        if (!valueFromDataStore.isNullOrBlank()) {
            synchronized(this) {
                // Double-check if the value was set while we were reading from DataStore
                if (!encryptedSharedPreferences.contains(encryptedPrefsKey)) {
                    encryptedSharedPreferences.edit().putString(encryptedPrefsKey, valueFromDataStore).apply()
                    onValueMigrated(valueFromDataStore)
                    coroutineScope.launch {
                        dataStore.edit { preferences ->
                            preferences.remove(dataStoreKey)
                        }
                    }
                } else {
                    // A value was set concurrently, so we use it instead
                    onValueMigrated(encryptedSharedPreferences.getString(encryptedPrefsKey, "") ?: "")
                }
            }
        } else {
            onValueMigrated("")
        }
    }

    // Keys
    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
        val EVENING_REMINDER_HOUR = intPreferencesKey("evening_reminder_hour")
        val EVENING_REMINDER_MINUTE = intPreferencesKey("evening_reminder_minute")
        val WISDOM_NOTIFICATION_ENABLED = booleanPreferencesKey("wisdom_notification_enabled")
        val JOURNAL_REMINDER_ENABLED = booleanPreferencesKey("journal_reminder_enabled")
        val CURRENT_STREAK = intPreferencesKey("current_streak")
        val LAST_ACTIVE_DATE = longPreferencesKey("last_active_date")
        val DAILY_WISDOM_LAST_SHOWN = longPreferencesKey("daily_wisdom_last_shown")
        val SELECTED_WISDOM_CATEGORIES = stringPreferencesKey("selected_wisdom_categories")
        val PREFERRED_WORD_CATEGORIES = stringPreferencesKey("preferred_word_categories")
        val PRACTICE_SESSION_SIZE = intPreferencesKey("practice_session_size")
        val VOCABULARY_DIFFICULTY_PREFERENCE = intPreferencesKey("vocabulary_difficulty")
        val AUTO_PLAY_PRONUNCIATION = booleanPreferencesKey("auto_play_pronunciation")
        val COMPACT_CARD_VIEW = booleanPreferencesKey("compact_card_view")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback")
        val FIRST_LAUNCH_TIME = longPreferencesKey("first_launch_time")
        val ONLINE_CONTENT_LAST_REFRESH = longPreferencesKey("online_content_last_refresh")
        val JOURNAL_DRAFT = stringPreferencesKey("journal_draft")
        val USER_ID = stringPreferencesKey("user_id")

        // Gamification
        val GAMIFICATION_INITIALIZED = booleanPreferencesKey("gamification_initialized")

        // Gemini AI Settings
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val BUDDHA_AI_ENABLED = booleanPreferencesKey("buddha_ai_enabled")

        // Buddha AI Feature Toggles
        val BUDDHA_DAILY_WISDOM_ENABLED = booleanPreferencesKey("buddha_daily_wisdom_enabled")
        val BUDDHA_QUOTE_EXPLANATION_ENABLED = booleanPreferencesKey("buddha_quote_explanation_enabled")
        val BUDDHA_JOURNAL_INSIGHTS_ENABLED = booleanPreferencesKey("buddha_journal_insights_enabled")
        val BUDDHA_PATTERN_TRACKING_ENABLED = booleanPreferencesKey("buddha_pattern_tracking_enabled")
        val BUDDHA_PLAYFUL_MODE = booleanPreferencesKey("buddha_playful_mode")
        val BUDDHA_REDUCE_AI_USAGE = booleanPreferencesKey("buddha_reduce_ai_usage")
        val BUDDHA_PERSONALITY_MODE = stringPreferencesKey("buddha_personality_mode")

        // Debug: Special Badge Preview Toggles
        val DEBUG_PREVIEW_DEV_BADGE = booleanPreferencesKey("debug_preview_dev_badge")
        val DEBUG_PREVIEW_BETA_BADGE = booleanPreferencesKey("debug_preview_beta_badge")

        // Debug: AI Proof Mode - Shows AI generation metadata in UI
        val DEBUG_AI_PROOF_MODE = booleanPreferencesKey("debug_ai_proof_mode")

        // Privacy Mode settings
        val PRIVACY_LOCK_JOURNAL = booleanPreferencesKey("privacy_lock_journal")
        val PRIVACY_LOCK_FUTURE_MESSAGES = booleanPreferencesKey("privacy_lock_future_messages")
        val PRIVACY_LOCK_ON_BACKGROUND = booleanPreferencesKey("privacy_lock_on_background")
        val PRIVACY_LAST_UNLOCKED_AT = longPreferencesKey("privacy_last_unlocked_at")

        // Weekly Summary settings
        val WEEKLY_SUMMARY_ENABLED = booleanPreferencesKey("weekly_summary_enabled")
        val WEEKLY_SUMMARY_DAY = intPreferencesKey("weekly_summary_day") // 0-6 (Sunday-Saturday)
        val LAST_WEEKLY_SUMMARY_SHOWN = longPreferencesKey("last_weekly_summary_shown")
        val WEEKLY_SUMMARY_NOTIFICATIONS = booleanPreferencesKey("weekly_summary_notifications")

        // Daily Ritual settings
        val MORNING_RITUAL_ENABLED = booleanPreferencesKey("morning_ritual_enabled")
        val EVENING_RITUAL_ENABLED = booleanPreferencesKey("evening_ritual_enabled")
        val MORNING_RITUAL_START_HOUR = intPreferencesKey("morning_ritual_start_hour")
        val MORNING_RITUAL_END_HOUR = intPreferencesKey("morning_ritual_end_hour")
        val EVENING_RITUAL_START_HOUR = intPreferencesKey("evening_ritual_start_hour")
        val RITUAL_REMINDER_ENABLED = booleanPreferencesKey("ritual_reminder_enabled")

        // Quiet Mode settings
        val QUIET_MODE_ENABLED = booleanPreferencesKey("quiet_mode_enabled")
        val QUIET_MODE_AUTO_SUGGEST_THRESHOLD = intPreferencesKey("quiet_mode_auto_suggest_threshold")
        val QUIET_MODE_LAST_SUGGESTED_AT = longPreferencesKey("quiet_mode_last_suggested_at")
        val QUIET_MODE_ENABLED_AT = longPreferencesKey("quiet_mode_enabled_at")
        val QUIET_MODE_LAST_CHECK_IN_AT = longPreferencesKey("quiet_mode_last_check_in_at")

        // Haven Personal Therapist settings
        val HAVEN_ENABLED = booleanPreferencesKey("haven_enabled")
        val HAVEN_NOTIFICATIONS_ENABLED = booleanPreferencesKey("haven_notifications_enabled")
        val HAVEN_DAILY_CHECK_IN_TIME = intPreferencesKey("haven_daily_check_in_time")
        val HAVEN_LAST_SESSION_AT = longPreferencesKey("haven_last_session_at")
        val THERAPIST_API_KEY = stringPreferencesKey("therapist_api_key")

        // ===== SOUL LAYER PREFERENCES =====

        // First Week Journey
        val FIRST_WEEK_MILESTONES = stringPreferencesKey("first_week_milestones")

        // Memory surfacing
        val LAST_MEMORY_SURFACED_AT = longPreferencesKey("last_memory_surfaced_at")
        val MEMORY_SURFACING_ENABLED = booleanPreferencesKey("memory_surfacing_enabled")
        val MEMORY_SURFACING_FREQUENCY = intPreferencesKey("memory_surfacing_frequency") // 1=rarely, 2=sometimes, 3=often

        // Notification intelligence
        val MORNING_REMINDER_ENABLED = booleanPreferencesKey("morning_reminder_enabled_v2")
        val EVENING_REFLECTION_ENABLED = booleanPreferencesKey("evening_reflection_enabled_v2")
        val QUIET_MODE_START = intPreferencesKey("quiet_mode_start_hour")
        val QUIET_MODE_END = intPreferencesKey("quiet_mode_end_hour")
        val LAST_NOTIFICATION_SENT_AT = longPreferencesKey("last_notification_sent_at")
        val NOTIFICATIONS_SENT_TODAY = intPreferencesKey("notifications_sent_today")
        val NOTIFICATION_RESET_DATE = longPreferencesKey("notification_reset_date")
        val LAST_APP_OPEN_AT = longPreferencesKey("last_app_open_at")

        // User context caching
        val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
        val USER_BIRTHDAY_TIMESTAMP = longPreferencesKey("user_birthday_timestamp")
        val CACHED_USER_ARCHETYPE = stringPreferencesKey("cached_user_archetype")
        val CACHED_TRUST_LEVEL = stringPreferencesKey("cached_trust_level")
        val CACHED_CONTEXT_UPDATED_AT = longPreferencesKey("cached_context_updated_at")

        // Haven session tracking
        val HAVEN_SESSION_COUNT = intPreferencesKey("haven_session_count")
        val LAST_HAVEN_SESSION_SUMMARY = stringPreferencesKey("last_haven_session_summary")
        val HAVEN_CRISIS_HISTORY = booleanPreferencesKey("haven_crisis_history")

        // Buddha interaction tracking
        val LAST_BUDDHA_INTERACTION_AT = longPreferencesKey("last_buddha_interaction_at")
        val PREFERRED_WISDOM_STYLE = stringPreferencesKey("preferred_wisdom_style")

        // Temporal content
        val LAST_TEMPORAL_GREETING_DATE = longPreferencesKey("last_temporal_greeting_date")
        val LAST_TEMPORAL_PROMPT_DATE = longPreferencesKey("last_temporal_prompt_date")

        // Premium Intelligence (Opt-in local analysis)
        val PREMIUM_INTELLIGENCE_ENABLED = booleanPreferencesKey("premium_intelligence_enabled")
    }

    // Onboarding - Critical path, requires synchronous access
    val onboardingCompleted: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    // Theme
    val themeMode: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.THEME_MODE] ?: "system"
        }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    val dynamicColors: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLORS] ?: false
        }

    suspend fun setDynamicColors(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLORS] = enabled
        }
    }

    // Notifications
    val notificationsEnabled: Flow<Boolean> = notificationPreferences.notificationsEnabled
    suspend fun setNotificationsEnabled(enabled: Boolean) = notificationPreferences.setNotificationsEnabled(enabled)
    val dailyReminderHour: Flow<Int> = notificationPreferences.dailyReminderHour
    val dailyReminderMinute: Flow<Int> = notificationPreferences.dailyReminderMinute
    suspend fun setDailyReminderTime(hour: Int, minute: Int) = notificationPreferences.setDailyReminderTime(hour, minute)
    val wisdomNotificationEnabled: Flow<Boolean> = notificationPreferences.wisdomNotificationEnabled
    suspend fun setWisdomNotificationEnabled(enabled: Boolean) = notificationPreferences.setWisdomNotificationEnabled(enabled)
    val journalReminderEnabled: Flow<Boolean> = notificationPreferences.journalReminderEnabled
    suspend fun setJournalReminderEnabled(enabled: Boolean) = notificationPreferences.setJournalReminderEnabled(enabled)
    val eveningReminderHour: Flow<Int> = notificationPreferences.eveningReminderHour
    val eveningReminderMinute: Flow<Int> = notificationPreferences.eveningReminderMinute
    suspend fun setEveningReminderTime(hour: Int, minute: Int) = notificationPreferences.setEveningReminderTime(hour, minute)

    // Streak
    val currentStreak: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.CURRENT_STREAK] ?: 0
        }

    suspend fun setCurrentStreak(streak: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_STREAK] = streak
        }
    }

    val lastActiveDate: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_ACTIVE_DATE] ?: 0L
        }

    suspend fun setLastActiveDate(date: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_ACTIVE_DATE] = date
        }
    }

    // Wisdom
    val dailyWisdomLastShown: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.DAILY_WISDOM_LAST_SHOWN] ?: 0L
        }

    suspend fun setDailyWisdomLastShown(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_WISDOM_LAST_SHOWN] = timestamp
        }
    }

    val selectedWisdomCategories: Flow<Set<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_WISDOM_CATEGORIES]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: setOf("wisdom", "motivation", "stoic", "life")
        }

    suspend fun setSelectedWisdomCategories(categories: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_WISDOM_CATEGORIES] = categories.joinToString(",")
        }
    }

    /**
     * Word interests chosen during setup. Kept separate from quote interests so a
     * user can love stoic quotes without being forced to study business words.
     * Falls back to the quote categories when never set (existing installs).
     */
    val preferredWordCategories: Flow<Set<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.PREFERRED_WORD_CATEGORIES]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: preferences[PreferencesKeys.SELECTED_WISDOM_CATEGORIES]
                    ?.split(",")
                    ?.filter { it.isNotBlank() }
                    ?.toSet()
                    ?: DEFAULT_WORD_CATEGORIES
        }

    suspend fun setPreferredWordCategories(categories: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_WORD_CATEGORIES] = categories.joinToString(",")
        }
    }

    /** How many cards a practice session should aim for. */
    val practiceSessionSize: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.PRACTICE_SESSION_SIZE] ?: DEFAULT_PRACTICE_SESSION_SIZE
        }

    suspend fun setPracticeSessionSize(size: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRACTICE_SESSION_SIZE] = size.coerceIn(3, 20)
        }
    }

    // Vocabulary
    val vocabularyDifficulty: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.VOCABULARY_DIFFICULTY_PREFERENCE] ?: 3
        }

    suspend fun setVocabularyDifficulty(difficulty: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.VOCABULARY_DIFFICULTY_PREFERENCE] = difficulty
        }
    }

    val autoPlayPronunciation: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.AUTO_PLAY_PRONUNCIATION] ?: false
        }

    suspend fun setAutoPlayPronunciation(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_PLAY_PRONUNCIATION] = enabled
        }
    }

    // UI Preferences
    val compactCardView: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.COMPACT_CARD_VIEW] ?: false
        }

    suspend fun setCompactCardView(compact: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.COMPACT_CARD_VIEW] = compact
        }
    }

    val hapticFeedbackEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] ?: true
        }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }

    // User Info
    val firstLaunchTime: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.FIRST_LAUNCH_TIME] ?: 0L
        }

    /** Auto-saved journal draft (content only; cleared on successful save). */
    val journalDraft: Flow<String> = dataStore.data
        .map { it[PreferencesKeys.JOURNAL_DRAFT] ?: "" }

    suspend fun setJournalDraft(content: String) {
        dataStore.edit { it[PreferencesKeys.JOURNAL_DRAFT] = content.take(20_000) }
    }

    /** Last time curated online content was refreshed (0 = never). */
    val lastOnlineRefreshDate: Flow<Long> = dataStore.data
        .map { it[PreferencesKeys.ONLINE_CONTENT_LAST_REFRESH] ?: 0L }

    suspend fun setLastOnlineRefreshDate(timestamp: Long) {
        dataStore.edit { it[PreferencesKeys.ONLINE_CONTENT_LAST_REFRESH] = timestamp }
    }

    suspend fun setFirstLaunchTime(timestamp: Long) {
        dataStore.edit { preferences ->
            if (preferences[PreferencesKeys.FIRST_LAUNCH_TIME] == null) {
                preferences[PreferencesKeys.FIRST_LAUNCH_TIME] = timestamp
            }
        }
    }

    /**
     * Cached user ID updated by the userId Flow.
     * Safe to access from any thread without runBlocking.
     */
    @Volatile
    private var cachedUserId: String? = null

    val userId: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.USER_ID] ?: ""
        }
        .onEach { id -> cachedUserId = id.ifBlank { null } }

    suspend fun setUserId(id: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = id
        }
    }

    /**
     * Synchronous access to current user ID.
     * Returns cached value from the userId Flow — no runBlocking deadlock risk.
     * Returns null if the Flow hasn't emitted yet (app startup).
     */
    fun getCurrentUserId(): String? = cachedUserId

    // Gamification Initialized Flag
    val gamificationInitialized: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.GAMIFICATION_INITIALIZED] ?: false
        }

    suspend fun setGamificationInitialized(initialized: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GAMIFICATION_INITIALIZED] = initialized
        }
    }

    suspend fun setGeminiApiKey(apiKey: String) {
        val key = PreferencesKeys.GEMINI_API_KEY.name
        encryptedSharedPreferences.edit().putString(key, apiKey).apply()
        _geminiApiKey.value = apiKey
        // Ensure the key is removed from the old location in case it was set there before migration.
        dataStore.edit { preferences ->
            if (preferences.contains(PreferencesKeys.GEMINI_API_KEY)) {
                preferences.remove(PreferencesKeys.GEMINI_API_KEY)
            }
        }
    }

    val geminiModel: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.GEMINI_MODEL] ?: "gemini-1.5-flash"
        }

    suspend fun setGeminiModel(model: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.GEMINI_MODEL] = model
        }
    }

    val buddhaAiEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.BUDDHA_AI_ENABLED] ?: true
        }

    suspend fun setBuddhaAiEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUDDHA_AI_ENABLED] = enabled
        }
    }

    // Buddha AI Feature Toggles
    val buddhaDailyWisdomEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.BUDDHA_DAILY_WISDOM_ENABLED] ?: true
        }

    suspend fun setBuddhaDailyWisdomEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUDDHA_DAILY_WISDOM_ENABLED] = enabled
        }
    }

    val buddhaQuoteExplanationEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.BUDDHA_QUOTE_EXPLANATION_ENABLED] ?: true
        }

    suspend fun setBuddhaQuoteExplanationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUDDHA_QUOTE_EXPLANATION_ENABLED] = enabled
        }
    }

    val buddhaJournalInsightsEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.BUDDHA_JOURNAL_INSIGHTS_ENABLED] ?: true
        }

    suspend fun setBuddhaJournalInsightsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUDDHA_JOURNAL_INSIGHTS_ENABLED] = enabled
        }
    }

    val buddhaPatternTrackingEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.BUDDHA_PATTERN_TRACKING_ENABLED] ?: true
        }

    suspend fun setBuddhaPatternTrackingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUDDHA_PATTERN_TRACKING_ENABLED] = enabled
        }
    }

    val buddhaPlayfulMode: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.BUDDHA_PLAYFUL_MODE] ?: false
        }

    suspend fun setBuddhaPlayfulMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUDDHA_PLAYFUL_MODE] = enabled
        }
    }

    val buddhaReduceAiUsage: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.BUDDHA_REDUCE_AI_USAGE] ?: false
        }

    suspend fun setBuddhaReduceAiUsage(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUDDHA_REDUCE_AI_USAGE] = enabled
        }
    }

    val buddhaPersonalityMode: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.BUDDHA_PERSONALITY_MODE] ?: "STOIC"
        }

    suspend fun setBuddhaPersonalityMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BUDDHA_PERSONALITY_MODE] = mode
        }
    }

    // ===== DEBUG: SPECIAL BADGE PREVIEW =====
    // These are for previewing Dev/Beta badges without OAuth
    // Only shown in debug builds, not in production

    val debugPreviewDevBadge: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.DEBUG_PREVIEW_DEV_BADGE] ?: false
        }

    suspend fun setDebugPreviewDevBadge(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEBUG_PREVIEW_DEV_BADGE] = enabled
        }
    }

    val debugPreviewBetaBadge: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.DEBUG_PREVIEW_BETA_BADGE] ?: false
        }

    suspend fun setDebugPreviewBetaBadge(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEBUG_PREVIEW_BETA_BADGE] = enabled
        }
    }

    // Debug: AI Proof Mode - Shows AI generation metadata in UI (DEBUG builds only)
    val debugAiProofMode: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.DEBUG_AI_PROOF_MODE] ?: false
        }

    suspend fun setDebugAiProofMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEBUG_AI_PROOF_MODE] = enabled
        }
    }

    // ===== PRIVACY MODE SETTINGS =====

    val privacyLockJournal: Flow<Boolean> = privacyPreferences.privacyLockJournal
    suspend fun setPrivacyLockJournal(enabled: Boolean) = privacyPreferences.setPrivacyLockJournal(enabled)
    val privacyLockFutureMessages: Flow<Boolean> = privacyPreferences.privacyLockFutureMessages
    suspend fun setPrivacyLockFutureMessages(enabled: Boolean) = privacyPreferences.setPrivacyLockFutureMessages(enabled)
    val privacyLockOnBackground: Flow<Boolean> = privacyPreferences.privacyLockOnBackground
    suspend fun setPrivacyLockOnBackground(enabled: Boolean) = privacyPreferences.setPrivacyLockOnBackground(enabled)
    val privacyLastUnlockedAt: Flow<Long> = privacyPreferences.privacyLastUnlockedAt
    suspend fun setPrivacyLastUnlockedAt(timestamp: Long) = privacyPreferences.updatePrivacyLastUnlockedAt(timestamp)

    // ===== WEEKLY SUMMARY SETTINGS =====

    val weeklySummaryEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.WEEKLY_SUMMARY_ENABLED] ?: true
        }

    suspend fun setWeeklySummaryEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEEKLY_SUMMARY_ENABLED] = enabled
        }
    }

    val weeklySummaryDay: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.WEEKLY_SUMMARY_DAY] ?: 0 // Default Sunday (0)
        }

    suspend fun setWeeklySummaryDay(dayOfWeek: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEEKLY_SUMMARY_DAY] = dayOfWeek.coerceIn(0, 6)
        }
    }

    val lastWeeklySummaryShown: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_WEEKLY_SUMMARY_SHOWN] ?: 0L
        }

    suspend fun setLastWeeklySummaryShown(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_WEEKLY_SUMMARY_SHOWN] = timestamp
        }
    }

    val weeklySummaryNotifications: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.WEEKLY_SUMMARY_NOTIFICATIONS] ?: true
        }

    suspend fun setWeeklySummaryNotifications(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEEKLY_SUMMARY_NOTIFICATIONS] = enabled
        }
    }

    // ===== DAILY RITUAL SETTINGS =====

    val morningRitualEnabled: Flow<Boolean> = ritualPreferences.morningRitualEnabled
    suspend fun setMorningRitualEnabled(enabled: Boolean) = ritualPreferences.setMorningRitualEnabled(enabled)
    val eveningRitualEnabled: Flow<Boolean> = ritualPreferences.eveningRitualEnabled
    suspend fun setEveningRitualEnabled(enabled: Boolean) = ritualPreferences.setEveningRitualEnabled(enabled)
    val morningRitualStartHour: Flow<Int> = ritualPreferences.morningRitualStartHour
    suspend fun setMorningRitualStartHour(hour: Int) = ritualPreferences.setMorningRitualStartHour(hour)
    val morningRitualEndHour: Flow<Int> = ritualPreferences.morningRitualEndHour
    suspend fun setMorningRitualEndHour(hour: Int) = ritualPreferences.setMorningRitualEndHour(hour)
    val eveningRitualStartHour: Flow<Int> = ritualPreferences.eveningRitualStartHour
    suspend fun setEveningRitualStartHour(hour: Int) = ritualPreferences.setEveningRitualStartHour(hour)
    val ritualReminderEnabled: Flow<Boolean> = ritualPreferences.ritualReminderEnabled
    suspend fun setRitualReminderEnabled(enabled: Boolean) = ritualPreferences.setRitualReminderEnabled(enabled)

    // ===== QUIET MODE SETTINGS =====

    val quietModeEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.QUIET_MODE_ENABLED] ?: false
        }

    suspend fun setQuietModeEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUIET_MODE_ENABLED] = enabled
            if (enabled) {
                preferences[PreferencesKeys.QUIET_MODE_ENABLED_AT] = System.currentTimeMillis()
            }
        }
    }

    // Premium Intelligence
    val premiumIntelligenceEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.PREMIUM_INTELLIGENCE_ENABLED] ?: false
        }

    suspend fun setPremiumIntelligenceEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREMIUM_INTELLIGENCE_ENABLED] = enabled
        }
    }

    val quietModeAutoSuggestThreshold: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.QUIET_MODE_AUTO_SUGGEST_THRESHOLD] ?: 3 // Default: 3 negative entries
        }

    suspend fun setQuietModeAutoSuggestThreshold(threshold: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUIET_MODE_AUTO_SUGGEST_THRESHOLD] = threshold
        }
    }

    val quietModeLastSuggestedAt: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.QUIET_MODE_LAST_SUGGESTED_AT] ?: 0L
        }

    suspend fun setQuietModeLastSuggestedAt(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUIET_MODE_LAST_SUGGESTED_AT] = timestamp
        }
    }

    val quietModeEnabledAt: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.QUIET_MODE_ENABLED_AT] ?: 0L
        }

    val quietModeLastCheckInAt: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.QUIET_MODE_LAST_CHECK_IN_AT] ?: 0L
        }

    suspend fun setQuietModeLastCheckInAt(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUIET_MODE_LAST_CHECK_IN_AT] = timestamp
        }
    }

    // ===== HAVEN PERSONAL THERAPIST SETTINGS =====

    val havenEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.HAVEN_ENABLED] ?: true
        }

    suspend fun setHavenEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAVEN_ENABLED] = enabled
        }
    }

    val havenNotificationsEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.HAVEN_NOTIFICATIONS_ENABLED] ?: true
        }

    suspend fun setHavenNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAVEN_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    val havenDailyCheckInTime: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.HAVEN_DAILY_CHECK_IN_TIME] ?: 9
        }

    suspend fun setHavenDailyCheckInTime(hour: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAVEN_DAILY_CHECK_IN_TIME] = hour
        }
    }

    val havenLastSessionAt: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.HAVEN_LAST_SESSION_AT] ?: 0L
        }

    suspend fun setHavenLastSessionAt(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAVEN_LAST_SESSION_AT] = timestamp
        }
    }

    suspend fun setTherapistApiKey(apiKey: String) {
        val key = PreferencesKeys.THERAPIST_API_KEY.name
        encryptedSharedPreferences.edit().putString(key, apiKey).apply()
        _therapistApiKey.value = apiKey
        // Ensure the key is removed from the old location.
        dataStore.edit { preferences ->
            if (preferences.contains(PreferencesKeys.THERAPIST_API_KEY)) {
                preferences.remove(PreferencesKeys.THERAPIST_API_KEY)
            }
        }
    }

    // ===== SOUL LAYER PREFERENCES ACCESSORS =====

    // First Week Journey
    val firstWeekMilestones: Flow<Set<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.FIRST_WEEK_MILESTONES]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: emptySet()
        }

    suspend fun updateFirstWeekMilestones(milestones: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FIRST_WEEK_MILESTONES] = milestones.joinToString(",")
        }
    }

    // Memory Surfacing
    val lastMemorySurfacedAt: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_MEMORY_SURFACED_AT] ?: 0L
        }

    suspend fun setLastMemorySurfacedAt(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_MEMORY_SURFACED_AT] = timestamp
        }
    }

    val memorySurfacingEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.MEMORY_SURFACING_ENABLED] ?: true
        }

    suspend fun setMemorySurfacingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MEMORY_SURFACING_ENABLED] = enabled
        }
    }

    val memorySurfacingFrequency: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.MEMORY_SURFACING_FREQUENCY] ?: 2 // Default: sometimes
        }

    suspend fun setMemorySurfacingFrequency(frequency: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MEMORY_SURFACING_FREQUENCY] = frequency.coerceIn(1, 3)
        }
    }

    // Notification Intelligence
    val morningReminderEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.MORNING_REMINDER_ENABLED] ?: true
        }

    suspend fun setMorningReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MORNING_REMINDER_ENABLED] = enabled
        }
    }

    val eveningReflectionEnabled: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.EVENING_REFLECTION_ENABLED] ?: true
        }

    suspend fun setEveningReflectionEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EVENING_REFLECTION_ENABLED] = enabled
        }
    }

    val quietModeStart: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.QUIET_MODE_START] ?: 22 // Default: 10 PM
        }

    suspend fun setQuietModeStart(hour: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUIET_MODE_START] = hour.coerceIn(0, 23)
        }
    }

    val quietModeEnd: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.QUIET_MODE_END] ?: 7 // Default: 7 AM
        }

    suspend fun setQuietModeEnd(hour: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.QUIET_MODE_END] = hour.coerceIn(0, 23)
        }
    }

    val lastNotificationSentAt: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_NOTIFICATION_SENT_AT] ?: 0L
        }

    suspend fun setLastNotificationSentAt(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_NOTIFICATION_SENT_AT] = timestamp
        }
    }

    val notificationsSentToday: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_SENT_TODAY] ?: 0
        }

    val notificationResetDate: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_RESET_DATE] ?: 0L
        }

    suspend fun incrementNotificationsSentToday(now: Long = System.currentTimeMillis()) {
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.NOTIFICATIONS_SENT_TODAY] ?: 0
            preferences[PreferencesKeys.NOTIFICATIONS_SENT_TODAY] = current + 1
            preferences[PreferencesKeys.LAST_NOTIFICATION_SENT_AT] = now
        }
    }

    suspend fun resetNotificationsSentToday(now: Long = System.currentTimeMillis()) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_SENT_TODAY] = 0
            preferences[PreferencesKeys.NOTIFICATION_RESET_DATE] = now
        }
    }

    val lastAppOpenAt: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_APP_OPEN_AT] ?: 0L
        }

    suspend fun setLastAppOpenAt(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_APP_OPEN_AT] = timestamp
        }
    }

    // User Context Caching
    val userDisplayName: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.USER_DISPLAY_NAME] ?: "Friend"
        }

    suspend fun setUserDisplayName(name: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_DISPLAY_NAME] = name
        }
    }

    val userBirthdayTimestamp: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.USER_BIRTHDAY_TIMESTAMP] ?: 0L
        }

    suspend fun setUserBirthdayTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_BIRTHDAY_TIMESTAMP] = timestamp
        }
    }

    val cachedUserArchetype: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.CACHED_USER_ARCHETYPE] ?: "EXPLORER"
        }

    suspend fun setCachedUserArchetype(archetype: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CACHED_USER_ARCHETYPE] = archetype
            preferences[PreferencesKeys.CACHED_CONTEXT_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    val cachedTrustLevel: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.CACHED_TRUST_LEVEL] ?: "NEW"
        }

    suspend fun setCachedTrustLevel(trustLevel: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CACHED_TRUST_LEVEL] = trustLevel
            preferences[PreferencesKeys.CACHED_CONTEXT_UPDATED_AT] = System.currentTimeMillis()
        }
    }

    // Haven Session Tracking
    val havenSessionCount: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.HAVEN_SESSION_COUNT] ?: 0
        }

    suspend fun incrementHavenSessionCount() {
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.HAVEN_SESSION_COUNT] ?: 0
            preferences[PreferencesKeys.HAVEN_SESSION_COUNT] = current + 1
        }
    }

    val lastHavenSessionSummary: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_HAVEN_SESSION_SUMMARY] ?: ""
        }

    suspend fun setLastHavenSessionSummary(summary: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_HAVEN_SESSION_SUMMARY] = summary
        }
    }

    val havenCrisisHistory: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.HAVEN_CRISIS_HISTORY] ?: false
        }

    suspend fun setHavenCrisisHistory(hasCrisisHistory: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAVEN_CRISIS_HISTORY] = hasCrisisHistory
        }
    }

    // Buddha Interaction Tracking
    val lastBuddhaInteractionAt: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_BUDDHA_INTERACTION_AT] ?: 0L
        }

    suspend fun setLastBuddhaInteractionAt(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_BUDDHA_INTERACTION_AT] = timestamp
        }
    }

    val preferredWisdomStyle: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.PREFERRED_WISDOM_STYLE] ?: "STOIC"
        }

    suspend fun setPreferredWisdomStyle(style: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PREFERRED_WISDOM_STYLE] = style
        }
    }

    // Temporal Content Tracking
    val lastTemporalGreetingDate: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_TEMPORAL_GREETING_DATE] ?: 0L
        }

    suspend fun setLastTemporalGreetingDate(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_TEMPORAL_GREETING_DATE] = timestamp
        }
    }

    val lastTemporalPromptDate: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LAST_TEMPORAL_PROMPT_DATE] ?: 0L
        }

    suspend fun setLastTemporalPromptDate(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_TEMPORAL_PROMPT_DATE] = timestamp
        }
    }

    // Clear all preferences
    suspend fun clearAllPreferences() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Resets all preferences to their default values.
     */
    suspend fun resetToDefaults() {
        dataStore.edit { preferences ->
            preferences.clear()
            // Set default values
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = false
            preferences[PreferencesKeys.THEME_MODE] = "system"
            preferences[PreferencesKeys.DYNAMIC_COLORS] = false
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = true
            preferences[PreferencesKeys.DAILY_REMINDER_HOUR] = 9
            preferences[PreferencesKeys.DAILY_REMINDER_MINUTE] = 0
            preferences[PreferencesKeys.EVENING_REMINDER_HOUR] = 20
            preferences[PreferencesKeys.EVENING_REMINDER_MINUTE] = 0
            preferences[PreferencesKeys.WISDOM_NOTIFICATION_ENABLED] = true
            preferences[PreferencesKeys.JOURNAL_REMINDER_ENABLED] = true
            preferences[PreferencesKeys.CURRENT_STREAK] = 0
            preferences[PreferencesKeys.VOCABULARY_DIFFICULTY_PREFERENCE] = 3
            preferences[PreferencesKeys.AUTO_PLAY_PRONUNCIATION] = false
            preferences[PreferencesKeys.COMPACT_CARD_VIEW] = false
            preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] = true
            preferences[PreferencesKeys.BUDDHA_AI_ENABLED] = true
            preferences[PreferencesKeys.BUDDHA_DAILY_WISDOM_ENABLED] = true
            preferences[PreferencesKeys.BUDDHA_QUOTE_EXPLANATION_ENABLED] = true
            preferences[PreferencesKeys.BUDDHA_JOURNAL_INSIGHTS_ENABLED] = true
            preferences[PreferencesKeys.BUDDHA_PATTERN_TRACKING_ENABLED] = true
            preferences[PreferencesKeys.BUDDHA_PLAYFUL_MODE] = false
            preferences[PreferencesKeys.BUDDHA_REDUCE_AI_USAGE] = false
            preferences[PreferencesKeys.PRIVACY_LOCK_JOURNAL] = false
            preferences[PreferencesKeys.PRIVACY_LOCK_FUTURE_MESSAGES] = false
            preferences[PreferencesKeys.PRIVACY_LOCK_ON_BACKGROUND] = true
            preferences[PreferencesKeys.WEEKLY_SUMMARY_ENABLED] = true
            preferences[PreferencesKeys.WEEKLY_SUMMARY_DAY] = 0 // Sunday
            preferences[PreferencesKeys.WEEKLY_SUMMARY_NOTIFICATIONS] = true
            preferences[PreferencesKeys.MORNING_RITUAL_ENABLED] = true
            preferences[PreferencesKeys.EVENING_RITUAL_ENABLED] = true
            preferences[PreferencesKeys.MORNING_RITUAL_START_HOUR] = 5
            preferences[PreferencesKeys.MORNING_RITUAL_END_HOUR] = 12
            preferences[PreferencesKeys.EVENING_RITUAL_START_HOUR] = 18
            preferences[PreferencesKeys.RITUAL_REMINDER_ENABLED] = true
        }
    }

    private companion object {
        /** Sensible word interests for users who never set them explicitly. */
        val DEFAULT_WORD_CATEGORIES = setOf(
            "self-improvement", "communication", "mindfulness", "reflection"
        )

        const val DEFAULT_PRACTICE_SESSION_SIZE = 5
    }
}
