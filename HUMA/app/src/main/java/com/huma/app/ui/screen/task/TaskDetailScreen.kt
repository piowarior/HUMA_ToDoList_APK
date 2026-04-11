package com.huma.app.ui.screen.task

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.huma.app.data.local.TaskMood
import com.huma.app.data.local.TaskPriority
import com.huma.app.ui.components.task.moodEmoji
import com.huma.app.ui.components.task.moodLabel
import com.huma.app.ui.components.task.priorityColor
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
    val context = LocalContext.current
    val task by viewModel.getTaskById(taskId).collectAsState(initial = null)

    if (task == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF6C63FF))
        }
        return
    }

    val dateFull = remember(task) {
        SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date(task!!.startDate))
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FF),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Task Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("edit_task/${task!!.id}") }) {
                        Icon(Icons.Default.Edit, null, tint = Color(0xFF6C63FF))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TITLE & DESC CARD (Matches Edit Screen Style)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        color = Color(0xFF6C63FF).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = task!!.lifeArea.uppercase(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6C63FF),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        text = task!!.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1C1E)
                    )
                    
                    if (!task!!.description.isNullOrBlank()) {
                        Divider(color = Color(0xFFF0F0F0))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Notes, null, tint = Color.Gray, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = task!!.description!!,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF454749),
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
            }

            // DATE & TIME CARD (Matches Edit Screen Style)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF6C63FF))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Target Date", fontSize = 12.sp, color = Color.Gray)
                            Text(dateFull, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (task!!.dueDate != null) {
                        Divider(color = Color(0xFFF0F0F0))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, tint = Color(0xFF6C63FF))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Scheduled Time", fontSize = 12.sp, color = Color.Gray)
                                Text(task!!.dueDate!!, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // PRIORITY & MOOD CARD (Matches Edit Screen Style)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Flag, null, tint = priorityColor(task!!.priority), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Priority", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text(task!!.priority.name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 24.dp))
                    }
                    
                    Divider(modifier = Modifier.height(40.dp).width(1.dp), color = Color(0xFFF0F0F0))
                    
                    Column(Modifier.weight(1f).padding(start = 20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Mood, null, tint = Color(0xFF6C63FF), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Mood", fontSize = 12.sp, color = Color.Gray)
                        }
                        Text("${moodEmoji(task!!.mood)} ${moodLabel(task!!.mood)}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 24.dp))
                    }
                }
            }

            // COMPLETION STATUS (Interactive)
            val statusColor = if (task!!.isDone) Color(0xFF4CAF50) else Color(0xFF6C63FF)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.clickable { viewModel.toggleTaskCompletion(context, task!!) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task!!.isDone,
                        onCheckedChange = { viewModel.toggleTaskCompletion(context, task!!) },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4CAF50))
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (task!!.isDone) "Task Completed! ✨" else "Ongoing Progress",
                            fontWeight = FontWeight.Bold,
                            color = if (task!!.isDone) Color(0xFF2E7D32) else Color(0xFF1A1C1E)
                        )
                        Text(
                            text = if (task!!.isDone) "Tap to reactive task" else "Tap to mark as finished",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            // DELETE ACTION
            TextButton(
                onClick = {
                    viewModel.deleteTask(context, task!!)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935))
            ) {
                Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Delete task permanent", fontWeight = FontWeight.Medium)
            }
            
            Spacer(Modifier.height(30.dp))
        }
    }
}
