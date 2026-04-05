package com.huma.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_capsules")
data class CapsuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val openAt: Long,
    val isOpened: Boolean = false
)
