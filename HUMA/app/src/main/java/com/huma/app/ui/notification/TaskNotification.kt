package com.huma.app.ui.notification

import android.app.AlarmManager
import java.util.Calendar
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

fun scheduleTaskNotification(
    context: Context,
    dateMillis: Long, // Tanggal yang dipilih (Long)
    timeText: String?, // Format "HH:mm" atau "HH:mm - HH:mm"
    title: String
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = dateMillis

    if (!timeText.isNullOrBlank()) {
        try {
            // Ambil bagian jam mulai (misal "08:30")
            val startTime = timeText.split("-")[0].trim()
            val parts = startTime.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            // Set jam dan menit ke calendar
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
        } catch (e: Exception) {
            // Jika gagal parse, default ke jam 8 pagi
            calendar.set(Calendar.HOUR_OF_DAY, 8)
        }
    } else {
        // Jika user tidak set jam, default notifikasi muncul jam 8 pagi di hari tersebut
        calendar.set(Calendar.HOUR_OF_DAY, 8)
        calendar.set(Calendar.MINUTE, 0)
    }

    // JANGAN kirim notifikasi jika waktunya sudah lewat
    if (calendar.timeInMillis <= System.currentTimeMillis()) return

    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("title", title)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        title.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Logika AlarmManager (Tetap sama seperti punyamu)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}
