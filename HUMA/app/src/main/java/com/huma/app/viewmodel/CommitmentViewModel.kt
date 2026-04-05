package com.huma.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huma.app.data.local.AppDatabase
import com.huma.app.data.local.CommitmentEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CommitmentViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).commitmentDao()

    val allCommitments = dao.getAllCommitments().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addCommitment(title: String, desc: String) {
        viewModelScope.launch {
            dao.insertCommitment(CommitmentEntity(title = title, description = desc))
        }
    }

    fun toggleCompleteToday(commitment: CommitmentEntity) {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val newList = if (commitment.completedDays.contains(today)) {
                commitment.completedDays.filter { it != today }
            } else {
                commitment.completedDays + today
            }
            dao.updateCommitment(commitment.copy(completedDays = newList))
        }
    }

    fun deleteCommitment(commitment: CommitmentEntity) {
        viewModelScope.launch {
            dao.deleteCommitment(commitment)
        }
    }
}
