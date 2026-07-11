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

fun Modifier.glassmorphism(): Modifier = composed {
    this
        .clip(RoundedCornerShape(16.dp))
        .background(Color.White.copy(alpha = 0.1f))
        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        .padding(16.dp)
}
