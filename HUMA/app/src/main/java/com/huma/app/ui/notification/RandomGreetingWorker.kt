// File: C:/Users/rohisul iman/AndroidStudioProjects/HUMA/app/src/main/java/com/huma/app/ui/notification/RandomGreetingWorker.kt
package com.huma.app.ui.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.Calendar

class RandomGreetingWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour !in 6..11) return Result.success()

        val data = listOf(
            "Pagi yang Cerah! ☀️" to "Awali harimu dengan niat yang kuat. Mari produktif!",
            "Halo, Human! ☕" to "Kopi sudah siap, semangat juga harus siap. Apa goalsmu hari ini?",
            "Waktunya Bangun! 🌈" to "Dunia menunggumu. Jangan lupa tersenyum pagi ini.",
            "Hey! ✨" to "Hari ini adalah kesempatan baru untuk menjadi versi terbaikmu.",
            "Good Morning! 🌸" to "Semoga harimu dipenuhi dengan hal-hal positif."
        ).random()

        NotificationHelper.show(
            applicationContext,
            data.first,
            data.second,
            NotificationHelper.ID_GREETING,
            colorHex = "#4FC3F7", // Sky Blue
            category = NotificationCompat.CATEGORY_PROMO
        )
        return Result.success()
    }
}