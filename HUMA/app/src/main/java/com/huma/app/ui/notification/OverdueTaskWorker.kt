package com.huma.app.ui.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.huma.app.data.local.AppDatabase
import com.huma.app.utils.getTodayDayId
import kotlinx.coroutines.flow.firstOrNull

class OverdueTaskWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val allTasks = db.taskDao().getAllTasks().firstOrNull() ?: return Result.success()
        
        val todayStartMillis = getTodayDayId() * (1000L * 60 * 60 * 24)
        
        // Cari task yang belum selesai dan tanggal mulainya sudah lewat hari ini
        val overdueTasks = allTasks.filter { !it.isDone && it.startDate < todayStartMillis }

        if (overdueTasks.isNotEmpty()) {
            val count = overdueTasks.size
            val title = "Tugas Terbengkalai... 📋"
            val message = if (count == 1) {
                "Ada 1 tugas yang terlewatkan. Jangan biarkan menumpuk, yuk selesaikan sekarang!"
            } else {
                "Ada $count tugas yang belum kelar dari hari-hari sebelumnya. Yuk cicil satu per satu!"
            }

            NotificationHelper.show(
                applicationContext,
                title,
                message,
                notifId = 2001, // ID khusus overdue
                colorHex = "#FF5252", // Red
                category = NotificationCompat.CATEGORY_EVENT
            )
        }

        return Result.success()
    }
}
