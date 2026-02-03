package com.huma.app.ui.screen.focus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.huma.app.data.local.TaskEntity
import com.huma.app.data.local.TaskMood
import com.huma.app.data.local.TaskPriority
import com.huma.app.ui.notification.ACTION_PAUSE
import com.huma.app.ui.notification.ACTION_RESUME
import com.huma.app.ui.notification.ACTION_STOP
import com.huma.app.ui.notification.FocusService
import com.huma.app.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/* =========================
   FOCUS METHOD
========================= */
enum class FocusMethod(val label: String, val defaultMinutes: Int?, val editable: Boolean) {
    NONE("None", null, true),
    POMODORO("Pomodoro", 25, false),
    DEEP_WORK("Deep Work", 50, false),
    FLOW("Flow Mode", 90, false)
}

enum class FocusPhase {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK,
    FLOW
}

data class FocusSessionState(
    val method: FocusMethod,
    val phase: FocusPhase,
    val cycle: Int,
    val remainingSeconds: Int
)

/* =========================
   MAIN SCREEN
========================= */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    navController: NavController,
    taskViewModel: TaskViewModel
) {
    val context = LocalContext.current

    val todayTasks by taskViewModel.todayTasks.collectAsState()
    val upcomingTasks by taskViewModel.upcomingTasks.collectAsState()

    var selectedTask by remember { mutableStateOf<TaskEntity?>(null) }
    var showSetup by remember { mutableStateOf(false) }
    var focusMinutes by remember { mutableStateOf(25) }
    var isFocusing by remember { mutableStateOf(false) }

    var showQuickAdd by remember { mutableStateOf(false) }

    var sessionState by remember { mutableStateOf<FocusSessionState?>(null) }

    if (sessionState != null && selectedTask != null) {
        FocusTimerScreen(
            task = selectedTask!!,
            state = sessionState!!,
            onStateChange = { sessionState = it },
            onCancel = {
                context.stopService(Intent(context, FocusService::class.java))
                sessionState = null
                selectedTask = null
                // Karena tadi tidak di-insert ke DB, maka otomatis "hilang" saat di-stop
            },
            onFinishTask = { done ->
                if (done) {
                    // Jika task baru (id == 0), insert dulu baru set selesai
                    if (selectedTask!!.id == 0) {
                        taskViewModel.addTask(selectedTask!!.copy(isDone = true))
                    } else {
                        // Jika task lama, tinggal toggle completion
                        taskViewModel.toggleTaskCompletion(selectedTask!!)
                    }
                }
                context.stopService(Intent(context, FocusService::class.java))
                sessionState = null
                selectedTask = null
            }
        )
    } else {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Focus Mode", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, null)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showQuickAdd = true }) {
                    Icon(Icons.Default.Add, null)
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                item {
                    Text(
                        "Pilih satu task. Fokus tanpa distraksi.",
                        color = Color.Gray
                    )
                }

                if (todayTasks.isNotEmpty()) {
                    item { FocusHeader("Today") }
                    items(todayTasks) { task ->
                        FocusTaskItem(task, showDate = true) {
                            selectedTask = task
                            showSetup = true
                        }
                    }
                }

                if (upcomingTasks.isNotEmpty()) {
                    item { FocusHeader("Upcoming") }
                    items(upcomingTasks) { task ->
                        FocusTaskItem(task, showDate = true) {
                            selectedTask = task
                            showSetup = true
                        }
                    }
                }
            }
        }
    }

    /* =========================
       SETUP DIALOG
    ========================= */
    if (showSetup && selectedTask != null) {
        FocusSetupDialog(
            taskTitle = selectedTask!!.title,
            onDismiss = { showSetup = false },
            onStart = { state ->

                val intent = Intent(context, FocusService::class.java).apply {
                    putExtra("task", selectedTask!!.title)
                    putExtra("method", state.method.label)
                    putExtra("phase", state.phase.name)
                    putExtra("time", "${state.remainingSeconds / 60}:00")
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }

                sessionState = state
                showSetup = false
            }
        )
    }

    /* =========================
       QUICK ADD
    ========================= */
    /* =========================
       QUICK ADD
    ========================= */
    if (showQuickAdd) {
        QuickAddFocusDialog(
            onDismiss = { showQuickAdd = false },
            onConfirm = { task, minutes ->
                // JANGAN panggil taskViewModel.addTask(task) di sini
                // Kita biarkan task ini "melayang" di memory saja dulu

                selectedTask = task

                sessionState = FocusSessionState(
                    method = FocusMethod.NONE,
                    phase = FocusPhase.FOCUS,
                    cycle = 1,
                    remainingSeconds = minutes * 60
                )

                val intent = Intent(context, FocusService::class.java).apply {
                    putExtra("task", task.title)
                    putExtra("method", "Quick Focus")
                    putExtra("phase", FocusPhase.FOCUS.name)
                    putExtra("time", "$minutes:00")
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }

                showQuickAdd = false
            }
        )
    }
}

