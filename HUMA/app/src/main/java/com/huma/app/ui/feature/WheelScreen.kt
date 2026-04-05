package com.huma.app.ui.feature

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelScreen() {
    val scope = rememberCoroutineScope()
    var useCustom by remember { mutableStateOf(false) }
    var customInputText by remember { mutableStateOf("") }
    var showManageSheet by remember { mutableStateOf(false) }

    // State untuk list harian agar bisa dihapus/reset
    var dailyChallengesState by remember {
        mutableStateOf(DailyWheelGenerator.generateDailyWheel())
    }

    val challenges = if (useCustom) {
        if (CustomWheelRepository.customChallenges.isEmpty()) listOf("Tambahkan tantangan!")
        else CustomWheelRepository.customChallenges
    } else {
        dailyChallengesState
    }

    // STATE ROTASI TUNGGAL (Seamless)
    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf<String?>(null) }
    var pendingResult by remember { mutableStateOf<String?>(null) }

    // 1. Animasi Idle (Muter pelan hanya jika tidak sedang spin & tidak ada popup)
    LaunchedEffect(isSpinning, resultText) {
        if (!isSpinning && resultText == null) {
            while (true) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(durationMillis = 25000, easing = LinearEasing)
                )
            }
        }
    }

    // 2. Animasi Pulse (Kedut-kedut halus)
    val infiniteTransition = rememberInfiniteTransition(label = "PulseAnim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF6F7FB), Color(0xFFEDEBFF))))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header & Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Wheel of Action 🎡",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A4A4A)
            )

            if (!useCustom) {
                IconButton(onClick = {
                    dailyChallengesState = DailyWheelGenerator.generateDailyWheel()
                }, enabled = !isSpinning) {
                    Icon(Icons.Default.Refresh, "Reset", tint = Color(0xFF6C63FF))
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Selector Mode
        Row(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                .padding(4.dp)
        ) {
            FilterChip(
                selected = !useCustom,
                onClick = { useCustom = false },
                label = { Text("Daily Random") },
                border = null,
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF6C63FF), selectedLabelColor = Color.White)
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = useCustom,
                onClick = { useCustom = true },
                label = { Text("My Custom") },
                border = null,
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF6C63FF), selectedLabelColor = Color.White)
            )
        }

        Spacer(Modifier.height(20.dp))

        // Input Custom & Manage Button
        if (useCustom) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = customInputText,
                    onValueChange = { customInputText = it },
                    placeholder = { Text("Ketik tantangan...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (customInputText.isNotBlank()) {
                                CustomWheelRepository.addChallenge(customInputText)
                                customInputText = ""
                            }
                        }) {
                            Icon(Icons.Default.Add, "Add", tint = Color(0xFF6C63FF))
                        }
                    }
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { showManageSheet = true },
                    modifier = Modifier.background(Color.White, CircleShape)
                ) {
                    Icon(Icons.Default.List, "List", tint = Color(0xFF6C63FF))
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        // Box Roda
        Box(
            modifier = Modifier
                .weight(1f)
                .scale(if (isSpinning) 1f else pulseScale),
            contentAlignment = Alignment.Center
        ) {
            Surface(modifier = Modifier.size(310.dp), shape = CircleShape, color = Color.Black.copy(alpha = 0.05f)) {}

            Wheel(challenges = challenges, rotation = rotation.value)

            Pointer()

            Surface(
                shape = CircleShape, color = Color.White, shadowElevation = 8.dp, modifier = Modifier.size(65.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { Text("🎡", fontSize = 28.sp) }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Button SPIN
        Button(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
            enabled = !isSpinning && challenges.isNotEmpty(),
            onClick = {
                scope.launch {
                    isSpinning = true
                    val currentRotation = rotation.value
                    val extraRotation = 360f * 8 + Random.nextInt(360)
                    val targetRotation = currentRotation + extraRotation

                    val degreePerItem = 360f / challenges.size
                    val pointerAngle = 270f
                    val normalizedFinalRotation = (targetRotation % 360)
                    val index = (((pointerAngle - normalizedFinalRotation + 360) % 360) / degreePerItem).toInt()
                    pendingResult = challenges[index % challenges.size]

                    rotation.animateTo(
                        targetValue = targetRotation,
                        animationSpec = tween(durationMillis = 4500, easing = FastOutSlowInEasing)
                    )

                    resultText = pendingResult
                    isSpinning = false
                }
            }
        ) {
            if (isSpinning) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text("SPINNING...", fontWeight = FontWeight.Bold)
            } else {
                Text("SPIN NOW", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    // Modal Bottom Sheet untuk Manage Daftar Custom
    if (showManageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showManageSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Daftar Custom", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { CustomWheelRepository.customChallenges.clear() }) {
                        Text("Hapus Semua", color = Color.Red)
                    }
                }
                Spacer(Modifier.height(16.dp))

                if (CustomWheelRepository.customChallenges.isEmpty()) {
                    Text("Belum ada tantangan custom.", color = Color.Gray, modifier = Modifier.padding(vertical = 20.dp).align(Alignment.CenterHorizontally))
                }

                LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                    items(CustomWheelRepository.customChallenges) { item ->
                        ListItem(
                            headlineContent = { Text(item) },
                            trailingContent = {
                                IconButton(onClick = { CustomWheelRepository.removeChallenge(item) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = Color.Gray)
                                }
                            }
                        )
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    ResultPopup(
        result = resultText,
        onDismiss = { resultText = null },
        onDelete = { item ->
            if (useCustom) CustomWheelRepository.removeChallenge(item)
            else dailyChallengesState = dailyChallengesState.filter { it != item }
        }
    )
}
