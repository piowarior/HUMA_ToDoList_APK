// File: C:/Users/rohisul iman/AndroidStudioProjects/HUMA/app/src/main/java/com/huma/app/ui/notification/ReminderWorker.kt
package com.huma.app.ui.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.huma.app.data.local.AppDatabase
import kotlinx.coroutines.flow.firstOrNull

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val streak = db.streakDao().getStreak().firstOrNull()

        if (streak?.isIgnitedToday == true) return Result.success()

        val data = listOf(
            "Nyalakan Apimu! 🔥" to "Streak kamu masih menunggu. Yuk ritual sebentar!",
            "Jangan Lupa! 🕯️" to "Konsistensi adalah kunci. Cuma butuh 1 menit kok.",
            "Api Mulai Redup... ⚡" to "Bakar kata hari ini agar streak kamu tetap membara!",
            "Panggilan Ritual 🏮" to "Jaga momentum kamu. Mari bakar satu kata sekarang.",
            "Reminder Streak 🧨" to "Jangan biarkan progresmu hilang. Yuk aktifkan api hari ini!"
        ).random()

        NotificationHelper.show(
            applicationContext,
            data.first,
            data.second,
            NotificationHelper.ID_REMINDER,
            colorHex = "#6C63FF", // Purple Huma
            category = NotificationCompat.CATEGORY_REMINDER
        )
        return Result.success()
    }
}