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
                color = Color(0xFF37474F).copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF37474F).copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FormatQuote, null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Success is the sum of small efforts.", color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Commitment 🔥", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onOpen() }) {
                Text("See All", color = Color(0xFFFFA726), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFFFFA726), modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(end = 16.dp)) {
            items(commitments) { commitment ->
                CommitmentFlameCardDashboard(commitment = commitment, onClick = onOpen)
            }
            item { AddCommitmentCardDashboard(onClick = onAddNew) }
        }
        Spacer(Modifier.height(12.dp))
        Text("Konsistensi adalah kunci perubahan besar 🧡✨", color = Color.Gray, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CommitmentFlameCardDashboard(commitment: CommitmentEntity, onClick: () -> Unit) {
    val orenColor = Color(0xFFFFA726)
    val actualElemColor = Color(android.graphics.Color.parseColor(commitment.colorHex))
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isBroken = isStreakBroken(commitment)
    val isDoneToday = commitment.completedDays.contains(today)

    Card(
        modifier = Modifier.width(195.dp).height(155.dp).clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isBroken) 1.dp else 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.White, Color(0xFFFFF3E0)))
        )) {
            // Watermark Api Berlayer (TETAP OREN)
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
                    Text(commitment.title, fontWeight = FontWeight.ExtraBold, maxLines = 1, fontSize = 16.sp, color = Color(0xFF2D3436))
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
                                .width(65.dp) // Fixed width to prevent layout jump
                                .clip(RoundedCornerShape(8.dp))
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
fun AddCommitmentCardDashboard(onClick: () -> Unit) {
    Card(modifier = Modifier.width(100.dp).height(155.dp).clickable { onClick() }, shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f))) {
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
