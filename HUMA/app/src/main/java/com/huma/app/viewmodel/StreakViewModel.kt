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

    init {
        viewModelScope.launch {
            dao.insertDefault(StreakEntity(id = 0))
        }
    }

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
            val data = dao.getStreak().firstOrNull() ?: return@launch

            // --- FIX: FIRST RUN GUARD ---
            if (data.lastLoginMillis == 0L && data.currentStreak == 0) {
                return@launch
            }

            val todayStart = getTodayStartMillis()
            val lastLoginDay = data.lastLoginMillis

            val diffDays = ((todayStart - lastLoginDay) / DAY).toInt()

            when {
                diffDays == 0 -> return@launch

                diffDays == 1 -> {
                    dao.upsertStreak(data.copy(isIgnitedToday = false))
                }

                diffDays > 1 -> handleStreakThreat(data)
            }
        }
    }




    // Helper untuk hitung selisih hari
    private fun getDaysDiff(last: Calendar, now: Calendar): Int {
        val date1 = last.clone() as Calendar
        val date2 = now.clone() as Calendar
        date1.set(Calendar.HOUR_OF_DAY, 0); date1.set(Calendar.MINUTE, 0); date1.set(Calendar.SECOND, 0); date1.set(Calendar.MILLISECOND, 0)
        date2.set(Calendar.HOUR_OF_DAY, 0); date2.set(Calendar.MINUTE, 0); date2.set(Calendar.SECOND, 0); date2.set(Calendar.MILLISECOND, 0)
        return ((date2.timeInMillis - date1.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
    }

    private suspend fun handleStreakThreat(data: StreakEntity) {
        when {
            data.hasShield -> {
                dao.upsertStreak(
                    data.copy(
                        hasShield = false,
                        isIgnitedToday = false
                    )
                )
            }

            data.lifeLineCount > 0 -> {
                isAwakeningActive = true
            }

            else -> {
                dao.resetStreak()
            }
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
            val data = _streakData.value ?: return@launch
            val todayStart = getTodayStartMillis()

            val isFirst = data.currentStreak == 0

            val updatedData = data.copy(
                currentStreak = data.currentStreak + 1,
                isIgnitedToday = true,
                lastBurnedWord = word,
                lastLoginMillis = todayStart,
                streakStartMillis = if (isFirst) todayStart else data.streakStartMillis,
                highestStreak = maxOf(data.highestStreak, data.currentStreak + 1),
                hasShield = data.currentStreak + 1 >= 25
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

            val now = System.currentTimeMillis()

            // Hitung hari bolong
            val diffDays = ((now - data.lastLoginMillis) / DAY).toInt()

            // Hari yang di-protect = hari sebelumnya
            val protectedDay = data.currentStreak + 1

            dao.upsertStreak(
                data.copy(
                    currentStreak = data.currentStreak + diffDays, // 🔥 TAMBAH STREAK SESUAI HARI YANG TERLEWAT
                    protectedDays = data.protectedDays + protectedDay, // 🔵 SIMPAN HARI YANG DIPROTEKSI
                    lastLoginMillis = now,
                    isIgnitedToday = false,
                    lifeLineCount = 0,
                    lifeLineRecoveryProgress = 0
                )
            )
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

private const val DAY = 1000L * 60 * 60 * 24

private fun getTodayStartMillis(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
