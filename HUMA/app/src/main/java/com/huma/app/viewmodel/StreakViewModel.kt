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
import com.huma.app.utils.daysBetween
import com.huma.app.utils.addDays

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

    // --- POPUPS & STATUS ---
    var isDeadPopup by mutableStateOf(false)
    var isOutOfLifePopup by mutableStateOf(false)
    var debugDaysOffset by mutableStateOf(0)
    var restoreCounter by mutableStateOf(0) 

    /**
     * CEK STATUS STREAK
     * Dipanggil saat aplikasi dibuka untuk mendeteksi apakah hari sudah berganti.
     */
    fun checkStreakLogic() {
        viewModelScope.launch {
            val data = dao.getStreak().firstOrNull() ?: return@launch

            // Jika user baru, jangan lakukan apa-apa
            if (data.lastLoginMillis == 0L && data.currentStreak == 0) {
                return@launch
            }

            val today = getTodayStartMillis()
            val diffDays = daysBetween(data.lastLoginMillis, today)

            when {
                diffDays <= 0 -> {
                    // Masih di hari yang sama, tidak perlu reset ritual
                    return@launch
                }

                diffDays == 1 -> {
                    // HARI BARU (Besoknya)
                    // Matikan api agar user bisa melakukan ritual lagi
                    if (data.isIgnitedToday) {
                        dao.upsertStreak(data.copy(isIgnitedToday = false))
                    }
                    
                    // Logika Shield Otomatis (Jika rutin login 7 hari)
                    var counter = restoreCounter + 1
                    if (!data.hasShield && counter >= 7) {
                        dao.upsertStreak(data.copy(hasShield = true))
                        restoreCounter = 0
                    } else {
                        restoreCounter = counter
                    }
                }

                diffDays == 2 -> {
                    // BOLONG 1 HARI (Kemarin tidak ritual)
                    handleStreakThreat(data)
                }

                diffDays >= 3 -> {
                    // MATI TOTAL (Lebih dari 2 hari tidak ritual)
                    isDeadPopup = true
                }
            }
        }
    }

    private suspend fun handleStreakThreat(data: StreakEntity) {
        when {
            data.hasShield -> {
                // Perisai pecah tapi streak selamat
                dao.upsertStreak(data.copy(hasShield = false, isIgnitedToday = false))
            }
            data.lifeLineCount > 0 -> {
                // Masuk mode penyelamatan (Awakening Ritual)
                isAwakeningActive = true
            }
            else -> {
                isDeadPopup = true
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
     * RITUAL 2 & 3: NYALAKAN API (The Great Ignition)
     */
    fun igniteTheFlame(word: String) {
        viewModelScope.launch {
            val data = _streakData.value ?: return@launch
            if (data.isIgnitedToday) return@launch

            val todayStart = getTodayStartMillis()
            var newStreak = data.currentStreak

            // Jika hari sebelumnya diproteksi (pakai nyawa), sinkronkan angka streak
            if (data.protectedDays.contains(newStreak + 1)) {
                newStreak += 1
            }

            // Tambah streak hari ini
            newStreak += 1

            val updatedData = data.copy(
                currentStreak = newStreak,
                isIgnitedToday = true,
                lastBurnedWord = word,
                lastLoginMillis = todayStart,
                streakStartMillis = if (data.currentStreak == 0) todayStart else data.streakStartMillis,
                highestStreak = maxOf(data.highestStreak, newStreak),
                hasShield = newStreak >= 25 // Perisai permanen di hari 25
            )

            dao.upsertStreak(updatedData)

            // Reset UI
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

            if (data.lifeLineCount <= 0 && !data.hasShield) {
                isOutOfLifePopup = true
                return@launch
            }

            val protectedDay = data.currentStreak + 1

            dao.upsertStreak(
                data.copy(
                    protectedDays = data.protectedDays + protectedDay,
                    lastLoginMillis = getTodayStartMillis(),
                    isIgnitedToday = false,
                    lifeLineCount = (data.lifeLineCount - 1).coerceAtLeast(0),
                    hasShield = false
                )
            )

            isAwakeningActive = false
            checkStreakLogic()
        }
    }

    fun getEffectiveStreak(): Int = (_streakData.value?.currentStreak ?: 0) + debugDaysOffset

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

    fun resetTotalStreak() {
        viewModelScope.launch {
            dao.upsertStreak(StreakEntity(id = 0))
            currentFriction = 0f
            showInquiry = false
            isAwakeningActive = false
            isDeadPopup = false
            isOutOfLifePopup = false
            restoreCounter = 0
            debugDaysOffset = 0
        }
    }

    // --- DEBUG TIME MACHINE ---
    fun debugAddDays(amount: Int) { debugDaysOffset += amount }
}

private fun getTodayStartMillis(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
