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
        val data = db.streakDao().getStreak().firstOrNull() ?: return Result.success()
        val today = getTodayDayId()
        val diff = (today - data.lastDayId).toInt()

        when {
            // diff == 2 artinya kemarin (diff 1) terlewat total.
            diff == 2 -> notify("Streak Terhenti! 🚨", "Kamu melewatkan ritual kemarin. Ayo selamatkan hari ini!", "#F44336")
            diff == 6 -> notify("Sudah 5 Hari... 🛑", "Streak kamu dalam bahaya besar. Kembalilah sebelum padam total!", "#D32F2F")
            diff >= 8 && (diff - 1) % 7 == 0 -> {
                notify("Streak Tertidur Pulas 💤", "Sudah seminggu lebih kamu tidak aktif. Kami menunggumu kembali.", "#B71C1C")
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
