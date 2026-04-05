package com.huma.app.ui.feature

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huma.app.data.local.AppDatabase
import com.huma.app.data.local.CommitmentEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommitmentScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getInstance(context)
    val dao = db.commitmentDao()
    
    val commitments by dao.getAllCommitments().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var commitmentToComplete by remember { mutableStateOf<CommitmentEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daily Commitment 🔥", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFFFA726),
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Default.Add, "Add") }
        },
        containerColor = Color(0xFFF6F7FB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color(0xFFF6F7FB), Color(0xFFFFF3E0))))
        ) {
            if (commitments.isEmpty()) {
                EmptyCommitmentView()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(commitments, key = { it.id }) { commitment ->
                        CommitmentItemCard(
                            commitment = commitment,
                            onCompleteClick = { commitmentToComplete = commitment },
                            onDelete = { scope.launch { dao.deleteCommitment(commitment) } }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCommitmentDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, desc ->
                scope.launch {
                    dao.insertCommitment(CommitmentEntity(title = title, description = desc))
                    showAddDialog = false
                }
            }
        )
    }

    if (commitmentToComplete != null) {
        AlertDialog(
            onDismissRequest = { commitmentToComplete = null },
            title = { Text("Konfirmasi Selesai ✅") },
            text = { Text("Apakah kamu benar-benar sudah menyelesaikan tugas \"${commitmentToComplete?.title}\" untuk hari ini?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            commitmentToComplete?.let {
                                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                if (!it.completedDays.contains(today)) {
                                    val newList = it.completedDays + today
                                    dao.updateCommitment(it.copy(completedDays = newList))
                                }
                            }
                            commitmentToComplete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("Ya, Sudah!") }
            },
            dismissButton = {
                TextButton(onClick = { commitmentToComplete = null }) { Text("Belum") }
            }
        )
    }
}

@Composable
fun CommitmentItemCard(
    commitment: CommitmentEntity,
    onCompleteClick: () -> Unit,
    onDelete: () -> Unit
) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isDoneToday = commitment.completedDays.contains(today)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(commitment.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(commitment.description, color = Color.Gray, fontSize = 14.sp)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // Grid Kotak-Kotak Progres (30 hari terakhir)
            Text("Last 30 Days Progres", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            
            ProgresGrid(commitment.completedDays)

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onCompleteClick,
                enabled = !isDoneToday,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDoneToday) Color(0xFF4CAF50) else Color(0xFFFFA726)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (isDoneToday) Icons.Default.Check else Icons.Default.FlashOn, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isDoneToday) "Sudah Selesai Hari Ini" else "Selesaikan Hari Ini")
            }
        }
    }
}

@Composable
fun ProgresGrid(completedDays: List<String>) {
    val calendar = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val days = remember {
        List(30) { index ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, - (29 - index))
            sdf.format(cal.time)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        days.forEach { date ->
            val isCompleted = completedDays.contains(date)
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isCompleted) Color(0xFFFFA726) else Color(0xFFEEEEEE))
            )
        }
    }
}

@Composable
fun EmptyCommitmentView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔥", fontSize = 60.sp)
        Text("No Commitments Yet", fontWeight = FontWeight.Bold)
        Text("Buat janji kecil untuk dirimu sendiri.", color = Color.Gray)
    }
}

@Composable
fun AddCommitmentDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Commitment Baru 🎯") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tentang apa?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Deskripsi singkat") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotBlank()) onSave(title, desc) }) { Text("Buat") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
