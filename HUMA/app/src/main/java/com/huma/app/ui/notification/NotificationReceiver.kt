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
import com.huma.app.data.local.PreferenceManager
import com.huma.app.utils.getTodayDayId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    @RequiresPermission("android.permission.POST_NOTIFICATIONS")
    override fun onReceive(context: Context, intent: Intent) {
        val pref = PreferenceManager(context)
        val type = intent.getStringExtra("type") ?: "task"
        val title = intent.getStringExtra("title") ?: "Reminder"
        val message = intent.getStringExtra("message") ?: ""
        val notificationId = intent.getIntExtra("id", title.hashCode() + type.hashCode())

        // 1. Reschedule "Mutlak" untuk besok (Gak peduli setting on/off, jadwal harus tetap berputar)
        rescheduleIfNeeded(context, intent, type)

        // 2. Cek apakah master notification aktif
        if (!pref.isNotifEnabled) return

        // 3. Cek setting spesifik per tipe
        when (type) {
            "greeting" -> if (!pref.isGreetingNotifEnabled) return
            "streak_daily" -> if (!pref.isStreakNotifEnabled) return
            "streak_miss" -> if (!pref.isStreakMissNotifEnabled) return
        }

        // 4. Khusus Streak Daily ada pengecekan database
        if (type == "streak_daily") {
            handleStreakNotification(context, pref)
            return
        }

        showNotification(context, pref, type, title, message, notificationId)
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
            "greeting" -> NotificationScheduler.scheduleDailyGreeting(context, forceNextDay = true)
            "streak_daily" -> NotificationScheduler.scheduleDailyStreakReminder(context, forceNextDay = true)
        }
    }

    private fun handleStreakNotification(context: Context, pref: PreferenceManager) {
        val db = AppDatabase.getInstance(context)
        CoroutineScope(Dispatchers.IO).launch {
            val streak = db.streakDao().getStreak().firstOrNull()
            
            if (streak == null || streak.lastDayId == 0L) {
                showNotification(context, pref, "streak", "Nyalakan Apimu! 🔥", "Kamu belum memulai perjalanan streak. Ayo buka fitur Streak dan nyalakan api pertamamu!", NotificationHelper.ID_REMINDER)
            } else if (!streak.isIgnitedToday) {
                showNotification(context, pref, "streak", "Api Hampir Padam! 🕯️", "Jangan biarkan kerja kerasmu hilang. Ayo login dan lakukan ritual penyulutan sekarang!", NotificationHelper.ID_REMINDER)
            }
            
            if (pref.isStreakMissNotifEnabled) {
                checkMissedStreak(context, db, pref)
            }
        }
    }

    private suspend fun checkMissedStreak(context: Context, db: AppDatabase, pref: PreferenceManager) {
        val streakData = db.streakDao().getStreak().firstOrNull() ?: return
        if (streakData.lastDayId == 0L || streakData.isIgnitedToday) return

        val today = getTodayDayId()
        val diff = (today - streakData.lastDayId).toInt()

        when {
            diff == 2 && pref.isStreakMiss1Enabled -> showNotification(context, pref, "streak_miss", "Masih Ada Peluang! 🔥", "Kamu kelewat 1 hari nih, tapi tenang! Masih ada peluang api menyala jika kamu memiliki protection. Yuk login! 🛡️", NotificationHelper.ID_STREAK_MISS)
            diff == 6 && pref.isStreakMiss5Enabled -> showNotification(context, pref, "streak_miss", "Sudah Lumayan Lama... ⏳", "Sudah 5 hari kamu gak mampir. Ayo kita ulangin lagi rutinitas baikmu, jangan sampai benar-benar padam! 🕯️", NotificationHelper.ID_STREAK_MISS)
            diff >= 8 && (diff - 1) % 7 == 0 && pref.isStreakMiss7Enabled -> showNotification(context, pref, "streak_miss", "Rindu Kehangatan Apimu ❄️", "Sudah seminggu lebih terlewatkan... HUMA merindukanmu. Mari nyalakan kembali semangatmu hari ini! ✨", NotificationHelper.ID_STREAK_MISS)
        }
    }

    private fun showNotification(context: Context, pref: PreferenceManager, type: String, title: String, message: String, notificationId: Int) {
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

        val builder = NotificationCompat.Builder(context, "huma_reminder") 
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

        // Setting Suara & Getar
        var defaults = 0
        if (pref.isNotifSoundEnabled) defaults = defaults or NotificationCompat.DEFAULT_SOUND
        if (pref.isNotifVibrateEnabled) defaults = defaults or NotificationCompat.DEFAULT_VIBRATE
        builder.setDefaults(defaults)

        try {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        } catch (e: SecurityException) {
        }
    }
}