/* =========================
   TASK ITEM
========================= */
@Composable
fun FocusTaskItem(
    task: TaskEntity,
    showDate: Boolean = true,
    onClick: () -> Unit
) {
    // Format tanggal untuk dateText di bawah judul (berdasarkan startDate)
    val dateText = remember(task.startDate) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(task.startDate))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White) // Ganti ke White agar sama
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
                    .padding(end = 48.dp), // Ruang untuk Icon Play di kanan
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 🔵 PRIORITY DOT
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(priorityColor(task.priority ?: TaskPriority.MEDIUM), CircleShape)
                )

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onClick() }
                ) {

                    // JUDUL
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    // TANGGAL CREATE (Warna Ungu khas desainmu)
                    if (showDate) {
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6C63FF),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // DESKRIPSI
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

                    // BARIS PALING BAWAH: DUE DATE & MOOD
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        task.dueDate?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Spacer(Modifier.width(10.dp))
                        }

                        // MOOD (Emoji + Label)
                        Text(
                            text = "${moodEmoji(task.mood ?: TaskMood.NORMAL)} ${moodLabel(task.mood ?: TaskMood.NORMAL)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            // ===================== PLAY BUTTON (Ganti Checkbox) =====================
            // Karena ini Fokus Mode, kita pakai Icon Play untuk mulai timer
            IconButton(
                onClick = { onClick() },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Focus",
                    tint = Color(0xFF6C63FF),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}


@Composable
fun FocusHeader(title: String) {
    Text(
        title,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E88E5),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

/* =========================
   SETUP FOCUS
========================= */
@Composable
fun FocusSetupDialog(
    taskTitle: String,
    onDismiss: () -> Unit,
    onStart: (FocusSessionState) -> Unit
) {
    var method by remember { mutableStateOf(FocusMethod.POMODORO) }
    var minutes by remember { mutableStateOf("") }

    LaunchedEffect(method) {
        minutes = method.defaultMinutes?.toString() ?: ""
    }

    // 🔥 LOGIKA VALIDASI: Tombol aktif jika menit tidak kosong DAN lebih dari 0
    val isValid = minutes.isNotEmpty() && (minutes.toIntOrNull() ?: 0) > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Setup Fokus", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Task: $taskTitle", fontWeight = FontWeight.SemiBold)

                FocusMethod.values().forEach { m ->
                    Row(
                        Modifier.fillMaxWidth().clickable { method = m },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = method == m, onClick = null)
                        Text(m.label)
                    }
                }

                OutlinedTextField(
                    value = minutes,
                    onValueChange = { if (it.all(Char::isDigit)) minutes = it },
                    enabled = method.editable,
                    label = { Text("Durasi (menit)") },
                    isError = !isValid && method == FocusMethod.NONE, // Kasih warna merah kalau error
                    supportingText = {
                        if (!isValid && method == FocusMethod.NONE) {
                            Text("Durasi harus diisi (min. 1 menit)", color = Color.Red)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val min = minutes.toIntOrNull() ?: 25
                    onStart(FocusSessionState(
                        method = method,
                        phase = FocusPhase.FOCUS,
                        cycle = 1,
                        remainingSeconds = min * 60
                    ))
                },
                enabled = isValid // 🔥 TOMBOL MATI KALAU GAK VALID
            ) {
                Text("Mulai Fokus")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}


/* =========================
   TIMER MODE
========================= */
@Composable
fun FocusTimerScreen(
    task: TaskEntity,
    state: FocusSessionState,
    onStateChange: (FocusSessionState) -> Unit,
    onCancel: () -> Unit,
    onFinishTask: (Boolean) -> Unit
) {
    var seconds by remember { mutableStateOf(state.remainingSeconds) }
    var paused by remember { mutableStateOf(false) }
    var showPopup by remember { mutableStateOf(true) }
    var showExit by remember { mutableStateOf(false) }
    var showFinish by remember { mutableStateOf(false) }
    val context = LocalContext.current

    BackHandler {
        showExit = true
    }

    // Perbaikan Flag RECEIVER_NOT_EXPORTED ada di sini
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.getStringExtra("action")
                when (action) {
                    ACTION_PAUSE -> paused = true
                    ACTION_RESUME -> paused = false
                    ACTION_STOP -> onCancel()
                }
            }
        }
        val filter = IntentFilter("FOCUS_UPDATE")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    LaunchedEffect(state.phase, paused, seconds) {
        if (!paused && seconds > 0) {
            delay(1000)
            seconds--

            val updateIntent = Intent(context, FocusService::class.java).apply {
                putExtra("task", task.title)
                putExtra("method", state.method.label)
                putExtra("phase", phaseTitle(state.phase))
                putExtra("time", String.format("%02d:%02d", seconds / 60, seconds % 60))
                putExtra("is_paused", false)
            }
            context.startService(updateIntent)
        }

        if (seconds == 0) {
            if (state.method == FocusMethod.POMODORO) {
                onStateChange(nextPhase(state))
            } else {
                showFinish = true
            }
        }
    }


    Surface(
        color = phaseColor(state.phase),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                phaseTitle(state.phase),
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(task.title, color = Color.White, fontSize = 22.sp)

            Spacer(Modifier.height(32.dp))

            Text(
                String.format("%02d:%02d", seconds / 60, seconds % 60),
                fontSize = 80.sp,
                color = Color.White
            )

            Spacer(Modifier.height(32.dp))

            Row {
                Button(onClick = {
                    paused = !paused
                    val intent = Intent(context, FocusService::class.java).apply {
                        action = if (paused) ACTION_PAUSE else ACTION_RESUME
                        putExtra("time", String.format("%02d:%02d", seconds / 60, seconds % 60))
                        putExtra("task", task.title)
                    }
                    context.startService(intent)
                }) {
                    Text(if (paused) "Resume" else "Pause")
                }
                Spacer(Modifier.width(12.dp))
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Stop")
                }
            }
        }
    }

    if (showPopup) {
        AlertDialog(
            onDismissRequest = { showPopup = false },
            title = { Text("💡 Fokus Mode") },
            text = { Text(focusMessage(state.method, state.phase)) },
            confirmButton = {
                Button(onClick = { showPopup = false }) {
                    Text("Gas!")
                }
            }
        )
    }

    if (showExit) {
        AlertDialog(
            onDismissRequest = { showExit = false },
            title = { Text("Keluar Fokus?") },
            confirmButton = {
                TextButton(onClick = onCancel) { Text("Keluar") }
            },
            dismissButton = {
                TextButton(onClick = { showExit = false }) { Text("Lanjut") }
            }
        )
    }

    if (showFinish) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("🎉 Fokus Selesai") },
            text = { Text("Tandai task sebagai selesai?") },
            confirmButton = {
                Button(onClick = { onFinishTask(true) }) { Text("Ya") }
            },
            dismissButton = {
                TextButton(onClick = { onFinishTask(false) }) { Text("Belum") }
            }
        )
    }
}

