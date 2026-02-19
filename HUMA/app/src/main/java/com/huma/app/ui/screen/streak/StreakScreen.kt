package com.huma.app.ui.screen.streak

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huma.app.data.local.streak.StreakEntity
import com.huma.app.viewmodel.StreakViewModel
import kotlinx.coroutines.launch
import kotlin.math.sin
import androidx.compose.foundation.ExperimentalFoundationApi // Wajib
import androidx.compose.foundation.pager.HorizontalPager    // Wajib
import androidx.compose.foundation.pager.rememberPagerState  // Wajib
import androidx.compose.ui.text.style.TextAlign
import java.util.Calendar
import java.util.Locale
import androidx.compose.animation.togetherWith // Pengganti 'with' yang eror
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith


// --- WARNA PREMIUM (FIX TIDAK ERROR) ---
val FlameOrange = Color(0xFFE65100)
val DeepGold = Color(0xFFFFD600)
val PlasmaBlue = Color(0xFF00E5FF)
val VoidBlack = Color(0xFF0A0A0A)
val FireRed = Color(0xFFD50000)
private const val DAY = 24 * 60 * 60 * 1000L
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StreakScreen(viewModel: StreakViewModel) {
    val isDeadMode = viewModel.isAwakeningActive
    val streakData by viewModel.streakData.collectAsState(initial = null)

    if (streakData == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = FlameOrange)
        }
        return   // ⬅️ WAJIB ADA
    }

    val data = streakData!!   // AMAN
    val effectiveStreak = viewModel.getEffectiveStreak()
    val fireState = calculateFireVisualState(
        if (isDeadMode) 0 else effectiveStreak
    )

    val dynamicBackground = getDynamicBackground(effectiveStreak)
    val scope = rememberCoroutineScope()
    val flashAlpha = remember { Animatable(0f) }

    var currentSubTab by remember { mutableStateOf("FLAME") }

    LaunchedEffect(Unit) {
        viewModel.checkStreakLogic()
    }

    if (streakData == null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = FlameOrange)
        }
    } else {
        val data = streakData!!

        Box(modifier = Modifier.fillMaxSize().background(dynamicBackground)) {
            VisualChains(isIgnited = data.isIgnitedToday)

            AnimatedContent(
                targetState = currentSubTab,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.92f))
                        .togetherWith(fadeOut(animationSpec = tween(400)))
                },
                label = "main_nav"
            ) { target ->
                when (target) {
                    "FLAME" -> {
                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(60.dp))
                            Text(text = "DAILY STREAK", color = Color.White.copy(alpha = 0.5f), letterSpacing = 4.sp)

                            Spacer(modifier = Modifier.height(20.dp))

                            val isDeadMode = viewModel.isAwakeningActive

                            if (isDeadMode) {

                                DeadFlameScreen()

                            } else if (!data.isIgnitedToday) {

                                if (viewModel.showInquiry) {
                                    InquirySection(
                                        onBurn = { word ->
                                            viewModel.igniteTheFlame(word)
                                            scope.launch {
                                                flashAlpha.animateTo(0.8f, tween(50))
                                                flashAlpha.animateTo(0f, tween(600))
                                            }
                                        }
                                    )
                                } else {
                                    FrictionSection(
                                        progress = viewModel.currentFriction,
                                        onSwipe = { delta -> viewModel.onFrictionSwipe(delta) }
                                    )
                                }

                            } else {

                                FireDisplaySection(
                                    streak = effectiveStreak,
                                    word = data.lastBurnedWord,
                                    fireState = fireState,
                                    flashAlpha = flashAlpha.value
                                )

                            }

                            Spacer(modifier = Modifier.height(40.dp))
                            HorizontalChainCalendar(
                                streak = effectiveStreak,
                                protectedDays = data.protectedDays
                            )
                            Spacer(modifier = Modifier.height(120.dp))
                        }
                    }
                    "CALENDAR" -> FullCalendarSection(effectiveStreak, viewModel)
                }
            }

            // Overlay Flash
            Box(Modifier.fillMaxSize().background(fireState.mainColor.copy(alpha = flashAlpha.value)))

            // Tombol Navigasi Bawah
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 30.dp), contentAlignment = Alignment.BottomCenter) {
                QuickNavigationBar(
                    currentTab = currentSubTab,
                    activeColor = fireState.mainColor,
                    onTabClick = { currentSubTab = it }
                )
            }

            // Status (Health/Shield)
            Box(modifier = Modifier.fillMaxSize().padding(30.dp), contentAlignment = Alignment.BottomStart) {
                StatusIndicators(data = data)
            }

            // --- DIALOG PENYELAMATAN (LIFE LINE) ---
            if (viewModel.isAwakeningActive) {
                AlertDialog(
                    onDismissRequest = { }, // Mengunci agar user harus memilih
                    containerColor = Color(0xFF1A1A1A),
                    title = {
                        Text("API TELAH PADAM!", color = FireRed, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Text(
                            "Namun kamu memiliki ${data.lifeLineCount} Keselamatan. Gunakan untuk menyambung api atau mengulang dari nol?",
                            color = Color.White
                        )
                    },
                    confirmButton = {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                            onClick = {
                                viewModel.useLifeLineRitual()
                                viewModel.isAwakeningActive = false
                            }
                        ) { Text("Gunakan Nyawa", color = Color.White) }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            // Reset streak jika user memilih mengulang
                            viewModel.debugSetDays(0) // Atau panggil fungsi reset di VM
                            viewModel.isAwakeningActive = false
                        }) { Text("Mulai dari Awal", color = Color.Gray) }
                    }
                )
            }
        }
    }
}
// --- HELPER FUNCTIONS ---

