package com.huma.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,

    val description: String? = null,

    // ⏰ tanggal & waktu kerja
    val startDate: Long,        // timestamp (AMAN untuk Room)
    val deadlineDate: Long?,    // boleh null
    val dueDate: String?,          // "08:00"

    // 🔥 struktur lebih aman
    val priority: TaskPriority,
    val mood: TaskMood,
    val lifeArea: String = "PRIBADI",

    val isDone: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)
