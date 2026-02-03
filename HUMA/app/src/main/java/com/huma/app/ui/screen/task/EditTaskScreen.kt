package com.huma.app.ui.screen.task

import android.app.DatePickerDialog
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
fun EditTaskScreen(
    taskId: Int,
    navController: NavController,
    viewModel: TaskViewModel
) {
    val task by viewModel.getTaskById(taskId).collectAsState()
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val scrollState = rememberScrollState()

    if (task == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    /* ================= STATE ================= */

    var title by remember { mutableStateOf(task!!.title) }
    var desc by remember { mutableStateOf(task!!.description ?: "") }

    var priority by remember { mutableStateOf(task!!.priority) }
    var mood by remember { mutableStateOf(task!!.mood) }

    var selectedDate by remember { mutableStateOf(task!!.startDate) }

    var useStartTime by remember { mutableStateOf(task!!.dueDate != null) }
    var useEndTime by remember { mutableStateOf(task!!.dueDate != null) }

    val startPickerState = rememberTimePickerState(8, 0, true)
    val endPickerState = rememberTimePickerState(9, 0, true)

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    var showConfirmExit by remember { mutableStateOf(false) }

    val timeText =
        if (useStartTime && useEndTime)
            "%02d:%02d - %02d:%02d".format(
                startPickerState.hour,
                startPickerState.minute,
                endPickerState.hour,
                endPickerState.minute
            )
        else null

    val hasChanges =
        title != task!!.title ||
                desc != (task!!.description ?: "") ||
                priority != task!!.priority ||
                mood != task!!.mood ||
                selectedDate != task!!.startDate ||
                timeText != task!!.dueDate

    /* ================= BACK HANDLER ================= */

    BackHandler {
        if (hasChanges) showConfirmExit = true
        else navController.popBackStack()
    }

    /* ================= UI ================= */

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Task") })
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // TITLE
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // DESCRIPTION
            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            // DATE
            Text("Tanggal")
            Button(onClick = {
                calendar.timeInMillis = selectedDate
                DatePickerDialog(
                    context,
                    { _, y, m, d ->
                        calendar.set(y, m, d)
                        selectedDate = calendar.timeInMillis
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) {
                Text(
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(Date(selectedDate))
                )
            }

            Divider()

            // TIME CHECKBOXES
            Text("Jam")

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Jam Mulai")
                Checkbox(
                    checked = useStartTime,
                    onCheckedChange = { useStartTime = it }
                )
            }

            if (useStartTime) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showStartPicker = true }
                ) {
                    Text("Pilih Jam Mulai: %02d:%02d".format(
                        startPickerState.hour,
                        startPickerState.minute
                    ))
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Jam Selesai")
                Checkbox(
                    checked = useEndTime,
                    onCheckedChange = { useEndTime = it }
                )
            }

            if (useEndTime) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showEndPicker = true }
                ) {
                    Text("Pilih Jam Selesai: %02d:%02d".format(
                        endPickerState.hour,
                        endPickerState.minute
                    ))
                }
            }

            // PRIORITY
            Text("Priority")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskPriority.values().forEach {
                    FilterChip(
                        selected = priority == it,
                        onClick = { priority = it },
                        label = { Text(it.name) }
                    )
                }
            }

            // MOOD
            Text("Mood")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskMood.values().forEach {
                    FilterChip(
                        selected = mood == it,
                        onClick = { mood = it },
                        label = { Text(it.name) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // 🔥 SAVE BUTTON (PASTI KELIHATAN)
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = hasChanges,
                onClick = {
                    viewModel.updateTask(
                        task!!.copy(
                            title = title,
                            description = desc,
                            priority = priority,
                            mood = mood,
                            startDate = selectedDate,
                            dueDate = timeText
                        )
                    )
                    navController.popBackStack()
                }
            ) {
                Text("SAVE PERUBAHAN")
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    /* ================= DIALOG ================= */

    if (showStartPicker) {
        AlertDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text("OK")
                }
            },
            title = { Text("Jam Mulai") },
            text = { TimePicker(state = startPickerState) }
        )
    }

    if (showEndPicker) {
        AlertDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text("OK")
                }
            },
            title = { Text("Jam Selesai") },
            text = { TimePicker(state = endPickerState) }
        )
    }

    if (showConfirmExit) {
        AlertDialog(
            onDismissRequest = { showConfirmExit = false },
            title = { Text("Keluar tanpa menyimpan?") },
            text = { Text("Perubahan belum disimpan.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmExit = false
                    navController.popBackStack()
                }) {
                    Text("Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmExit = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
