package com.huma.app.ui.feature

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huma.app.data.local.CapsuleEntity
import com.huma.app.viewmodel.CapsuleViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeCapsuleScreen(viewModel: CapsuleViewModel = viewModel()) {
    val capsules by viewModel.allCapsules.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var capsuleToDelete by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Time Capsule ⏳", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF6C63FF),
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Default.Add, contentDescription = "Add Capsule") }
        },
        containerColor = Color(0xFFF6F7FB)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Brush.verticalGradient(listOf(Color(0xFFF6F7FB), Color(0xFFEDEBFF))))
            ) {
                if (capsules.isEmpty()) {
                    EmptyCapsuleView()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(capsules, key = { it.id }) { capsule ->
                            SwipeToRevealDelete(
                                capsule = capsule,
                                onDeleteRequest = { capsuleToDelete = capsule.id }
                            ) {
                                CapsuleCard(capsule, viewModel)
                            }
                        }
                    }
                }
            }

            if (capsuleToDelete != null) {
                AlertDialog(
                    onDismissRequest = { capsuleToDelete = null },
                    title = { Text("Hapus Kapsul? 🗑️") },
                    text = { Text("Pesan ini akan hilang selamanya. Yakin?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                capsuleToDelete?.let { viewModel.deleteCapsule(it) }
                                capsuleToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("Hapus") }
                    },
                    dismissButton = {
                        TextButton(onClick = { capsuleToDelete = null }) { Text("Batal") }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddCapsuleDialog(
            onDismiss = { showAddDialog = false },
            onSave = { content, days ->
                viewModel.saveCapsule(content, days)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeToRevealDelete(
    capsule: CapsuleEntity,
    onDeleteRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val deleteBtnWidth = 80.dp
    val deleteBtnWidthPx = with(density) { deleteBtnWidth.toPx() }

    // Gunakan constructor yang sesuai dengan versi Compose kamu
    val state = remember {
        AnchoredDraggableState<DragValue>(
            initialValue = DragValue.Settled,
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = spring(),
            decayAnimationSpec = exponentialDecay()
        )
    }

    // Update anchors saat lebar tombol delete diketahui
    LaunchedEffect(deleteBtnWidthPx) {
        state.updateAnchors(
            DraggableAnchors {
                DragValue.Settled at 0f
                DragValue.Revealed at -deleteBtnWidthPx
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFFFEBEE))
    ) {
        // Tombol Delete di background (hanya muncul saat digeser)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(deleteBtnWidth)
                .fillMaxHeight()
                .background(Color.Red)
                .clickable { onDeleteRequest() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
        }

        // Card utama yang digeser
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    val offset = try { state.requireOffset() } catch (e: Exception) { 0f }
                    IntOffset(x = offset.roundToInt(), y = 0)
                }
                .anchoredDraggable(state, Orientation.Horizontal)
        ) {
            content()
        }
    }
}

enum class DragValue { Settled, Revealed }

@Composable
fun CapsuleCard(capsule: CapsuleEntity, viewModel: CapsuleViewModel) {
    val currentTime = System.currentTimeMillis()
    val isLocked = currentTime < capsule.openAt && !capsule.isOpened
    var showConfetti by remember { mutableStateOf(false) }
    var isBouncing by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isBouncing) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        finishedListener = { isBouncing = false },
        label = "Bouncy"
    )

    Box(contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth().scale(scale),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                            .background(if (isLocked) Color(0xFFF2F2F2) else Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (isLocked) Color.Gray else Color(0xFF4CAF50)
                        )
                    }

                    Text(
                        text = if (isLocked) "Locked" else if (capsule.isOpened) "Memories" else "Ready",
                        color = if (isLocked) Color.Gray else Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.background(if (isLocked) Color(0xFFF2F2F2) else Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                AnimatedContent(targetState = isLocked, label = "Content") { targetIsLocked ->
                    if (targetIsLocked) {
                        LockedContent(capsule, currentTime)
                    } else {
                        UnlockedContent(capsule, viewModel) {
                            isBouncing = true
                            showConfetti = true
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "Created on ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(capsule.createdAt))}",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }
        }

        if (showConfetti) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ConfettiEffect { showConfetti = false }
            }
        }
    }
}

@Composable
fun ConfettiEffect(onFinished: () -> Unit) {
    val particles = remember { List(80) { Particle() } }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, tween(1800, easing = LinearOutSlowInEasing))
        onFinished()
    }

    Canvas(modifier = Modifier.size(300.dp)) {
        particles.forEach { p ->
            val progress = animProgress.value
            val distance = p.speed * progress
            val x = center.x + Math.cos(p.angle) * distance
            val y = center.y + Math.sin(p.angle) * distance + (progress * progress * 400f) // gravity effect

            rotate(p.rotation * progress) {
                drawRect(
                    color = p.color.copy(alpha = (1f - progress).coerceIn(0f, 1f)),
                    topLeft = Offset(x.toFloat(), y.toFloat()),
                    size = androidx.compose.ui.geometry.Size(p.size, p.size)
                )
            }
        }
    }
}

