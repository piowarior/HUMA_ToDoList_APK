package com.huma.app.ui.components.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huma.app.data.local.TaskEntity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment

@Composable
fun UpcomingPreviewSection(
    groupedTasks: Map<String, List<TaskEntity>>,
    onAddClick: () -> Unit,
    onSeeAll: () -> Unit,
    onTaskClick: (Int) -> Unit,
    onToggleDone: (TaskEntity) -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp)) {

        // HEADER
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Upcoming Tasks", fontWeight = FontWeight.Bold)

            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }

        Spacer(Modifier.height(8.dp))

        var shownCount = 0

        groupedTasks.forEach { (date, tasks) ->
            if (shownCount >= 5) return@forEach

            // 🔵 BUBBLE TANGGAL
            Text(
                text = date,
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .background(
                        Color(0xFFE3F2FD),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E88E5)
            )

            tasks.forEach { task ->
                if (shownCount < 5) {
                    TaskItem(
                        task = task,
                        showDate = true,
                        onClick = {
                            onTaskClick(task.id) // 🔥 CALLBACK
                        },
                        onToggleDone = {
                            onToggleDone(task)
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    shownCount++
                }
            }
        }

        Text(
            "See All →",
            color = Color(0xFF6C63FF),
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onSeeAll() }
        )
    }
}
