package com.huma.app.ui.screen.lifearea

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue // Penting untuk delegasi 'by'
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.huma.app.data.local.LifeArea
import com.huma.app.data.local.TaskEntity // Pastikan ini terimport
import com.huma.app.ui.viewmodel.TaskViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeAreaScreen(
    navController: NavController,
    taskViewModel: TaskViewModel
) {
    // 🔥 DISINKRONKAN: Menggunakan .tasks sesuai isi TaskViewModel kamu
    val allTasks by taskViewModel.tasks.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Life Balance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF6F7FB)
                )
            )
        },
        containerColor = Color(0xFFF6F7FB)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ================== HEADER: OVERALL PROGRESS ==================
            val totalDone = allTasks.count { it.isDone }
            val totalTasksCount = allTasks.size
            val totalProgress = if (totalTasksCount > 0) totalDone.toFloat() / totalTasksCount else 0f

            LifeBalanceOverview(progress = totalProgress, totalTasks = totalTasksCount)

            Text(
                text = "Pilih Area Fokus",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // ================== GRID AREAS ==================
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(LifeArea.values()) { area ->
                    // 🔥 Logic filter data asli
                    val areaTasks = allTasks.filter { it.lifeArea == area.name }
                    val areaDone = areaTasks.count { it.isDone }
                    val areaCount = areaTasks.size
                    val areaProgress = if (areaCount > 0) areaDone.toFloat() / areaCount else 0f

                    AreaCard(
                        area = area,
                        taskCount = areaCount,
                        progress = areaProgress,
                        onClick = {
                            navController.navigate("area_detail/${area.name}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LifeBalanceOverview(progress: Float, totalTasks: Int) {
    val percentage = (progress * 100).toInt()

    val statusText = when {
        totalTasks == 0 -> "Mulai Hari Ini!"
        progress >= 1f -> "Luar Biasa!"
        progress >= 0.7f -> "Hampir Seimbang!"
        progress >= 0.4f -> "On Track!"
        else -> "Tetap Semangat!"
    }

    val descriptionText = when {
        totalTasks == 0 -> "Belum ada rencana? Ayo susun tugas pertamamu!"
        progress >= 1f -> "Semua target tercapai. Waktunya self-reward!"
        progress >= 0.7f -> "Hanya butuh sedikit dorongan lagi!"
        else -> "Hidup terasa lebih tertata saat tugas terselesaikan."
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(80.dp),
                    color = Color(0xFF6C63FF),
                    strokeWidth = 8.dp,
                    trackColor = Color(0xFF6C63FF).copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "$percentage%",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF6C63FF)
                )
            }

            Spacer(Modifier.width(20.dp))

            Column {
                Text(text = statusText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = descriptionText, fontSize = 13.sp, color = Color.Gray, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun AreaCard(
    area: LifeArea,
    taskCount: Int,
    progress: Float,
    onClick: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progressAnim"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(180.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(area.color.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = area.icon, contentDescription = null, tint = area.color, modifier = Modifier.size(24.dp))
                }

                Surface(color = Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = "$taskCount",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }

            Column {
                Text(text = area.label, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A1A))
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when {
                        taskCount == 0 -> "Belum ada tugas"
                        progress >= 1f -> "Selesai! 🎉"
                        else -> "Progres ${(progress * 100).toInt()}%"
                    },
                    fontSize = 11.sp,
                    color = if(progress >= 1f && taskCount > 0) area.color else Color.Gray
                )
                Spacer(Modifier.height(10.dp))
                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(area.color.copy(alpha = 0.1f))) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (taskCount == 0) 0f else animatedProgress)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(area.color.copy(alpha = 0.7f), area.color)))
                    )
                }
            }
        }
    }
}