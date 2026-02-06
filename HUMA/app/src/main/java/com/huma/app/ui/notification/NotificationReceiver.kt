package com.huma.app.ui.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.huma.app.R

class NotificationReceiver : BroadcastReceiver() {

    @RequiresPermission("android.permission.POST_NOTIFICATIONS")
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Task Reminder"

        // 1. Intent untuk membuka aplikasi (Main Activity)
        val rootIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            // Memberitahu Android untuk membuka task yang sudah ada atau buat baru
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            title.hashCode(), // Gunakan hashCode agar unik jika ada banyak notif
            rootIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Build Tampilan Notifikasi yang lebih "Pro"
        val notification = NotificationCompat.Builder(context, "task_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Huma Reminder 🎯") // Tambah emoji biar manis
            .setContentText(title)
            .setStyle(NotificationCompat.BigTextStyle()
                .setBigContentTitle("Ayo selesaikan tugasmu!")
                .bigText("Tugas: $title\n\nJangan ditunda ya, semangat! ✨"))

            .setColor(Color.parseColor("#6C63FF")) // Warna Indigo khas Huma
            .setColorized(true)

            // Mengarahkan klik notifikasi ke aplikasi
            .setContentIntent(pendingIntent)

            // 🔥 Menambahkan Tombol Aksi di bawah notifikasi
            .addAction(0, "BUKA APLIKASI", pendingIntent)

            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Bunyi + Getar default
            .setAutoCancel(true) // Hilang setelah diklik
            .build()

        // 3. Tampilkan
        try {
            NotificationManagerCompat.from(context).notify(title.hashCode(), notification)
        } catch (e: SecurityException) {
            // Handle jika permission hilang tiba-tiba
        }
    }
}