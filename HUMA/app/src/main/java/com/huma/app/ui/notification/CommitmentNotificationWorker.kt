package com.huma.app.ui.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.huma.app.data.local.AppDatabase
import java.text.SimpleDateFormat
import java.util.*

class CommitmentNotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val commitmentId = inputData.getInt("commitment_id", -1)
        if (commitmentId == -1) return Result.success()

        val db = AppDatabase.getInstance(applicationContext)
        val commitment = db.commitmentDao().getCommitmentById(commitmentId) ?: return Result.success()

        // Cek apakah sudah dikerjakan hari ini
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (commitment.completedDays.contains(today)) return Result.success()

        // Cek apakah hari ini hari libur di jadwal custom
        if (commitment.isCustomSchedule) {
            val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            if (!commitment.scheduledDays.contains(dayOfWeek)) return Result.success()
        }

        NotificationHelper.show(
            applicationContext,
            "Ritual Belum Selesai! 🔥",
            "Waktunya melakukan \"${commitment.title}\". Jangan biarkan streak terputus!",
            notifId = commitment.id + 1000, // ID unik per commitment
            colorHex = commitment.colorHex,
            category = NotificationCompat.CATEGORY_REMINDER
        )

        return Result.success()
    }
}
