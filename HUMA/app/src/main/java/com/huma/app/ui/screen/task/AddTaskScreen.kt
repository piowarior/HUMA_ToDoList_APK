package com.huma.app.ui.screen.task

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.huma.app.data.local.TaskEntity
import com.huma.app.data.local.TaskMood
import com.huma.app.data.local.TaskPriority
import com.huma.app.ui.notification.scheduleTaskNotification
import com.huma.app.data.local.LifeArea
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

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var mood by remember { mutableStateOf(TaskMood.NORMAL) }

    var expandedPriority by remember { mutableStateOf(false) }
    var expandedMood by remember { mutableStateOf(false) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    var selectedDate by remember { mutableStateOf<Long?>(null) }

    val startPickerState = rememberTimePickerState(8, 0, true)
    val endPickerState = rememberTimePickerState(9, 0, true)

    var useStartTime by remember { mutableStateOf(false) }
    var useEndTime by remember { mutableStateOf(false) }

    // 🔥 1. Tambahkan State untuk menyimpan hasil tebakan
    var selectedArea by remember { mutableStateOf(LifeArea.PRIBADI) }

    val scrollState = rememberScrollState()

    LaunchedEffect(title) {
        val input = title.trim().lowercase()
        if (input.isBlank()) {
            selectedArea = LifeArea.PRIBADI
            return@LaunchedEffect
        }

        val akademikKeys = listOf("belajar", "tugas", "kuliah", "kerja", "kantor", "meeting", "rapat", "proyek", "project", "skripsi", "coding", "ujian", "quiz", "laporan", "magang", "bisnis", "omset", "nasabah", "klien", "presentasi", "webinar", "workshop", "modul", "praktikum", "resume", "cv", "interview", "gaji", "deadline", "lembur", "briefing", "training", "bimbel", "kursus", "pr", "tes", "sidang", "revisi", "editing", "investasi", "saham", "trading")
        val kesehatanKeys = listOf("lari", "gym", "workout", "olahraga", "sehat", "obat", "vitamin", "sakit", "dokter", "periksa", "puskesmas", "rs", "diet", "kalori", "puasa", "tidur", "istirahat", "medis", "vaksin", "minum", "yoga", "sepeda", "renang", "pagi", "kardio", "checkup", "klinik", "apotek", "skincare", "maskeran", "salon", "pijat", "urut", "tensi", "gula darah", "futsal", "badminton", "bola", "basket", "tenis", "maraton", "gowes")
        val spiritualKeys = listOf("sholat", "doa", "ibadah", "ngaji", "spiritual", "meditasi", "dzikir", "yasinan", "kajian", "gereja", "misa", "alkitab", "quran", "sedekah", "zakat", "shalawat", "vihara", "pura", "tahajud", "dhuha", "tarawih", "puasa sunnah", "sholawat", "taubat", "majelis", "sholawatan", "khotbah", "renungan", "zuhur", "ashar", "maghrib", "isya", "subuh", "witir", "infak")
        val rumahTanggaKeys = listOf("sapu", "pel", "masak", "cuci", "piring", "baju", "jemur", "setrika", "belanja", "pasar", "listrik", "air", "token", "beres", "kebun", "tanaman", "servis", "renovasi", "gas", "dapur", "sayur", "galon", "sampah", "perabot", "indomaret", "alfamart", "supermarket", "shopee", "tokped", "paket", "kurir", "ojol", "grab", "gojek", "mandi", "sarapan", "makan siang", "makan malam", "makan")
        val sosialKeys = listOf("nongkrong", "kencan", "date", "ketemu", "main", "futsal", "nobar", "silaturahmi", "reuni", "chat", "telpon", "pesta", "kondangan", "nikahan", "kumpul", "komunitas", "organisasi", "bukber", "party", "hangout", "ngopi", "makan bareng", "telp", "vc", "zoom", "discord", "game", "mabar", "nonton", "bioskop", "netflix", "youtube", "healing", "jalan-jalan", "trip", "liburan", "pantai", "gunung")

        val keywordsMap = mapOf(
            LifeArea.AKADEMIK to akademikKeys,
            LifeArea.KESEHATAN to kesehatanKeys,
            LifeArea.SPIRITUAL to spiritualKeys,
            LifeArea.RUMAH_TANGGA to rumahTanggaKeys,
            LifeArea.SOSIAL to sosialKeys
        )

        val inputWords = input.split(Regex("[^a-zA-Z0-9]+")).filter { it.isNotBlank() }

        // --- TAHAP 1: CEK SAMA PERSIS (EXACT) ---
        // Jika ketik "makan", dia akan cari di semua list.
        // Kalau ketemu di Rumah Tangga, langsung kunci dan BERHENTI.
        var foundExactArea: LifeArea? = null
        for (word in inputWords) {
            for (entry in keywordsMap) {
                if (entry.value.any { it.lowercase() == word }) {
                    foundExactArea = entry.key
                    break
                }
            }
            if (foundExactArea != null) break
        }

        if (foundExactArea != null) {
            selectedArea = foundExactArea
            return@LaunchedEffect // <--- Kuncinya di sini, dia gak akan lanjut ke cek typo
        }

        // --- TAHAP 2: CEK TYPO (Hanya jalan jika Tahap 1 Gagal) ---
        var bestArea = LifeArea.PRIBADI
        var highestScore = 0

        keywordsMap.forEach { (area, keys) ->
            val currentScore = calculateAreaScore(input, keys)
            if (currentScore > highestScore) {
                highestScore = currentScore
                bestArea = area
            }
        }

        selectedArea = if (highestScore > 0) bestArea else LifeArea.PRIBADI
    }

    Column(
        modifier = Modifier
            .fillMaxSize()           // Tambahkan ini agar memenuhi layar
            .padding(16.dp)
            .verticalScroll(scrollState) // Tambahkan ini agar bisa digulir
    ) {

        Text("Tambah Task Baru", style = MaterialTheme.typography.headlineMedium)

        // ===== DATE PICKER (UPCOMING ONLY) =====
        if (isUpcoming) {
            Button(
                modifier = Modifier.padding(vertical = 8.dp),
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            calendar.set(year, month, day)
                            selectedDate = calendar.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            ) {
                Text(
                    selectedDate?.let {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            .format(Date(it))
                    } ?: "Pilih Tanggal"
                )
            }
        }

        // ===== TITLE =====
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Judul") },
            modifier = Modifier.fillMaxWidth()
        )

        // 🔥 3. Indikator Smart Detect (Visual Feedback)
        Card(
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = selectedArea.color.copy(alpha = 0.1f))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(selectedArea.icon, null, tint = selectedArea.color, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Smart Detect: ${selectedArea.label}", fontWeight = FontWeight.Bold, color = selectedArea.color, fontSize = 14.sp)
                    Text("System otomatis mendeteksi area hidupmu", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ===== START TIME =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Jam Mulai")
            Checkbox(
                checked = useStartTime,
                onCheckedChange = { useStartTime = it }
            )
        }

        if (useStartTime) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                onClick = { showStartPicker = true }
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Jam Mulai")
                    Text("%02d:%02d".format(
                        startPickerState.hour,
                        startPickerState.minute
                    ))
                }
            }
        }

        if (showStartPicker && useStartTime) {
            AlertDialog(
                onDismissRequest = { showStartPicker = false },
                confirmButton = {
                    TextButton(onClick = { showStartPicker = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Pilih Jam Mulai") },
                text = { TimePicker(state = startPickerState) }
            )
        }


        Spacer(Modifier.height(8.dp))

        // ===== END TIME =====

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Jam Selesai")
            Checkbox(
                checked = useEndTime,
                onCheckedChange = { useEndTime = it }
            )
        }

        if (useEndTime) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                onClick = { showEndPicker = true }
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Jam Selesai")
                    Text("%02d:%02d".format(
                        endPickerState.hour,
                        endPickerState.minute
                    ))
                }
            }
        }

        if (showEndPicker && useEndTime) {
            AlertDialog(
                onDismissRequest = { showEndPicker = false },
                confirmButton = {
                    TextButton(onClick = { showEndPicker = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Pilih Jam Selesai") },
                text = { TimePicker(state = endPickerState) }
            )
        }

        Spacer(Modifier.height(8.dp))

        // ===== PRIORITY =====
        ExposedDropdownMenuBox(
            expanded = expandedPriority,
            onExpandedChange = { expandedPriority = !expandedPriority }
        ) {
            OutlinedTextField(
                value = priority.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Priority") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedPriority,
                onDismissRequest = { expandedPriority = false }
            ) {
                TaskPriority.values().forEach {
                    DropdownMenuItem(
                        text = { Text(it.name) },
                        onClick = {
                            priority = it
                            expandedPriority = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ===== MOOD =====
        ExposedDropdownMenuBox(
            expanded = expandedMood,
            onExpandedChange = { expandedMood = !expandedMood }
        ) {
            OutlinedTextField(
                value = mood.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Mood") },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedMood,
                onDismissRequest = { expandedMood = false }
            ) {
                TaskMood.values().forEach {
                    DropdownMenuItem(
                        text = { Text(it.name) },
                        onClick = {
                            mood = it
                            expandedMood = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Deskripsi") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // ===== SAVE BUTTON =====
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (title.isBlank()) return@Button

                if (isUpcoming && selectedDate == null) {
                    // 👉 BISA DIGANTI SNACKBAR
                    return@Button
                }

                val startDate = if (isUpcoming) {
                    selectedDate!!
                } else {
                    System.currentTimeMillis()
                }

                val timeText = when {
                    useStartTime && useEndTime -> {
                        "%02d:%02d - %02d:%02d".format(
                            startPickerState.hour,
                            startPickerState.minute,
                            endPickerState.hour,
                            endPickerState.minute
                        )
                    }

                    useStartTime -> {
                        "%02d:%02d".format(
                            startPickerState.hour,
                            startPickerState.minute
                        )
                    }

                    else -> null
                }


                viewModel.addTask(
                    TaskEntity(
                        title = title,
                        description = description,
                        startDate = startDate,
                        deadlineDate = null,
                        dueDate = timeText,
                        priority = priority,
                        mood = mood,
                        lifeArea = selectedArea.name,
                        isDone = false
                    )
                )

                scheduleTaskNotification(context, startDate, title)

                navController.popBackStack()
            }
        )
        {
            Text("Simpan")
        }
    }
}

// 🔥 Versi High-Precision: Exact Match adalah Raja, Typo tetap dilayani
fun String.isSimilarTo(target: String): Boolean {
    val s1 = this.lowercase().trim()
    val s2 = target.lowercase().trim()

    // 1. Jika sama persis, abaikan perhitungan algoritma (sudah ditangani di Score)
    if (s1 == s2) return true

    // 2. Batas masuk akal: Beda panjang karakter tidak boleh lebih dari 2
    if (Math.abs(s1.length - s2.length) > 2) return false

    val costs = IntArray(s2.length + 1)
    for (i in 0..s1.length) {
        var lastValue = i
        for (j in 0..s2.length) {
            if (i == 0) costs[j] = j
            else if (j > 0) {
                var newValue = costs[j - 1]
                if (s1[i - 1] != s2[j - 1])
                    newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1
                costs[j - 1] = lastValue
                lastValue = newValue
            }
        }
        if (i > 0) costs[s2.length] = lastValue
    }

    val distance = costs[s2.length]

    // 3. Toleransi Typo yang cerdas:
    return when {
        s2.length <= 4 -> distance <= 1 // Kata pendek (4 huruf) boleh salah 1 huruf
        s2.length <= 7 -> distance <= 2 // Kata sedang boleh salah 2 huruf
        else -> distance <= 3           // Kata panjang boleh salah 3 huruf
    }
}

fun calculateAreaScore(title: String, keywords: List<String>): Int {
    val words = title.lowercase().split(Regex("[^a-zA-Z0-9]+")).filter { it.isNotBlank() }
    var score = 0
    words.forEach { word ->
        keywords.forEach { key ->
            if (word.isSimilarTo(key)) {
                score += 100 // Poin typo kecil saja
            }
        }
    }
    return score
}