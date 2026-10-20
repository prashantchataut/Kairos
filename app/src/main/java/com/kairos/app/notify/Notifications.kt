package com.kairos.app.notify

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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kairos.app.MainActivity
import com.kairos.app.R
import com.kairos.app.KairosApp
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object Channels {
    const val CAPSULE = "capsule"
    const val DAILY = "daily"
    const val DOWNLOADS = "downloads"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannels(
            listOf(
                NotificationChannel(CAPSULE, "Time capsules", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "When a letter to future you is ready to open"
                },
                NotificationChannel(DAILY, "Daily hello", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "One gentle nudge a day with today's word"
                },
                NotificationChannel(DOWNLOADS, "Game downloads", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "Progress while a game downloads"
                },
            ),
        )
    }

    fun canPost(context: Context) = Build.VERSION.SDK_INT < 33 ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}

fun openAppIntent(context: Context, route: String?): PendingIntent {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        if (route != null) putExtra(MainActivity.EXTRA_ROUTE, route)
    }
    return PendingIntent.getActivity(
        context, route?.hashCode() ?: 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

class CapsuleScheduler(private val context: Context) {
    fun schedule(id: Long, title: String, unlockAt: Long) {
        val delay = (unlockAt - System.currentTimeMillis()).coerceAtLeast(0)
        val req = OneTimeWorkRequestBuilder<CapsuleUnlockWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("id" to id, "title" to title))
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork("capsule-$id", ExistingWorkPolicy.REPLACE, req)
    }

    fun cancel(id: Long) = WorkManager.getInstance(context).cancelUniqueWork("capsule-$id")
}

class CapsuleUnlockWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getLong("id", -1)
        if (id < 0 || !Channels.canPost(applicationContext)) return Result.success()
        val capsule = (applicationContext as KairosApp).container.db.capsules().get(id) ?: return Result.success()
        if (capsule.openedAt != null) return Result.success()
        val n = NotificationCompat.Builder(applicationContext, Channels.CAPSULE)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("A letter from past you is ready ✉")
            .setContentText("“${capsule.title}” just unsealed. Open it when you have a quiet minute.")
            .setContentIntent(openAppIntent(applicationContext, "capsule/open/$id"))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        runCatching { NotificationManagerCompat.from(applicationContext).notify(("capsule$id").hashCode(), n) }
        return Result.success()
    }
}

object DailyNudge {
    private const val NAME = "daily-nudge"

    fun schedule(context: Context, hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var next = now.with(LocalTime.of(hour, minute))
        if (!next.isAfter(now)) next = next.plusDays(1)
        val delay = Duration.between(now, next).toMillis()
        val req = PeriodicWorkRequestBuilder<DailyNudgeWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    fun cancel(context: Context) = WorkManager.getInstance(context).cancelUniqueWork(NAME)
}

class DailyNudgeWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!Channels.canPost(applicationContext)) return Result.success()
        val container = (applicationContext as KairosApp).container
        val picks = runCatching { container.learn.todayPicks() }.getOrNull() ?: return Result.success()
        val n = NotificationCompat.Builder(applicationContext, Channels.DAILY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Today's word: ${picks.word.word}")
            .setContentText(picks.word.definition)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${picks.word.definition}\n\nSmall joy: ${picks.joy.title}"))
            .setContentIntent(openAppIntent(applicationContext, null))
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(applicationContext).notify(1001, n) }
        return Result.success()
    }
}
