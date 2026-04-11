package com.huma.app.ui.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.huma.app.data.local.AppDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val streak = db.streakDao().getStreak().firstOrNull()

        // Jangan kirim notifikasi jika hari ini streak sudah dinyalakan
        if (streak?.isIgnitedToday == true) return Result.success()

        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        val (title, message) = if (hour < 12) {
            // Pesan Pagi
            listOf(
                "Semangat Pagi! 🔥" to "Apimu belum menyala hari ini. Yuk ritual sebentar agar semangatmu membara!",
                "Ritual Pagi HUMA 🕯️" to "Mumpung masih pagi, ayo amankan streak kamu sekarang juga.",
                "Start Your Day! 🔋" to "Jangan biarkan apimu padam. Satu menit ritual akan merubah harimu.",
                "Ayo Nyalakan! 💡" to "Streak kamu sedang menunggu. Mari buat progres hari ini."
            ).random()
        } else {
            // Pesan Malam
            listOf(
                "Sudah Malam, Human! 🌙" to "Ih dah malem, ayo nyalakan apimu sebelum hari ini berakhir dan streak padam!",
                "Hampir Terlambat! ⏰" to "Jangan sampai api hari ini padam sepenuhnya. Yuk ritual sekarang!",
                "Panggilan Terakhir 🔥" to "Dikit lagi hari berganti. Amankan streak kamu sebelum kelewatan!",
                "Awas Padam! 🌑" to "Tinggal beberapa jam lagi. Jangan biarkan konsistensimu sia-sia malam ini."
            ).random()
        }

        NotificationHelper.show(
            applicationContext,
            title,
            message,
            NotificationHelper.ID_REMINDER,
            colorHex = "#6C63FF", // Purple Huma
            category = NotificationCompat.CATEGORY_REMINDER
        )
        return Result.success()
    }
}
