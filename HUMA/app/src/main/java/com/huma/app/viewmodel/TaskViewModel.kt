package com.huma.app.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huma.app.data.local.LifeArea
import com.huma.app.data.local.TaskEntity
import com.huma.app.data.repository.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    // ================= ALL TASK =================
    val tasks: StateFlow<List<TaskEntity>> =
        repository.getAllTasks()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    // ================= TASK TODAY =================
    val todayTasks: StateFlow<List<TaskEntity>> =
        tasks.map { list ->
            val startOfToday = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val endOfToday = LocalDate.now()
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            list.filter { task ->
                task.startDate < endOfToday && !task.isDone
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    // ================= UPCOMING TASK =================
    val upcomingTasks: StateFlow<List<TaskEntity>> =
        tasks.map { list ->
            val endOfToday = LocalDate.now()
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            list.filter { task ->
                task.startDate >= endOfToday && !task.isDone
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    // ================= UPCOMING GROUPED (BUBBLE DATE) =================
    val upcomingGrouped: StateFlow<Map<String, List<TaskEntity>>> =
        upcomingTasks.map { tasks ->
            tasks.groupBy { task ->
                SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                ).format(Date(task.startDate))
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyMap()
        )

    // ================= DONE TASK =================
    val doneTasks: StateFlow<Map<String, List<TaskEntity>>> =
        tasks.map { list ->
            list.filter { it.isDone }
                .groupBy { task ->
                    SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                    ).format(Date(task.createdAt)) // atau task.doneDate kalau ada timestamp selesai
                }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyMap()
        )


    // ================= GET BY ID =================
    fun getTaskById(taskId: Int): StateFlow<TaskEntity?> =
        tasks
            .map { list -> list.find { it.id == taskId } }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                null
            )


    // ================= CRUD =================
    fun addTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.insert(task)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.update(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.update(task.copy(isDone = !task.isDone))
        }
    }

    // 🔥 FUNGSI BARU: Hitung Statistik per Area
    fun getAreaStats(area: LifeArea): Flow<Pair<Int, Float>> = tasks.map { list ->
        val areaTasks = list.filter { it.lifeArea == area.name }
        val total = areaTasks.size
        val done = areaTasks.count { it.isDone }

        val progress = if (total == 0) 0f else done.toFloat() / total.toFloat()
        Pair(total, progress)
    }

    // 🔥 FUNGSI BARU: Hitung Total Progress Hidup (Untuk Grafik Lingkaran 70% tadi)
    val overallLifeBalance: StateFlow<Float> = tasks.map { list ->
        if (list.isEmpty()) 0f
        else list.count { it.isDone }.toFloat() / list.size.toFloat()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)
}

