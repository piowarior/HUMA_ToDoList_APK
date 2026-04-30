package com.huma.app.ui.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.huma.app.R

class NotificationReceiver : BroadcastReceiver() {

    @RequiresPermission("android.permission.POST_NOTIFICATIONS")
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val type = intent.getStringExtra("type") ?: "task"
        val notificationId = title.hashCode() + type.hashCode()

        val rootIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            rootIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent for Close button
        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            putExtra("notif_id", notificationId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.logohumaicon)
        } catch (e: Exception) {
            null
        }

        val notificationTitle = if (type == "commitment") "Commitment Reminder 🎯" else "Huma Reminder 🎯"
        val bigText = if (type == "commitment") {
            "Waktunya melakukan: $title\n\nJaga momentum kamu! Semangat terus ya! 🔥"
        } else {
            "Tugas: $title\n\nJangan ditunda ya, mari selesaikan sekarang! ✨"
        }

        val notification = NotificationCompat.Builder(context, "huma_reminder") 
            .setSmallIcon(R.drawable.logohumaicon) 
            .setLargeIcon(largeIcon)
            .setContentTitle(notificationTitle)
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle(if (type == "commitment") "Ayo penuhi komitmenmu!" else "Ayo selesaikan tugasmu!")
                .bigText(bigText))
            .setColor(Color.parseColor("#FFA726"))
            .setColorized(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "BUKA", pendingIntent)
            .addAction(0, "TUTUP", dismissPendingIntent) // Tombol Close
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true) // 🔥 STICKY: Gak bisa dihapus geser
            .setAutoCancel(false) 
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
        }
    }
}
