package com.huma.app.ui.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.huma.app.R
import com.huma.app.data.local.AppDatabase
import com.huma.app.utils.getTodayDayId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    @RequiresPermission("android.permission.POST_NOTIFICATIONS")
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type") ?: "task"
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: ""
        val notificationId = intent.getIntExtra("id", title.hashCode() + type.hashCode())

        // 🔥 SELF RESCHEDULE LOGIC (Agar "Mutlak Wajib" Setiap Hari)
        rescheduleIfNeeded(context, intent, type)

        // 🔥 STREAK LOGIC CHECK
        if (type == "streak_daily") {
            handleStreakNotification(context)
            return
        }

        showNotification(context, type, title, message, notificationId)
    }

    private fun rescheduleIfNeeded(context: Context, intent: Intent, type: String) {
        when (type) {
            "commitment" -> {
                val hour = intent.getIntExtra("hour", -1)
                val minute = intent.getIntExtra("minute", -1)
                val timeIndex = intent.getIntExtra("time_index", 0)
                val title = intent.getStringExtra("title") ?: ""
                val id = intent.getIntExtra("id", -1)
                if (hour != -1 && minute != -1 && id != -1) {
                    CommitmentNotification.scheduleExactAlarm(context, id, title, timeIndex, hour, minute)
                }
            }
            "greeting" -> {
                NotificationScheduler.scheduleDailyGreeting(context)
            }
            "streak_daily" -> {
                NotificationScheduler.scheduleDailyStreakReminder(context)
            }
        }
    }

    private fun handleStreakNotification(context: Context) {
        val db = AppDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            val streak = db.streakDao().getStreak().firstOrNull()
            
            if (streak == null || streak.lastDayId == 0L) {
                // User belum pernah buka fitur streak
                showNotification(
                    context, "streak", 
                    "Nyalakan Apimu! 🔥", 
                    "Kamu belum memulai perjalanan streak. Ayo buka fitur Streak dan nyalakan api pertamamu!", 
                    NotificationHelper.ID_REMINDER
                )
            } else if (!streak.isIgnitedToday) {
                // Streak sudah berjalan tapi belum dinyalakan hari ini
                showNotification(
                    context, "streak", 
                    "Api Hampir Padam! 🕯️", 
                    "Jangan biarkan kerja kerasmu hilang. Ayo login dan lakukan ritual penyulutan sekarang!", 
                    NotificationHelper.ID_REMINDER
                )
            }
            
            // Cek juga kelewatan (diff logic dari StreakCheckWorker bisa digabung di sini agar terpusat)
            checkMissedStreak(context, db)
        }
    }

    private suspend fun checkMissedStreak(context: Context, db: AppDatabase) {
        val streakData = db.streakDao().getStreak().firstOrNull() ?: return
        if (streakData.lastDayId == 0L || streakData.isIgnitedToday) return

        val today = getTodayDayId()
        val diff = (today - streakData.lastDayId).toInt()

        when {
            diff == 2 -> showNotification(context, "streak_miss", "Masih Ada Peluang! 🔥", "Kamu kelewat 1 hari nih, tapi tenang! Masih ada peluang api menyala jika kamu memiliki protection. Yuk login! 🛡️", NotificationHelper.ID_STREAK_MISS)
            diff == 6 -> showNotification(context, "streak_miss", "Sudah Lumayan Lama... ⏳", "Sudah 5 hari kamu gak mampir. Ayo kita ulangin lagi rutinitas baikmu, jangan sampai benar-benar padam! 🕯️", NotificationHelper.ID_STREAK_MISS)
            diff >= 8 && (diff - 1) % 7 == 0 -> showNotification(context, "streak_miss", "Rindu Kehangatan Apimu ❄️", "Sudah seminggu lebih terlewatkan... HUMA merindukanmu. Mari nyalakan kembali semangatmu hari ini! ✨", NotificationHelper.ID_STREAK_MISS)
        }
    }

    private fun showNotification(context: Context, type: String, title: String, message: String, notificationId: Int) {
        val rootIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, rootIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            putExtra("notif_id", notificationId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, notificationId + 1, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            BitmapFactory.decodeResource(context.resources, R.drawable.logohumaicon)
        } catch (e: Exception) {
            null
        }

        val displayTitle = when(type) {
            "commitment" -> "Commitment Reminder 🎯"
            "greeting" -> title
            "streak", "streak_miss" -> title
            else -> "Huma Reminder 🎯"
        }

        val displayText = when(type) {
            "commitment" -> "Waktunya melakukan: $title\n\nJaga momentum kamu! Semangat terus ya! 🔥"
            "greeting" -> message
            "streak", "streak_miss" -> message
            else -> title
        }

        val notification = NotificationCompat.Builder(context, "huma_reminder") 
            .setSmallIcon(R.drawable.logohumaicon) 
            .setLargeIcon(largeIcon)
            .setContentTitle(displayTitle)
            .setContentText(displayText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(displayText))
            .setColor(Color.parseColor("#FFA726"))
            .setColorized(true)
            .setContentIntent(pendingIntent)
            .addAction(0, "BUKA", pendingIntent)
            .addAction(0, "TUTUP", dismissPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setOngoing(true)
            .setAutoCancel(false) 
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
        }
    }
}