fun getDynamicBackground(streak: Int): Brush {
    val coreColor = when {
        streak >= 100 -> Color(0xFF0D0221)
        streak >= 50 -> Color(0xFF001219)
        streak >= 30 -> Color(0xFF1A0000)
        else -> VoidBlack
    }
    return Brush.verticalGradient(listOf(coreColor, Color.Black))
}

data class FireVisual(val mainColor: Color, val glowColor: Color, val scale: Float, val message: String, val particleCount: Int)

fun calculateFireVisualState(days: Int): FireVisual {
    val growth = ((days % 5) * 0.02f) // Bonus ukuran kecil setiap hari dalam level yang sama

    return when {
        days == 0 ->
            FireVisual(Color.Gray, Color.DarkGray, 0.8f, "COLD", 0)
        days in 1..4 ->
            FireVisual(Color(0xFFFF9800), Color(0xFFFF5722), 1.0f + growth, "CANDLE FLAME", 5)
        days in 5..14 ->
            FireVisual(FlameOrange, FireRed, 1.3f + growth, "CAMPFIRE", 12)
        days in 15..29 ->
            FireVisual(Color(0xFFFFD600), Color(0xFFFF3D00), 1.6f + growth, "BLAZING TORCH", 20)
        days in 30..49 ->
            FireVisual(FireRed, Color(0xFF311B92), 1.8f + growth, "HELLFIRE INFERNO", 30)
        days in 50..99 ->
            FireVisual(DeepGold, Color(0xFFE65100), 2.0f + growth, "PLASMA CORE", 45)
        days in 100..199 ->
            FireVisual(PlasmaBlue, Color(0xFF006064), 2.3f + growth, "SUPERNOVA", 60)
        days in 200..364 ->
            FireVisual(Color(0xFF76FF03), Color(0xFF1B5E20), 2.6f + growth, "DRAGON BREATH", 80)
        else ->
            FireVisual(Color.White, DeepGold, 3.0f + growth, "ETERNAL SUN", 120)
    }
}

@Composable
fun DeadFlameScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.LocalFireDepartment,
            null,
            modifier = Modifier.size(100.dp),
            tint = Color.DarkGray
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "API TELAH PADAM",
            color = FireRed,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(Modifier.height(10.dp))

        Text(
            "Gunakan perlindungan untuk menyelamatkan streak\natau biarkan padam sepenuhnya",
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}


// --- UI COMPONENTS ---

