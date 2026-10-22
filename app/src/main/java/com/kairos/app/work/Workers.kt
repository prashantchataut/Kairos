package com.kairos.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kairos.app.KairosApp
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/** Posts the daily nudge, then books tomorrow's. One-shot chains survive Doze better than periodic work. */
class DailyReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as KairosApp).container
        val settings = container.prefs.current()
        if (!settings.remindersOn) return Result.success()
        container.kairos.ensureDay()
        val day = container.db.days().get(com.kairos.app.data.repo.today())
        val word = day?.let { container.db.words().get(it.wordId) }
        val name = settings.name.ifBlank { "friend" }
        Notifier.show(
            applicationContext, 1001, Notifier.CHANNEL_DAILY,
            title = word?.let { "Today's word: ${it.word}" } ?: "Your moment is ready",
            text = word?.let { "${it.definition.replaceFirstChar { c -> c.lowercase() }} Come say hi, $name." } ?: "One word, one thought. Come say hi, $name.",
            route = "today",
        )
        ReminderScheduler.schedule(applicationContext, settings.reminderHour, settings.reminderMinute)
        return Result.success()
    }
}

class CapsuleUnlockWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getLong(KEY_ID, -1)
        val container = (applicationContext as KairosApp).container
        val capsule = container.capsules.observe(id).first()?.capsule ?: return Result.success()
        if (capsule.openedAt != null) return Result.success()
        Notifier.show(
            applicationContext, (2000 + id).toInt(), Notifier.CHANNEL_CAPSULE,
            title = "A letter from past you is ready",
            text = capsule.title.ifBlank { "Sealed ${java.text.DateFormat.getDateInstance().format(capsule.createdAt)}" } + " — tap to open it.",
            route = "capsule/$id",
        )
        return Result.success()
    }

    companion object { const val KEY_ID = "capsule_id" }
}

object ReminderScheduler {
    private const val NAME = "daily-reminder"

    fun schedule(context: Context, hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!next.isAfter(now.plusMinutes(1))) next = next.plusDays(1)
        val request = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .setInitialDelay(Duration.between(now, next).toMillis(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context) = WorkManager.getInstance(context).cancelUniqueWork(NAME)
}

object CapsuleScheduler {
    private fun name(id: Long) = "capsule-$id"

    fun schedule(context: Context, id: Long, opensAt: Long) {
        val delay = (opensAt - System.currentTimeMillis()).coerceAtLeast(0)
        val request = OneTimeWorkRequestBuilder<CapsuleUnlockWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(Data.Builder().putLong(CapsuleUnlockWorker.KEY_ID, id).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(name(id), ExistingWorkPolicy.KEEP, request)
    }

    fun cancel(context: Context, id: Long) = WorkManager.getInstance(context).cancelUniqueWork(name(id))
}

/** Keeps a few personal, unseen words and quotes ready so Today opens instantly, even offline. */
class RefillWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repo = (applicationContext as KairosApp).container.kairos
        return runCatching { repo.topUpPool(refresh = inputData.getBoolean(KEY_REFRESH, false)) }.fold(
            onSuccess = { Result.success() },
            onFailure = { e ->
                when (e) {
                    is com.kairos.app.data.ai.AiError.NoKey, is com.kairos.app.data.ai.AiError.BadKey, is com.kairos.app.data.ai.AiError.NoCredits -> Result.success()
                    else -> if (runAttemptCount < 3) Result.retry() else Result.success()
                }
            },
        )
    }

    companion object {
        private const val KEY_REFRESH = "refresh"

        /** Every eight hours: retire stale unseen items and write fresh ones, so the pool keeps changing. */
        fun schedulePeriodic(context: Context) {
            val request = androidx.work.PeriodicWorkRequestBuilder<RefillWorker>(8, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).setRequiresBatteryNotLow(true).build())
                .setInputData(androidx.work.workDataOf(KEY_REFRESH to true))
                .build()
            runCatching { WorkManager.getInstance(context).enqueueUniquePeriodicWork("refresh", androidx.work.ExistingPeriodicWorkPolicy.KEEP, request) }
        }

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<RefillWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            // Never let background scheduling break a foreground action.
            runCatching { WorkManager.getInstance(context).enqueueUniqueWork("refill", ExistingWorkPolicy.KEEP, request) }
        }
    }
}
