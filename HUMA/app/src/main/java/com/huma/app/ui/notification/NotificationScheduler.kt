package com.huma.app.ui.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.huma.app.data.local.PreferenceManager
import java.util.Calendar
import kotlin.random.Random

object NotificationScheduler {

    const val REQ_GREETING = 5001
    const val REQ_STREAK_DAILY = 5002

    /**
     * Menjadwalkan semua notifikasi rutin (Greeting & Streak).
     * Cek preference dulu sebelum jadwal.
     */
    fun scheduleAll(context: Context) {
        val pref = PreferenceManager(context)
        if (!pref.isNotifEnabled) {
            // Master off → cancel semua
            cancelGreeting(context)
            cancelStreakReminder(context)
            return
        }
        if (pref.isGreetingNotifEnabled) {
            scheduleDailyGreeting(context, forceNextDay = false)
        } else {
            cancelGreeting(context)
        }
        if (pref.isStreakNotifEnabled) {
            scheduleDailyStreakReminder(context, forceNextDay = false)
        } else {
            cancelStreakReminder(context)
        }
    }

    /**
     * 1️⃣ Greeting Notif - Wajib muncul sekali sehari jam 06:00 - 12:00.
     */
    fun scheduleDailyGreeting(context: Context, forceNextDay: Boolean = false) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (!forceNextDay) {
            val checkIntent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("type", "greeting")
            }
            val pending = PendingIntent.getBroadcast(
                context, REQ_GREETING, checkIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pending != null) return
        }

        val randomHour = Random.nextInt(6, 12)
        val randomMinute = Random.nextInt(0, 60)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, randomHour)
            set(Calendar.MINUTE, randomMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            if (forceNextDay || timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            val greetings = listOf(
                "Pagi yang Cerah! ☀️" to "Awali harimu dengan niat yang kuat. Mari produktif bersama HUMA!",
                "Halo, Human! ☕" to "Kopi sudah siap, semangat juga harus siap. Apa goalsmu hari ini?",
                "Waktunya Bangun! 🌈" to "Dunia menunggumu. Jangan lupa tersenyum dan mulai ritual pagimu.",
                "Hey! ✨" to "Hari ini adalah kesempatan baru untuk menjadi versi terbaikmu. Yuk fokus!",
                "Semangat Pagi! 🔋" to "Energi baru, semangat baru. Jangan biarkan hari ini berlalu tanpa arti."
            ).random()
            
            putExtra("title", greetings.first)
            putExtra("message", greetings.second)
            putExtra("type", "greeting")
            putExtra("id", NotificationHelper.ID_GREETING)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, REQ_GREETING, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setExactAlarm(alarmManager, calendar.timeInMillis, pendingIntent)
    }

    /**
     * 2️⃣ Daily Streak Reminder - Wajib muncul sehari sekali jika belum nyala.
     */
    fun scheduleDailyStreakReminder(context: Context, forceNextDay: Boolean = false) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        if (!forceNextDay) {
            val checkIntent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("type", "streak_daily")
            }
            val pending = PendingIntent.getBroadcast(
                context, REQ_STREAK_DAILY, checkIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pending != null) return
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            if (forceNextDay || timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("type", "streak_daily")
            putExtra("id", NotificationHelper.ID_REMINDER)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, REQ_STREAK_DAILY, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setExactAlarm(alarmManager, calendar.timeInMillis, pendingIntent)
    }

    /**
     * Cancel greeting alarm.
     */
    fun cancelGreeting(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("type", "greeting")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQ_GREETING, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it); it.cancel() }
    }

    /**
     * Cancel streak daily reminder alarm.
     */
    fun cancelStreakReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("type", "streak_daily")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQ_STREAK_DAILY, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it); it.cancel() }
    }

    private fun setExactAlarm(alarmManager: AlarmManager, timeInMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }
}
