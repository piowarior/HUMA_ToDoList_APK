package com.huma.app.ui.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.huma.app.MainActivity
import com.huma.app.R
import kotlin.random.Random

object NotificationHelper {

    private const val CHANNEL_ID = "huma_reminder"

    const val ID_GREETING = 1001
    const val ID_REMINDER = 1002
    const val ID_STREAK_MISS = 1003

    // Data Sumber untuk Greeting
    val GREETING_DATA = listOf(
        "Pagi yang Cerah! ☀️" to "Awali harimu dengan niat yang kuat. Mari produktif!",
        "Halo, Human! ☕" to "Kopi sudah siap, semangat juga harus siap. Apa goalsmu hari ini?",
        "Waktunya Bangun! 🌈" to "Dunia menunggumu. Jangan lupa tersenyum pagi ini.",
        "Hey! ✨" to "Hari ini adalah kesempatan baru untuk menjadi versi terbaikmu.",
        "Good Morning! 🌸" to "Semoga harimu dipenuhi dengan hal-hal positif."
    )

    // Data Sumber untuk Reminder
    val REMINDER_DATA = listOf(
        "Nyalakan Apimu! 🔥" to "Streak kamu masih menunggu. Yuk ritual sebentar!",
        "Jangan Lupa! 🕯️" to "Konsistensi adalah kunci. Cuma butuh 1 menit kok.",
        "Api Mulai Redup... ⚡" to "Bakar kata hari ini agar streak kamu tetap membara!",
        "Panggilan Ritual 🏮" to "Jaga momentum kamu. Mari bakar satu kata sekarang.",
        "Reminder Streak 🧨" to "Jangan biarkan progresmu hilang. Yuk aktifkan api hari ini!"
    )

    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "HUMA Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    fun show(
        context: Context,
        title: String,
        message: String,
        notifId: Int = Random.nextInt(),
        colorHex: String = "#6C63FF",
        category: String = NotificationCompat.CATEGORY_REMINDER
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logohumaicon)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(Color.parseColor(colorHex))
            .setColorized(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(category)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .build()

        NotificationManagerCompat.from(context).notify(notifId, notif)
    }
}
