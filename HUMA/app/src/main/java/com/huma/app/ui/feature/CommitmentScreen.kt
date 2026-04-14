package com.huma.app.ui.feature

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as itemsGrid
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.huma.app.data.local.AppDatabase
import com.huma.app.data.local.CommitmentEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommitmentScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = AppDatabase.getInstance(context)
    val dao = db.commitmentDao()
    
    val commitments by dao.getAllCommitments().collectAsState(initial = emptyList())
    var selectedCommitment by remember { mutableStateOf<CommitmentEntity?>(null) }
    var showConfirmComplete by remember { mutableStateOf<CommitmentEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daily Commitment 🚀", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_commitment") },
                containerColor = Color(0xFFFFA726),
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Default.Add, "Add") }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFF9C4), Color.White, Color(0xFFFFE0B2))
                    )
                )
        ) {
            // Background Watermark
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 100.dp, y = 60.dp)
                    .alpha(0.04f)
                    .scale(6f)
                    .rotate(-20f)
            ) {
                LayeredFireIcon(modifier = Modifier.size(150.dp), baseColor = Color(0xFFFF5722))
            }

            Column(modifier = Modifier.padding(padding)) {
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
                                onClick = { selectedCommitment = commitment },
                                onEdit = { navController.navigate("add_commitment?id=${commitment.id}") }
                            )
                        }
                    }
                }
            }

            if (selectedCommitment != null) {
                CommitmentDetailView(
                    commitment = selectedCommitment!!,
                    onDismiss = { selectedCommitment = null },
                    onCompleteRequest = { showConfirmComplete = it },
                    onReset = {
                        scope.launch {
                            val reset = selectedCommitment!!.copy(
                                currentStreak = 0,
                                completedDays = emptyList(),
                                lastCompletedDate = null,
                                isBroken = false
                            )
                            dao.updateCommitment(reset)
                            selectedCommitment = reset
                        }
                    }
                )
            }

            if (showConfirmComplete != null) {
                AlertDialog(
                    onDismissRequest = { showConfirmComplete = null },
                    title = { Text("Ritual Selesai? ✅") },
                    text = { Text("Apakah kamu benar-benar sudah melakukan komitmen \"${showConfirmComplete?.title}\" hari ini?") },
                    confirmButton = {
                        Button(onClick = {
                            scope.launch {
                                val updated = checkAndUpdateStreak(showConfirmComplete!!)
                                dao.updateCommitment(updated)
                                selectedCommitment = updated
                                showConfirmComplete = null
                            }
                        }) { Text("Ya, Sudah!") }
                    },
                    dismissButton = { TextButton(onClick = { showConfirmComplete = null }) { Text("Belum") } }
                )
            }
        }
    }
}

fun isStreakBroken(commitment: CommitmentEntity): Boolean {
    if (commitment.lastCompletedDate == null) return false
    
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = Calendar.getInstance().apply { 
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) 
    }
    val lastComp = Calendar.getInstance().apply { 
        time = sdf.parse(commitment.lastCompletedDate!!)!!
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) 
    }
    
    val checkDay = lastComp.clone() as Calendar
    checkDay.add(Calendar.DAY_OF_YEAR, 1)
    
    while (checkDay.before(today)) {
        val dayOfWeek = checkDay.get(Calendar.DAY_OF_WEEK)
        val isScheduled = if (commitment.isCustomSchedule) {
            commitment.scheduledDays.contains(dayOfWeek)
        } else {
            true
        }
        
        if (isScheduled) {
            val dateStr = sdf.format(checkDay.time)
            if (!commitment.completedDays.contains(dateStr)) {
                return true
            }
        }
        checkDay.add(Calendar.DAY_OF_YEAR, 1)
    }
    return false
}

