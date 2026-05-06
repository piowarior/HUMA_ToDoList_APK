package com.huma.app.ui.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.huma.app.data.local.CommitmentEntity
import java.util.*

object CommitmentNotification {

    /**
     * 🔥 Fungsi Abadi: Menjadwalkan notifikasi harian untuk Commitment.
     * Menggunakan setExactAndAllowWhileIdle agar "Mutlak Wajib" muncul tepat waktu.
     */
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
                
                scheduleExactAlarm(context, commitment.id, commitment.title, index, hour, minute)
            }
        }
    }

    fun scheduleExactAlarm(
        context: Context,
        commitmentId: Int,
        title: String,
        timeIndex: Int,
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("type", "commitment")
            putExtra("id", commitmentId)
            putExtra("time_index", timeIndex)
            putExtra("hour", hour)
            putExtra("minute", minute)
        }

        val requestCode = commitmentId * 100 + timeIndex

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
            
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelNotifications(context: Context, commitment: CommitmentEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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
