package com.huma.app.ui.screen.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.huma.app.ui.viewmodel.TaskViewModel
import com.huma.app.ui.components.task.TaskSection
import com.huma.app.ui.components.task.UpcomingPreviewSection
import com.huma.app.ui.screen.task.DoneTasksSection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Date
import java.util.Calendar

import androidx.compose.ui.platform.LocalContext
import com.huma.app.data.local.CommitmentEntity
import com.huma.app.ui.feature.WheelChallengeCard
import com.huma.app.ui.feature.TimeCapsuleCard
import com.huma.app.ui.feature.MindGymCard
import com.huma.app.ui.notification.NotificationHelper
import com.huma.app.viewmodel.CommitmentViewModel
import java.text.SimpleDateFormat

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(
    navController: NavController,
    taskViewModel: TaskViewModel,
    commitmentViewModel: CommitmentViewModel
) {
    val todayTasks by taskViewModel.todayTasks.collectAsState()
    val upcomingGrouped by taskViewModel.upcomingGrouped.collectAsState()
    val commitments by commitmentViewModel.allCommitments.collectAsState()
    var showDoneTasks by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            .verticalScroll(rememberScrollState())
    ) {
        HeaderSection()
        Spacer(Modifier.height(16.dp))
        
        // 🔥 DAILY COMMITMENT
        DailyCommitmentSection(
            commitments = commitments,
            onOpen = { navController.navigate("commitments") },
            onAddNew = { navController.navigate("add_commitment") }
        )

        Spacer(Modifier.height(22.dp))
        FeatureSlider(navController)
        Spacer(Modifier.height(26.dp))
        QuickMenu(navController)
        Spacer(Modifier.height(28.dp))
        
        TaskSection(
            title = "Tasks Today",
            tasks = todayTasks,
            onAddClick = { navController.navigate("add_task/today") },
            onSeeAll = { navController.navigate("tasks_today") },
            onTaskClick = { taskId -> navController.navigate("task_detail/$taskId") },
            onToggleDone = { task -> taskViewModel.toggleTaskCompletion(context, task) }
        )
        Spacer(Modifier.height(24.dp))
        UpcomingPreviewSection(
            groupedTasks = upcomingGrouped,
            onAddClick = { navController.navigate("add_task/upcoming") },
            onSeeAll = { navController.navigate("tasks_upcoming") },
            onTaskClick = { taskId -> navController.navigate("task_detail/$taskId") },
            onToggleDone = { task -> taskViewModel.toggleTaskCompletion(context, task) }
        )
        Spacer(Modifier.height(80.dp))
        Text(
            text = if (!showDoneTasks) "See all done tasks →" else "Hide done tasks ↑",
            color = Color(0xFF6C63FF),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable { showDoneTasks = !showDoneTasks }
        )
        Spacer(Modifier.height(12.dp))
        AnimatedVisibility(visible = showDoneTasks) {
            DoneTasksSection(
                groupedTasks = taskViewModel.doneTasks.collectAsState().value,
                onRestore = { task -> taskViewModel.toggleTaskCompletion(context, task) },
                onDelete = { task -> taskViewModel.deleteTask(context,task) }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeaderSection() {
    // Mengambil tanggal hari ini
    val today = LocalDate.now()
    val dayName = today.format(DateTimeFormatter.ofPattern("EEEE", Locale("id", "ID"))) // Nama Hari (e.g. Senin)
    val fullDate = today.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID"))) // Format Lengkap

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp) // Sedikit lebih pendek agar compact
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF87CEEB), // Sky Blue Terang
                        Color(0x8CE4FF)  // Sky Blue agak Deep (Cadet Blue)
                    )
                )
            )
    ) {
        // Dekorasi simpel tanpa animasi (Static)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.15f),
                radius = 250f,
                center = Offset(size.width * 0.9f, size.height * 0.1f)
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            // Tampilan Hari dan Tanggal
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$dayName, $fullDate",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Hi, Human! 👋",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Light
            )

            Text(
                "Make Every\nDay Count",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                lineHeight = 40.sp
            )

            Spacer(Modifier.height(16.dp))

            // Quote Box: Lebih kotak & Warna Abu-abu Gelap
            Surface(
                color = Color(0xFF37474F).copy(alpha = 0.08f), // Abu-abu tipis untuk box
                shape = RoundedCornerShape(8.dp), // Dibuat lebih kotak (dari 16 ke 8)
                border = BorderStroke(1.dp, Color(0xFF37474F).copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FormatQuote,
                        null,
                        tint = Color(0xFF64748B), // Ikon Abu-abu Tua
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Success is the sum of small efforts.",
                        color = Color(0xFF64748B), // Teks Abu-abu Tua (Sangat Jelas)
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}


@Composable
fun DailyCommitmentSection(
    commitments: List<CommitmentEntity>,
    onOpen: () -> Unit,
    onAddNew: () -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Daily Commitment 🔥", fontWeight = FontWeight.Bold)
            Text("See all →", color = Color(0xFFFFA726), modifier = Modifier.clickable { onOpen() })
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(commitments) { commitment ->
                CommitmentFlameCard(
                    commitment = commitment,
                    onClick = onOpen
                )
            }
            item {
                AddCommitmentCard(onClick = onAddNew)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("Konsistensi adalah kunci perubahan besar 🧡", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun CommitmentFlameCard(
    commitment: CommitmentEntity,
    onClick: () -> Unit
) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isDoneToday = commitment.completedDays.contains(today)
    
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFA726), // Orange
                            Color(0xFFFFF3E0)  // Whitish Orange
                        )
                    )
                )
                .padding(14.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    commitment.title, 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold, 
                    maxLines = 2,
                    style = MaterialTheme.typography.titleMedium
                )
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        val last7Days = remember {
                            List(7) { index ->
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DAY_OF_YEAR, -(6 - index))
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                            }
                        }
                        last7Days.forEach { date ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(
                                        if (commitment.completedDays.contains(date)) Color.White 
                                        else Color.White.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (isDoneToday) "Done today ✅" else "Pending 🔥",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AddCommitmentCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(140.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFFFA726).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Add, null, tint = Color(0xFFFFA726))
            Spacer(Modifier.height(6.dp))
            Text("New", color = Color(0xFFFFA726), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FeatureSlider(navController: NavController) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        item { WheelChallengeCard(onClick = { navController.navigate("wheel") }) }
        item { TimeCapsuleCard(onClick = { navController.navigate("capsule") }) }
        item { MindGymCard(onClick = { navController.navigate("mind_games") }) }
    }
}

