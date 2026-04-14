package com.huma.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huma.app.data.local.AppDatabase
import com.huma.app.data.local.CommitmentEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CommitmentViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).commitmentDao()

    val allCommitments = try {
        dao.getAllCommitments()
    } catch (e: Exception) {
        flowOf(emptyList())
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addCommitment(commitment: CommitmentEntity) {
        viewModelScope.launch {
            dao.insertCommitment(commitment)
        }
    }

    fun updateCommitment(commitment: CommitmentEntity) {
        viewModelScope.launch {
            dao.updateCommitment(commitment)
        }
    }

    fun deleteCommitment(commitment: CommitmentEntity) {
        viewModelScope.launch {
            dao.deleteCommitment(commitment)
        }
    }
}
