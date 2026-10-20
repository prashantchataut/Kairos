package com.kairos.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kairos.app.data.local.dao.FutureMessageDao
import com.kairos.app.data.local.preferences.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val futureMessageDao: FutureMessageDao
) {
    // Safely obtain AlarmManager - this can return null on some devices/configurations
    private val alarmManager: AlarmManager? by lazy {
        try {
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to get AlarmManager", e)
            null
        }
    }

    companion object {
        private const val TAG = "NotificationScheduler"
        private const val REQUEST_MORNING = 100
        private const val REQUEST_EVENING = 101
        private const val REQUEST_WORD = 102
        private const val REQUEST_STREAK = 103
        private const val REQUEST_JOURNAL = 104
        private const val REQUEST_WEEKLY_SUMMARY = 105
        private const val REQUEST_FUTURE_BASE = 1000
    }

    /**
     * Reschedules all notifications based on user preferences.
     *
     * This method is typically called:
     * - After device boot (via BootReceiver)
     * - After app update
     * - When user enables notifications in settings
     *
     * Each notification type is scheduled independently to ensure that
     * if one fails, the others can still be scheduled successfully.
     */
    suspend fun rescheduleAllNotifications() {
        try {
            val notificationsEnabled = preferencesManager.notificationsEnabled.first()
            if (!notificationsEnabled) {
                android.util.Log.d(TAG, "Notifications disabled, cancelling all")
                cancelAllNotifications()
                return
            }

            android.util.Log.d(TAG, "Rescheduling all notifications...")

            // Remove legacy alarms first. They remain recognized by the receiver so
            // old installed PendingIntents fail closed instead of spamming users.
            cancelScheduledAlarm(REQUEST_WORD, NotificationReceiver.ACTION_WORD_OF_DAY)
            cancelScheduledAlarm(REQUEST_STREAK, NotificationReceiver.ACTION_STREAK_REMINDER)
            cancelScheduledAlarm(REQUEST_JOURNAL, NotificationReceiver.ACTION_JOURNAL_REMINDER)

            if (preferencesManager.wisdomNotificationEnabled.first() &&
                preferencesManager.morningReminderEnabled.first()
            ) {
                runCatching { scheduleMorningWisdom() }
                    .onFailure { android.util.Log.e(TAG, "Failed to schedule daily moment", it) }
            } else {
                cancelScheduledAlarm(REQUEST_MORNING, NotificationReceiver.ACTION_MORNING_WISDOM)
            }

            if (preferencesManager.journalReminderEnabled.first() &&
                preferencesManager.eveningReflectionEnabled.first()
            ) {
                runCatching { scheduleEveningReflection() }
                    .onFailure { android.util.Log.e(TAG, "Failed to schedule evening reflection", it) }
            } else {
                cancelScheduledAlarm(REQUEST_EVENING, NotificationReceiver.ACTION_EVENING_REFLECTION)
            }

            if (preferencesManager.weeklySummaryNotifications.first()) {
                runCatching { scheduleWeeklySummaryNotification() }
                    .onFailure { android.util.Log.e(TAG, "Failed to schedule weekly recap", it) }
            } else {
                cancelScheduledAlarm(REQUEST_WEEKLY_SUMMARY, NotificationReceiver.ACTION_WEEKLY_SUMMARY)
            }

            runCatching { scheduleFutureMessages() }
                .onFailure { android.util.Log.e(TAG, "Failed to schedule future messages", it) }

            android.util.Log.d(TAG, "Notification rescheduling completed")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to reschedule notifications", e)
        }
    }

    suspend fun scheduleMorningWisdom() {
        try {
            val hour = preferencesManager.dailyReminderHour.first()
            val minute = preferencesManager.dailyReminderMinute.first()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            scheduleRepeatingAlarm(
                action = NotificationReceiver.ACTION_MORNING_WISDOM,
                requestCode = REQUEST_MORNING,
                triggerTime = calendar.timeInMillis,
                intervalMillis = AlarmManager.INTERVAL_DAY
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to schedule morning wisdom", e)
        }
    }

    suspend fun scheduleEveningReflection() {
        try {
            val hour = preferencesManager.eveningReminderHour.first()
            val minute = preferencesManager.eveningReminderMinute.first()

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            scheduleRepeatingAlarm(
                action = NotificationReceiver.ACTION_EVENING_REFLECTION,
                requestCode = REQUEST_EVENING,
                triggerTime = calendar.timeInMillis,
                intervalMillis = AlarmManager.INTERVAL_DAY
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to schedule evening reflection", e)
        }
    }

    fun scheduleWordOfDay() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        scheduleRepeatingAlarm(
            action = NotificationReceiver.ACTION_WORD_OF_DAY,
            requestCode = REQUEST_WORD,
            triggerTime = calendar.timeInMillis,
            intervalMillis = AlarmManager.INTERVAL_DAY
        )
    }

    fun scheduleStreakReminder() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        scheduleRepeatingAlarm(
            action = NotificationReceiver.ACTION_STREAK_REMINDER,
            requestCode = REQUEST_STREAK,
            triggerTime = calendar.timeInMillis,
            intervalMillis = AlarmManager.INTERVAL_DAY
        )
    }

    fun scheduleJournalReminder() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 19)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        scheduleRepeatingAlarm(
            action = NotificationReceiver.ACTION_JOURNAL_REMINDER,
            requestCode = REQUEST_JOURNAL,
            triggerTime = calendar.timeInMillis,
            intervalMillis = AlarmManager.INTERVAL_DAY
        )
    }

    fun scheduleWeeklySummaryNotification() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        scheduleRepeatingAlarm(
            action = NotificationReceiver.ACTION_WEEKLY_SUMMARY,
            requestCode = REQUEST_WEEKLY_SUMMARY,
            triggerTime = calendar.timeInMillis,
            intervalMillis = AlarmManager.INTERVAL_DAY * 7
        )
    }

    suspend fun scheduleFutureMessages() {
        try {
            val pendingMessages = futureMessageDao.getAllMessagesSync()
            val currentTime = System.currentTimeMillis()

            pendingMessages.forEach { message ->
                try {
                    if (message.deliveryDate > currentTime) {
                        scheduleExactAlarm(
                            action = NotificationReceiver.ACTION_FUTURE_MESSAGE,
                            requestCode = futureMessageRequestCode(message.id),
                            triggerTime = message.deliveryDate,
                            extras = mapOf(
                                NotificationReceiver.EXTRA_MESSAGE_TITLE to message.title,
                                NotificationReceiver.EXTRA_MESSAGE_BODY to "A letter from your past self is ready to open."
                            )
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e(TAG, "Failed to schedule future message: ${message.id}", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to fetch future messages from database", e)
        }
    }

    fun scheduleFutureMessage(messageId: Long, deliveryDate: Long, title: String, content: String) {
        if (deliveryDate <= System.currentTimeMillis()) return

        scheduleExactAlarm(
            action = NotificationReceiver.ACTION_FUTURE_MESSAGE,
            requestCode = futureMessageRequestCode(messageId),
            triggerTime = deliveryDate,
            extras = mapOf(
                NotificationReceiver.EXTRA_MESSAGE_TITLE to title,
                NotificationReceiver.EXTRA_MESSAGE_BODY to content.take(200)
            )
        )
    }

    fun cancelFutureMessageNotification(messageId: Long) {
        val manager = alarmManager ?: run {
            android.util.Log.w(TAG, "AlarmManager not available, skipping future message cancellation")
            return
        }

        try {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationReceiver.ACTION_FUTURE_MESSAGE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                futureMessageRequestCode(messageId),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let {
                manager.cancel(it)
                it.cancel()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to cancel future message notification: $messageId", e)
        }
    }

    private fun futureMessageRequestCode(messageId: Long): Int {
        val normalized = (messageId % (Int.MAX_VALUE - REQUEST_FUTURE_BASE)).toInt()
        return REQUEST_FUTURE_BASE + normalized.coerceAtLeast(0)
    }

    private fun scheduleRepeatingAlarm(
        action: String,
        requestCode: Int,
        triggerTime: Long,
        intervalMillis: Long
    ) {
        val manager = alarmManager ?: run {
            android.util.Log.w(TAG, "AlarmManager not available, skipping alarm scheduling")
            return
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            this.action = action
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            manager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                intervalMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to schedule repeating alarm", e)
        }
    }

    private fun scheduleExactAlarm(
        action: String,
        requestCode: Int,
        triggerTime: Long,
        extras: Map<String, String> = emptyMap()
    ) {
        val manager = alarmManager ?: run {
            android.util.Log.w(TAG, "AlarmManager not available, skipping exact alarm scheduling")
            return
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            this.action = action
            extras.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (manager.canScheduleExactAlarms()) {
                    manager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    manager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to schedule exact alarm", e)
        }
    }

    /**
     * DEBUG ONLY: Triggers a test notification immediately.
     * This method is useful for verifying that notifications work correctly
     * without waiting for scheduled times.
     *
     * Usage: Call from AiDebugScreen or via adb:
     * adb shell am broadcast -a com.kairos.app.MORNING_WISDOM -n com.kairos.app/.notification.NotificationReceiver
     *
     * @param type The type of notification to test (morning, evening, word, streak, journal, future)
     */
    fun debugTriggerNotificationNow(type: String) {
        if (!com.kairos.app.BuildConfig.DEBUG) {
            android.util.Log.w(TAG, "Debug notifications only available in debug builds")
            return
        }

        android.util.Log.d(TAG, "Triggering debug notification: $type")

        val action = when (type.lowercase()) {
            "morning" -> NotificationReceiver.ACTION_MORNING_WISDOM
            "evening" -> NotificationReceiver.ACTION_EVENING_REFLECTION
            "word" -> NotificationReceiver.ACTION_WORD_OF_DAY
            "streak" -> NotificationReceiver.ACTION_STREAK_REMINDER
            "journal" -> NotificationReceiver.ACTION_JOURNAL_REMINDER
            "future" -> NotificationReceiver.ACTION_FUTURE_MESSAGE
            else -> {
                android.util.Log.w(TAG, "Unknown notification type: $type")
                return
            }
        }

        android.util.Log.d(TAG, "Debug notification broadcast sent for: $type")

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            this.action = action
            if (action == NotificationReceiver.ACTION_FUTURE_MESSAGE) {
                putExtra(NotificationReceiver.EXTRA_MESSAGE_TITLE, "Test Message from Past You")
                putExtra(NotificationReceiver.EXTRA_MESSAGE_BODY, "This is a debug test notification for future messages.")
            }
        }

        // Send broadcast immediately
        context.sendBroadcast(intent)
    }

    /**
     * DEBUG ONLY: Schedules a notification to fire in the specified number of seconds.
     * Useful for testing without waiting for daily schedules.
     *
     * @param type The type of notification (morning, evening, word, streak, journal)
     * @param delaySeconds How many seconds from now to fire the notification
     */
    fun debugScheduleNotificationIn(type: String, delaySeconds: Int) {
        if (!com.kairos.app.BuildConfig.DEBUG) {
            android.util.Log.w(TAG, "Debug notifications only available in debug builds")
            return
        }

        val (action, requestCode) = when (type.lowercase()) {
            "morning" -> NotificationReceiver.ACTION_MORNING_WISDOM to REQUEST_MORNING
            "evening" -> NotificationReceiver.ACTION_EVENING_REFLECTION to REQUEST_EVENING
            "word" -> NotificationReceiver.ACTION_WORD_OF_DAY to REQUEST_WORD
            "streak" -> NotificationReceiver.ACTION_STREAK_REMINDER to REQUEST_STREAK
            "journal" -> NotificationReceiver.ACTION_JOURNAL_REMINDER to REQUEST_JOURNAL
            else -> {
                android.util.Log.w(TAG, "Unknown notification type: $type")
                return
            }
        }

        val triggerTime = System.currentTimeMillis() + (delaySeconds * 1000L)

        android.util.Log.d(TAG, "Scheduling debug notification '$type' in $delaySeconds seconds")

        scheduleExactAlarm(
            action = action,
            requestCode = requestCode + 9000, // Use different request code to not interfere with daily
            triggerTime = triggerTime
        )
    }

    private fun cancelScheduledAlarm(requestCode: Int, action: String) {
        val manager = alarmManager ?: return
        val intent = Intent(context, NotificationReceiver::class.java).apply { this.action = action }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            manager.cancel(it)
            it.cancel()
        }
    }

    private fun cancelAllNotifications() {
        val manager = alarmManager ?: run {
            android.util.Log.w(TAG, "AlarmManager not available, skipping notification cancellation")
            return
        }

        listOf(
            REQUEST_MORNING to NotificationReceiver.ACTION_MORNING_WISDOM,
            REQUEST_EVENING to NotificationReceiver.ACTION_EVENING_REFLECTION,
            REQUEST_WORD to NotificationReceiver.ACTION_WORD_OF_DAY,
            REQUEST_STREAK to NotificationReceiver.ACTION_STREAK_REMINDER,
            REQUEST_JOURNAL to NotificationReceiver.ACTION_JOURNAL_REMINDER,
            REQUEST_WEEKLY_SUMMARY to NotificationReceiver.ACTION_WEEKLY_SUMMARY
        ).forEach { (requestCode, action) ->
            try {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    this.action = action
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                pendingIntent?.let {
                    manager.cancel(it)
                    it.cancel()
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to cancel notification for action: $action", e)
            }
        }
    }
}
