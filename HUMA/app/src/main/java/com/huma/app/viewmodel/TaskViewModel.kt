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

    // 2. Sekarang baru bisa buat radarData dan kawan-kawan menggunakan 'tasks'
// 2. Turunan data (Radar, Heatmap, Balance)
    val radarData: StateFlow<List<Float>> = tasks.map { list ->
        val areas = listOf("AKADEMIK", "KESEHATAN", "SPIRITUAL", "RUMAH_TANGGA", "SOSIAL")
        areas.map { areaName ->
            val areaTasks = list.filter { it.lifeArea == areaName }
            if (areaTasks.isEmpty()) 0.1f
            else (areaTasks.count { it.isDone }.toFloat() / areaTasks.size).coerceAtLeast(0.1f)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(0.1f, 0.1f, 0.1f, 0.1f, 0.1f))

    val hourlyCompletionStats: StateFlow<List<Float>> = tasks.map { list ->
        val hourCounts = IntArray(24) { 0 }
        list.filter { it.isDone }.forEach { task ->
            val cal = Calendar.getInstance().apply { timeInMillis = task.createdAt }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hourCounts[hour]++
        }
        val max = hourCounts.maxOrNull()?.coerceAtLeast(1) ?: 1
        hourCounts.map { it.toFloat() / max }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(24) { 0f })

    // HANYA SATUoverallLifeBalance DI SINI
    val overallLifeBalance: StateFlow<Float> = tasks.map { list ->
        if (list.isEmpty()) 0f
        else list.count { it.isDone }.toFloat() / list.size.toFloat()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

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
                .sortedByDescending { it.completedAt ?: 0L }
                .groupBy { task ->
                    SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                    ).format(Date(task.completedAt ?: task.createdAt))
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

    // ================= TOGGLE =================
    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val nextState = !task.isDone
            repository.update(
                task.copy(
                    isDone = nextState,
                    // 🔥 Kalau true isi waktu sekarang, kalau false (restore) balikin null
                    completedAt = if (nextState) System.currentTimeMillis() else null
                )
            )
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


}

