package com.huma.app.ui.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val messages = listOf(
            "Sedikit langkah hari ini jauh lebih baik daripada diam.",
            "Waktu sebentar hari ini bisa berdampak besar nanti.",
            "Mulai saja dulu, sisanya akan mengikuti.",
            "Jangan tunda, hari ini masih punya kesempatan.",
            "Lakukan versi terbaikmu hari ini."
        )

        NotificationHelper.show(
            context,
            "Pengingat Harian",
            messages.random()
        )

        return Result.success()
    }
}