package com.huma.app.ui.screen.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
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
import com.huma.app.viewmodel.CommitmentViewModel
import com.huma.app.ui.feature.isStreakBroken
import com.huma.app.ui.feature.LayeredFireIcon
import java.text.SimpleDateFormat
import kotlinx.coroutines.launch
import com.huma.app.data.local.PreferenceManager
import com.huma.app.ui.screen.settings.SettingsCategory
import com.huma.app.ui.screen.settings.ThemeOption
import com.huma.app.ui.screen.settings.SwitchOption
import com.huma.app.ui.screen.settings.ClickableOption
import com.huma.app.ui.screen.settings.InfoRow
import com.huma.app.ui.screen.settings.getCacheSize
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.huma.app.ui.notification.NotificationScheduler

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(
    navController: NavController,
    taskViewModel: TaskViewModel,
    commitmentViewModel: CommitmentViewModel,
    onThemeChanged: (Int) -> Unit
) {
    val todayTasks by taskViewModel.todayTasks.collectAsState()
    val upcomingGrouped by taskViewModel.upcomingGrouped.collectAsState()
    val commitments by commitmentViewModel.allCommitments.collectAsState()
    var showDoneTasks by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // State untuk Sidebar
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val prefManager = remember { PreferenceManager(context) }

    // State Lokal untuk UI Responsif Drawer Settings
    var themeModeState by remember { mutableIntStateOf(prefManager.themeMode) }
    var isNotifEnabledState by remember { mutableStateOf(prefManager.isNotifEnabled) }
    var isNotifSoundEnabledState by remember { mutableStateOf(prefManager.isNotifSoundEnabled) }
    var isNotifVibrateEnabledState by remember { mutableStateOf(prefManager.isNotifVibrateEnabled) }
    var isGreetingNotifEnabledState by remember { mutableStateOf(prefManager.isGreetingNotifEnabled) }
    var isStreakNotifEnabledState by remember { mutableStateOf(prefManager.isStreakNotifEnabled) }
    var isStreakMiss1EnabledState by remember { mutableStateOf(prefManager.isStreakMiss1Enabled) }
    var isStreakMiss5EnabledState by remember { mutableStateOf(prefManager.isStreakMiss5Enabled) }
    var isStreakMiss7EnabledState by remember { mutableStateOf(prefManager.isStreakMiss7Enabled) }
    var languageState by remember { mutableStateOf(prefManager.language) }
    var cacheSizeState by remember { mutableStateOf(getCacheSize(context)) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }

    val isDark = when (themeModeState) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(if (languageState == "en") "Privacy Policy" else "Kebijakan Privasi") },
            text = { Text(if (languageState == "en") "Privacy policy is under construction. It will be available in the next update." else "Kebijakan privasi sedang dalam proses penyusunan. Akan segera tersedia di update berikutnya.") },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // FAQ Dialog
    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            title = { Text("FAQ") },
            text = {
                Column {
                    Text(if (languageState == "en") "Q: How to use Streak?" else "Q: Bagaimana cara menggunakan Streak?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(if (languageState == "en") "A: Open the Streak menu, swipe the flint stone, write your commitment, and ignite it!" else "A: Buka menu Streak, gesek batu api, tulis niatmu, dan nyalakan!", fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(if (languageState == "en") "Q: Notifications not showing?" else "Q: Notifikasi tidak muncul?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(if (languageState == "en") "A: Make sure notifications are enabled in Settings and allow HUMA notifications in system settings." else "A: Pastikan notifikasi diaktifkan di Settings dan izinkan notifikasi HUMA di pengaturan sistem.", fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(if (languageState == "en") "Q: What is Protection in Streak?" else "Q: Apa itu Protection di Streak?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(if (languageState == "en") "A: Protection is a streak shield obtained after 25 consecutive days. It protects you from losing your streak if you miss 1 day." else "A: Protection adalah perlindungan streak yang didapat setelah 25 hari berturut-turut. Melindungimu dari kehilangan streak jika melewatkan 1 hari.", fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showFaqDialog = false }) {
                    Text(if (languageState == "en") "Close" else "Tutup")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxHeight().width(320.dp),
                drawerContainerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF6F7FB)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        if (languageState == "en") "Settings" else "Pengaturan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color.White else Color.Black,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    SettingsCategory(title = if (languageState == "en") "🌙 Theme" else "🌙 Tema") {
                        ThemeOption(if (languageState == "en") "Light Mode" else "Light Mode", themeModeState == 1) { 
                            prefManager.themeMode = 1
                            themeModeState = 1
                            onThemeChanged(1)
                        }
                        ThemeOption(if (languageState == "en") "Dark Mode" else "Dark Mode", themeModeState == 2) { 
                            prefManager.themeMode = 2
                            themeModeState = 2
                            onThemeChanged(2)
                        }
                        ThemeOption(if (languageState == "en") "System Default" else "System Default", themeModeState == 0) { 
                            prefManager.themeMode = 0
                            themeModeState = 0
                            onThemeChanged(0)
                        }
                    }

                    SettingsCategory(title = if (languageState == "en") "🔔 Notifications" else "🔔 Notifikasi") {
                        SwitchOption(if (languageState == "en") "Enable Notifications" else "Aktifkan Notifikasi", isNotifEnabledState) {
                            prefManager.isNotifEnabled = it
                            isNotifEnabledState = it
                            if (it) {
                                NotificationScheduler.scheduleAll(context)
                            } else {
                                NotificationScheduler.cancelGreeting(context)
                                NotificationScheduler.cancelStreakReminder(context)
                            }
                        }
                        if (isNotifEnabledState) {
                            SwitchOption(if (languageState == "en") "Sound" else "Suara", isNotifSoundEnabledState) {
                                prefManager.isNotifSoundEnabled = it
                                isNotifSoundEnabledState = it
                            }
                            SwitchOption(if (languageState == "en") "Vibrate" else "Getar", isNotifVibrateEnabledState) {
                                prefManager.isNotifVibrateEnabled = it
                                isNotifVibrateEnabledState = it
                            }
                            HorizontalDivider(Modifier.padding(vertical = 4.dp), color = if (isDark) Color.DarkGray else Color.LightGray)
                            SwitchOption(if (languageState == "en") "Morning Greeting" else "Greeting Pagi", isGreetingNotifEnabledState) {
                                prefManager.isGreetingNotifEnabled = it
                                isGreetingNotifEnabledState = it
                                if (it) {
                                    NotificationScheduler.scheduleDailyGreeting(context)
                                } else {
                                    NotificationScheduler.cancelGreeting(context)
                                }
                            }
                            SwitchOption(if (languageState == "en") "Daily Streak" else "Streak Harian", isStreakNotifEnabledState) {
                                prefManager.isStreakNotifEnabled = it
                                isStreakNotifEnabledState = it
                                if (it) {
                                    NotificationScheduler.scheduleDailyStreakReminder(context)
                                } else {
                                    NotificationScheduler.cancelStreakReminder(context)
                                }
                            }
                            Text(if (languageState == "en") "Missed Streak Reminder:" else "Pengingat Streak Miss:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
                            SwitchOption(if (languageState == "en") "1 Day Missed" else "Terlewat 1 Hari", isStreakMiss1EnabledState) {
                                prefManager.isStreakMiss1Enabled = it
                                isStreakMiss1EnabledState = it
                            }
                            SwitchOption(if (languageState == "en") "5 Days Missed" else "Terlewat 5 Hari", isStreakMiss5EnabledState) {
                                prefManager.isStreakMiss5Enabled = it
                                isStreakMiss5EnabledState = it
                            }
                            SwitchOption(if (languageState == "en") "7+ Days Missed" else "Terlewat 7+ Hari", isStreakMiss7EnabledState) {
                                prefManager.isStreakMiss7Enabled = it
                                isStreakMiss7EnabledState = it
                            }
                        }
                    }

                    SettingsCategory(title = if (languageState == "en") "🌐 Language" else "🌐 Bahasa") {
                        ThemeOption("Indonesia", languageState == "in") {
                            prefManager.language = "in"
                            languageState = "in"
                            val localeList = LocaleListCompat.forLanguageTags("in")
                            AppCompatDelegate.setApplicationLocales(localeList)
                        }
                        ThemeOption("English", languageState == "en") {
                            prefManager.language = "en"
                            languageState = "en"
                            val localeList = LocaleListCompat.forLanguageTags("en")
                            AppCompatDelegate.setApplicationLocales(localeList)
                        }
                    }

                    SettingsCategory(title = if (languageState == "en") "📍 Location" else "📍 Lokasi") {
                        SwitchOption(if (languageState == "en") "Enable Location Access" else "Aktifkan Akses Lokasi", false) { }
                        ClickableOption(if (languageState == "en") "Location Accuracy" else "Akurasi Lokasi", Icons.Default.GpsFixed) { }
                    }

                    SettingsCategory(title = if (languageState == "en") "💾 Storage" else "💾 Penyimpanan") {
                        ClickableOption(if (languageState == "en") "Clear Cache" else "Hapus Cache", Icons.Default.DeleteSweep) {
                            context.cacheDir.deleteRecursively()
                            cacheSizeState = getCacheSize(context)
                            Toast.makeText(context, if (languageState == "en") "Cache cleared!" else "Cache berhasil dihapus!", Toast.LENGTH_SHORT).show()
                        }
                        Text("${if (languageState == "en") "Cache" else "Cache"}: $cacheSizeState", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp))
                    }

                    SettingsCategory(title = if (languageState == "en") "🔒 Privacy" else "🔒 Privasi") {
                        ClickableOption(if (languageState == "en") "Privacy Policy" else "Kebijakan Privasi", Icons.Default.PrivacyTip) {
                            showPrivacyDialog = true
                        }
                        ClickableOption(if (languageState == "en") "App Permissions" else "Izin Aplikasi", Icons.Default.Security) {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    }

                    SettingsCategory(title = if (languageState == "en") "ℹ️ About Application" else "ℹ️ Tentang Aplikasi") {
                        InfoRow(if (languageState == "en") "Version" else "Versi", "1.0.0")
                        InfoRow(if (languageState == "en") "Developer" else "Developer", "HUMA Team")
                    }

                    SettingsCategory(title = if (languageState == "en") "🆘 Help" else "🆘 Bantuan") {
                        ClickableOption("FAQ", Icons.Default.QuestionAnswer) {
                            showFaqDialog = true
                        }
                        ClickableOption(if (languageState == "en") "Contact Developer" else "Hubungi Developer", Icons.Default.Email) {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:huma.team@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "HUMA App - Feedback")
                            }
                            try {
                                context.startActivity(emailIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        ClickableOption(if (languageState == "en") "Report Bug" else "Laporkan Bug", Icons.Default.BugReport) {
                            val bugIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:huma.team@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "HUMA App - Bug Report")
                                putExtra(Intent.EXTRA_TEXT, if (languageState == "en") "Please describe the bug:\n\n" else "Deskripsikan bug yang ditemukan:\n\n")
                            }
                            try {
                                context.startActivity(bugIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF121212) else Color(0xFFF6F7FB))
                .verticalScroll(rememberScrollState())
        ) {
            HeaderSection(isDark = isDark, onMenuClick = { scope.launch { drawerState.open() } })
            Spacer(Modifier.height(16.dp))

            DailyCommitmentSection(
                isDark = isDark,
                commitments = commitments,
                onOpen = { navController.navigate("commitments") },
                onAddNew = { navController.navigate("add_commitment") }
            )

            Spacer(Modifier.height(22.dp))
            FeatureSlider(navController)
            Spacer(Modifier.height(26.dp))
            QuickMenu(navController, isDark = isDark)
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
                color = if (isDark) Color(0xFFFFB74D) else Color(0xFF6C63FF),
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
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HeaderSection(isDark: Boolean, onMenuClick: () -> Unit) {
    val today = LocalDate.now()
    val dayName = today.format(DateTimeFormatter.ofPattern("EEEE", Locale("id", "ID")))
    val fullDate = today.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID")))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF87CEEB), Color(0x8CE4FF))))
    ) {
        // Logo / Settings Button di Pojok Kanan Atas
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Pengaturan",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // Decorative Half Circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = Color.White.copy(alpha = 0.15f),
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(size.width * 0.1f, -size.height * 0.45f),
                size = size.copy(width = size.width * 0.8f, height = size.height * 0.9f)
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(text = "$dayName, $fullDate", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(8.dp))
            Text("Hi, Human! 👋", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Light)
            Text("Make Every\nDay Count", color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, lineHeight = 40.sp)

            Spacer(Modifier.height(16.dp))
            Surface(
                color = if (isDark) Color(0xFF37474F).copy(alpha = 0.2f) else Color(0xFF37474F).copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else Color(0xFF37474F).copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FormatQuote, null, tint = if (isDark) Color.LightGray else Color(0xFF64748B), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Success is the sum of small efforts.", color = if (isDark) Color.LightGray else Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
        }
    }
}

@Composable
fun DailyCommitmentSection(
    isDark: Boolean,
    commitments: List<CommitmentEntity>,
    onOpen: () -> Unit,
    onAddNew: () -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Commitment 🔥", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = if (isDark) Color.White else Color.Black)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onOpen() }) {
                Text("See All", color = Color(0xFFFFA726), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFFFFA726), modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(end = 16.dp)) {
            items(commitments) { commitment ->
                CommitmentFlameCardDashboard(isDark = isDark, commitment = commitment, onClick = onOpen)
            }
            item { AddCommitmentCardDashboard(isDark = isDark, onClick = onAddNew) }
        }
        Spacer(Modifier.height(12.dp))
        Text("Konsistensi adalah kunci perubahan besar 🧡✨", color = Color.Gray, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CommitmentFlameCardDashboard(isDark: Boolean, commitment: CommitmentEntity, onClick: () -> Unit) {
    val orenColor = Color(0xFFFFA726)
    val actualElemColor = Color(android.graphics.Color.parseColor(commitment.colorHex))
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isBroken = isStreakBroken(commitment)
    val isDoneToday = commitment.completedDays.contains(today)

    Card(
        modifier = Modifier.width(195.dp).height(155.dp).clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isBroken) 1.dp else 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                if (isDark) listOf(Color(0xFF1E1E1E), Color(0xFF2D2010)) 
                else listOf(Color.White, Color(0xFFFFF3E0))
            )
        )) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 30.dp)
                    .alpha(if (isBroken) 0.05f else 0.15f)
                    .scale(2.2f)
                    .rotate(-15f)
            ) {
                LayeredFireIcon(baseColor = orenColor)
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(18.dp).alpha(if (isBroken) 0.5f else 1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(38.dp).background(actualElemColor.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when(commitment.iconType) {
                                "FIRE" -> Icons.Default.LocalFireDepartment
                                "WATER" -> Icons.Default.WaterDrop
                                "LEAF" -> Icons.Default.Eco
                                "STAR" -> Icons.Default.AutoAwesome
                                else -> Icons.Default.Favorite
                            },
                            contentDescription = null,
                            tint = actualElemColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(commitment.title, fontWeight = FontWeight.ExtraBold, maxLines = 1, fontSize = 16.sp, color = if (isDark) Color.White else Color(0xFF2D3436))
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Whatshot, null, tint = if (isBroken) Color.Gray else actualElemColor, modifier = Modifier.size(16.dp))
                        Text(" ${commitment.currentStreak} Streak", color = if (isBroken) Color.Gray else actualElemColor, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            List(5) { index ->
                                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(4 - index)) }
                                    val dateStr = sdf.format(cal.time)
                                    val isComp = commitment.completedDays.contains(dateStr)
                                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(if (isComp) actualElemColor else Color.LightGray.copy(alpha = 0.3f)))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(65.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isBroken) Color.Red.copy(alpha = 0.1f) 
                                    else if (isDoneToday) (if (isDark) Color(0xFF1B5E20) else Color(0xFFE8F5E9)) 
                                    else (if (isDark) Color.DarkGray else Color(0xFFF0F0F0))
                                )
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBroken) "BROKEN" else if (isDoneToday) "DONE" else "ACTION", 
                                color = if (isBroken) Color.Red else if (isDoneToday) (if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)) else Color.Gray, 
                                fontWeight = FontWeight.Black, 
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddCommitmentCardDashboard(isDark: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(100.dp).height(155.dp).clickable { onClick() }, 
        shape = RoundedCornerShape(28.dp), 
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White), 
        border = BorderStroke(1.dp, if (isDark) Color.DarkGray.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.AddCircle, null, tint = Color(0xFFFFA726), modifier = Modifier.size(32.dp))
            Text("NEW", color = Color(0xFFFFA726), fontWeight = FontWeight.Black, fontSize = 12.sp)
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
fun QuickMenu(navController: NavController, isDark: Boolean) {
    Text("Quick Access", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
    val scrollState = rememberScrollState()
    Spacer(Modifier.height(12.dp))
    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        MenuIconAnimated("Focus", Icons.Default.CenterFocusStrong, isDark) { navController.navigate("focus") }
        MenuIconAnimated("Streak", Icons.Default.LocalFireDepartment, isDark) { navController.navigate("streak") }
        MenuIconAnimated("Notes", Icons.Default.Description, isDark) { navController.navigate("notes_list") }
        MenuIconAnimated("Life", Icons.Default.Dashboard, isDark) { navController.navigate("life_area") }
        MenuIconAnimated("Stats", Icons.Default.BarChart, isDark) { navController.navigate("analytics") }
    }
}

@Composable
fun MenuIconAnimated(title: String, icon: ImageVector, isDark: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(56.dp).background(if (isDark) Color(0xFF1E1E1E) else Color.White, CircleShape), contentAlignment = Alignment.Center) { 
            Icon(icon, null, tint = if (isDark) Color(0xFFFFB74D) else Color(0xFF6C63FF)) 
        }
        Spacer(Modifier.height(6.dp))
        Text(title, color = if (isDark) Color.White else Color.Black)
    }
}
