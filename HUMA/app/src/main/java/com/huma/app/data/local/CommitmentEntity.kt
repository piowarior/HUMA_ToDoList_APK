package com.huma.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commitments")
data class CommitmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String = "General",
    val iconType: String = "FIRE", // FIRE, WATER, LEAF, STAR, HEART
    val colorHex: String = "#FFA726",
    val createdAt: Long = System.currentTimeMillis(),

    // 🔥 Notifications
    val isNotificationEnabled: Boolean = false,
    val notificationTimes: List<String> = emptyList(), // List of "HH:mm"

    // 🔥 Schedule
    val isCustomSchedule: Boolean = false,
    val startTime: String? = null, // "HH:mm"
    val endTime: String? = null,   // "HH:mm"
    val scheduledDays: List<Int> = emptyList(), // 1 (Sun) to 7 (Sat)

    // 🔥 Streak & Progress
    val completedDays: List<String> = emptyList(), // List of dates in "yyyy-MM-dd" format
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: String? = null, // "yyyy-MM-dd"
    val isBroken: Boolean = false
)
