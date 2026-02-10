package com.huma.app.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.huma.app.R

@Composable
fun SplashScreen(navController: NavController) {

    var startAnimation by remember { mutableStateOf(false) }

    // Animasi Scale (Logo membesar dikit)
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.2f else 0.8f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )

    // Animasi Alpha (Transparansi)
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(1200L) // Dikurangi jadi 1.2 detik (pas, gak kelamaan)

        // 🔥 Ganti destination ke "dashboard"
        navController.navigate("dashboard") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF6C63FF), Color(0xFF4B45B2))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo_huma),
                contentDescription = "HUMA Logo",
                modifier = Modifier
                    .size(140.dp)
                    .scale(scaleAnim)
                    .alpha(alphaAnim)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "HUMA",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                ),
                modifier = Modifier.alpha(alphaAnim)
            )

            Text(
                text = "Manage Your Life Better",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Light
                ),
                modifier = Modifier.alpha(alphaAnim)
            )
        }
    }
}