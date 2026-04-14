package com.huma.app.ui.feature

import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.huma.app.data.local.AppDatabase
import com.huma.app.data.local.CommitmentEntity
import com.huma.app.ui.notification.CommitmentNotification
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCommitmentScreen(navController: NavController, commitmentId: Int? = null) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getInstance(context)
    val dao = db.commitmentDao()
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Umum") }
    var iconType by remember { mutableStateOf("FIRE") }
    var selectedColor by remember { mutableStateOf("#FFA726") }
    
    var isNotifEnabled by remember { mutableStateOf(false) }
    val notificationTimes = remember { mutableStateListOf<String>() }
    
    var isCustomSchedule by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("00:00") }
    var endTime by remember { mutableStateOf("23:59") }
    val selectedDays = remember { mutableStateListOf(1, 2, 3, 4, 5, 6, 7) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var existingCommitment by remember { mutableStateOf<CommitmentEntity?>(null) }

    LaunchedEffect(commitmentId) {
        if (commitmentId != null && commitmentId != -1) {
            val c = dao.getCommitmentById(commitmentId)
            if (c != null) {
                existingCommitment = c
                title = c.title
                desc = c.description
                category = c.category
                iconType = c.iconType
                selectedColor = c.colorHex
                isNotifEnabled = c.isNotificationEnabled
                notificationTimes.clear()
                notificationTimes.addAll(c.notificationTimes)
                isCustomSchedule = c.isCustomSchedule
                startTime = c.startTime ?: "00:00"
                endTime = c.endTime ?: "23:59"
                selectedDays.clear()
                selectedDays.addAll(c.scheduledDays)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (commitmentId == null) "New Commitment 🎯" else "Edit Commitment ✏️", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (commitmentId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                        }
                    }
                }
            )
        },
        containerColor = Color(0xFFF8F9FF)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // BASIC INFO CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nama Commitment") },
                        placeholder = { Text("Misal: Meditasi Pagi") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Deskripsi / Motivasi") },
                        placeholder = { Text("Mengapa kamu melakukan ini?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // ICON & COLOR CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Pilih Icon & Elemen", fontWeight = FontWeight.Bold)
                    
                    val icons = listOf(
                        "FIRE" to "#FF5722",
                        "WATER" to "#2196F3",
                        "LEAF" to "#4CAF50",
                        "STAR" to "#FFC107",
                        "HEART" to "#E91E63"
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        icons.forEach { (type, color) ->
                            val isSelected = iconType == type
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(android.graphics.Color.parseColor(color)).copy(alpha = 0.2f) else Color.Transparent)
                                    .border(if (isSelected) 2.dp else 0.dp, Color(android.graphics.Color.parseColor(color)), CircleShape)
                                    .clickable { 
                                        iconType = type
                                        selectedColor = color
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when(type) {
                                        "FIRE" -> Icons.Default.LocalFireDepartment
                                        "WATER" -> Icons.Default.WaterDrop
                                        "LEAF" -> Icons.Default.Eco
                                        "STAR" -> Icons.Default.AutoAwesome
                                        else -> Icons.Default.Favorite
                                    },
                                    contentDescription = null,
                                    tint = Color(android.graphics.Color.parseColor(color))
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = when(iconType) {
                            "FIRE" -> "Elemen Api: Untuk komitmen yang membakar semangat!"
                            "WATER" -> "Elemen Air: Untuk ketenangan dan konsistensi."
                            "LEAF" -> "Elemen Daun: Untuk pertumbuhan dan kesehatan."
                            "STAR" -> "Elemen Bintang: Untuk pencapaian dan mimpi."
                            else -> "Elemen Hati: Untuk kasih sayang dan kepedulian diri."
                        },
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // NOTIFICATION CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, null, tint = Color(0xFFFFA726))
                        Spacer(Modifier.width(12.dp))
                        Text("Aktifkan Pengingat", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Switch(checked = isNotifEnabled, onCheckedChange = { isNotifEnabled = it })
                    }
                    
                    AnimatedVisibility(visible = isNotifEnabled) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            notificationTimes.forEachIndexed { index, time ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Pukul $time", fontWeight = FontWeight.Medium)
                                    IconButton(onClick = { notificationTimes.removeAt(index) }) {
                                        Icon(Icons.Default.RemoveCircleOutline, null, tint = Color.Red)
                                    }
                                }
                            }
                            Button(
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    TimePickerDialog(context, { _, h, m ->
                                        val timeStr = "%02d:%02d".format(h, m)
                                        if (!notificationTimes.contains(timeStr)) notificationTimes.add(timeStr)
                                    }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA726).copy(alpha = 0.1f), contentColor = Color(0xFFE65100))
                            ) {
                                Icon(Icons.Default.Add, null)
                                Text("Tambah Jam Notifikasi")
                            }
                        }
                    }
                }
            }

            // SCHEDULE CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EventRepeat, null, tint = Color(0xFF6C63FF))
                        Spacer(Modifier.width(12.dp))
                        Text("Jadwal Komitmen", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        FilterChip(
                            selected = isCustomSchedule,
                            onClick = { isCustomSchedule = !isCustomSchedule },
                            label = { Text(if (isCustomSchedule) "Custom" else "Harian") }
                        )
                    }
                    
                    if (isCustomSchedule) {
                        Spacer(Modifier.height(16.dp))
                        Text("Pilih Hari:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            val days = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")
                            days.forEachIndexed { index, day ->
                                val dayNum = index + 1
                                val isSelected = selectedDays.contains(dayNum)
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF6C63FF) else Color(0xFFF0F0F0))
                                        .clickable {
                                            if (isSelected) selectedDays.remove(dayNum) else selectedDays.add(dayNum)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(day.take(1), color = if (isSelected) Color.White else Color.Black, fontSize = 12.sp)
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(Modifier.weight(1f)) {
                                Text("Mulai", fontSize = 12.sp)
                                OutlinedCard(onClick = {
                                    TimePickerDialog(context, { _, h, m -> startTime = "%02d:%02d".format(h, m) }, 0, 0, true).show()
                                }) {
                                    Text(startTime, Modifier.padding(12.dp))
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                Text("Selesai", fontSize = 12.sp)
                                OutlinedCard(onClick = {
                                    TimePickerDialog(context, { _, h, m -> endTime = "%02d:%02d".format(h, m) }, 23, 59, true).show()
                                }) {
                                    Text(endTime, Modifier.padding(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // SAVE BUTTON
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        scope.launch {
                            val commitment = CommitmentEntity(
                                id = commitmentId ?: 0,
                                title = title,
                                description = desc,
                                category = category,
                                iconType = iconType,
                                colorHex = selectedColor,
                                isNotificationEnabled = isNotifEnabled,
                                notificationTimes = notificationTimes.toList(),
                                isCustomSchedule = isCustomSchedule,
                                startTime = startTime,
                                endTime = endTime,
                                scheduledDays = if (isCustomSchedule) selectedDays.toList() else (1..7).toList(),
                                completedDays = existingCommitment?.completedDays ?: emptyList(),
                                currentStreak = existingCommitment?.currentStreak ?: 0,
                                longestStreak = existingCommitment?.longestStreak ?: 0,
                                lastCompletedDate = existingCommitment?.lastCompletedDate,
                                isBroken = existingCommitment?.isBroken ?: false
                            )
                            
                            val finalId = if (commitmentId == null) {
                                dao.insertCommitment(commitment).toInt()
                            } else {
                                dao.updateCommitment(commitment)
                                commitment.id
                            }
                            
                            // 🔥 Schedule notification
                            CommitmentNotification.scheduleNotifications(context, commitment.copy(id = finalId))
                            
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(android.graphics.Color.parseColor(selectedColor)))
            ) {
                Text(if (commitmentId == null) "MULAI COMMITMENT 🚀" else "SIMPAN PERUBAHAN ✅", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Komitmen? 🗑️") },
            text = { Text("Semua progres dan streak kamu akan hilang permanen. Yakin?") },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        existingCommitment?.let { 
                            CommitmentNotification.cancelNotifications(context, it)
                            dao.deleteCommitment(it) 
                        }
                        navController.popBackStack()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") } }
        )
    }
}
