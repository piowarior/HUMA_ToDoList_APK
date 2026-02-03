package com.huma.app.ui.screen.task

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val task by viewModel.getTaskById(taskId).collectAsState(initial = null)

    if (task == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val dateText = SimpleDateFormat(
        "EEEE, dd MMM yyyy",
        Locale.getDefault()
    ).format(Date(task!!.startDate))

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Task Detail") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /* ================= TITLE CARD ================= */

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        task!!.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        dateText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    task!!.dueDate?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "⏰ $it",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            /* ================= DESCRIPTION ================= */

            task!!.description?.takeIf { it.isNotBlank() }?.let {
                Card(
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Description",
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            /* ================= INFO ROW ================= */

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                InfoChip(
                    title = "Priority",
                    value = task!!.priority.name,
                    color = priorityColor(task!!.priority)
                )

                InfoChip(
                    title = "Mood",
                    value = moodLabel(task!!.mood),
                    color = moodColor(task!!.mood)
                )
            }

            /* ================= DONE ================= */

            Card(
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task!!.isDone,
                        onCheckedChange = {
                            viewModel.toggleTaskCompletion(task!!)
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (task!!.isDone) "Task Completed 🎉"
                        else "Mark as Done",
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            /* ================= ACTIONS ================= */

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate("edit_task/${task!!.id}")
                }
            ) {
                Text("Edit Task")
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                onClick = {
                    viewModel.deleteTask(task!!)
                    navController.popBackStack()
                }
            ) {
                Text("Delete Task")
            }
        }
    }
}

/* ================= COMPONENTS ================= */

@Composable
private fun RowScope.InfoChip(
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .weight(1f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}


/* ================= HELPERS ================= */

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
