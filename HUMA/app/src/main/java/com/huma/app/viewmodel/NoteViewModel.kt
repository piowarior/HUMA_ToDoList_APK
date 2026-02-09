package com.huma.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huma.app.data.local.NoteDao
import com.huma.app.data.local.NoteEntity
import com.huma.app.ui.screen.note.NoteData
import kotlinx.coroutines.launch // 🔥 WAJIB ADA
import kotlinx.coroutines.flow.Flow

class NoteViewModel(private val noteDao: NoteDao) : ViewModel() {

    // Flow untuk mengambil semua catatan secara real-time
    val allNotes = noteDao.getAllNotes()

    fun saveNote(noteData: NoteData) {
        viewModelScope.launch { // 🔥 Melakukan operasi database di background thread
            val entity = NoteEntity(
                id = noteData.id,
                title = noteData.title,
                blocks = noteData.blocks,
                date = noteData.date
            )
            noteDao.insertNote(entity)
        }
    }

    fun deleteNote(noteData: NoteData) {
        viewModelScope.launch {
            val entity = NoteEntity(
                id = noteData.id,
                title = noteData.title,
                blocks = noteData.blocks,
                date = noteData.date
            )
            noteDao.deleteNote(entity)
        }
    }
}