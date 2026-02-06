package com.huma.app.ui.screen.lifearea

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.huma.app.data.local.LifeArea
import com.huma.app.ui.viewmodel.TaskViewModel
import com.huma.app.ui.components.task.TaskItem // 🔥 Import TaskItem kamu
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaDetailScreen(
    areaName: String,
    navController: NavController,
    taskViewModel: TaskViewModel
) {
    val allTasks by taskViewModel.tasks.collectAsState()

    val area = LifeArea.values().find { it.name == areaName } ?: LifeArea.PRIBADI

    // 🔍 FILTER: Pisahkan yang aktif dan yang sudah selesai
    val areaTasks = allTasks.filter { it.lifeArea == areaName }
    val activeTasks = areaTasks.filter { !it.isDone }
    val doneTasks = areaTasks.filter { it.isDone }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(area.label, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF6F7FB))
            )
        },
        containerColor = Color(0xFFF6F7FB)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ================== SECTION 1: ACTIVE TASKS ==================
            if (activeTasks.isEmpty() && doneTasks.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Belum ada tugas di area ini", color = Color.Gray)
                    }
                }
            } else if (activeTasks.isNotEmpty()) {
                val grouped = activeTasks.groupBy { task ->
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(task.startDate))
                }

                grouped.forEach { (date, tasks) ->
                    item {
                        Text(
                            text = date,
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .background(Color(0xFFE3F2FD), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E88E5),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    items(tasks) { task ->
                        Box(Modifier.padding(horizontal = 16.dp)) {
                            TaskItem(
                                task = task,
                                showDate = false,
                                onClick = { navController.navigate("task_detail/${task.id}") },
                                onToggleDone = { taskViewModel.toggleTaskCompletion(it) }
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            // ================== SECTION 2: DONE TASKS ==================
            if (doneTasks.isNotEmpty()) {
                item {
                    Divider(Modifier.padding(vertical = 20.dp, horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
                    Text(
                        text = "Completed (${doneTasks.size})",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.Gray
                    )
                }

                // Grouping Done Tasks berdasarkan completedAt (seperti diskusi sebelumnya)
                val groupedDone = doneTasks.groupBy { task ->
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(task.completedAt ?: task.createdAt))
                }

                groupedDone.forEach { (date, tasks) ->
                    // Kita gunakan DoneTaskItem yang tampilannya pake Icon Restore & Delete
                    items(tasks) { task ->
                        Box(Modifier.padding(horizontal = 16.dp)) {
                            // 🔥 Pakai komponen DoneTaskItem agar tampilannya beda (pudar & tanpa checkbox)
                            com.huma.app.ui.screen.task.DoneTaskItem(
                                task = task,
                                onRestore = { taskViewModel.toggleTaskCompletion(it) },
                                onDelete = { taskViewModel.deleteTask(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}