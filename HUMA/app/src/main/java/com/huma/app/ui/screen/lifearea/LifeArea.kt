package com.huma.app.data.local

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class LifeArea(val label: String, val color: Color, val icon: ImageVector) {
    AKADEMIK("Akademik", Color(0xFF6C63FF), Icons.Default.School),
    KESEHATAN("Kesehatan", Color(0xFF4CAF50), Icons.Default.Favorite),
    SPIRITUAL("Spiritual", Color(0xFFFFD700), Icons.Default.SelfImprovement),
    RUMAH_TANGGA("Rumah Tangga", Color(0xFF03A9F4), Icons.Default.Home),
    SOSIAL("Sosial", Color(0xFFE91E63), Icons.Default.Group),
    PRIBADI("Pribadi", Color(0xFF9C27B0), Icons.Default.Person)
}