package com.huma.app.ui.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.huma.app.data.local.AppDatabase
import com.huma.app.utils.getTodayDayId
import kotlinx.coroutines.flow.firstOrNull

class StreakCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val streakData = db.streakDao().getStreak().firstOrNull() ?: return Result.success()
        
        // Jika belum pernah ada streak (lastDayId = 0), tidak perlu cek kelewatan
        if (streakData.lastDayId == 0L) return Result.success()
        
        // Jika hari ini sudah dinyalakan, tidak perlu cek kelewatan
        if (streakData.isIgnitedToday) return Result.success()

        val today = getTodayDayId()
        val diff = (today - streakData.lastDayId).toInt()

        when {
            // Lewat 1 hari (Hari ke-2 tidak streak)
            diff == 2 -> {
                notify(
                    "Masih Ada Kesempatan! 🕯️",
                    "Streak kamu hampir terputus. Ayo login kembali dan nyalakan apimu sekarang!",
                    "#FF9800" // Orange
                )
            }
            // Lewat 2 hari (Hari ke-3 tidak streak)
            diff == 3 -> {
                notify(
                    "Api Telah Padam... 🌑",
                    "Sayang sekali, api kamu sudah padam sepenuhnya. Kamu kembali ke awal, tapi jangan menyerah! Ayo mulai lagi.",
                    "#F44336" // Red
                )
            }
            // Lewat seminggu (Hari ke-8) dan setiap minggu berikutnya
            diff >= 8 && (diff - 1) % 7 == 0 -> {
                notify(
                    "Apimu Merindukanmu ❄️",
                    "Sudah beberapa hari terlewatkan... Bagaimana kabar apimu? HUMA menunggumu kembali aktif.",
                    "#B71C1C" // Deep Red
                )
            }
        }
        return Result.success()
    }

    private fun notify(title: String, msg: String, colorHex: String) {
        NotificationHelper.show(
            applicationContext, 
            title, 
            msg,
            NotificationHelper.ID_STREAK_MISS,
            colorHex = colorHex,
            category = NotificationCompat.CATEGORY_STATUS
        )
    }
}
