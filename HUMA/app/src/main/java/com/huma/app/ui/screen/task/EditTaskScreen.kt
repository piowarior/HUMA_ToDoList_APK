package com.huma.app.ui.screen.task

import android.app.DatePickerDialog
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
fun EditTaskScreen(
    taskId: Int,
    navController: NavController,
    viewModel: TaskViewModel
) {
    val taskFlow = remember(taskId) { viewModel.getTaskById(taskId) }
    val task by taskFlow.collectAsState(initial = null)
    
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val scrollState = rememberScrollState()

    if (task == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF6C63FF))
        }
        return
    }

    /* ================= INITIAL STATE LOGIC ================= */
    
    // Parse existing time if available (e.g., "08:00 - 09:00" or "08:00")
    val existingTime = task!!.dueDate ?: ""
    val timeParts = existingTime.split(" - ")
    
    val initialStartHour = timeParts.getOrNull(0)?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 8
    val initialStartMin = timeParts.getOrNull(0)?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0
    val initialEndHour = timeParts.getOrNull(1)?.split(":")?.getOrNull(0)?.toIntOrNull() ?: (initialStartHour + 1)
    val initialEndMin = timeParts.getOrNull(1)?.split(":")?.getOrNull(1)?.toIntOrNull() ?: initialStartMin

    var title by remember { mutableStateOf(task!!.title) }
    var desc by remember { mutableStateOf(task!!.description ?: "") }
    var priority by remember { mutableStateOf(task!!.priority) }
    var mood by remember { mutableStateOf(task!!.mood) }
    var selectedDate by remember { mutableStateOf(task!!.startDate) }

    // Fix Bug: Hanya centang yang benar-benar ada datanya
    var useStartTime by remember { mutableStateOf(existingTime.isNotEmpty()) }
    var useEndTime by remember { mutableStateOf(timeParts.size > 1) }

    val startPickerState = rememberTimePickerState(initialStartHour, initialStartMin, true)
    val endPickerState = rememberTimePickerState(initialEndHour, initialEndMin, true)

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showConfirmExit by remember { mutableStateOf(false) }

    val currentTimeText = remember(useStartTime, useEndTime, startPickerState.hour, startPickerState.minute, endPickerState.hour, endPickerState.minute) {
        when {
            useStartTime && useEndTime -> "%02d:%02d - %02d:%02d".format(startPickerState.hour, startPickerState.minute, endPickerState.hour, endPickerState.minute)
            useStartTime -> "%02d:%02d".format(startPickerState.hour, startPickerState.minute)
            else -> null
        }
    }

    val hasChanges = title != task!!.title || 
                     desc != (task!!.description ?: "") || 
                     priority != task!!.priority || 
                     mood != task!!.mood || 
                     selectedDate != task!!.startDate || 
                     currentTimeText != task!!.dueDate

    BackHandler {
        if (hasChanges) showConfirmExit = true else navController.popBackStack()
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FF),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Task", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { if (hasChanges) showConfirmExit = true else navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // JUDUL & DESKRIPSI CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // DATE & TIME CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Date Picker
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF6C63FF))
                        Spacer(Modifier.width(12.dp))
                        Text("Target Date", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = {
                            calendar.timeInMillis = selectedDate
                            DatePickerDialog(context, { _, y, m, d ->
                                calendar.set(y, m, d)
                                selectedDate = calendar.timeInMillis
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        }) {
                            Text(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDate)))
                        }
                    }

                    Divider(color = Color(0xFFF0F0F0))

                    // Start Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = Color(0xFF6C63FF))
                        Spacer(Modifier.width(12.dp))
                        Text("Start Time", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Checkbox(checked = useStartTime, onCheckedChange = { useStartTime = it })
                    }
                    if (useStartTime) {
                        OutlinedButton(
                            onClick = { showStartPicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Set Start: %02d:%02d".format(startPickerState.hour, startPickerState.minute))
                        }
                    }

                    // End Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = Color.Gray)
                        Spacer(Modifier.width(12.dp))
                        Text("End Time", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Checkbox(checked = useEndTime, onCheckedChange = { useEndTime = it })
                    }
                    if (useEndTime) {
                        OutlinedButton(
                            onClick = { showEndPicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Set End: %02d:%02d".format(endPickerState.hour, endPickerState.minute))
                        }
                    }
                }
            }

            // PRIORITY & MOOD CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Priority", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TaskPriority.values().forEach {
                            FilterChip(
                                selected = priority == it,
                                onClick = { priority = it },
                                label = { Text(it.name) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Text("How's your mood?", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TaskMood.values().forEach {
                            FilterChip(
                                selected = mood == it,
                                onClick = { mood = it },
                                label = { Text(it.name) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.updateTask(context, task!!.copy(
                        title = title,
                        description = desc,
                        priority = priority,
                        mood = mood,
                        startDate = selectedDate,
                        dueDate = currentTimeText
                    ))
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = hasChanges,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
            ) {
                Text("SAVE CHANGES", fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(30.dp))
        }
    }

    /* ================= TIME PICKER DIALOGS ================= */
    if (showStartPicker) {
        TimePickerDialog(onDismiss = { showStartPicker = false }, state = startPickerState)
    }
    if (showEndPicker) {
        TimePickerDialog(onDismiss = { showEndPicker = false }, state = endPickerState)
    }
    if (showConfirmExit) {
        AlertDialog(
            onDismissRequest = { showConfirmExit = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to go back?") },
            confirmButton = { TextButton(onClick = { navController.popBackStack() }) { Text("Discard") } },
            dismissButton = { TextButton(onClick = { showConfirmExit = false }) { Text("Keep Editing") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(onDismiss: () -> Unit, state: TimePickerState) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("SET") } },
        title = { Text("Select Time") },
        text = { TimePicker(state = state) }
    )
}
