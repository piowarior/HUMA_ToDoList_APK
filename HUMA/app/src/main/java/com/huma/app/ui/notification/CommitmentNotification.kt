package com.huma.app.ui.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.huma.app.data.local.CommitmentEntity
import java.util.*

object CommitmentNotification {

    fun scheduleNotifications(context: Context, commitment: CommitmentEntity) {
        if (!commitment.isNotificationEnabled) {
            cancelNotifications(context, commitment)
            return
        }

        commitment.notificationTimes.forEachIndexed { index, time ->
            val parts = time.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toIntOrNull() ?: return@forEachIndexed
                val minute = parts[1].toIntOrNull() ?: return@forEachIndexed
                
                scheduleDailyNotification(context, commitment, index, hour, minute)
            }
        }
    }

    private fun scheduleDailyNotification(
        context: Context,
        commitment: CommitmentEntity,
        timeIndex: Int,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", commitment.title)
            putExtra("type", "commitment")
            putExtra("id", commitment.id)
        }

        // Unique ID for each commitment and each time slot
        val requestCode = commitment.id * 100 + timeIndex

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If the time has already passed today, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelNotifications(context: Context, commitment: CommitmentEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // We don't know exactly how many times were scheduled before, 
        // but we can try to cancel a reasonable number (e.g., 10) or better:
        // When updating, we should probably have a way to track active requestCodes.
        // For simplicity, let's assume max 10 notifications per commitment.
        for (i in 0 until 10) {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                commitment.id * 100 + i,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