private fun checkAndUpdateStreak(commitment: CommitmentEntity): CommitmentEntity {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = sdf.format(Date())
    if (commitment.completedDays.contains(today)) return commitment
    val newCompletedDays = commitment.completedDays + today
    val newStreak = commitment.currentStreak + 1
    return commitment.copy(
        completedDays = newCompletedDays, 
        currentStreak = newStreak, 
        longestStreak = maxOf(newStreak, commitment.longestStreak), 
        lastCompletedDate = today, 
        isBroken = false
    )
}

@Composable
fun CommitmentItemCard(commitment: CommitmentEntity, onClick: () -> Unit, onEdit: () -> Unit) {
    val color = Color(android.graphics.Color.parseColor(commitment.colorHex))
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isDoneToday = commitment.completedDays.contains(todayStr)
    val isBroken = isStreakBroken(commitment)

    // Evolution Logic
    val totalDone = commitment.completedDays.size
    val currentLevel = (totalDone / 7) + 1
    val progressInLevel = totalDone % 7

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .alpha(if (isBroken) 0.6f else 1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isBroken) 2.dp else 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Glow based on Level
            if (currentLevel > 1 && !isBroken) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = (-20).dp, y = (-20).dp)
                        .blur(30.dp)
                        .alpha(0.15f * minOf(currentLevel, 5))
                        .background(color, CircleShape)
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(contentAlignment = Alignment.TopEnd) {
                        DynamicIcon(
                            type = commitment.iconType, 
                            color = if (isBroken) Color.Gray else color, 
                            modifier = Modifier.size(52.dp)
                        )
                        // Level Badge
                        Box(
                            modifier = Modifier
                                .offset(x = 4.dp, y = (-4).dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isBroken) Color.Gray else color)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Lv.$currentLevel", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    
                    Spacer(Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                commitment.title, 
                                fontWeight = FontWeight.ExtraBold, 
                                fontSize = 18.sp, 
                                color = if (isBroken) Color.Gray else Color(0xFF2D3436),
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (commitment.isCustomSchedule) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE3F2FD))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("CUSTOM", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                                }
                            }
                        }
                        
                        Text(commitment.description, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                    }
                    IconButton(onClick = onEdit) { 
                        Icon(Icons.Default.Settings, null, tint = Color.LightGray.copy(alpha = 0.6f), modifier = Modifier.size(20.dp)) 
                    }
                }
                
                Spacer(Modifier.height(14.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        // Progress squares (Reset every 7)
                        Text("Next Milestone Progress:", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            List(7) { index ->
                                val isFilled = index < progressInLevel
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (isFilled) color else color.copy(alpha = 0.1f)
                                        )
                                        .border(
                                            0.5.dp, 
                                            if (isFilled) Color.Transparent else color.copy(alpha = 0.2f), 
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (commitment.isNotificationEnabled && commitment.notificationTimes.isNotEmpty()) {
                            Icon(Icons.Default.NotificationsActive, null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(commitment.notificationTimes.firstOrNull() ?: "", fontSize = 10.sp, color = Color.Gray)
                            Spacer(Modifier.width(12.dp))
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isBroken) Color.Gray.copy(alpha = 0.1f) else color.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalFireDepartment, null, tint = if (isBroken) Color.Gray else color, modifier = Modifier.size(12.dp))
                                Text(" ${commitment.currentStreak}", color = if (isBroken) Color.Gray else color, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                        
                        Spacer(Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isBroken) Color.Red.copy(alpha = 0.1f) else if (isDoneToday) Color(0xFFE8F5E9) else Color(0xFFF0F0F0))
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = if (isBroken) "BROKEN" else if (isDoneToday) "DONE" else "ACTION", color = if (isBroken) Color.Red else if (isDoneToday) Color(0xFF2E7D32) else Color.Gray, fontWeight = FontWeight.Black, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LayeredFireIcon(modifier: Modifier = Modifier, baseColor: Color) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Icon(Icons.Default.LocalFireDepartment, null, tint = baseColor.copy(alpha = 0.3f), modifier = Modifier.fillMaxSize())
        Icon(Icons.Default.LocalFireDepartment, null, tint = baseColor.copy(alpha = 0.5f), modifier = Modifier.fillMaxSize(0.7f).offset(y = 5.dp))
        Icon(Icons.Default.LocalFireDepartment, null, tint = baseColor, modifier = Modifier.fillMaxSize(0.4f).offset(y = 10.dp))
    }
}

@Composable
fun DynamicIcon(type: String, color: Color, modifier: Modifier = Modifier) {
    val icon = when(type) {
        "FIRE" -> Icons.Default.LocalFireDepartment
        "WATER" -> Icons.Default.WaterDrop
        "LEAF" -> Icons.Default.Eco
        "STAR" -> Icons.Default.AutoAwesome
        else -> Icons.Default.Favorite
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(color.copy(alpha = 0.15f)))
        Icon(icon, null, tint = color.copy(alpha = 0.4f), modifier = Modifier.fillMaxSize(0.85f).offset(y = 2.dp))
        Icon(icon, null, tint = color.copy(alpha = 0.7f), modifier = Modifier.fillMaxSize(0.65f))
        Icon(icon, null, tint = Color.White, modifier = Modifier.fillMaxSize(0.38f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommitmentDetailView(commitment: CommitmentEntity, onDismiss: () -> Unit, onCompleteRequest: (CommitmentEntity) -> Unit, onReset: () -> Unit) {
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isDoneToday = commitment.completedDays.contains(todayStr)
    val color = Color(android.graphics.Color.parseColor(commitment.colorHex))
    val isBroken = isStreakBroken(commitment)
    val scrollState = rememberScrollState()
    
    val todayCal = Calendar.getInstance()
    val isScheduledToday = if (commitment.isCustomSchedule) commitment.scheduledDays.contains(todayCal.get(Calendar.DAY_OF_WEEK)) else true

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState) // 🔥 Fix: Add scroll support
                .padding(bottom = 32.dp, start = 24.dp, end = 24.dp), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DynamicIcon(commitment.iconType, if (isBroken) Color.Gray else color, Modifier.size(100.dp))
            Spacer(Modifier.height(16.dp))
            Text(commitment.title, fontSize = 28.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(commitment.description, color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StreakStatBox("Current Streak", commitment.currentStreak.toString(), if (isBroken) Color.Gray else color)
                StreakStatBox("Best Record", commitment.longestStreak.toString(), Color(0xFF6C63FF))
            }
            Spacer(Modifier.height(24.dp))
            
            val calendar = Calendar.getInstance()
            val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)
            Text("Progres $monthName", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            CalendarGrid(commitment, color)

            Spacer(Modifier.height(32.dp))
            
            when {
                isBroken -> {
                    Button(
                        onClick = onReset, 
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red), 
                        modifier = Modifier.fillMaxWidth().height(56.dp), 
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Streak Terputus! Reset & Ulangi 🔄", fontWeight = FontWeight.Bold)
                    }
                }
                !isScheduledToday -> {
                    Button(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text("HARI LIBUR / REST DAY 💤", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                else -> {
                    Button(
                        onClick = { onCompleteRequest(commitment) },
                        enabled = !isDoneToday,
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDoneToday) Color(0xFF4CAF50) else color)
                    ) {
                        Icon(if (isDoneToday) Icons.Default.CheckCircle else Icons.Default.FlashOn, null)
                        Spacer(Modifier.width(12.dp))
                        Text(if (isDoneToday) "Ritual Selesai ✅" else "Lakukan Ritual 🔥", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarGrid(commitment: CommitmentEntity, color: Color) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = sdf.format(Date())

    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(280.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val daysOfWeek = listOf("M", "S", "S", "R", "K", "J", "S")
        itemsGrid(daysOfWeek) { day -> Text(day, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray, textAlign = TextAlign.Center) }
        items(firstDayOfWeek) { Box(Modifier.size(32.dp)) }
        items(daysInMonth) { dayIndex ->
            val day = dayIndex + 1
            val cal = Calendar.getInstance().apply { set(currentYear, currentMonth, day); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
            val dateStr = sdf.format(cal.time)
            val isCompleted = commitment.completedDays.contains(dateStr)
            val isToday = dateStr == todayStr
            
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val isScheduled = if (commitment.isCustomSchedule) commitment.scheduledDays.contains(dayOfWeek) else true
            
            // 🔥 Fix: Include the creation day in the missed logic
            val creationCal = Calendar.getInstance().apply { 
                timeInMillis = commitment.createdAt
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            
            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) 
            }

            val isMissed = !isCompleted && isScheduled && !cal.before(creationCal) && cal.before(todayCal)

            var triggerExplosion by remember { mutableStateOf(false) }
            LaunchedEffect(isCompleted) { if (isCompleted && isToday) { triggerExplosion = true; delay(2000); triggerExplosion = false } }

            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                isCompleted -> color
                                !isScheduled -> Color.Gray.copy(alpha = 0.05f)
                                isMissed -> Color.Red.copy(alpha = 0.1f)
                                isToday -> color.copy(alpha = 0.1f)
                                else -> Color(0xFFF0F0F0)
                            }
                        )
                        .border(if (isToday) 2.dp else 0.dp, color, RoundedCornerShape(10.dp))
                ) {
                    Text(
                        day.toString(), 
                        modifier = Modifier.align(Alignment.Center), 
                        fontSize = 12.sp, 
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal, 
                        color = when {
                            isCompleted -> Color.White
                            isMissed -> Color.Red
                            !isScheduled -> Color.LightGray
                            else -> Color.Black
                        }
                    )
                    
                    if (!isScheduled && !isCompleted) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
                            Icon(Icons.Default.Close, null, tint = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.size(8.dp))
                        }
                    }
                }
                if (triggerExplosion) ElementExplosion(commitment.iconType, color)
            }
        }
    }
}

@Composable
fun ElementExplosion(type: String, color: Color) {
    val transition = rememberInfiniteTransition()
    val duration = 1500
    
    val ringScale by transition.animateFloat(
        initialValue = 0f, targetValue = 4f,
        animationSpec = infiniteRepeatable(animation = tween(duration, easing = LinearOutSlowInEasing))
    )
    val ringAlpha by transition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(duration))
    )

    Box(contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(40.dp).scale(ringScale).alpha(ringAlpha).border(2.dp, color, CircleShape))

        when (type) {
            "FIRE" -> FireExplosion(color, transition, duration)
            "WATER" -> WaterExplosion(color, transition, duration)
            "LEAF" -> LeafExplosion(color, transition, duration)
            "STAR" -> StarExplosion(color, transition, duration)
            else -> HeartExplosion(color, transition, duration)
        }
    }
}

