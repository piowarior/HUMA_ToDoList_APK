package com.huma.app.ui.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.huma.app.data.local.AppDatabase
import com.huma.app.data.local.PreferenceManager
import com.huma.app.utils.getTodayDayId
import kotlinx.coroutines.flow.firstOrNull

class StreakCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val pref = PreferenceManager(applicationContext)

        // Cek master + streak miss toggle (single toggle)
        if (!pref.isNotifEnabled || !pref.isStreakMissNotifEnabled) {
            return Result.success()
        }

        val db = AppDatabase.getInstance(applicationContext)
        val streakData = db.streakDao().getStreak().firstOrNull() ?: return Result.success()
        
        // Jika belum pernah ada streak (lastDayId = 0), tidak perlu cek kelewatan
        if (streakData.lastDayId == 0L) return Result.success()
        
        // Jika hari ini sudah dinyalakan, tidak perlu cek kelewatan
        if (streakData.isIgnitedToday) return Result.success()

        val today = getTodayDayId()
        val diff = (today - streakData.lastDayId).toInt()

        when {
            // Lewat 1 hari (Missed yesterday completely, now it's the day after)
            diff == 2 -> {
                if (pref.isStreakMiss1Enabled) {
                    notify(
                        "Masih Ada Peluang! 🔥",
                        "Kamu kelewat 1 hari nih, tapi tenang! Masih ada peluang api menyala jika kamu memiliki protection. Yuk login! 🛡️",
                        "#FF9800"
                    )
                }
            }
            // Lewat 5 hari
            diff == 6 -> {
                if (pref.isStreakMiss5Enabled) {
                    notify(
                        "Sudah Lumayan Lama... ⏳",
                        "Sudah 5 hari kamu gak mampir. Ayo kita ulangin lagi rutinitas baikmu, jangan sampai benar-benar padam! 🕯️",
                        "#F44336"
                    )
                }
            }
            // Lewat 7 hari dan setiap kelipatan 7 hari berikutnya
            diff >= 8 && (diff - 1) % 7 == 0 -> {
                if (pref.isStreakMiss7Enabled) {
                    notify(
                        "Rindu Kehangatan Apimu ❄️",
                        "Sudah seminggu lebih terlewatkan... HUMA merindukanmu. Mari nyalakan kembali semangatmu hari ini! ✨",
                        "#B71C1C"
                    )
                }
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
