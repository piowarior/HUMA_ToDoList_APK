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

import androidx.compose.ui.platform.LocalContext
import com.huma.app.ui.notification.NotificationHelper


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(
    navController: NavController,
    taskViewModel: TaskViewModel
) {
    val todayTasks by taskViewModel.todayTasks.collectAsState()
    val upcomingGrouped by taskViewModel.upcomingGrouped.collectAsState()
    var showDoneTasks by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
            .verticalScroll(rememberScrollState())
    ) {

        HeaderSection()

        Spacer(Modifier.height(16.dp))

        // 🔥 DAILY STREAK (HUMA FLAME)
        DailyStreakSection(
            onOpen = { navController.navigate("streak") }
        )

        Spacer(Modifier.height(22.dp))

        FeatureSlider()

        Spacer(Modifier.height(26.dp))

        QuickMenu(navController)

        Spacer(Modifier.height(24.dp))

        NotificationTestPanel()

        Spacer(Modifier.height(28.dp))

        // ================= TASK TODAY =================
        TaskSection(
            title = "Tasks Today",
            tasks = taskViewModel.todayTasks.collectAsState().value,
            onAddClick = {
                navController.navigate("add_task/today")
            },
            onSeeAll = {
                navController.navigate("tasks_today")
            },
            onTaskClick = { taskId ->
                navController.navigate("task_detail/$taskId")
            },
            onToggleDone = { task ->
                taskViewModel.toggleTaskCompletion(task)
            }
        )

        Spacer(Modifier.height(24.dp))

// ================= UPCOMING TASK =================
        UpcomingPreviewSection(
            groupedTasks = upcomingGrouped,
            onAddClick = {
                navController.navigate("add_task/upcoming")
            },
            onSeeAll = {
                navController.navigate("tasks_upcoming")
            },
            onTaskClick = { taskId ->
                navController.navigate("task_detail/$taskId")
            },
            onToggleDone = { task ->
                taskViewModel.toggleTaskCompletion(task)
            }
        )

        Spacer(Modifier.height(80.dp))

        Spacer(Modifier.height(24.dp))

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
                groupedTasks = taskViewModel.doneTasks.collectAsState().value, // pake doneTasks, bukan doneGrouped
                onRestore = { task -> taskViewModel.toggleTaskCompletion(task) },
                onDelete = { task -> taskViewModel.deleteTask(task) }
            )
        }

    }
}


@Composable
fun NotificationTestPanel() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {

        Text("🔔 Test Notification", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            Button(onClick = {
                NotificationHelper.show(
                    context,
                    "Hai 👋",
                    "Semoga harimu berjalan dengan baik hari ini."
                )
            }) {
                Text("Greeting")
            }

            Button(onClick = {
                NotificationHelper.show(
                    context,
                    "Pengingat Harian",
                    "Sedikit langkah hari ini jauh lebih baik daripada diam."
                )
            }) {
                Text("Reminder")
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            Button(onClick = {
                NotificationHelper.show(
                    context,
                    "Hari ini belum tercatat",
                    "Masih ada waktu untuk melanjutkan hari ini."
                )
            }) {
                Text("Streak +1")
            }

            Button(onClick = {
                NotificationHelper.show(
                    context,
                    "Sudah lama tidak aktif",
                    "Tidak ada kata terlambat untuk memulai lagi."
                )
            }) {
                Text("Streak 5+")
            }
        }
    }
}

// ================= HEADER =================

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

// ================= 🔥 DAILY STREAK =================

@Composable
fun DailyStreakSection(onOpen: () -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Daily Commitment 🔥", fontWeight = FontWeight.Bold)
            Text(
                "See all →",
                color = Color(0xFF6C63FF),
                modifier = Modifier.clickable { onOpen() }
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(3) {
                StreakFlameCard(
                    title = when (it) {
                        0 -> "Read 10 minutes"
                        1 -> "Stretch body"
                        else -> "No doomscroll"
                    },
                    days = listOf(1, 3, 7)[it],
                    doneToday = it != 1
                )
            }

            item {
                AddStreakCard()
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            "Gapapa kalau belum. Kita lanjut hari ini 💙",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun StreakFlameCard(
    title: String,
    days: Int,
    doneToday: Boolean
) {
    val pulse by rememberInfiniteTransition().animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            tween(1200),
            RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(140.dp)
            .scale(if (days >= 7) pulse else 1f),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFA726),
                            Color(0xFFFF7043)
                        )
                    )
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            Column {
                Text("🔥 $days days", color = Color.White)
                Text(
                    if (doneToday) "Done today ✅" else "Pending ⏳",
                    color = Color.White.copy(0.9f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun AddStreakCard() {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(140.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable { },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Add, null, tint = Color(0xFF6C63FF))
            Spacer(Modifier.height(6.dp))
            Text("New", color = Color(0xFF6C63FF))
        }
    }
}

// ================= FEATURE SLIDER =================

@Composable
fun FeatureSlider() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(3) {
            FeatureCardAnimated(it)
        }
    }
}

// ================= QUICK MENU =================

@Composable
fun QuickMenu(navController: NavController) {
    Text(
        "Quick Access",
        modifier = Modifier.padding(horizontal = 16.dp),
        fontWeight = FontWeight.Bold
    )

    val scrollState = rememberScrollState() // 🔥 State untuk nginget posisi geser

    Spacer(Modifier.height(12.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState) // 🔥 Ini yang bikin bisa digeser ke kanan
            .padding(horizontal = 16.dp), // Kasih padding dikit biar nggak nempel tembok pas di-scroll
        horizontalArrangement = Arrangement.spacedBy(24.dp) // 🔥 Atur jarak antar menu biar lega
    ) {
        MenuIconAnimated("Focus", Icons.Default.CenterFocusStrong) {
            navController.navigate("focus")
        }
        MenuIconAnimated("Streak", Icons.Default.LocalFireDepartment) {
            navController.navigate("streak")
        }
        MenuIconAnimated("Notes", Icons.Default.Description) {
            navController.navigate("notes_list")
        }
        MenuIconAnimated("Life", Icons.Default.Dashboard) {
            navController.navigate("life_area")
        }
        MenuIconAnimated("Stats", Icons.Default.BarChart) {
            navController.navigate("analytics")
        }
    }
}

// ================= REUSABLE =================

@Composable
fun FeatureCardAnimated(index: Int) {
    Card(
        modifier = Modifier.width(260.dp).height(140.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF9D50BB), Color(0xFF6E48AA))
                    )
                )
                .padding(18.dp)
        ) {
            Text(
                when (index) {
                    0 -> "Stay Focused"
                    1 -> "Track Mood"
                    else -> "Balance Life"
                },
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MenuIconAnimated(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Color(0xFF6C63FF))
        }
        Spacer(Modifier.height(6.dp))
        Text(title)
    }
}

// ================= TASK PREVIEW =================

@Composable
fun TaskPreviewCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Today's Tasks", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("• Finish dashboard UI")
            Text("• Add animation")
            Spacer(Modifier.height(10.dp))
            Text(
                "See All →",
                color = Color(0xFF6C63FF),
                modifier = Modifier.clickable { onClick() }
            )
        }
    }
}