@Composable
fun FireExplosion(color: Color, transition: InfiniteTransition, duration: Int) {
    val particles = remember { List(15) { index -> index * 24f } }
    particles.forEach { angle ->
        val distance by transition.animateFloat(0f, 100f, infiniteRepeatable(tween(duration, easing = FastOutSlowInEasing)))
        val alpha by transition.animateFloat(1f, 0f, infiniteRepeatable(tween(duration)))
        val x = (Math.cos(Math.toRadians(angle.toDouble())) * distance).dp
        val y = (Math.sin(Math.toRadians(angle.toDouble())) * distance).dp
        Icon(Icons.Default.LocalFireDepartment, null, tint = color, modifier = Modifier.offset(x, y).scale(0.8f).alpha(alpha).size(20.dp))
    }
}

@Composable
fun WaterExplosion(color: Color, transition: InfiniteTransition, duration: Int) {
    List(4) { i ->
        val delay = i * 250
        val scale by transition.animateFloat(0f, 5f, infiniteRepeatable(tween(duration, delayMillis = delay, easing = LinearEasing)))
        val alpha by transition.animateFloat(0.7f, 0f, infiniteRepeatable(tween(duration, delayMillis = delay)))
        Box(Modifier.size(35.dp).scale(scale).alpha(alpha).border(4.dp, color, CircleShape))
    }
    val drips = remember { List(6) { index -> index * 60f } }
    drips.forEach { angle ->
        val distance by transition.animateFloat(0f, 60f, infiniteRepeatable(tween(duration, easing = LinearEasing)))
        val x = (Math.cos(Math.toRadians(angle.toDouble())) * distance).dp
        val y = (Math.sin(Math.toRadians(angle.toDouble())) * distance).dp
        Icon(Icons.Default.WaterDrop, null, tint = color, modifier = Modifier.offset(x, y).scale(0.5f).size(15.dp))
    }
}

