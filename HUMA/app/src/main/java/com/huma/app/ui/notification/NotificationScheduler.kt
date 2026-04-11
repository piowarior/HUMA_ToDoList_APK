package com.huma.app.ui.notification

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    fun scheduleAll(context: Context) {
        scheduleDailyGreeting(context)
        scheduleStreakReminders(context)
        scheduleStreakCheck(context)
    }

    /**
     * 1️⃣ Greeting Notif - Wajib muncul setiap pagi jam 06:00 - 12:00
     */
    private fun scheduleDailyGreeting(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<RandomGreetingWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelayUntil(6, 0), TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "daily_greeting",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * 2️⃣ Reminder Streak - Wajib 2x sehari (Pagi & Malam)
     * Hanya muncul jika streak belum dinyalakan hari ini.
     */
    private fun scheduleStreakReminders(context: Context) {
        // Pagi (Jam 08:00)
        val morningReminder = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelayUntil(8, 0), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "streak_reminder_morning",
            ExistingPeriodicWorkPolicy.UPDATE,
            morningReminder
        )

        // Malam (Jam 20:00)
        val eveningReminder = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelayUntil(20, 0), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "streak_reminder_evening",
            ExistingPeriodicWorkPolicy.UPDATE,
            eveningReminder
        )
    }

    /**
     * 3️⃣ Streak Miss Checker - Berjalan setiap 12 jam untuk mengecek kelewatan streak
     */
    private fun scheduleStreakCheck(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<StreakCheckWorker>(12, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "streak_miss_checker",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun calculateDelayUntil(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        if (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return calendar.timeInMillis - now
    }
}
