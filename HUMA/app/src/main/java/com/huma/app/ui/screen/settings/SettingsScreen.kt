package com.huma.app.ui.screen.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
    var isStreakMiss1Enabled by remember { mutableStateOf(prefManager.isStreakMiss1Enabled) }
    var isStreakMiss5Enabled by remember { mutableStateOf(prefManager.isStreakMiss5Enabled) }
    var isStreakMiss7Enabled by remember { mutableStateOf(prefManager.isStreakMiss7Enabled) }
    var language by remember { mutableStateOf(prefManager.language) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan", fontWeight = FontWeight.Bold) },
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
            SettingsCategory(title = "🌙 Tema") {
                ThemeOption("Light Mode", themeMode == 1) { 
                    prefManager.themeMode = 1
                    themeMode = 1
                }
                ThemeOption("Dark Mode", themeMode == 2) { 
                    prefManager.themeMode = 2
                    themeMode = 2
                }
                ThemeOption("System Default", themeMode == 0) { 
                    prefManager.themeMode = 0
                    themeMode = 0
                }
            }

            SettingsCategory(title = "🔔 Notifikasi") {
                SwitchOption("Aktifkan Notifikasi", isNotifEnabled) { 
                    prefManager.isNotifEnabled = it
                    isNotifEnabled = it
                    // Hubungkan ke sistem penjadwalan
                    if (it) NotificationScheduler.scheduleAll(context)
                }
                
                if (isNotifEnabled) {
                    SwitchOption("Suara", isNotifSoundEnabled) { 
                        prefManager.isNotifSoundEnabled = it
                        isNotifSoundEnabled = it
                    }
                    SwitchOption("Getar", isNotifVibrateEnabled) { 
                        prefManager.isNotifVibrateEnabled = it
                        isNotifVibrateEnabled = it
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    Text("Fitur Spesifik:", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp))
                    
                    SwitchOption("Greeting Pagi", isGreetingNotifEnabled) { 
                        prefManager.isGreetingNotifEnabled = it
                        isGreetingNotifEnabled = it
                    }
                    SwitchOption("Streak Harian", isStreakNotifEnabled) { 
                        prefManager.isStreakNotifEnabled = it
                        isStreakNotifEnabled = it
                    }
                    
                    Text("Pengingat Streak Terlewat:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
                    SwitchOption("1 Hari", isStreakMiss1Enabled) { 
                        prefManager.isStreakMiss1Enabled = it
                        isStreakMiss1Enabled = it
                    }
                    SwitchOption("5 Hari", isStreakMiss5Enabled) { 
                        prefManager.isStreakMiss5Enabled = it
                        isStreakMiss5Enabled = it
                    }
                    SwitchOption("7 Hari", isStreakMiss7Enabled) { 
                        prefManager.isStreakMiss7Enabled = it
                        isStreakMiss7Enabled = it
                    }
                }
            }

            SettingsCategory(title = "🌐 Bahasa") {
                ThemeOption("Indonesia", language == "in") { 
                    prefManager.language = "in"
                    language = "in"
                }
                ThemeOption("English", language == "en") { 
                    prefManager.language = "en"
                    language = "en"
                }
            }

            SettingsCategory(title = "💾 Penyimpanan") {
                ClickableOption("Hapus Cache", Icons.Default.DeleteSweep) {
                    context.cacheDir.deleteRecursively()
                }
                Text("Ukuran cache: ${getCacheSize(context)}", fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp, bottom = 8.dp), color = Color.Gray)
            }

            SettingsCategory(title = "🔒 Privasi") {
                ClickableOption("Kebijakan Privasi", Icons.Default.PrivacyTip) { }
                ClickableOption("Izin Aplikasi", Icons.Default.Security) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            }

            SettingsCategory(title = "ℹ️ Tentang") {
                InfoRow("Versi", "1.0.0")
                InfoRow("Tim", "HUMA Team")
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