@Composable
fun LeafExplosion(color: Color, transition: InfiniteTransition, duration: Int) {
    val particles = remember { List(10) { index -> index * 36f } }
    particles.forEach { angle ->
        val distance by transition.animateFloat(0f, 90f, infiniteRepeatable(tween(duration, easing = LinearOutSlowInEasing)))
        val rotation by transition.animateFloat(0f, 720f, infiniteRepeatable(tween(duration)))
        val alpha by transition.animateFloat(1f, 0f, infiniteRepeatable(tween(duration)))
        val x = (Math.cos(Math.toRadians(angle.toDouble())) * distance).dp
        val y = (Math.sin(Math.toRadians(angle.toDouble())) * distance).dp
        Icon(Icons.Default.Eco, null, tint = color, modifier = Modifier.offset(x, y).rotate(rotation).alpha(alpha).size(22.dp))
    }
}

@Composable
fun StarExplosion(color: Color, transition: InfiniteTransition, duration: Int) {
    val particles = remember { List(12) { (0..360).random().toFloat() } }
    particles.forEach { angle ->
        val distance by transition.animateFloat(0f, 110f, infiniteRepeatable(tween(duration, easing = FastOutLinearInEasing)))
        val rotation by transition.animateFloat(0f, 180f, infiniteRepeatable(tween(duration)))
        val alpha by transition.animateFloat(1f, 0f, infiniteRepeatable(tween(duration)))
        val x = (Math.cos(Math.toRadians(angle.toDouble())) * distance).dp
        val y = (Math.sin(Math.toRadians(angle.toDouble())) * distance).dp
        Icon(Icons.Default.AutoAwesome, null, tint = color, modifier = Modifier.offset(x, y).rotate(rotation).alpha(alpha).size(18.dp))
    }
}

