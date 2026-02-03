package com.huma.app.ui.components.task

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huma.app.data.local.TaskEntity
import com.huma.app.data.local.TaskMood
import com.huma.app.data.local.TaskPriority
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskItem(
    task: TaskEntity,
    showDate: Boolean,
    onClick: (() -> Unit)? = null,
    onToggleDone: ((TaskEntity) -> Unit)? = null
) {
    val dateText = remember(task.startDate) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(task.startDate))
    }

    var showConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {

            // ===================== CONTENT =====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 48.dp), // ⬅️ RUANG KHUSUS CHECKBOX
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 🔵 PRIORITY DOT
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(priorityColor(task.priority), CircleShape)
                )

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = onClick != null) {
                            onClick?.invoke()
                        }
                ) {

                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold
                    )

                    if (showDate) {
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6C63FF),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    task.description?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray,
                            maxLines = 2
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        task.dueDate?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Spacer(Modifier.width(10.dp))
                        }

                        Text(
                            text = "${moodEmoji(task.mood)} ${moodLabel(task.mood)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            // ===================== CHECKBOX (PASTI MUNCUL) =====================
            if (onToggleDone != null) {
                Checkbox(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    checked = task.isDone,
                    onCheckedChange = {
                        if (!task.isDone) {
                            showConfirm = true
                        } else {
                            onToggleDone?.invoke(task)
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.White
                    )
                )
            }
        }
    }

    // ===================== DIALOG =====================
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Selesaikan Task?") },
            text = { Text("Yakin task ini sudah benar-benar selesai?") },
            confirmButton = {
                TextButton(onClick = {
                    onToggleDone?.invoke(task)
                    showConfirm = false
                }) {
                    Text("YA, SELESAI")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("BATAL")
                }
            }
        )
    }
}



/* ================= HELPERS ================= */

fun priorityColor(priority: TaskPriority): Color =
    when (priority) {
        TaskPriority.HIGH -> Color(0xFFE53935)
        TaskPriority.MEDIUM -> Color(0xFFFFA726)
        TaskPriority.LOW -> Color(0xFF43A047)
    }

fun moodEmoji(mood: TaskMood): String =
    when (mood) {
        TaskMood.CALM -> "😌"
        TaskMood.NORMAL -> "🙂"
        TaskMood.STRESS -> "😵"
    }

fun moodLabel(mood: TaskMood): String =
    when (mood) {
        TaskMood.CALM -> "Calm"
        TaskMood.NORMAL -> "Normal"
        TaskMood.STRESS -> "Stress"
    }
