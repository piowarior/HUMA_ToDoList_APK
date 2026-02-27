package com.huma.app.data.local.streak

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak_table")
data class StreakEntity(
    @PrimaryKey val id: Int = 0, // Hanya ada satu baris data streak global

    // --- STATUS DASAR ---
    val currentStreak: Int = 0,
    val highestStreak: Int = 0,
    val lastLoginMillis: Long = 0,
    val lastDayId: Long = 0,

    // --- STATUS RITUAL PENYULUTAN (The Great Ignition) ---
    // false = belum ritual hari ini (api dingin/bara), true = sudah ritual (api membara)
    val isIgnitedToday: Boolean = false,
    val isFirstTimeUser: Boolean = true, // Untuk trigger "The Awakening"
    val lastBurnedWord: String = "",     // Kata yang dilemparkan ke api hari ini
    val frictionProgress: Float = 0f,    // Progress gesek batu api (0.0 sampai 1.0)

    // --- STATUS NYAWA & PERISAI (The Life Line & Shield) ---
    val lifeLineCount: Int = 1,          // 1 = Tersedia, 0 = Sudah dipakai
    val lifeLineRecoveryProgress: Int = 0, // Sudah berapa hari sejak nyawa dipakai (target 7 hari)
    val hasShield: Boolean = false,      // Aktif otomatis di hari ke-25
    val streakStartMillis: Long = 0L, // hari pertama streak dimulai
    val protectedDays: List<Int> = emptyList(),



    // --- STATUS VISUAL EVOLUSI (Berdasarkan Konsep Hari) ---
    /* Level Table (Internal Logic):
       1-4   : Candle Flame (Biru-Oranye, Kalem)
       5-14  : Campfire (Membesar, Sparks)
       15-29 : Blazing Torch (Merah Terang, Haptic)
       25+   : Shield Unlocked (Golden Chain)
       30-49 : Hellfire/Inferno (Aura Ungu/Putih, Vignette Merah)
       50-99 : Plasma Core (Biru Electric, Rantai Pijar Putih, Heat Haze)
       100-199: Supernova (Putih Silau, Aura Emas, Suara Humming)
       200-364: Dragon Breath (Spiral Tornado, Naga Api Partikel)
       365+  : Eternal Sun (Legend, Orbit Mode)
    */
    val currentFireLevel: String = "COLD", // Level visual saat ini
    val fireIntensityBonus: Float = 0f    // Tambahan efek per hari (misal hari 51, 52 dst)
) {
    /**
     * Fungsi Helper untuk menentukan level visual berdasarkan hari.
     * Ini akan memudahkan di UI nanti untuk ganti-ganti animasi.
     */
    fun getFireLevel(): FireLevel {
        return when (currentStreak) {
            0 -> FireLevel.COLD
            in 1..4 -> FireLevel.CANDLE_FLAME
            in 5..14 -> FireLevel.CAMPFIRE
            in 15..29 -> FireLevel.BLAZING_TORCH
            in 30..49 -> FireLevel.HELLFIRE_INFERNO
            in 50..99 -> FireLevel.PLASMA_CORE
            in 100..199 -> FireLevel.SUPERNOVA
            in 200..364 -> FireLevel.DRAGON_BREATH
            else -> FireLevel.ETERNAL_SUN
        }
    }
}

/**
 * Enum untuk memudahkan identifikasi fase api di UI
 */
enum class FireLevel {
    COLD,
    CANDLE_FLAME,
    CAMPFIRE,
    BLAZING_TORCH,
    HELLFIRE_INFERNO,
    PLASMA_CORE,
    SUPERNOVA,
    DRAGON_BREATH,
    ETERNAL_SUN
}