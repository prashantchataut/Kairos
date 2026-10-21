package com.kairos.app.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.kairos.app.MainActivity
import com.kairos.app.R

object Notifier {
    const val CHANNEL_DAILY = "daily_moment"
    const val CHANNEL_CAPSULE = "capsule"
    const val EXTRA_ROUTE = "kairos.route"

    fun createChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_DAILY, "Daily moment", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "A gentle nudge with today's word"
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_CAPSULE, "Time capsule", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "When a letter from past you is ready to open"
            }
        )
    }

    fun canNotify(context: Context) =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ||
            android.os.Build.VERSION.SDK_INT < 33

    fun show(context: Context, id: Int, channel: String, title: String, text: String, route: String?) {
        if (!canNotify(context)) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            route?.let { putExtra(EXTRA_ROUTE, it) }
        }
        val pending = PendingIntent.getActivity(
            context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(0xFF3461E8.toInt())
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(id, notification) }
    }
}
