package com.huma.app.data.local.streak

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {

    /**
     * Mengambil data streak tunggal.
     * Menggunakan Flow agar UI otomatis terupdate saat ada perubahan (misal: saat gesek/bakar kata).
     */
    @Query("SELECT * FROM streak_table WHERE id = 0")
    fun getStreak(): Flow<StreakEntity?>

    /**
     * Fungsi utama untuk menyimpan atau memperbarui seluruh data streak.
     */
    @Upsert
    suspend fun upsertStreak(streak: StreakEntity)

    /**
     * UPDATE KHUSUS RITUAL:
     * Digunakan saat user sedang melakukan ritual "Friction" (Gesek).
     * Kita update progress geseknya saja agar performa lebih ringan.
     */
    @Query("UPDATE streak_table SET frictionProgress = :progress WHERE id = 0")
    suspend fun updateFrictionProgress(progress: Float)

    /**
     * UPDATE SETELAH BAKAR KATA:
     * Dipanggil saat langkah 3 ritual selesai. Api menyala (isIgnitedToday = true).
     */
    @Query("""
        UPDATE streak_table SET 
        currentStreak = currentStreak + 1, 
        isIgnitedToday = 1, 
        lastBurnedWord = :word, 
        lastLoginMillis = :timestamp,
        frictionProgress = 0.0 
        WHERE id = 0
    """)
    suspend fun igniteFire(word: String, timestamp: Long)

    /**
     * RESET STREAK:
     * Dipanggil jika user gagal (lewat 48 jam) dan tidak punya nyawa/perisai.
     */
    @Query("""
        UPDATE streak_table SET 
        currentStreak = 0, 
        isIgnitedToday = 0, 
        lifeLineCount = 1, 
        hasShield = 0,
        frictionProgress = 0.0 
        WHERE id = 0
    """)
    suspend fun resetStreak()

    /**
     * PENGGUNAAN NYAWA (Life Line):
     * Dipanggil saat ritual penyelamatan dilakukan.
     */
    @Query("UPDATE streak_table SET lifeLineCount = 0, lifeLineRecoveryProgress = 0 WHERE id = 0")
    suspend fun useLifeLine()

    /**
     * PENGEMBALIAN NYAWA:
     * Dipanggil otomatis jika user bertahan 7 hari setelah nyawa hilang.
     */
    @Query("UPDATE streak_table SET lifeLineCount = 1 WHERE id = 0")
    suspend fun restoreLifeLine()
}