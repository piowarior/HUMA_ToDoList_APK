package com.huma.app.ui.notification

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object NotificationScheduler {

    fun scheduleAll(context: Context) {

        scheduleReminder(context)
        scheduleGreeting(context)
        scheduleStreakCheck(context)

    }

    // 1️⃣ Reminder streak (1–2x random sehari)
    private fun scheduleReminder(context: Context) {

        val delayMinutes = Random.nextLong(30, 720) // 30 menit – 12 jam

        val work = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "daily_reminder",
                ExistingWorkPolicy.REPLACE,
                work
            )
    }

    // 2️⃣ Sapaan random (1x sehari)
    private fun scheduleGreeting(context: Context) {

        val delayMinutes = Random.nextLong(60, 1440) // 1 – 24 jam

        val work = OneTimeWorkRequestBuilder<RandomGreetingWorker>()
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "daily_greeting",
                ExistingWorkPolicy.REPLACE,
                work
            )
    }

    // 3️⃣ Cek streak miss
    private fun scheduleStreakCheck(context: Context) {

        val work = PeriodicWorkRequestBuilder<StreakCheckWorker>(
            12, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                "streak_checker",
                ExistingPeriodicWorkPolicy.UPDATE,
                work
            )
    }
}