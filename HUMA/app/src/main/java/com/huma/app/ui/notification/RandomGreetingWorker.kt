package com.huma.app.ui.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.Calendar

class RandomGreetingWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        // Hanya muncul jika dipicu antara jam 6 pagi sampai 12 siang (untuk jaga-jaga)
        if (hour !in 6..12) return Result.success()

        val greetings = listOf(
            "Pagi yang Cerah! ☀️" to "Awali harimu dengan niat yang kuat. Mari produktif bersama HUMA!",
            "Halo, Human! ☕" to "Kopi sudah siap, semangat juga harus siap. Apa goalsmu hari ini?",
            "Waktunya Bangun! 🌈" to "Dunia menunggumu. Jangan lupa tersenyum dan mulai ritual pagimu.",
            "Hey! ✨" to "Hari ini adalah kesempatan baru untuk menjadi versi terbaikmu. Yuk fokus!",
            "Good Morning! 🌸" to "Semoga harimu dipenuhi dengan hal-hal positif dan tugas yang tuntas.",
            "Sapaan Pagi HUMA 🌿" to "Sudah siap menaklukkan hari? Mari kita buat kemajuan kecil hari ini.",
            "Semangat Pagi! 🔋" to "Energi baru, semangat baru. Jangan biarkan hari ini berlalu tanpa arti.",
            "Rise and Shine! 💎" to "Setiap detik berharga. Mari kita susun rencana hebat untuk hari ini."
        ).random()

        NotificationHelper.show(
            applicationContext,
            greetings.first,
            greetings.second,
            NotificationHelper.ID_GREETING,
            colorHex = "#4FC3F7", // Sky Blue
            category = NotificationCompat.CATEGORY_PROMO
        )
        return Result.success()
    }
}
