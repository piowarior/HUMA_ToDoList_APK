package com.huma.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huma.app.data.local.AppDatabase
import com.huma.app.data.local.CapsuleEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CapsuleViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).capsuleDao()

    val allCapsules = dao.getAllCapsules().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun saveCapsule(content: String, days: Int) {
        viewModelScope.launch {
            val openAt = System.currentTimeMillis() + (days.toLong() * 24 * 60 * 60 * 1000)
            dao.insertCapsule(CapsuleEntity(content = content, openAt = openAt))
        }
    }

    fun deleteCapsule(id: Int) {
        viewModelScope.launch {
            dao.deleteCapsule(id)
        }
    }

    fun markAsOpened(id: Int) {
        viewModelScope.launch {
            dao.markAsOpened(id)
        }
    }
}
