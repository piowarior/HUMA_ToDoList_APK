package com.huma.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.huma.app.ui.screen.note.NoteBlock

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val blocks: List<NoteBlock>, // Converter akan mengubah ini jadi String secara otomatis
    val date: String,
    val createdAt: Long = System.currentTimeMillis()
)