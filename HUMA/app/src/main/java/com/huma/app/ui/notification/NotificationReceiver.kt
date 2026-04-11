package com.huma.app.ui.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.huma.app.R

class NotificationReceiver : BroadcastReceiver() {

    @RequiresPermission("android.permission.POST_NOTIFICATIONS")
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Task Reminder"

        val rootIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            title.hashCode(),
            rootIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "task_channel")
            .setSmallIcon(R.drawable.logohumaicon) // Ganti ke logo huma icon
            .setContentTitle("Huma Reminder 🎯")
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle("Ayo selesaikan tugasmu!")
                .bigText("Tugas: $title\n\nJangan ditunda ya, semangat! ✨"))

            .setColor(Color.parseColor("#6C63FF"))
            .setColorized(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "BUKA APLIKASI", pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(title.hashCode(), notification)
        } catch (e: SecurityException) {
        }
    }
}
