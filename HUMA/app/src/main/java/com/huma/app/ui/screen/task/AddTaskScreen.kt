package com.huma.app.ui.screen.task

import android.app.DatePickerDialog
import android.os.Build
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
import com.huma.app.data.local.LifeArea
import com.huma.app.data.local.TaskEntity
import com.huma.app.data.local.TaskMood
import com.huma.app.data.local.TaskPriority
import com.huma.app.ui.notification.scheduleTaskNotification
import com.huma.app.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    navController: NavController,
    viewModel: TaskViewModel,
    isUpcoming: Boolean
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var mood by remember { mutableStateOf(TaskMood.NORMAL) }
    var selectedDate by remember { mutableStateOf<Long?>(if (isUpcoming) null else System.currentTimeMillis()) }
    
    var useStartTime by remember { mutableStateOf(false) }
    var useEndTime by remember { mutableStateOf(false) }
    
    val startPickerState = rememberTimePickerState(8, 0, true)
    val endPickerState = rememberTimePickerState(9, 0, true)
    
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var selectedArea by remember { mutableStateOf(LifeArea.PRIBADI) }

    // Smart Detect Logic with Typo Correction
    LaunchedEffect(title) {
        val input = title.trim().lowercase()
        if (input.isBlank()) {
            selectedArea = LifeArea.PRIBADI
            return@LaunchedEffect
        }

        val keywordsMap = mapOf(
            LifeArea.AKADEMIK to listOf(
                "belajar", "tugas", "kuliah", "kerja", "meeting", "rapat", "proyek", "project", "skripsi", "coding", 
                "ujian", "quiz", "laporan", "magang", "bisnis", "kantor", "presentasi", "buku", "lab", "praktikum",
                "sempro", "sidang", "praktek", "office", "work", "study", "exam", "assignment", "deadline", "course",
                "lecture", "univ", "sekolah", "pr", "homework", "training", "workshop", "seminar", "riset", "research"
            ),
            LifeArea.KESEHATAN to listOf(
                "lari", "gym", "workout", "olahraga", "sehat", "obat", "vitamin", "sakit", "dokter", "diet", "puasa", 
                "tidur", "yoga", "renang", "sepeda", "health", "fitness", "bola", "basket", "badminton", "medis", "checkup",
                "vaksing", "klinik", "rs", "rumah sakit", "apotek", "suplemen", "istirahat", "rest", "sport", "futsal",
                "tenis", "voli", "karate", "pencak silat", "jalan santai", "jogging", "marathon", "kalori", "protein"
            ),
            LifeArea.SPIRITUAL to listOf(
                "sholat", "doa", "ibadah", "ngaji", "meditasi", "dzikir", "yasinan", "kajian", "gereja", "alkitab", 
                "quran", "sedekah", "pray", "religious", "tahajud", "dhuha", "maghrib", "isya", "subuh", "zuhur", "ashar",
                "puasa", "ramadhan", "misa", "kebaktian", "vihara", "pura", "sholawat", "taubat", "tasbih", "tahlil",
                "zakat", "infaq", "wakaf", "haji", "umroh", "ustad", "pastur", "pendeta", "bhikkhu"
            ),
            LifeArea.RUMAH_TANGGA to listOf(
                "sapu", "pel", "masak", "cuci", "piring", "baju", "belanja", "listrik", "beres", "kebun", "sampah",
                "bersih", "home", "house", "cleaning", "masak", "dapur", "pasar", "supermarket", "furniture", "rusak",
                "perbaiki", "service", "ac", "genteng", "cat", "lampu", "keran", "taman", "siram", "bunga", "tanaman",
                "makan", "sarapan", "lunch", "dinner", "jemur", "setrika", "folding", "debu"
            ),
            LifeArea.SOSIAL to listOf(
                "nongkrong", "kencan", "date", "ketemu", "main", "futsal", "nobar", "silaturahmi", "reuni", "chat", 
                "telpon", "pesta", "social", "party", "hangout", "dinner", "makan bareng", "teman", "pacar", "keluarga",
                "family", "friends", "visit", "kunjung", "ngopi", "kafe", "mall", "bioskop", "film", "telp", "wa",
                "video call", "vc", "kumpul", "arisan", "wedding", "nikahan", "kondangan", "ulang tahun", "ultah"
            )
        )

        fun levenshteinDistance(s1: String, s2: String): Int {
            val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
            for (i in 0..s1.length) dp[i][0] = i
            for (j in 0..s2.length) dp[0][j] = j
            for (i in 1..s1.length) {
                for (j in 1..s2.length) {
                    val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                    dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
                }
            }
            return dp[s1.length][s2.length]
        }

        val inputWords = input.split(Regex("[^a-zA-Z0-9]+")).filter { it.isNotBlank() }
        var bestMatchArea: LifeArea? = null
        var minDistance = 3 // Max typo tolerance (threshold)

        for (word in inputWords) {
            for (entry in keywordsMap) {
                for (keyword in entry.value) {
                    if (word == keyword) {
                        bestMatchArea = entry.key
                        minDistance = 0
                        break
                    }
                    val dist = levenshteinDistance(word, keyword)
                    if (dist < minDistance) {
                        minDistance = dist
                        bestMatchArea = entry.key
                    }
                }
                if (minDistance == 0) break
            }
            if (minDistance == 0) break
        }

        selectedArea = bestMatchArea ?: LifeArea.PRIBADI
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FF),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (isUpcoming) "New Upcoming Task" else "New Today Task", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            // TITLE & DESC CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("What are you planning?") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Add more details...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    // Smart Detect Info
                    Surface(
                        color = selectedArea.color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(selectedArea.icon, null, tint = selectedArea.color, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Area: ${selectedArea.label}", color = selectedArea.color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // DATE & TIME CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF6C63FF))
                        Spacer(Modifier.width(12.dp))
                        Text("Task Date", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        TextButton(
                            onClick = {
                                if (isUpcoming) {
                                    DatePickerDialog(context, { _, y, m, d ->
                                        calendar.set(y, m, d)
                                        selectedDate = calendar.timeInMillis
                                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                                }
                            },
                            enabled = isUpcoming // Hanya bisa di-klik jika mode Upcoming
                        ) {
                            Text(
                                text = selectedDate?.let { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it)) } ?: "Pick Date",
                                color = if (isUpcoming) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }

                    Divider(color = Color(0xFFF0F0F0))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = Color(0xFF6C63FF))
                        Spacer(Modifier.width(12.dp))
                        Text("Start Time", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Checkbox(checked = useStartTime, onCheckedChange = { useStartTime = it })
                    }
                    if (useStartTime) {
                        OutlinedButton(onClick = { showStartPicker = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Text("Set Start: %02d:%02d".format(startPickerState.hour, startPickerState.minute))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = Color.Gray)
                        Spacer(Modifier.width(12.dp))
                        Text("End Time", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Checkbox(checked = useEndTime, onCheckedChange = { useEndTime = it })
                    }
                    if (useEndTime) {
                        OutlinedButton(onClick = { showEndPicker = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
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
                            FilterChip(selected = priority == it, onClick = { priority = it }, label = { Text(it.name) }, shape = RoundedCornerShape(12.dp))
                        }
                    }
                    Text("Current Mood", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TaskMood.values().forEach {
                            FilterChip(selected = mood == it, onClick = { mood = it }, label = { Text(it.name) }, shape = RoundedCornerShape(12.dp))
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (title.isBlank() || selectedDate == null) return@Button
                    val timeTextForNotification = if (useStartTime) "%02d:%02d".format(startPickerState.hour, startPickerState.minute) else null
                    val timeTextForDb = when {
                        useStartTime && useEndTime -> "%02d:%02d - %02d:%02d".format(startPickerState.hour, startPickerState.minute, endPickerState.hour, endPickerState.minute)
                        useStartTime -> timeTextForNotification
                        else -> null
                    }
                    viewModel.addTask(context, TaskEntity(title = title, description = description, startDate = selectedDate!!, deadlineDate = null, dueDate = timeTextForDb, priority = priority, mood = mood, lifeArea = selectedArea.name, isDone = false))
                    scheduleTaskNotification(context, selectedDate!!, timeTextForNotification, title)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
            ) {
                Text("CREATE TASK", fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(30.dp))
        }
    }

    if (showStartPicker) TimePickerDialog(onDismiss = { showStartPicker = false }, state = startPickerState)
    if (showEndPicker) TimePickerDialog(onDismiss = { showEndPicker = false }, state = endPickerState)
}
