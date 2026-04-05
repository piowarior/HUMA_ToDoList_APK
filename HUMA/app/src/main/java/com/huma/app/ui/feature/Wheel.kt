package com.huma.app.ui.feature

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Wheel(
    challenges: List<String>,
    rotation: Float
) {
    val density = LocalDensity.current
    val fontSize = with(density) { 12.sp.toPx() }

    Box(
        modifier = Modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = rotation
                }
        ) {
            val centerOffset = center // center is a property of DrawScope (returns Offset)
            val radius = size.minDimension / 2f
            val sweep = 360f / challenges.size.coerceAtLeast(1)

            challenges.forEachIndexed { index, text ->
                val startAngle = sweep * index
                
                // Draw segment
                val color = if (index % 2 == 0) Color(0xFF6C63FF) else Color(0xFF9D8CFF)
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true
                )

                // Draw motif/dots
                rotate(startAngle + sweep / 2f) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = 4.dp.toPx(),
                        center = Offset(centerOffset.x + radius * 0.8f, centerOffset.y)
                    )
                }

                // Draw Text
                drawContext.canvas.nativeCanvas.apply {
                    save()
                    // Rotate the canvas to draw text horizontally within the segment
                    rotate((startAngle + sweep / 2f), centerOffset.x, centerOffset.y)
                    val paint = android.graphics.Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        this.textSize = fontSize
                        this.textAlign = android.graphics.Paint.Align.CENTER
                        this.isFakeBoldText = true
                    }
                    // Limit text length
                    val displayLines = if (text.length > 15) text.take(13) + ".." else text
                    // Position text along the radius
                    drawText(displayLines, centerOffset.x + radius * 0.55f, centerOffset.y + fontSize / 3, paint)
                    restore()
                }
            }

            // Outer border
            drawCircle(
                color = Color.White,
                radius = radius,
                style = Stroke(width = 8f),
                center = centerOffset
            )
            
            // Inner lines separating segments
            challenges.forEachIndexed { index, _ ->
                val angleInRadians = (sweep * index) * (PI / 180f).toFloat()
                drawLine(
                    color = Color.White.copy(alpha = 0.3f),
                    start = centerOffset,
                    end = Offset(
                        centerOffset.x + radius * cos(angleInRadians),
                        centerOffset.y + radius * sin(angleInRadians)
                    ),
                    strokeWidth = 2f
                )
            }
        }
    }
}
