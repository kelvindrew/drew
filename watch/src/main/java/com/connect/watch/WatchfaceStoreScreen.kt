package com.connect.watch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class Watchface(val id: Int, val name: String, val author: String, val imageUrl: String)

val mockWatchfaces = listOf(
    Watchface(1, "Neon Cyber", "Connect Premium", "https://example.com/mock1.png"),
    Watchface(2, "Classic Gold", "Rolex Studio", "https://example.com/mock2.png"),
    Watchface(3, "Minimalist White", "Zenith", "https://example.com/mock3.png"),
    Watchface(4, "Sport Chrono", "Garmin Style", "https://example.com/mock4.png")
)

@Composable
fun WatchfaceStoreScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Fallback to theme background if needed
            .padding(16.dp)
    ) {
        Text(
            "Boutique de Cadrans",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Personnalisez votre montre avec notre sélection Premium.",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(mockWatchfaces) { watchface ->
                WatchfaceCard(watchface)
            }
        }
    }
}

@Composable
fun WatchfaceCard(watchface: Watchface) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E293B))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = watchface.imageUrl,
                contentDescription = watchface.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                // Use a placeholder or error in real scenario
            )
            // Fake watchface for visual until URL is real
            Text("⌚", fontSize = 48.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = watchface.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = watchface.author,
            color = Color(0xFF64FFDA), // Theme Primary
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
