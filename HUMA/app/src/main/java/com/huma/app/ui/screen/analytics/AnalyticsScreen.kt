package com.huma.app.ui.screen.analytics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.huma.app.ui.viewmodel.TaskViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: TaskViewModel
) {
    // Ambil Data Real-time
    val tasks by viewModel.tasks.collectAsState()
    val radarData by viewModel.radarData.collectAsState()
    val hourlyStats by viewModel.hourlyCompletionStats.collectAsState()
    val overallProgress by viewModel.overallLifeBalance.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CORE STATISTICS", fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B))))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ================= 1. ENERGY ORB (DYNAMIC) =================
            val rankText = when {
                overallProgress >= 0.8f -> "LEGENDARY"
                overallProgress >= 0.5f -> "CONSISTENT"
                else -> "EVOLVING"
            }
            val orbColor = if (overallProgress >= 0.6f) Color(0xFF38BDF8) else Color(0xFFFACC15)

            Box(
                modifier = Modifier.size(200.dp).scale(breathScale),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(brush = Brush.radialGradient(colors = listOf(orbColor.copy(0.2f), Color.Transparent)))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(rankText, fontWeight = FontWeight.Bold, color = orbColor, letterSpacing = 2.sp)
                    Text("${(overallProgress * 100).toInt()}%", fontSize = 56.sp, color = Color.White, fontWeight = FontWeight.Black)
                    Text("Productivity Index", color = Color.White.copy(0.5f), fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(32.dp))

            // ================= 2. SINKRON RADAR CHART =================
            AnalyticsCard(title = "Life Area Balance") {
                val labels = listOf("Akademik", "Kesehatan", "Spiritual", "Rumah", "Sosial")
                Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
                    LifeRadarChart(
                        modifier = Modifier.size(220.dp),
                        data = radarData,
                        labels = labels
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ================= 3. WEEKLY VELOCITY (DYNAMIC STREAK) =================
            AnalyticsCard(title = "Weekly Velocity") {
                Text("Konsistensi penyelesaian tugas 7 hari terakhir", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val today = LocalDate.now()
                    (6 downTo 0).forEach { i ->
                        val date = today.minusDays(i.toLong())
                        val dayName = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())

                        // Cek apakah ada tugas yang selesai pada tanggal ini
                        val isDayActive = tasks.any { task ->
                            if (task.isDone) {
                                val taskDate = Instant.ofEpochMilli(task.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
                                taskDate.isEqual(date)
                            } else false
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDayActive) Color(0xFF22C55E) else Color.White.copy(0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDayActive) Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Text(dayName, color = if(i == 0) Color.White else Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ================= 4. TASK PROPORTION (PIE STATS) =================
            AnalyticsCard(title = "Task Composition") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val doneCount = tasks.count { it.isDone }
                    val totalCount = tasks.size.coerceAtLeast(1)
                    val ratio = doneCount.toFloat() / totalCount.toFloat()

                    Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Color.White.copy(0.1f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = Color(0xFF818CF8),
                                startAngle = -90f,
                                sweepAngle = ratio * 360f,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text("${(ratio * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(24.dp))
                    Column {
                        Text("Total Tasks: $totalCount", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Completed: $doneCount", color = Color(0xFF22C55E), fontSize = 13.sp)
                        Text("Remaining: ${totalCount - doneCount}", color = Color(0xFFF87171), fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ================= 5. PEAK FOCUS HEATMAP =================
            AnalyticsCard(title = "Peak Performance Hours") {
                Column {
                    Row(modifier = Modifier.fillMaxWidth().height(50.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        hourlyStats.forEach { intensity ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        Color(0xFF38BDF8).copy(alpha = intensity.coerceAtLeast(0.05f)),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Midnight", color = Color.Gray, fontSize = 10.sp)
                        Text("Noon", color = Color.Gray, fontSize = 10.sp)
                        Text("Night", color = Color.Gray, fontSize = 10.sp)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun LifeRadarChart(modifier: Modifier, data: List<Float>, labels: List<String>) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2 * 0.8f
            val sides = data.size
            val angleStep = (2 * Math.PI / sides).toFloat()

            // 1. Grid Background (Pentagon-pentagon transparan)
            repeat(4) { i ->
                val r = radius * (i + 1) / 4
                val path = Path()
                for (j in 0 until sides) {
                    val angle = j * angleStep - (Math.PI / 2).toFloat()
                    val x = center.x + r * cos(angle.toDouble()).toFloat()
                    val y = center.y + r * sin(angle.toDouble()).toFloat()
                    if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, Color.White.copy(0.05f), style = Stroke(1.dp.toPx()))
            }

            // 2. Data Shape
            val dataPath = Path()
            data.forEachIndexed { j, value ->
                val angle = j * angleStep - (Math.PI / 2).toFloat()
                val r = radius * value.coerceIn(0.1f, 1f)
                val x = center.x + r * cos(angle.toDouble()).toFloat()
                val y = center.y + r * sin(angle.toDouble()).toFloat()
                if (j == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)

                // Garis penolong dari tengah
                drawLine(
                    color = Color.White.copy(0.05f),
                    start = center,
                    end = Offset(center.x + radius * cos(angle.toDouble()).toFloat(), center.y + radius * sin(angle.toDouble()).toFloat()),
                    strokeWidth = 1.dp.toPx()
                )
            }
            dataPath.close()
            drawPath(dataPath, Color(0xFF38BDF8).copy(0.3f))
            drawPath(dataPath, Color(0xFF38BDF8), style = Stroke(2.dp.toPx()))

            // 3. Data Points
            data.forEachIndexed { j, value ->
                val angle = j * angleStep - (Math.PI / 2).toFloat()
                val r = radius * value.coerceIn(0.1f, 1f)
                drawCircle(Color.Cyan, 3.dp.toPx(), center = Offset(center.x + r * cos(angle.toDouble()).toFloat(), center.y + r * sin(angle.toDouble()).toFloat()))
            }
        }

        // 4. Labels yang SINKRON (Menghitung posisi di dalam Box yang sama)
        labels.forEachIndexed { j, label ->
            val angle = j * (2 * Math.PI / labels.size).toFloat() - (Math.PI / 2).toFloat()
            // Jarak teks harus lebih besar dari radius canvas agar tidak menimpa garis
            val xOffset = (cos(angle.toDouble()) * 130).toInt().dp
            val yOffset = (sin(angle.toDouble()) * 130).toInt().dp

            Text(
                text = label,
                color = Color.White.copy(0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(x = xOffset, y = yOffset),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AnalyticsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.03f)),
        border = BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(20.dp))
            content()
        }
    }
}