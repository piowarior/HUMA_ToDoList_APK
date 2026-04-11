package com.huma.app.ui.screen.focus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.huma.app.R
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
import kotlin.random.Random

/* =========================
   FOCUS METHOD
========================= */
enum class FocusMethod(val label: String, val description: String, val defaultMinutes: Int?, val editable: Boolean) {
    NONE("None", "Timer manual sesuai keinginanmu.", null, true),
    POMODORO("Pomodoro", "25m fokus, 5m istirahat. Setelah 4 sesi, istirahat panjang (20m).", 25, false),
    DEEP_WORK("Deep Work", "50m fokus penuh tanpa distraksi.", 50, false),
    FLOW("Flow Mode", "Ritme Otak: Fokus tanpa batas, istirahat saat kamu butuh.", 0, false)
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
   HELPER FUNCTIONS
========================= */
private var mediaPlayer: MediaPlayer? = null

fun playFocusSound(context: Context, loop: Boolean = false) {
    stopFocusSound()
    try {
        mediaPlayer = MediaPlayer.create(context, R.raw.notiffocushuma).apply {
            isLooping = loop
            setOnCompletionListener { if (!loop) it.release() }
            start()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun stopFocusSound() {
    mediaPlayer?.let {
        try {
            if (it.isPlaying) it.stop()
        } catch (e: Exception) {}
        it.release()
    }
    mediaPlayer = null
}

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
    var sessionState by remember { mutableStateOf<FocusSessionState?>(null) }
    var showQuickAdd by remember { mutableStateOf(false) }

    if (sessionState != null && selectedTask != null) {
        FocusTimerScreen(
            task = selectedTask!!,
            state = sessionState!!,
            onStateChange = { sessionState = it },
            onCancel = {
                context.stopService(Intent(context, FocusService::class.java))
                stopFocusSound()
                sessionState = null
                selectedTask = null
            },
            onFinishTask = { done ->
                if (done) {
                    if (selectedTask!!.id == 0) {
                        taskViewModel.addTask(context, selectedTask!!.copy(isDone = true))
                    } else {
                        taskViewModel.toggleTaskCompletion(context, selectedTask!!)
                    }
                }
                context.stopService(Intent(context, FocusService::class.java))
                stopFocusSound()
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
                FloatingActionButton(onClick = { showQuickAdd = true }, containerColor = Color(0xFF6C63FF), contentColor = Color.White) {
                    Icon(Icons.Default.Bolt, null)
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
                    Text("Pilih satu task. Fokus tanpa distraksi.", color = Color.Gray)
                }

                if (todayTasks.isNotEmpty()) {
                    item { FocusHeader("Today") }
                    items(todayTasks) { task ->
                        FocusTaskItem(task) {
                            selectedTask = task
                            showSetup = true
                        }
                    }
                }

                if (upcomingTasks.isNotEmpty()) {
                    item { FocusHeader("Upcoming") }
                    items(upcomingTasks) { task ->
                        FocusTaskItem(task) {
                            selectedTask = task
                            showSetup = true
                        }
                    }
                }
            }
        }
    }

    if (showSetup && selectedTask != null) {
        FocusSetupDialog(
            taskTitle = selectedTask!!.title,
            onDismiss = { showSetup = false },
            onStart = { state ->
                val intent = Intent(context, FocusService::class.java).apply {
                    putExtra("task", selectedTask!!.title)
                    putExtra("method", state.method.label)
                    putExtra("phase", state.phase.name)
                    putExtra("time", String.format("%02d:%02d", state.remainingSeconds / 60, state.remainingSeconds % 60))
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

    if (showQuickAdd) {
        QuickAddFocusDialog(
            onDismiss = { showQuickAdd = false },
            onConfirm = { task, minutes, method ->
                selectedTask = task
                val startSeconds = if (method == FocusMethod.FLOW) 0 else minutes * 60
                sessionState = FocusSessionState(
                    method = method,
                    phase = if (method == FocusMethod.FLOW) FocusPhase.FLOW else FocusPhase.FOCUS,
                    cycle = 1,
                    remainingSeconds = startSeconds
                )

                val intent = Intent(context, FocusService::class.java).apply {
                    putExtra("task", task.title)
                    putExtra("method", method.label)
                    putExtra("phase", sessionState!!.phase.name)
                    putExtra("time", String.format("%02d:%02d", startSeconds / 60, startSeconds % 60))
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
    val dateText = remember(task.startDate) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(task.startDate))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(12.dp).background(priorityColor(task.priority ?: TaskPriority.MEDIUM), CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f).clickable { onClick() }) {
                    Text(text = task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (showDate) {
                        Text(text = dateText, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6C63FF), fontWeight = FontWeight.Medium)
                    }
                    task.description?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(2.dp))
                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, maxLines = 2)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        task.dueDate?.let {
                            Text(text = it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(text = "${moodEmoji(task.mood ?: TaskMood.NORMAL)} ${moodLabel(task.mood ?: TaskMood.NORMAL)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
            IconButton(onClick = { onClick() }, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start Focus", tint = Color(0xFF6C63FF), modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun FocusHeader(title: String) {
    Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5), modifier = Modifier.padding(vertical = 8.dp))
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

    val isValid = method == FocusMethod.FLOW || (minutes.isNotEmpty() && (minutes.toIntOrNull() ?: 0) >= 1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Setup Fokus", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Task: $taskTitle", fontWeight = FontWeight.SemiBold)
                FocusMethod.values().forEach { m ->
                    Column(Modifier.fillMaxWidth().clickable { method = m }.padding(vertical = 4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = method == m, onClick = { method = m })
                            Text(m.label, fontWeight = FontWeight.Bold)
                        }
                        Text(m.description, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 32.dp))
                    }
                }
                if (method == FocusMethod.NONE) {
                    Column {
                        OutlinedTextField(
                            value = minutes,
                            onValueChange = { if (it.all(Char::isDigit)) minutes = it },
                            label = { Text("Durasi (menit)") },
                            placeholder = { Text("Minimal 1 menit") },
                            isError = minutes.isNotEmpty() && (minutes.toIntOrNull() ?: 0) < 1,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (minutes.isNotEmpty() && (minutes.toIntOrNull() ?: 0) < 1) {
                            Text("Minimal 1 menit ya!", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val min = minutes.toIntOrNull() ?: 25
                    onStart(FocusSessionState(
                        method = method,
                        phase = if (method == FocusMethod.FLOW) FocusPhase.FLOW else FocusPhase.FOCUS,
                        cycle = 1,
                        remainingSeconds = min * 60
                    ))
                },
                enabled = isValid
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
    var seconds by remember(state) { mutableStateOf(state.remainingSeconds) }
    var paused by remember { mutableStateOf(true) } // Start paused for Intro
    var showIntroPopup by remember { mutableStateOf(true) }
    var showExit by remember { mutableStateOf(false) }
    
    // Pomodoro specific popups
    var showPomoSessionEndPopup by remember { mutableStateOf(false) }
    var showPomoBreakReadyPopup by remember { mutableStateOf(false) }
    var showPomoBreakPopup by remember { mutableStateOf(false) }
    
    // Generic phase popup for other methods
    var showPhasePopup by remember { mutableStateOf(false) }
    
    var showFlowBreakPopup by remember { mutableStateOf(false) }
    var suggestedBreakMinutes by remember { mutableStateOf(5) }
    
    // Motivational Popups
    var motivationalMessage by remember { mutableStateOf("") }
    var showMotivationalPopup by remember { mutableStateOf(false) }

    val context = LocalContext.current

    BackHandler { showExit = true }

    // Set showPomoBreakPopup when phase changes to break in Pomodoro
    LaunchedEffect(state.phase) {
        if (state.method == FocusMethod.POMODORO && (state.phase == FocusPhase.SHORT_BREAK || state.phase == FocusPhase.LONG_BREAK)) {
            showPomoBreakPopup = true
        } else {
            showPomoBreakPopup = false
        }
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.getStringExtra("action")) {
                    ACTION_PAUSE -> paused = true
                    ACTION_RESUME -> paused = false
                    ACTION_STOP -> onCancel()
                }
            }
        }
        val filter = IntentFilter("FOCUS_UPDATE")
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose {
            context.unregisterReceiver(receiver)
            stopFocusSound()
        }
    }

    // Timer Logic
    LaunchedEffect(state, paused, seconds) {
        if (!paused && !showIntroPopup) {
            delay(1000)
            val nextSeconds = if (state.method == FocusMethod.FLOW && state.phase == FocusPhase.FLOW) {
                seconds + 1
            } else if (seconds > 0) {
                seconds - 1
            } else 0
            
            seconds = nextSeconds

            val updateIntent = Intent(context, FocusService::class.java).apply {
                putExtra("task", task.title)
                putExtra("method", state.method.label)
                putExtra("phase", phaseTitle(state.phase))
                putExtra("time", String.format("%02d:%02d", seconds / 60, seconds % 60))
                putExtra("is_paused", false)
            }
            context.startService(updateIntent)

            // Pomodoro specific logic
            if (state.method == FocusMethod.POMODORO) {
                if (state.phase == FocusPhase.FOCUS && seconds == 0) {
                    playFocusSound(context, loop = true)
                    showPomoSessionEndPopup = true
                    paused = true
                }
                
                if ((state.phase == FocusPhase.SHORT_BREAK || state.phase == FocusPhase.LONG_BREAK) && seconds == 10) {
                    playFocusSound(context, loop = true)
                    showPomoBreakReadyPopup = true
                }
                
                if ((state.phase == FocusPhase.SHORT_BREAK || state.phase == FocusPhase.LONG_BREAK) && seconds == 0) {
                    paused = true
                }
            } else if (state.phase != FocusPhase.FLOW && seconds == 0) {
                playFocusSound(context, loop = true)
                showPhasePopup = true
                paused = true
            }
        }
    }

    // Motivational Popup Logic - every 5 minutes
    LaunchedEffect(paused) {
        if (!paused) {
            while (true) {
                delay(300000) // 5 menit
                motivationalMessage = getRandomMotivation()
                showMotivationalPopup = true
                delay(7000)
                showMotivationalPopup = false
            }
        }
    }

    Surface(color = phaseColor(state.phase), modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                val currentTitle = if (state.method == FocusMethod.POMODORO && (state.phase == FocusPhase.SHORT_BREAK || state.phase == FocusPhase.LONG_BREAK)) {
                    if (seconds <= 10 && seconds > 0) "BERSIAP-SIAP MULAI LAGI..." else phaseTitle(state.phase)
                } else phaseTitle(state.phase)
                
                Text(currentTitle, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(12.dp))
                Text(task.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(Modifier.height(32.dp))
                Text(String.format("%02d:%02d", seconds / 60, seconds % 60), fontSize = 80.sp, color = Color.White, fontWeight = FontWeight.Light)
                if (state.method == FocusMethod.POMODORO && state.phase == FocusPhase.FOCUS) {
                    Text("Session ${state.cycle} / 4", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                }
                Spacer(Modifier.height(48.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = {
                            paused = !paused
                            val intent = Intent(context, FocusService::class.java).apply {
                                action = if (paused) ACTION_PAUSE else ACTION_RESUME
                                putExtra("time", String.format("%02d:%02d", seconds / 60, seconds % 60))
                                putExtra("task", task.title)
                            }
                            context.startService(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(if (paused) Icons.Default.PlayArrow else Icons.Default.Pause, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(if (paused) "Resume" else "Pause", color = Color.White)
                    }
                    Spacer(Modifier.width(16.dp))
                    Button(
                        onClick = {
                            stopFocusSound()
                            if (state.method == FocusMethod.FLOW && state.phase == FocusPhase.FLOW) {
                                playFocusSound(context, loop = true)
                                val workedMinutes = seconds / 60
                                suggestedBreakMinutes = when {
                                    workedMinutes < 30 -> 5
                                    workedMinutes >= 60 -> 15
                                    else -> 10
                                }
                                paused = true
                                showFlowBreakPopup = true
                            } else {
                                showExit = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Stop", color = Color.White)
                    }
                }
            }

            // Motivational Popup
            HebohMotivationalPopup(
                visible = showMotivationalPopup,
                message = motivationalMessage
            )
            
            // POMODORO BREAK STATUS POPUP
            if (showPomoBreakPopup && !showPomoBreakReadyPopup) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(enabled = false) {}, 
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("☕ Istirahat", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Ambil napas dalam-dalam...", color = Color.Gray)
                            Spacer(Modifier.height(16.dp))
                            Text(String.format("%02d:%02d", seconds / 60, seconds % 60), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                        }
                    }
                }
            }
        }
    }

    if (showIntroPopup) {
        AlertDialog(
            onDismissRequest = { showIntroPopup = false; paused = false },
            title = { Text("💡 Fokus Mode", fontWeight = FontWeight.Bold) },
            text = { Text(focusMessage(state.method, state.phase)) },
            confirmButton = { Button(onClick = { showIntroPopup = false; paused = false }) { Text("Gas!") } }
        )
    }

    // FLOW MODE BREAK POPUP - Consolidated Choices
    if (showFlowBreakPopup) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("🌊 Flow Session Berhenti", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Kamu sudah fokus selama ${seconds / 60} menit. Berdasarkan ritme otakmu, HUMA menyarankan istirahat selama $suggestedBreakMinutes menit.")
                    Spacer(Modifier.height(8.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            stopFocusSound()
                            showFlowBreakPopup = false
                            paused = false
                            onStateChange(state.copy(
                                phase = FocusPhase.SHORT_BREAK,
                                remainingSeconds = suggestedBreakMinutes * 60
                            ))
                        }
                    ) { Text("☕ Mulai Istirahat ($suggestedBreakMinutes m)") }
                    
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            stopFocusSound()
                            showFlowBreakPopup = false
                            onFinishTask(true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) { Text("✅ Tandai Selesai") }

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            stopFocusSound()
                            showFlowBreakPopup = false
                            paused = false
                        }
                    ) { Text("🔥 Lanjutkan Fokus") }

                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            stopFocusSound()
                            showFlowBreakPopup = false
                            onFinishTask(false)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) { Text("❌ Keluar (Belum Selesai)") }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    // POMODORO SESSION END POPUP
    if (showPomoSessionEndPopup) {
        val next = nextPhase(state)
        AlertDialog(
            onDismissRequest = { },
            title = { Text("🍅 Sesi ${state.cycle} Selesai!", fontWeight = FontWeight.Bold) },
            text = { Text("Waktunya istirahat sejenak. Klik OK untuk mulai istirahat.") },
            confirmButton = {
                Button(onClick = {
                    stopFocusSound()
                    showPomoSessionEndPopup = false
                    paused = false
                    onStateChange(next)
                }) { Text("OK") }
            }
        )
    }

    // POMODORO BREAK READY POPUP (Last 10 seconds)
    if (showPomoBreakReadyPopup) {
        val next = nextPhase(state)
        AlertDialog(
            onDismissRequest = { },
            title = { Text("⏰ Bersiap Kembali", fontWeight = FontWeight.Bold) },
            text = { Text("Istirahat hampir selesai. Siap untuk sesi berikutnya?") },
            confirmButton = {
                Button(onClick = {
                    stopFocusSound()
                    showPomoBreakReadyPopup = false
                    showPomoBreakPopup = false
                    paused = false
                    onStateChange(next)
                }) { Text("Lanjutkan Sesi") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        stopFocusSound()
                        showPomoBreakReadyPopup = false
                        seconds += 300 
                        paused = false
                    }) { Text("Tambah 5 Menit") }
                    
                    TextButton(onClick = {
                        stopFocusSound()
                        showPomoBreakReadyPopup = false
                        onFinishTask(false)
                    }) { Text("Berhenti") }
                }
            }
        )
    }

    // GENERIC PHASE POPUP (For Deep Work / None)
    if (showPhasePopup) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Waktu Habis!", fontWeight = FontWeight.Bold) },
            text = { Text("Target waktu tercapai. Apakah task ini beneran sudah selesai?") },
            confirmButton = {
                Button(onClick = {
                    stopFocusSound()
                    showPhasePopup = false
                    onFinishTask(true)
                }) { Text("Ya, Selesai") }
            },
            dismissButton = {
                TextButton(onClick = {
                    stopFocusSound()
                    showPhasePopup = false
                    onFinishTask(false) 
                }) { Text("Belum Selesai") }
            }
        )
    }

    if (showExit) {
        AlertDialog(
            onDismissRequest = { showExit = false },
            title = { Text("Hentikan Fokus?") },
            text = { Text("Apakah kamu ingin keluar? Task ini akan dianggap belum selesai.") },
            confirmButton = { 
                TextButton(onClick = { 
                    stopFocusSound()
                    onFinishTask(false) 
                }) { Text("Keluar") } 
            },
            dismissButton = { 
                TextButton(onClick = { 
                    showExit = false 
                }) { Text("Lanjutkan Fokus") } 
            }
        )
    }
}

/* =========================
   HEBOH MOTIVATION COMPONENTS
========================= */
@Composable
fun HebohMotivationalPopup(visible: Boolean, message: String) {
    val infiniteTransition = rememberInfiniteTransition()

    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(animationSpec = tween(600)) + scaleIn(initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut() + scaleOut(targetScale = 0.9f),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 140.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

            ConfettiBurst(visible)

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier.scale(bounceScale)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF6C63FF), Color(0xFF4895EF))
                            )
                        )
                        .padding(2.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "SURPRISE! ✨",
                                style = TextStyle(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    brush = Brush.horizontalGradient(listOf(Color(0xFF6C63FF), Color(0xFF4895EF))),
                                )
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = message,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                color = Color(0xFF1E1B4B),
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfettiBurst(active: Boolean) {
    if (!active) return

    val particles = remember { List(20) { ConfettiParticle() } }
    val infiniteTransition = rememberInfiniteTransition()

    Box {
        for (particle in particles) {
            val progress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = particle.duration, delayMillis = particle.delay),
                    repeatMode = RepeatMode.Restart
                )
            )

            val x = (particle.targetX * progress).dp
            val y = (particle.targetY * progress - (80 * progress * (1 - progress))).dp
            val scale = (1f - progress)
            val rotation = (particle.rotationSpeed * progress)

            Box(
                Modifier
                    .offset(x = x, y = y)
                    .graphicsLayer(
                        rotationZ = rotation,
                        scaleX = scale,
                        scaleY = scale,
                        alpha = 1f - progress
                    )
                    .size(particle.size.dp)
                    .background(particle.color, if (particle.isCircle) CircleShape else RoundedCornerShape(1.dp))
            )
        }
    }
}

class ConfettiParticle {
    val color = listOf(Color(0xFF6C63FF), Color(0xFF4895EF), Color(0xFF4CC9F0), Color.White, Color(0xFF3F37C9)).random()
    val size = Random.nextInt(4, 10)
    val duration = Random.nextInt(1000, 1500)
    val delay = Random.nextInt(0, 300)
    val targetX = Random.nextInt(-150, 150)
    val targetY = Random.nextInt(-200, 50)
    val rotationSpeed = Random.nextFloat() * 360f
    val isCircle = Random.nextBoolean()
}

/* =========================
   REMAINING COMPONENTS
========================= */
fun nextPhase(state: FocusSessionState): FocusSessionState {
    return when (state.method) {
        FocusMethod.POMODORO -> when (state.phase) {
            FocusPhase.FOCUS ->
                if (state.cycle >= 4)
                    state.copy(phase = FocusPhase.LONG_BREAK, remainingSeconds = 20 * 60, cycle = 4)
                else
                    state.copy(phase = FocusPhase.SHORT_BREAK, remainingSeconds = 5 * 60)
            FocusPhase.SHORT_BREAK ->
                state.copy(phase = FocusPhase.FOCUS, remainingSeconds = 25 * 60, cycle = state.cycle + 1)
            FocusPhase.LONG_BREAK ->
                state.copy(phase = FocusPhase.FOCUS, remainingSeconds = 25 * 60, cycle = 1)
            else -> state
        }
        FocusMethod.DEEP_WORK -> {
            if (state.phase == FocusPhase.FOCUS) state.copy(remainingSeconds = 0) else state
        }
        else -> state
    }
}

fun focusMessage(method: FocusMethod, phase: FocusPhase): String =
    when (method) {
        FocusMethod.POMODORO -> when (phase) {
            FocusPhase.FOCUS -> "🍅 Fokus 25 menit. Gunakan teknik Pomodoro untuk efisiensi maksimal."
            FocusPhase.SHORT_BREAK -> "☕ Istirahat sejenak (5 menit). Regangkan ototmu."
            FocusPhase.LONG_BREAK -> "🎉 Istirahat panjang (20 menit). Kamu luar biasa!"
            else -> ""
        }
        FocusMethod.DEEP_WORK -> "🔥 Deep Work. Fokus penuh selama 50 menit tanpa gangguan."
        FocusMethod.FLOW -> "🌊 Flow Mode. Menghitung maju, berhenti saat kamu merasa benar-benar butuh istirahat."
        FocusMethod.NONE -> "🎯 Fokus sekarang. Kerjakan sampai selesai."
    }

fun phaseTitle(phase: FocusPhase): String =
    when (phase) {
        FocusPhase.FOCUS -> "FOCUS SESSION"
        FocusPhase.SHORT_BREAK -> "SHORT BREAK"
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddFocusDialog(
    onDismiss: () -> Unit,
    onConfirm: (TaskEntity, Int, FocusMethod) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(FocusMethod.POMODORO) }
    var minutes by remember { mutableStateOf("25") }

    LaunchedEffect(method) {
        minutes = method.defaultMinutes?.toString() ?: ""
    }

    val isValidDuration = if (method == FocusMethod.NONE) {
        minutes.isNotEmpty() && (minutes.toIntOrNull() ?: 0) >= 1
    } else true

    val canConfirm = title.isNotBlank() && (method == FocusMethod.FLOW || isValidDuration)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fokus Cepat", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Task") },
                    placeholder = { Text("Apa yang ingin dikerjakan?") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi (opsional)") },
                    placeholder = { Text("Detail pengerjaan...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Pilih Metode:", style = MaterialTheme.typography.labelMedium)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FocusMethod.values().forEach { m ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clickable { method = m }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = (method == m), onClick = { method = m })
                                Spacer(Modifier.width(8.dp))
                                Text(text = m.label, fontWeight = FontWeight.Bold)
                            }
                            Text(m.description, fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 40.dp))
                        }
                    }
                }

                if (method == FocusMethod.NONE) {
                    Column {
                        OutlinedTextField(
                            value = minutes,
                            onValueChange = { if (it.all(Char::isDigit)) minutes = it },
                            label = { Text("Durasi (menit)") },
                            placeholder = { Text("Minimal 1 menit") },
                            isError = minutes.isNotEmpty() && (minutes.toIntOrNull() ?: 0) < 1,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (minutes.isNotEmpty() && (minutes.toIntOrNull() ?: 0) < 1) {
                            Text("Minimal 1 menit ya!", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    onConfirm(
                        TaskEntity(
                            title = title,
                            description = description,
                            priority = TaskPriority.MEDIUM,
                            mood = TaskMood.NORMAL,
                            startDate = now,
                            deadlineDate = now,
                            dueDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                            isDone = false,
                            createdAt = now
                        ),
                        minutes.toIntOrNull() ?: 0,
                        method
                    )
                },
                enabled = canConfirm
            ) { Text("Mulai") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

fun priorityColor(priority: TaskPriority): Color = when (priority) {
    TaskPriority.HIGH -> Color(0xFFFF6B6B)
    TaskPriority.MEDIUM -> Color(0xFFFFC75F)
    TaskPriority.LOW -> Color(0xFF4D96FF)
}

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

fun getRandomMotivation(): String {
    val quotes = listOf(
        "Semangat terus! Kamu pasti bisa! 💪",
        "Sedikit lagi, jangan menyerah ya! 🔥",
        "Fokus yuk, HUMA percaya padamu! ✨",
        "Ingat mimpimu, ayo kerja keras! 🚀",
        "Kamu luar biasa hari ini! 🌟",
        "Satu langkah lagi menuju kesuksesan! 🎯",
        "Jangan lupa bernapas dan tetap tenang 😌",
        "Setiap progres kecil itu berharga! 📈",
        "Ayo tuntaskan task ini! 🛠️",
        "Huma bangga melihat usahamu! 🥰"
    )
    return quotes.random()
}
