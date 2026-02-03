package com.huma.app.ui.components.task

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huma.app.data.local.TaskEntity
import com.huma.app.data.local.TaskPriority



@Composable
fun TaskSection(
    title: String,
    tasks: List<TaskEntity>,
    onAddClick: () -> Unit,
    onSeeAll: () -> Unit,
    onTaskClick: (Int) -> Unit,
    onToggleDone: (TaskEntity) -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp)) {

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Bold)

            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }

        Spacer(Modifier.height(8.dp))

        tasks.take(5).forEach {
            TaskItem(
                task =  it,
                showDate = true,
                onClick = {
                    onTaskClick(it.id) // atau langsung navigate detail (opsional)
                },
                onToggleDone = onToggleDone
            )
            Spacer(Modifier.height(8.dp))
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

@Composable
fun TaskBubble(task: TaskEntity) {

    val priorityColor = when (task.priority) {
        TaskPriority.HIGH -> Color(0xFFFF6B6B)
        TaskPriority.MEDIUM -> Color(0xFFFFC75F)
        TaskPriority.LOW -> Color(0xFF4D96FF)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // PRIORITY DOT
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(priorityColor, CircleShape)
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.SemiBold)

                task.description?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall)
                }
            }

            Checkbox(
                checked = task.isDone,
                onCheckedChange = {
                    // nanti connect ke ViewModel (update task)
                }
            )
        }
    }
}