fun nextPhase(state: FocusSessionState): FocusSessionState {
    return when (state.method) {
        FocusMethod.POMODORO -> when (state.phase) {
            FocusPhase.FOCUS ->
                if (state.cycle == 4)
                    state.copy(phase = FocusPhase.LONG_BREAK, remainingSeconds = 15 * 60, cycle = 0)
                else
                    state.copy(phase = FocusPhase.SHORT_BREAK, remainingSeconds = 5 * 60)
            FocusPhase.SHORT_BREAK ->
                state.copy(phase = FocusPhase.FOCUS, remainingSeconds = 25 * 60, cycle = state.cycle + 1)
            FocusPhase.LONG_BREAK ->
                state.copy(phase = FocusPhase.FOCUS, remainingSeconds = 25 * 60, cycle = 1)
            else -> state
        }
        else -> state
    }
}

fun focusMessage(method: FocusMethod, phase: FocusPhase): String =
    when (method) {
        FocusMethod.POMODORO -> when (phase) {
            FocusPhase.FOCUS -> "🍅 Fokus 25 menit. Jangan buka HP."
            FocusPhase.SHORT_BREAK -> "☕ Break 5 menit. Tarik napas."
            FocusPhase.LONG_BREAK -> "🎉 Mantap! Long break dulu."
            else -> ""
        }
        FocusMethod.DEEP_WORK -> "🔥 Deep Work. Jangan multitasking."
        FocusMethod.FLOW -> "🌊 Masuk Flow. Nikmati prosesnya."
        FocusMethod.NONE -> "🎯 Fokus sekarang. Kerjakan satu hal sampai selesai."
    }

