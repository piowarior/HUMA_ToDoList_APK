package com.huma.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commitments")
data class CommitmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val completedDays: List<String> = emptyList() // List of dates in "yyyy-MM-dd" format
)
