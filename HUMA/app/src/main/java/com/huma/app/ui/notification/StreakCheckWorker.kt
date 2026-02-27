package com.huma.app.ui.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.huma.app.data.local.streak.StreakDao
import com.huma.app.utils.getTodayDayId
import kotlinx.coroutines.flow.firstOrNull

class StreakCheckWorker(
    private val context: Context,
    params: WorkerParameters,
    private val dao: StreakDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        val data = dao.getStreak().firstOrNull() ?: return Result.success()
        val today = getTodayDayId()
        val diff = (today - data.lastDayId).toInt()

        when {
            diff == 1 -> notify("Hari ini belum tercatat", "Masih ada waktu untuk melanjutkan hari ini.")
            diff == 2 -> notify("Dua hari terlewat", "Yuk mulai kembali dan lanjutkan langkahmu.")
            diff >= 5 -> notify("Waktu berlalu cukup lama", "Tidak ada kata terlambat untuk memulai lagi.")
        }

        return Result.success()
    }

    private fun notify(title: String, msg: String) {
        NotificationHelper.show(context, title, msg)
    }
}