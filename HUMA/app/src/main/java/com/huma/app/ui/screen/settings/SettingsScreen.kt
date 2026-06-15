package com.huma.app.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import com.huma.app.data.local.PreferenceManager
import com.huma.app.ui.notification.NotificationScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefManager = remember { PreferenceManager(context) }
    val scrollState = rememberScrollState()

    // State Lokal untuk UI Responsif
    var themeMode by remember { mutableIntStateOf(prefManager.themeMode) }
    var isNotifEnabled by remember { mutableStateOf(prefManager.isNotifEnabled) }
    var isNotifSoundEnabled by remember { mutableStateOf(prefManager.isNotifSoundEnabled) }
    var isNotifVibrateEnabled by remember { mutableStateOf(prefManager.isNotifVibrateEnabled) }
    var isGreetingNotifEnabled by remember { mutableStateOf(prefManager.isGreetingNotifEnabled) }
    var isStreakNotifEnabled by remember { mutableStateOf(prefManager.isStreakNotifEnabled) }
    var isStreakMissNotifEnabled by remember { mutableStateOf(prefManager.isStreakMissNotifEnabled) }
    var isStreakMiss1Enabled by remember { mutableStateOf(prefManager.isStreakMiss1Enabled) }
    var isStreakMiss5Enabled by remember { mutableStateOf(prefManager.isStreakMiss5Enabled) }
    var isStreakMiss7Enabled by remember { mutableStateOf(prefManager.isStreakMiss7Enabled) }
    var language by remember { mutableStateOf(prefManager.language) }
    var cacheSize by remember { mutableStateOf(getCacheSize(context)) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Kebijakan Privasi") },
            text = { Text("Kebijakan privasi sedang dalam proses penyusunan. Akan segera tersedia di update berikutnya.") },
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
                    Text("Q: Bagaimana cara menggunakan Streak?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("A: Buka menu Streak, gesek batu api, tulis niatmu, dan nyalakan!", fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Q: Notifikasi tidak muncul?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("A: Pastikan notifikasi diaktifkan di Settings dan izinkan notifikasi HUMA di pengaturan sistem.", fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Q: Apa itu Protection di Streak?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("A: Protection adalah perlindungan streak yang didapat setelah 25 hari berturut-turut. Melindungimu dari kehilangan streak jika melewatkan 1 hari.", fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showFaqDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (language == "en") "Settings" else "Pengaturan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
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
            // ===== TEMA =====
            SettingsCategory(title = if (language == "en") "🌙 Theme" else "🌙 Tema") {
                ThemeOption(if (language == "en") "Light Mode" else "Light Mode", themeMode == 1) { 
                    prefManager.themeMode = 1
                    themeMode = 1
                }
                ThemeOption(if (language == "en") "Dark Mode" else "Dark Mode", themeMode == 2) { 
                    prefManager.themeMode = 2
                    themeMode = 2
                }
                ThemeOption(if (language == "en") "System Default" else "System Default", themeMode == 0) { 
                    prefManager.themeMode = 0
                    themeMode = 0
                }
            }

            // ===== NOTIFIKASI =====
            SettingsCategory(title = if (language == "en") "🔔 Notifications" else "🔔 Notifikasi") {
                SwitchOption(
                    if (language == "en") "Enable Notifications" else "Aktifkan Notifikasi",
                    isNotifEnabled
                ) { 
                    prefManager.isNotifEnabled = it
                    isNotifEnabled = it
                    // Connect ke sistem penjadwalan
                    if (it) {
                        NotificationScheduler.scheduleAll(context)
                    } else {
                        NotificationScheduler.cancelGreeting(context)
                        NotificationScheduler.cancelStreakReminder(context)
                    }
                }
                
                if (isNotifEnabled) {
                    SwitchOption(
                        if (language == "en") "Sound" else "Suara",
                        isNotifSoundEnabled
                    ) { 
                        prefManager.isNotifSoundEnabled = it
                        isNotifSoundEnabled = it
                    }
                    SwitchOption(
                        if (language == "en") "Vibrate" else "Getar",
                        isNotifVibrateEnabled
                    ) { 
                        prefManager.isNotifVibrateEnabled = it
                        isNotifVibrateEnabled = it
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    Text(
                        if (language == "en") "Specific Features:" else "Fitur Spesifik:",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    
                    SwitchOption(
                        if (language == "en") "Morning Greeting" else "Greeting Pagi",
                        isGreetingNotifEnabled
                    ) { 
                        prefManager.isGreetingNotifEnabled = it
                        isGreetingNotifEnabled = it
                        // Connect ke alarm scheduler
                        if (it) {
                            NotificationScheduler.scheduleDailyGreeting(context)
                        } else {
                            NotificationScheduler.cancelGreeting(context)
                        }
                    }
                    SwitchOption(
                        if (language == "en") "Daily Streak" else "Streak Harian",
                        isStreakNotifEnabled
                    ) { 
                        prefManager.isStreakNotifEnabled = it
                        isStreakNotifEnabled = it
                        // Connect ke alarm scheduler
                        if (it) {
                            NotificationScheduler.scheduleDailyStreakReminder(context)
                        } else {
                            NotificationScheduler.cancelStreakReminder(context)
                        }
                    }
                    SwitchOption(
                        if (language == "en") "Missed Streak Reminder" else "Pengingat Streak Terlewat",
                        isStreakMissNotifEnabled
                    ) { 
                        prefManager.isStreakMissNotifEnabled = it
                        isStreakMissNotifEnabled = it
                    }

                    if (isStreakMissNotifEnabled) {
                        Column(modifier = Modifier.padding(start = 24.dp)) {
                            SwitchOption(
                                if (language == "en") "1 Day Missed" else "Terlewat 1 Hari",
                                isStreakMiss1Enabled
                            ) {
                                prefManager.isStreakMiss1Enabled = it
                                isStreakMiss1Enabled = it
                            }
                            SwitchOption(
                                if (language == "en") "5 Days Missed" else "Terlewat 5 Hari",
                                isStreakMiss5Enabled
                            ) {
                                prefManager.isStreakMiss5Enabled = it
                                isStreakMiss5Enabled = it
                            }
                            SwitchOption(
                                if (language == "en") "7+ Days Missed" else "Terlewat 7+ Hari",
                                isStreakMiss7Enabled
                            ) {
                                prefManager.isStreakMiss7Enabled = it
                                isStreakMiss7Enabled = it
                            }
                        }
                    }
                }
            }

            // ===== BAHASA =====
            SettingsCategory(title = if (language == "en") "🌐 Language" else "🌐 Bahasa") {
                ThemeOption("Indonesia", language == "in") { 
                    prefManager.language = "in"
                    language = "in"
                    // Apply locale change
                    val localeList = LocaleListCompat.forLanguageTags("in")
                    AppCompatDelegate.setApplicationLocales(localeList)
                }
                ThemeOption("English", language == "en") { 
                    prefManager.language = "en"
                    language = "en"
                    // Apply locale change
                    val localeList = LocaleListCompat.forLanguageTags("en")
                    AppCompatDelegate.setApplicationLocales(localeList)
                }
            }

            // ===== PENYIMPANAN =====
            SettingsCategory(title = if (language == "en") "💾 Storage" else "💾 Penyimpanan") {
                ClickableOption(
                    if (language == "en") "Clear Cache" else "Hapus Cache",
                    Icons.Default.DeleteSweep
                ) {
                    context.cacheDir.deleteRecursively()
                    cacheSize = getCacheSize(context)
                    Toast.makeText(
                        context,
                        if (language == "en") "Cache cleared!" else "Cache berhasil dihapus!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Text(
                    "${if (language == "en") "Cache size" else "Ukuran cache"}: $cacheSize",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
                    color = Color.Gray
                )
            }

            // ===== PRIVASI =====
            SettingsCategory(title = if (language == "en") "🔒 Privacy" else "🔒 Privasi") {
                ClickableOption(
                    if (language == "en") "Privacy Policy" else "Kebijakan Privasi",
                    Icons.Default.PrivacyTip
                ) {
                    showPrivacyDialog = true
                }
                ClickableOption(
                    if (language == "en") "App Permissions" else "Izin Aplikasi",
                    Icons.Default.Security
                ) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            }

            // ===== TENTANG =====
            SettingsCategory(title = if (language == "en") "ℹ️ About" else "ℹ️ Tentang") {
                InfoRow(if (language == "en") "Version" else "Versi", "1.0.0")
                InfoRow(if (language == "en") "Team" else "Tim", "HUMA Team")
            }

            // ===== BANTUAN =====
            SettingsCategory(title = if (language == "en") "🆘 Help" else "🆘 Bantuan") {
                ClickableOption("FAQ", Icons.Default.QuestionAnswer) {
                    showFaqDialog = true
                }
                ClickableOption(
                    if (language == "en") "Contact Developer" else "Hubungi Developer",
                    Icons.Default.Email
                ) {
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
                ClickableOption(
                    if (language == "en") "Report Bug" else "Laporkan Bug",
                    Icons.Default.BugReport
                ) {
                    val bugIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:huma.team@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "HUMA App - Bug Report")
                        putExtra(Intent.EXTRA_TEXT, if (language == "en") "Please describe the bug:\n\n" else "Deskripsikan bug yang ditemukan:\n\n")
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

@Composable
fun SettingsCategory(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF6C63FF), modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column { content() }
        }
    }
}

@Composable
fun ThemeOption(text: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text, modifier = Modifier.padding(start = 12.dp), color = Color.Black, fontSize = 15.sp)
    }
}

@Composable
fun SwitchOption(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                role = Role.Switch
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, fontSize = 15.sp, color = Color.Black)
        Switch(
            checked = checked, 
            onCheckedChange = null, // Toggleable modifier pada Row yang menangani
            modifier = Modifier.scale(0.85f)
        )
    }
}

@Composable
fun ClickableOption(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        Text(text, modifier = Modifier.padding(start = 12.dp), fontSize = 15.sp, color = Color.Black)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 15.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.Black)
    }
}

fun getCacheSize(context: Context): String {
    return try {
        val size = context.cacheDir.walk().map { it.length() }.sum()
        "%.2f MB".format(size / (1024f * 1024f))
    } catch (e: Exception) {
        "0.00 MB"
    }
}
