package com.huma.app.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

const val FOCUS_CHANNEL_ID = "focus_channel"
const val FOCUS_NOTIFICATION_ID = 2001

fun createFocusNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            FOCUS_CHANNEL_ID,
            "Focus Mode",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifikasi selama mode fokus aktif"
            setSound(null, null)
            enableVibration(false)
        }

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