@Composable
fun FireDisplaySection(streak: Int, word: String, fireState: FireVisual, flashAlpha: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "fire")

    // Animasi Flicker yang makin kencang kalau level tinggi
    val flickerSpeed = if (streak > 50) 100 else 150
    val flicker by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(flickerSpeed, easing = LinearEasing), RepeatMode.Reverse), label = "flicker"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(350.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height * 0.75f)
                val scale = fireState.scale * flicker

                // 1. DYNAMIC GLOW (Aura di belakang api)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(fireState.glowColor.copy(0.5f), Color.Transparent),
                        center = center,
                        radius = 200f * scale
                    ),
                    radius = 250f * scale,
                    center = center
                )

                // 2. MULTI-LAYER FLAME
                fun drawFlame(w: Float, h: Float, col: Color, blur: Float = 0f) {
                    val path = Path().apply {
                        moveTo(center.x, center.y)
                        // Bikin bentuk api lebih tajam di atas
                        cubicTo(
                            center.x - w, center.y - (h * 0.2f),
                            center.x - (w * 0.1f), center.y - (h * 0.8f),
                            center.x, center.y - h
                        )
                        cubicTo(
                            center.x + (w * 0.1f), center.y - (h * 0.8f),
                            center.x + w, center.y - (h * 0.2f),
                            center.x, center.y
                        )
                    }
                    drawPath(path, color = col)
                }

                // Layer Api Berdasarkan Level
                drawFlame(120f * scale, 260f * scale, fireState.mainColor.copy(0.3f)) // Aura Luar
                drawFlame(80f * scale, 200f * scale, fireState.mainColor)             // Api Utama
                drawFlame(40f * scale, 120f * scale, Color.White.copy(0.6f))        // Inti Panas

                // 3. SPARKLES (Makin tinggi level, makin liar partikelnya)
                repeat(fireState.particleCount) { i ->
                    val t = (System.currentTimeMillis() + (i * 150)) % 2500 / 2500f
                    val drift = sin(i.toFloat() + t * 6) * 100f
                    val xPos = center.x + drift
                    val yPos = center.y - (t * (400f * scale))

                    drawCircle(
                        color = if (streak > 100) Color.Cyan else fireState.mainColor.copy(alpha = 1f - t),
                        radius = (3f + flicker),
                        center = Offset(xPos, yPos)
                    )
                }
            }
        }

        // --- INFO TEXT (DENGAN TEMA WARNA LEVEL) ---
        Text(
            text = "$streak",
            fontSize = (90 + (streak / 10)).sp, // Angka membesar seiring streak
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.graphicsLayer(translationY = -40f)
        )
        Text(
            text = fireState.message,
            color = fireState.mainColor,
            fontWeight = FontWeight.ExtraBold,
            style = androidx.compose.ui.text.TextStyle(shadow = Shadow(fireState.glowColor, blurRadius = 20f)),
            letterSpacing = 2.sp,
            modifier = Modifier.graphicsLayer(translationY = -40f)
        )
        Text(
            text = "\"$word\"",
            color = Color.Gray,
            fontSize = 15.sp,
            modifier = Modifier.graphicsLayer(translationY = -30f)
        )
    }
}

