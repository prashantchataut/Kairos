package com.kairos.app.engine.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kairos.app.KairosApp
import com.kairos.app.MainActivity
import com.kairos.app.R
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

object Notify {
    const val CH_CAPSULE = "capsules"
    const val CH_GENTLE = "gentle"
    const val CH_DOWNLOADS = "downloads"
    const val EXTRA_ROUTE = "kairos.route"

    fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannels(
            listOf(
                NotificationChannel(CH_CAPSULE, context.getString(R.string.channel_capsules), NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = context.getString(R.string.channel_capsules_desc) },
                NotificationChannel(CH_GENTLE, context.getString(R.string.channel_gentle), NotificationManager.IMPORTANCE_DEFAULT)
                    .apply { description = context.getString(R.string.channel_gentle_desc) },
                NotificationChannel(CH_DOWNLOADS, context.getString(R.string.channel_downloads), NotificationManager.IMPORTANCE_LOW)
                    .apply { description = context.getString(R.string.channel_downloads_desc) },
            ),
        )
    }

    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun routeIntent(context: Context, route: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ROUTE, route)
        }
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    @Suppress("MissingPermission")
    fun post(context: Context, id: Int, channel: String, title: String, text: String, route: String) {
        if (!canPost(context)) return
        val n = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_kairos)
            .setColor(0xFF3B6FEA.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(routeIntent(context, route, id))
            .build()
        NotificationManagerCompat.from(context).notify(id, n)
    }
}

object Scheduler {
    private fun capsuleWork(id: String) = "capsule-$id"
    private const val NUDGE = "daily-nudge"

    fun scheduleCapsule(context: Context, id: String, title: String, unlockAt: Long) {
        val delay = (unlockAt - System.currentTimeMillis()).coerceAtLeast(0)
        val req = OneTimeWorkRequestBuilder<CapsuleUnlockWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("id" to id, "title" to title))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(capsuleWork(id), ExistingWorkPolicy.REPLACE, req)
    }

    fun cancelCapsule(context: Context, id: String) {
        WorkManager.getInstance(context).cancelUniqueWork(capsuleWork(id))
    }

    fun scheduleNudge(context: Context, minutesOfDay: Int) {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(minutesOfDay / 60, minutesOfDay % 60)
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delay = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis()
        val req = OneTimeWorkRequestBuilder<NudgeWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(NUDGE, ExistingWorkPolicy.REPLACE, req)
    }

    fun cancelNudge(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(NUDGE)
    }
}

class CapsuleUnlockWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getString("id") ?: return Result.success()
        val title = inputData.getString("title") ?: "A letter"
        Notify.post(
            applicationContext, id.hashCode(), Notify.CH_CAPSULE,
            "A letter from past you has arrived",
            "“$title” is ready to open. Take a breath first.",
            "capsule/$id",
        )
        return Result.success()
    }
}

class NudgeWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as KairosApp
        val settings = app.container.settings.current()
        if (!settings.remindersOn) return Result.success()
        val today = runCatching { app.container.today.ensureToday() }.getOrNull()
        val word = today?.word?.item
        Notify.post(
            applicationContext, 4201, Notify.CH_GENTLE,
            if (word != null) "Today's word: ${word.word}" else "A small moment for you",
            word?.definition?.let { "$it Kiri saved you a thought, too." } ?: "Your word and thought for today are waiting.",
            "today",
        )
        Scheduler.scheduleNudge(applicationContext, settings.reminderMinutes)
        return Result.success()
    }
}