@Composable
fun QuickMenu(navController: NavController) {
    Text("Quick Access", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
    val scrollState = rememberScrollState()
    Spacer(Modifier.height(12.dp))
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        MenuIconAnimated("Focus", Icons.Default.CenterFocusStrong) { navController.navigate("focus") }
        MenuIconAnimated("Streak", Icons.Default.LocalFireDepartment) { navController.navigate("streak") }
        MenuIconAnimated("Notes", Icons.Default.Description) { navController.navigate("notes_list") }
        MenuIconAnimated("Life", Icons.Default.Dashboard) { navController.navigate("life_area") }
        MenuIconAnimated("Stats", Icons.Default.BarChart) { navController.navigate("analytics") }
    }
}

@Composable
fun MenuIconAnimated(title: String, icon: ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(56.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color(0xFF6C63FF)) }
        Spacer(Modifier.height(6.dp))
        Text(title)
    }
}

@Composable
fun FeatureCardAnimated(index: Int) {
    Card(modifier = Modifier.width(260.dp).height(140.dp), shape = RoundedCornerShape(22.dp)) {
        Box(modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF9D50BB), Color(0xFF6E48AA)))).padding(18.dp)) {
            Text(when (index) { 0 -> "Stay Focused" 1 -> "Track Mood" else -> "Balance Life" }, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