class Particle {
    val angle = Random.nextDouble(0.0, Math.PI * 2)
    val speed = Random.nextFloat() * 400f + 100f
    val size = Random.nextFloat() * 12f + 6f
    val color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
    val rotation = Random.nextFloat() * 720f
}

@Composable
fun LockedContent(capsule: CapsuleEntity, currentTime: Long) {
    Column {
        val remainingMillis = capsule.openAt - currentTime
        val days = TimeUnit.MILLISECONDS.toDays(remainingMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingMillis) % 24

        Text("Dapat dibuka dalam:", fontSize = 14.sp, color = Color.Gray)
        Text("$days Hari $hours Jam", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF6C63FF))

        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = {
                val total = (capsule.openAt - capsule.createdAt).toFloat().coerceAtLeast(1f)
                val passed = (currentTime - capsule.createdAt).toFloat()
                (passed / total).coerceIn(0f, 1f)
            },
            modifier = Modifier.fillMaxWidth().clip(CircleShape).height(8.dp),
            color = Color(0xFF6C63FF),
            trackColor = Color(0xFFEDEBFF)
        )
    }
}

@Composable
fun UnlockedContent(capsule: CapsuleEntity, viewModel: CapsuleViewModel, onOpenAction: () -> Unit) {
    Column {
        if (capsule.isOpened) {
            Text(capsule.content, fontSize = 18.sp, fontWeight = FontWeight.Medium, lineHeight = 26.sp)
            Text("✨ Pesan dari masa lalumu.", fontSize = 12.sp, color = Color(0xFF6C63FF), modifier = Modifier.padding(top = 8.dp))
        } else {
            Text("Waktunya tiba! ⏳", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
            Text("Buka pesan yang kamu kunci dulu...", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
            Button(
                onClick = {
                    onOpenAction()
                    viewModel.markAsOpened(capsule.id)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Buka Kapsul ✨", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun EmptyCapsuleView() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("⏳", fontSize = 60.sp)
        Text("Belum Ada Kapsul", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Tulis pesan untuk dirimu di masa depan.", textAlign = TextAlign.Center, color = Color.Gray, modifier = Modifier.padding(24.dp))
    }
}

@Composable
fun AddCapsuleDialog(onDismiss: () -> Unit, onSave: (String, Int) -> Unit) {
    var text by remember { mutableStateOf("") }
    var days by remember { mutableStateOf(30) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pesan Untuk Masa Depan 💌") },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Apa kabar diriku di masa depan?") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("Kunci selama: $days Hari", fontWeight = FontWeight.Bold)
                Slider(value = days.toFloat(), onValueChange = { days = it.toInt() }, valueRange = 1f..365f)
            }
        },
        confirmButton = {
            Button(onClick = { if (text.isNotBlank()) onSave(text, days) }) { Text("Kunci Kapsul 🔒") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}