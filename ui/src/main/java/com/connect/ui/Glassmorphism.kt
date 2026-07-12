package com.connect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.appleCardStyle(): Modifier = composed {
    this
        .clip(RoundedCornerShape(20.dp))
        .background(Color(0xFF1C1C1E)) // Typical iOS Dark Mode Surface
        .padding(16.dp)
}

fun Modifier.glassmorphism(): Modifier = composed {
    this
        .clip(RoundedCornerShape(24.dp))
        .background(Color(0xFF333333).copy(alpha = 0.5f)) // Translucent gray
        .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
        .padding(16.dp)
}