@Composable
fun QuickNavigationBar(currentTab: String, activeColor: Color, onTabClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.1f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        Icon(
            Icons.Default.LocalFireDepartment,
            null,
            Modifier.size(32.dp).clickable { onTabClick("FLAME") },
            tint = if (currentTab == "FLAME") activeColor else Color.Gray
        )
        Icon(
            Icons.Default.CalendarMonth,
            null,
            Modifier.size(32.dp).clickable { onTabClick("CALENDAR") },
            tint = if (currentTab == "CALENDAR") Color.White else Color.Gray
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullCalendarSection(streak: Int, viewModel: StreakViewModel) {
    val streakData by viewModel.streakData.collectAsState(initial = null)
    val startIndex = 500
    val pagerState = rememberPagerState(initialPage = startIndex) { 1000 }
    val todayCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(60.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) { page ->
            val monthCal = Calendar.getInstance().apply {
                add(Calendar.MONTH, page - startIndex)
            }

            val monthName = java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                .format(monthCal.time).uppercase()

            Column {
                Text(monthName, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text("Track jalur apimu", color = Color.Gray)
                Spacer(Modifier.height(30.dp))

                val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                val firstDayCal = (monthCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
                val firstDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK) - 1
                val totalSlots = daysInMonth + firstDayOfWeek

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                            Text(it, Modifier.weight(1f), color = Color.Gray.copy(0.5f), fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }

                    val weeks = (0 until totalSlots).chunked(7)
                    weeks.forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            week.forEach { slot ->
                                val dayNumber = slot - firstDayOfWeek + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val currentSlotCal = (monthCal.clone() as Calendar).apply {
                                        set(Calendar.DAY_OF_MONTH, dayNumber)
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }

                                    val isToday = currentSlotCal.timeInMillis == todayCal.timeInMillis
                                    val isPast = currentSlotCal.timeInMillis < todayCal.timeInMillis

                                    val protectedDays = streakData?.protectedDays ?: emptyList()
                                    val start = streakData?.streakStartMillis ?: 0L

                                    val diff = ((currentSlotCal.timeInMillis - start) / DAY).toInt() + 1

                                    val isProtected = diff in protectedDays

                                    val isSuccess = when {
                                        isToday -> streakData?.isIgnitedToday == true
                                        isProtected -> true
                                        isPast -> diff in 1..streak
                                        else -> false
                                    }



                                    CalendarDayItem(
                                        day = dayNumber,
                                        isSuccess = isSuccess,
                                        isProtected = isProtected,
                                        isToday = isToday,
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                            if (week.size < 7) repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayItem(
    day: Int,
    isSuccess: Boolean,
    isProtected: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isProtected -> Color(0xFF2196F3).copy(alpha = 0.35f) // 🔵 PROTECTION BG
        isSuccess -> FlameOrange.copy(alpha = 0.25f)        // 🔥 SUCCESS
        else -> Color.White.copy(0.05f)
    }

    val borderColor = when {
        isToday -> Color.White
        isProtected -> Color(0xFF2196F3)
        isSuccess -> FlameOrange
        else -> Color.Transparent
    }

    val textColor = when {
        isProtected -> Color(0xFFBBDEFB)
        isSuccess -> Color.White
        else -> Color.DarkGray
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {

        if (isSuccess) {
            Icon(
                Icons.Default.LocalFireDepartment,
                null,
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd)
                    .padding(3.dp),
                tint = FlameOrange
            )
        }

        if (isProtected) {
            Icon(
                Icons.Default.Security,
                null,
                modifier = Modifier
                    .size(13.dp)
                    .align(Alignment.TopStart)
                    .padding(3.dp),
                tint = Color(0xFF64B5F6)
            )
        }

        Text(
            text = "$day",
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}



@Composable
fun FrictionSection(progress: Float, onSwipe: (Float) -> Unit) {
    val context = LocalContext.current
    // State untuk mencatat kapan terakhir kali suara diputar
    var lastPlayTime by remember { mutableStateOf(0L) }

    val soundPool: android.media.SoundPool = remember {
        android.media.SoundPool.Builder()
            .setMaxStreams(3) // Naikkan sedikit stream agar transisi halus
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .build()
            )
            .build()
    }

    val soundId: Int = remember {
        val resId = context.resources.getIdentifier("gosok", "raw", context.packageName)
        if (resId != 0) soundPool.load(context, resId, 1) else -1
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            StreakPhrases.getTodayPhrase().uppercase(),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
        Spacer(Modifier.height(30.dp))

        Box(
            modifier = Modifier
                .size(280.dp)
                .graphicsLayer {
                    clip = true
                    shape = CircleShape
                }
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        if (dragAmount.getDistance() > 5f) {
                            onSwipe(dragAmount.getDistance() / 1500f)

                            // --- LOGIKA ANTI SPAM SOUND ---
                            val currentTime = System.currentTimeMillis()
                            // Cek jika sudah lewat 250ms sejak suara terakhir diputar
                            if (soundId != -1 && currentTime - lastPlayTime > 250L) {
                                soundPool.play(soundId, 0.4f, 0.4f, 1, 0, 1.0f)
                                lastPlayTime = currentTime
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)

                if (progress > 0.01f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                FlameOrange.copy(alpha = progress * 0.5f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.width / 2
                        ),
                        radius = size.width / 2,
                        center = center
                    )

                    repeat(10) {
                        val angle = (0..360).random().toFloat()
                        val dist = (20..140).random().toFloat()
                        drawCircle(
                            color = Color(0xFFFFD600).copy(alpha = progress),
                            radius = (1..6).random().toFloat(),
                            center = Offset(
                                x = center.x + (kotlin.math.cos(angle) * dist * progress),
                                y = center.y + (kotlin.math.sin(angle) * dist * progress)
                            )
                        )
                    }
                }
            }

            Canvas(modifier = Modifier.size(150.dp)) {
                if (progress > 0.1f) {
                    drawCircle(
                        color = Color.Red.copy(alpha = progress.coerceIn(0f, 0.6f)),
                        radius = size.width / 2,
                        center = Offset(size.width / 2, size.height / 2)
                    )
                }
            }

            Icon(
                Icons.Default.LocalFireDepartment,
                null,
                Modifier
                    .size(100.dp + (60.dp * progress))
                    .blur(if (progress < 0.2f) 0.dp else (2.dp * progress)),
                tint = lerp(Color.DarkGray, FlameOrange, progress)
            )
            // TAMBAHKAN INI:
            if (progress < 0.3f) {
                Text(
                    text = "GOSOK UNTUK MEMANTIK API",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
                )
            }
        }
    }
}

// Fungsi pembantu buat transisi warna (taruh di luar composable)
fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + (stop.red - start.red) * fraction,
        green = start.green + (stop.green - start.green) * fraction,
        blue = start.blue + (stop.blue - start.blue) * fraction,
        alpha = start.alpha + (stop.alpha - start.alpha) * fraction
    )
}

@Composable
fun InquirySection(onBurn: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Ambil kalimat random harian sebagai judul
    val todayPhrase = remember { StreakPhrases.getTodayPhrase() }

    // Resource ID locator untuk suara ignite
    val soundResId = remember {
        context.resources.getIdentifier("ignite", "raw", context.packageName)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
    ) {
        // 1. JUDUL DINAMIS (Dari list motivasi)
        Text(
            text = todayPhrase.uppercase(),
            color = FlameOrange,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(12.dp))

        // 2. TEKS INSTRUKSI (Menjelaskan kegunaan input)
        Text(
            text = "Tuliskan niat atau hal yang ingin kau taklukkan. Kata-katamu akan tertanam di dalam kobaran api hari ini.",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        Spacer(Modifier.height(32.dp))

        // 3. INPUT FIELD DENGAN SHAPE & PLACEHOLDER BARU
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = {
                Text(
                    "Contoh: Konsisten workout / Berhenti menunda",
                    color = Color.DarkGray,
                    fontSize = 14.sp
                )
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = FlameOrange,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                cursorColor = FlameOrange
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        // 4. TOMBOL EKSEKUSI
        Button(
            onClick = {
                if (text.isNotBlank()) {
                    if (soundResId != 0) {
                        try {
                            val mp = android.media.MediaPlayer.create(context, soundResId)
                            mp.setOnCompletionListener { it.release() }
                            mp.start()
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                    onBurn(text)
                }
            },
            enabled = text.isNotBlank(), // Button aktif cuma kalau ada teks
            colors = ButtonDefaults.buttonColors(
                containerColor = FlameOrange,
                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.7f).height(50.dp)
        ) {
            Text(
                "AMANKAN STREAK",
                fontWeight = FontWeight.ExtraBold,
                color = if (text.isNotBlank()) Color.White else Color.DarkGray
            )
        }
    }
}

@Composable
fun HorizontalChainCalendar(
    streak: Int,
    protectedDays: List<Int>
) {
    val timelineLength = streak + protectedDays.size
    val totalDays = timelineLength + 10

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items((1..totalDays).toList()) { day ->

            val isProtected = day in protectedDays
            val isSuccess = day <= streak && day !in protectedDays

            val bgColor = when {
                isProtected -> Color(0xFF3A7BFF).copy(alpha = 0.30f) // 🔵 SHIELD
                isSuccess -> FlameOrange.copy(alpha = 0.25f)        // 🔥 SUCCESS
                else -> Color.Transparent
            }

            val borderColor = when {
                isProtected -> Color(0xFF3A7BFF)
                isSuccess -> FlameOrange
                else -> Color.DarkGray
            }

            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$day",
                    color = borderColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}






@Composable
fun VisualChains(isIgnited: Boolean) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween) {
        val alpha = if (isIgnited) 0.1f else 0.3f
        Column(Modifier.padding(10.dp)) { repeat(15) { Box(Modifier.size(2.dp, 15.dp).background(Color.Gray.copy(alpha = alpha)).padding(2.dp)) } }
        Column(Modifier.padding(10.dp)) { repeat(15) { Box(Modifier.size(2.dp, 15.dp).background(Color.Gray.copy(alpha = alpha)).padding(2.dp)) } }
    }
}

@Composable
fun StatusIndicators(data: StreakEntity) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape).padding(8.dp)) {
        Icon(Icons.Default.Favorite, null, Modifier.size(20.dp), if (data.lifeLineCount > 0) Color.Red else Color.Gray)
        Spacer(Modifier.width(10.dp))
        Icon(Icons.Default.Security, null, Modifier.size(20.dp), if (data.hasShield) Color.Cyan else Color.Gray)
    }
}

object StreakPhrases {
    private val phrases = listOf(
        "Apa satu hal yang akan kau taklukkan hari ini?",
        "Jika kau menyerah hari ini, apa yang tersisa darimu?",
        "Energi besar ini mau kau ledakkan untuk apa?",
        "Apa janji terkecil yang berani kau tepati hari ini?",
        "Berapa lama lagi kau akan membiarkan potensimu terkubur?",
        "Siapa yang akan bangga jika kau berhasil melewati hari ini?",
        "Apa satu rintangan yang selama ini kau takuti tapi harus kau hancurkan?",
        "Jika hari ini adalah kesempatan terakhirmu, apa yang akan kau bakar?",
        "Apa target yang bikin kau merasa berdebar sekaligus semangat?",
        "Apa 'sampah' di pikiranmu yang ingin kau bakar habis hari ini?",
        "Lebih sakit mana: disiplin hari ini atau penyesalan selamanya?",
        "Apa alasan paling kuat kenapa kau tidak boleh berhenti sekarang?",
        "Siapa yang ingin kau buktikan bahwa mereka salah menilaimu?",
        "Apa bukti nyata bahwa kau lebih kuat dari rasa malasmu?",
        "Sampai kapan kau hanya akan jadi penonton kesuksesan orang lain?",
        "Apa satu hal yang membuatmu merasa pantas disebut pemenang?",
        "Bagaimana caramu menjinakkan hari yang sulit ini?",
        "Apa ketakutan terbesar yang akan kau tatap matanya hari ini?",
        "Sudahkah kau memberikan yang terbaik, atau hanya sekadar 'cukup'?",
        "Apa satu langkah kecil yang akan mengubah segalanya?",
        "Bagaimana versi terbaik dirimu akan bertindak hari ini?",
        "Apa yang bersedia kau korbankan demi impian besarmu?",
        "Apakah kau mengendalikan hari ini, atau hari ini yang mengendalikanmu?",
        "Apa hal yang paling membuatmu merasa hidup saat melakukannya?",
        "Apa pesan yang ingin kau kirimkan untuk dirimu di masa depan?",
        "Apa beban mental yang ingin kau buang ke dalam api ini?",
        "Kenapa kau memilih untuk berjuang saat orang lain memilih tidur?",
        "Apa satu pencapaian yang akan kau syukuri saat memejamkan mata nanti?",
        "Distraksi apa yang paling sering mencuri waktumu, dan kapan kau menghentikannya?",
        "Apakah kau sedang membangun masa depan, atau hanya menghabiskan waktu?",
        "Apa yang akan kau lakukan jika rasa takutmu tiba-tiba hilang?",
        "Apa pelajaran paling mahal yang kau dapat dari kegagalan kemarin?",
        "Sudahkah kau berterima kasih pada dirimu yang sudah bertahan sejauh ini?",
        "Siapa orang yang paling ingin kau buat tersenyum dengan keberhasilanmu?",
        "Apa hal yang paling menantang yang berani kau mulai sekarang juga?",
        "Bagaimana caramu menjaga api ini tetap menyala di tengah badai?",
        "Apakah alasanmu lebih besar daripada keinginanmu untuk berubah?",
        "Apa satu kebiasaan buruk yang sudah saatnya kau bakar sampai jadi abu?",
        "Jika dunia berhenti hari ini, apakah kau sudah meninggalkan jejak?",
        "Apa kata-kata yang ingin kau dengar dari dirimu sendiri malam nanti?",
        "Kenapa kau harus jadi luar biasa kalau jadi biasa saja itu membosankan?",
        "Apa penghalang antara dirimu sekarang dan dirimu yang kau impikan?",
        "Bagaimana caramu membuktikan bahwa streak ini bukan sekadar angka?",
        "Apa misi rahasia yang sedang kau perjuangkan sendirian?",
        "Apa yang akan kau katakan pada rasa malasmu saat dia datang lagi?",
        "Sudahkah kau menjadi pahlawan bagi dirimu sendiri hari ini?",
        "Apa fokus utamamu yang tidak boleh diganggu gugat oleh siapapun?",
        "Bagaimana caramu membuat hari ini jadi sejarah pribadi yang hebat?",
        "Siapkah kau terbakar demi hasil yang luar biasa?",
        "Jadi, apakah kau akan mulai sekarang, atau mencari alasan lagi?"
    )

    fun getTodayPhrase(): String {
        // Menggunakan seed hari agar setiap hari ganti secara otomatis namun tetap sama seharian
        val dayIndex = (System.currentTimeMillis() / (1000 * 60 * 60 * 24)).toInt()
        return phrases[dayIndex % phrases.size]
    }
}