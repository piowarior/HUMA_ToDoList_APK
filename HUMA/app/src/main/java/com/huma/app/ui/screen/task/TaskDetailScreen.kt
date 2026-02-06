package com.huma.app.ui.screen.task

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.huma.app.data.local.TaskMood
import com.huma.app.data.local.TaskPriority
import com.huma.app.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    navController: NavController,
    taskId: Int,
    viewModel: TaskViewModel
) {
    // Collect state dari Flow
    val task by viewModel.getTaskById(taskId).collectAsState(initial = null)

    // Loading state jika task belum ketemu
    if (task == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF6C63FF))
        }
        return
    }

    // Format tanggal
    val dateText = remember(task) {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date(task!!.startDate))
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FE),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Task Detail",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Agar bisa di-scroll
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            /* ================= TITLE CARD ================= */
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF6C63FF)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = task!!.lifeArea,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = task!!.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        lineHeight = 34.sp
                    )

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    task!!.dueDate?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "⏰ Due at $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            /* ================= INFO ROW ================= */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(
                    title = "Priority",
                    value = task!!.priority.name,
                    color = priorityColor(task!!.priority),
                    icon = "🔥",
                    modifier = Modifier.weight(1f)
                )

                InfoChip(
                    title = "Mood Status",
                    value = moodLabel(task!!.mood),
                    color = moodColor(task!!.mood),
                    icon = "🎭",
                    modifier = Modifier.weight(1f)
                )
            }

            /* ================= DESCRIPTION ================= */
            if (!task!!.description.isNullOrBlank()) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(Modifier.padding(20.dp).fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(4.dp, 16.dp).background(Color(0xFF6C63FF), RoundedCornerShape(2.dp))
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Description",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.DarkGray
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = task!!.description!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            /* ================= DONE CHECKBOX ================= */
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (task!!.isDone) Color(0xFFE8F5E9) else Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (task!!.isDone) Color(0xFF4CAF50).copy(alpha = 0.5f) else Color.Transparent
                ),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task!!.isDone,
                        onCheckedChange = { viewModel.toggleTaskCompletion(task!!) },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4CAF50))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (task!!.isDone) "Task Completed 🎉" else "Mark task as completed",
                        fontWeight = FontWeight.Bold,
                        color = if (task!!.isDone) Color(0xFF2E7D32) else Color.DarkGray
                    )
                }
            }

            /* ================= ACTIONS ================= */
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E)),
                    onClick = { navController.navigate("edit_task/${task!!.id}") }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Edit Task Details", fontWeight = FontWeight.Bold)
                }

                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.deleteTask(task!!)
                        navController.popBackStack()
                    }
                ) {
                    Text(
                        "Delete Task",
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoChip(
    title: String,
    value: String,
    color: Color,
    icon: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    value,
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

fun priorityColor(priority: TaskPriority): Color =
    when (priority) {
        TaskPriority.HIGH -> Color(0xFFE53935)
        TaskPriority.MEDIUM -> Color(0xFFFFA726)
        TaskPriority.LOW -> Color(0xFF43A047)
    }

fun moodColor(mood: TaskMood): Color =
    when (mood) {
        TaskMood.CALM -> Color(0xFF42A5F5)
        TaskMood.NORMAL -> Color(0xFF7E57C2)
        TaskMood.STRESS -> Color(0xFFEF5350)
    }

fun moodLabel(mood: TaskMood): String =
    when (mood) {
        TaskMood.CALM -> "Calm 😌"
        TaskMood.NORMAL -> "Normal 🙂"
        TaskMood.STRESS -> "Stress 😵"
    }