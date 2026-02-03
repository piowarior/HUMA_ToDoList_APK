package com.huma.app.ui.screen.task

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.huma.app.data.local.TaskEntity
import com.huma.app.data.local.TaskMood
import com.huma.app.data.local.TaskPriority
import com.huma.app.ui.viewmodel.TaskViewModel
import com.huma.app.ui.components.task.TaskItem

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel,
    mode: String,
    navController: NavController
) {
    val tasks by viewModel.tasks.collectAsState()

    val calendar = remember { java.util.Calendar.getInstance() }

    // batas hari ini
    val todayStart = remember {
        calendar.apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todayEnd = remember {
        calendar.apply {
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 59)
            set(java.util.Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    // 🔥 FILTER SESUAI MODE
    val filteredTasks = remember(tasks, mode) {
        when (mode) {
            "today" -> tasks.filter {
                it.startDate < todayEnd && !it.isDone
            }

            "upcoming" -> tasks.filter {
                it.startDate > todayEnd
            }

            else -> tasks
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (mode) {
                            "today" -> "Today Tasks"
                            "upcoming" -> "Upcoming Tasks"
                            else -> "All Tasks"
                        }
                    )
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(filteredTasks) { task ->
                TaskItem(
                    task = task,
                    showDate = true, // ✅ tanggal tampil per task
                    onClick = {
                        navController.navigate("task_detail/${task.id}")
                    },
                    onToggleDone = {
                        viewModel.toggleTaskCompletion(it)
                    }
                )
            }
        }
    }
}


