package com.huma.app.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huma.app.data.local.streak.FireLevel
import com.huma.app.data.local.streak.StreakDao
import com.huma.app.data.local.streak.StreakEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class StreakViewModel(private val dao: StreakDao) : ViewModel() {

    // --- STATE UTAMA ---
    private val _streakData = dao.getStreak().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    val streakData = _streakData

    // --- STATE UNTUK RITUAL (UI ONLY) ---
    var currentFriction by mutableStateOf(0f)
    var showInquiry by mutableStateOf(false)
    var isAwakeningActive by mutableStateOf(false)

    // --- DEBUG MODE (Time Machine) ---
    // Gunakan ini untuk ngetest visual tanpa nunggu berhari-hari
    var debugDaysOffset by mutableStateOf(0)

    /**
     * CEK STATUS STREAK (Panggil setiap kali Screen dibuka)
     * Logika untuk mengecek apakah streak putus, terancam, atau aman.
     */
    // Di dalam StreakViewModel.kt, ubah fungsi ini:

    fun checkStreakLogic() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val currentData = dao.getStreak().firstOrNull()

            if (currentData == null) {
                val initial = StreakEntity(
                    id = 0,
                    currentStreak = 0,
                    isIgnitedToday = false,
                    lastBurnedWord = "Siap?",
                    lastLoginMillis = now
                )
                dao.upsertStreak(initial)
            } else {
                val lastLogin = Calendar.getInstance().apply { timeInMillis = currentData.lastLoginMillis }
                val today = Calendar.getInstance()

                // Cek apakah hari sudah berganti (Beda tanggal, bulan, atau tahun)
                val isDifferentDay = lastLogin.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR) ||
                        lastLogin.get(Calendar.YEAR) != today.get(Calendar.YEAR)

                if (isDifferentDay) {
                    // Jika hari sudah ganti, cek apakah kemarin api nyala
                    if (currentData.isIgnitedToday) {
                        // Aman, tapi status nyala hari ini di-reset jadi false (siap dinyalain lagi)
                        dao.upsertStreak(currentData.copy(
                            isIgnitedToday = false,
                            lastLoginMillis = now
                        ))
                    } else {
                        // Kemarin gak nyalain api sama sekali? Terancam putus.
                        handleStreakThreat(currentData)
                    }
                }
            }
        }
    }

    /**
     * LOGIKA PENYELAMATAN (Shield & Life Line)
     */
    private suspend fun handleStreakThreat(data: StreakEntity) {
        if (data.hasShield) {
            // Shield menyelamatkan tanpa mengurangi nyawa
            dao.upsertStreak(data.copy(hasShield = false, lastLoginMillis = System.currentTimeMillis()))
        } else if (data.lifeLineCount > 0) {
            // Status Terancam: UI nanti akan menampilkan ritual "Gunakan Nyawa"
            // Kita tidak reset otomatis, biarkan user masuk ke ritual penyelamatan
        } else {
            // Game Over: Hangus total
            dao.resetStreak()
        }
    }

    /**
     * RITUAL 1: FRICTION (Gesek Batu Api)
     */
    fun onFrictionSwipe(delta: Float) {
        val data = _streakData.value ?: return
        if (data.isIgnitedToday) return

        currentFriction = (currentFriction + delta).coerceIn(0f, 1.1f)

        if (currentFriction >= 1.0f && !showInquiry) {
            showInquiry = true
        }
    }

    /**
     * RITUAL 2 & 3: BAKAR KATA & NYALAKAN API (Ignite)
     */
    fun igniteTheFlame(word: String) {
        viewModelScope.launch {
            val data = _streakData.value ?: StreakEntity()
            val newStreak = data.currentStreak + 1

            // Cek pemulihan nyawa (7 hari konsistensi)
            var newLifeLineCount = data.lifeLineCount
            var newRecoveryProgress = data.lifeLineRecoveryProgress

            if (data.lifeLineCount == 0) {
                newRecoveryProgress += 1
                if (newRecoveryProgress >= 7) {
                    newLifeLineCount = 1
                    newRecoveryProgress = 0
                }
            }

            val updatedData = data.copy(
                currentStreak = newStreak,
                isIgnitedToday = true,
                lastBurnedWord = word,
                lastLoginMillis = System.currentTimeMillis(),
                highestStreak = if (newStreak > data.highestStreak) newStreak else data.highestStreak,
                hasShield = newStreak >= 25,
                lifeLineCount = newLifeLineCount,
                lifeLineRecoveryProgress = newRecoveryProgress,
                isFirstTimeUser = false
            )

            dao.upsertStreak(updatedData)
            showInquiry = false
            currentFriction = 0f
        }
    }

    /**
     * RITUAL 4: GUNAKAN NYAWA (Penyelamatan Manual)
     */
    fun useLifeLineRitual() {
        viewModelScope.launch {
            val data = _streakData.value ?: return@launch
            dao.useLifeLine()
            // Setelah nyawa dipakai, set login ke 'sekarang' agar tidak hangus
            dao.upsertStreak(data.copy(
                lastLoginMillis = System.currentTimeMillis(),
                isIgnitedToday = false
            ))
        }
    }

    /**
     * FUNGSI DINAMIS UNTUK UI (Evolusi Api Harian)
     * Menggabungkan data asli + debug offset
     */
    fun getEffectiveStreak(): Int = (_streakData.value?.currentStreak ?: 0) + debugDaysOffset

    /**
     * DETERMINASI VISUAL (Mega Detail Per Hari)
     */
    fun getFireLevel(): FireLevel {
        val days = getEffectiveStreak()
        return when {
            days == 0 -> FireLevel.COLD
            days in 1..4 -> FireLevel.CANDLE_FLAME
            days in 5..14 -> FireLevel.CAMPFIRE
            days in 15..29 -> FireLevel.BLAZING_TORCH
            days in 30..49 -> FireLevel.HELLFIRE_INFERNO
            days in 50..99 -> FireLevel.PLASMA_CORE
            days in 100..199 -> FireLevel.SUPERNOVA
            days in 200..364 -> FireLevel.DRAGON_BREATH
            else -> FireLevel.ETERNAL_SUN
        }
    }

    // --- DEBUG FUNCTIONS (Time Machine) ---
    fun debugAddDays(amount: Int) { debugDaysOffset += amount }
    fun debugSetDays(target: Int) {
        val currentReal = _streakData.value?.currentStreak ?: 0
        debugDaysOffset = target - currentReal
    }
    fun debugReset() { debugDaysOffset = 0 }

    // Di dalam StreakScreen.kt (tambahkan di luar Composable)
    fun playIgniteSound(context: android.content.Context, streak: Int) {
        val soundRes = when {
            streak >= 100 -> /* raw res supernova */ 0
            streak >= 30 -> /* raw res inferno */ 0
            streak % 10 == 0 -> /* raw res spesial tiap 10 */ 0
            else -> /* raw res api biasa */ 0
        }
        // Implementasi sederhana: MediaPlayer.create(context, soundRes).start()
    }
}