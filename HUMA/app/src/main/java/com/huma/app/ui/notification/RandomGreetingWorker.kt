package com.huma.app.ui.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class RandomGreetingWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val messages = listOf(
            "Semoga harimu berjalan dengan baik hari ini.",
            "Jangan lupa jaga semangat dan kesehatan.",
            "Semoga aktivitasmu hari ini lancar.",
            "Semoga semua rencanamu berjalan baik.",
            "Hari baru, kesempatan baru."
        )

        NotificationHelper.show(
            context,
            "Hai 👋",
            messages.random()
        )

        return Result.success()
    }
}