fun phaseTitle(phase: FocusPhase): String =
    when (phase) {
        FocusPhase.FOCUS -> "FOCUS"
        FocusPhase.SHORT_BREAK -> "BREAK"
        FocusPhase.LONG_BREAK -> "LONG BREAK"
        FocusPhase.FLOW -> "FLOW MODE"
    }

fun phaseColor(phase: FocusPhase): Color =
    when (phase) {
        FocusPhase.FOCUS -> Color(0xFF0F172A)
        FocusPhase.SHORT_BREAK -> Color(0xFF065F46)
        FocusPhase.LONG_BREAK -> Color(0xFF7C2D12)
        FocusPhase.FLOW -> Color(0xFF1E1B4B)
    }

@Composable
fun PriorityBadge(priority: TaskPriority) {
    Text(
        text = priority.name,
        color = Color.White,
        fontSize = 10.sp,
        modifier = Modifier
            .background(priorityColor(priority), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddFocusDialog(
    onDismiss: () -> Unit,
    onConfirm: (TaskEntity, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    // State untuk Method Fokus
    var method by remember { mutableStateOf(FocusMethod.POMODORO) }
    var minutes by remember { mutableStateOf("25") }

    // Update menit otomatis saat method berubah
    LaunchedEffect(method) {
        // Jika method NONE, kita kosongkan atau biarkan user isi
        // Jika selain NONE, ambil defaultMinutes-nya
        minutes = method.defaultMinutes?.toString() ?: ""
    }

    // 🔥 LOGIKA VALIDASI
    val isTitleValid = title.isNotBlank()
    val isMinutesValid = minutes.isNotEmpty() && (minutes.toIntOrNull() ?: 0) > 0
    val canConfirm = isTitleValid && isMinutesValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Fokus Cepat", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Input Judul
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Task Baru") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !isTitleValid && title.isNotEmpty()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Deskripsi (Opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Text("Pilih Metode Pengerjaan:", style = MaterialTheme.typography.labelMedium)

                FocusMethod.values().forEach { m ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { method = m }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (method == m), onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(text = m.label)
                    }
                }

                // Input Menit Manual
                OutlinedTextField(
                    value = minutes,
                    onValueChange = {
                        if (it.all(Char::isDigit)) minutes = it
                    },
                    enabled = method.editable,
                    label = { Text("Durasi (menit)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isMinutesValid, // 🔥 Merah jika kosong atau 0
                    supportingText = {
                        if (!isMinutesValid) {
                            Text("Durasi harus diisi (min. 1)", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canConfirm) {
                        val now = System.currentTimeMillis()
                        val selectedMinutes = minutes.toIntOrNull() ?: 0

                        onConfirm(
                            TaskEntity(
                                title = title,
                                description = desc,
                                priority = TaskPriority.MEDIUM,
                                mood = TaskMood.NORMAL,
                                startDate = now,
                                deadlineDate = now,
                                dueDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                                isDone = false,
                                createdAt = now
                            ),
                            selectedMinutes
                        )
                    }
                },
                enabled = canConfirm // 🔥 Tombol hanya aktif jika valid
            ) {
                Text("Mulai Fokus")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

fun priorityColor(priority: TaskPriority): Color = when (priority) {
    TaskPriority.HIGH -> Color(0xFFFF6B6B)
    TaskPriority.MEDIUM -> Color(0xFFFFC75F)
    TaskPriority.LOW -> Color(0xFF4D96FF)
}

// Tambahkan helper yang konsisten dengan TaskItem kamu
fun moodEmoji(mood: TaskMood): String = when (mood) {
    TaskMood.CALM -> "😌"
    TaskMood.NORMAL -> "🙂"
    TaskMood.STRESS -> "😵"
}

fun moodLabel(mood: TaskMood): String = when (mood) {
    TaskMood.CALM -> "Calm"
    TaskMood.NORMAL -> "Normal"
    TaskMood.STRESS -> "Stress"
}