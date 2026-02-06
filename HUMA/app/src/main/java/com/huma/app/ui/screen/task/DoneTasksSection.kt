package com.huma.app.ui.screen.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huma.app.data.local.TaskEntity
import com.huma.app.ui.components.task.moodEmoji
import java.text.SimpleDateFormat
import java.util.*
@Composable
fun DoneTasksSection(
    groupedTasks: Map<String, List<TaskEntity>>,
    onRestore: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        var shownCount = 0

        // Urutkan tanggal descending (Terbaru di atas)
        groupedTasks.toSortedMap(compareByDescending { it }).forEach { (date, tasks) ->
            if (shownCount >= 5) return@forEach

            // 🔵 BUBBLE TANGGAL (Sama dengan Upcoming)
            Text(
                text = date,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .background(
                        Color(0xFFE3F2FD),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E88E5),
                style = MaterialTheme.typography.bodySmall
            )

            tasks.forEach { task ->
                if (shownCount < 5) {
                    DoneTaskItem(
                        task = task,
                        onRestore = onRestore,
                        onDelete = onDelete
                    )
                    shownCount++
                }
            }
        }
    }
}

@Composable
fun DoneTaskItem(
    task: TaskEntity,
    onRestore: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit
) {
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dateText = remember(task.completedAt) {
        val timeToDisplay = task.completedAt ?: task.createdAt
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timeToDisplay))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)) // Sedikit abu untuk menandakan "Done"
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // ===================== CONTENT (Identik dengan TaskItem) =====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 80.dp), // Ruang lebih luas untuk 2 icon (Restore & Delete)
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔵 PRIORITY DOT
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(priorityColor(task.priority), CircleShape)
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray // Teks agak pudar karena sudah selesai
                    )

                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6C63FF),
                        fontWeight = FontWeight.Medium
                    )

                    task.description?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${moodEmoji(task.mood)} ${moodLabel(task.mood)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            // ===================== ACTIONS (Restore & Delete) =====================
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showRestoreDialog = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Restore,
                        contentDescription = "Restore",
                        tint = Color(0xFF6C63FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // --- Dialogs (Tetap sama seperti logika sebelumnya) ---
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Task") },
            text = { Text("Pindahkan tugas ini kembali ke daftar aktif?") },
            confirmButton = {
                TextButton(onClick = { onRestore(task); showRestoreDialog = false }) { Text("YA") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) { Text("BATAL") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Permanen") },
            text = { Text("Data ini tidak bisa dikembalikan lagi. Hapus?") },
            confirmButton = {
                TextButton(onClick = { onDelete(task); showDeleteDialog = false }) {
                    Text("HAPUS", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("BATAL") }
            }
        )
    }
}