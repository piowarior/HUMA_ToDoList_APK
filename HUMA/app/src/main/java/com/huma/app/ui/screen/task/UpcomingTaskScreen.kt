package com.huma.app.ui.screen.task

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.huma.app.ui.components.task.TaskItem
import com.huma.app.ui.viewmodel.TaskViewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UpcomingTaskScreen(
    taskViewModel: TaskViewModel,
    navController: NavController) {

    val groupedTasks by taskViewModel.upcomingGrouped
        .collectAsState(initial = emptyMap())

    LazyColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        groupedTasks.forEach { (date, tasks) ->

            // 🔵 BUBBLE TANGGAL (SATU KALI)
            item {
                Text(
                    text = date,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .background(
                            Color(0xFFE3F2FD),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E88E5)
                )
            }

            // 📋 TASK DI BAWAHNYA (TANPA TANGGAL)
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    showDate = true,
                    onClick = {
                        navController.navigate("task_detail/${task.id}")
                    },
                    onToggleDone = {
                        taskViewModel.toggleTaskCompletion(it)
                    }
                )
            }
        }
    }
}
