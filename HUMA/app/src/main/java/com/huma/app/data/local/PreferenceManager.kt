package com.huma.app.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("huma_settings", Context.MODE_PRIVATE)

    companion object {
        const val THEME_MODE = "theme_mode" // 0: System, 1: Light, 2: Dark
        const val NOTIF_ENABLED = "notif_enabled"
        const val NOTIF_SOUND = "notif_sound"
        const val NOTIF_VIBRATE = "notif_vibrate"
        const val NOTIF_STREAK = "notif_streak"
        const val NOTIF_STREAK_MISS_1 = "notif_streak_miss_1"
        const val NOTIF_STREAK_MISS_5 = "notif_streak_miss_5"
        const val NOTIF_STREAK_MISS_7 = "notif_streak_miss_7"
        const val NOTIF_GREETING = "notif_greeting"
        const val LANGUAGE = "language" // "in" or "en"
    }

    var themeMode: Int
        get() = prefs.getInt(THEME_MODE, 0)
        set(value) = prefs.edit().putInt(THEME_MODE, value).apply()

    var isNotifEnabled: Boolean
        get() = prefs.getBoolean(NOTIF_ENABLED, true)
        set(value) = prefs.edit().putBoolean(NOTIF_ENABLED, value).apply()

    var isNotifSoundEnabled: Boolean
        get() = prefs.getBoolean(NOTIF_SOUND, true)
        set(value) = prefs.edit().putBoolean(NOTIF_SOUND, value).apply()

    var isNotifVibrateEnabled: Boolean
        get() = prefs.getBoolean(NOTIF_VIBRATE, true)
        set(value) = prefs.edit().putBoolean(NOTIF_VIBRATE, value).apply()

    var isStreakNotifEnabled: Boolean
        get() = prefs.getBoolean(NOTIF_STREAK, true)
        set(value) = prefs.edit().putBoolean(NOTIF_STREAK, value).apply()

    var isStreakMiss1Enabled: Boolean
        get() = prefs.getBoolean(NOTIF_STREAK_MISS_1, true)
        set(value) = prefs.edit().putBoolean(NOTIF_STREAK_MISS_1, value).apply()

    var isStreakMiss5Enabled: Boolean
        get() = prefs.getBoolean(NOTIF_STREAK_MISS_5, true)
        set(value) = prefs.edit().putBoolean(NOTIF_STREAK_MISS_5, value).apply()

    var isStreakMiss7Enabled: Boolean
        get() = prefs.getBoolean(NOTIF_STREAK_MISS_7, true)
        set(value) = prefs.edit().putBoolean(NOTIF_STREAK_MISS_7, value).apply()

    val isStreakMissNotifEnabled: Boolean
        get() = isStreakMiss1Enabled || isStreakMiss5Enabled || isStreakMiss7Enabled

    var isGreetingNotifEnabled: Boolean
        get() = prefs.getBoolean(NOTIF_GREETING, true)
        set(value) = prefs.edit().putBoolean(NOTIF_GREETING, value).apply()

    var language: String
        get() = prefs.getString(LANGUAGE, "in") ?: "in"
        set(value) = prefs.edit().putString(LANGUAGE, value).apply()
}