@Composable
fun HeartExplosion(color: Color, transition: InfiniteTransition, duration: Int) {
    List(8) { i ->
        val xOff = ((-40)..40).random().dp
        val yDist by transition.animateFloat(0f, -120f, infiniteRepeatable(tween(duration, easing = LinearOutSlowInEasing)))
        val scale by transition.animateFloat(0.5f, 1.2f, infiniteRepeatable(tween(duration/2)))
        val alpha by transition.animateFloat(1f, 0f, infiniteRepeatable(tween(duration)))
        Icon(Icons.Default.Favorite, null, tint = color, modifier = Modifier.offset(xOff, yDist.dp).scale(scale).alpha(alpha).size(20.dp))
    }
}

@Composable
fun StreakStatBox(label: String, value: String, color: Color) {
    Column(modifier = Modifier.width(140.dp).clip(RoundedCornerShape(20.dp)).background(color.copy(alpha = 0.1f)).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val count = value.toIntOrNull() ?: 0
        val displayValue = if (count > 0) value else "0"
        Text(displayValue, fontSize = 36.sp, fontWeight = FontWeight.Black, color = color)
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun EmptyCommitmentView() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(120.dp), tint = Color.LightGray.copy(alpha = 0.4f))
        Spacer(Modifier.height(16.dp))
        Text("RITUAL BELUM ADA", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.Gray)
        Text("Buatlah janji kecil untuk dirimu sendiri.", color = Color.Gray.copy(alpha = 0.6f))
    }
}
