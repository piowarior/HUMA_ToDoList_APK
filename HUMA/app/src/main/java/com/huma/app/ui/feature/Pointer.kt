package com.huma.app.ui.feature

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp

@Composable
fun Pointer() {
    Box(
        modifier = Modifier
            .size(45.dp)
            .offset(y = (-158).dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(35.dp, 45.dp)) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, 0f)
                // Smoother top curve
                quadraticBezierTo(size.width / 2f, 15f, size.width, 0f)
                close()
            }

            // Shadow with blur effect (simulated with alpha)
            drawPath(
                path = path,
                color = Color.Black.copy(alpha = 0.15f),
            )

            // Main Pointer with vibrant Gradient
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF7676),
                        Color(0xFFFF1744)
                    )
                )
            )

            // Glossy Highlight
            val highlightPath = Path().apply {
                moveTo(size.width / 2f, size.height * 0.3f)
                lineTo(size.width * 0.25f, 8f)
                lineTo(size.width * 0.75f, 8f)
                close()
            }
            drawPath(highlightPath, Color.White.copy(alpha = 0.4f))
            
            // Pin Circle at the top (pivot look)
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(size.width / 2f, 5.dp.toPx())
            )
        }
    }
}
