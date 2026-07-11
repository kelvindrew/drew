package com.connect.watch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class Watchface(val id: Int, val name: String, val author: String, val imageUrl: String, val tag: String)

val mockWatchfaces = listOf(
    Watchface(1, "OLED Minimal", "Studio Premium", "https://example.com/mock1.png", "ÉDITORIAL"),
    Watchface(2, "Chronograph Pro", "Rolex Style", "https://example.com/mock2.png", "NOUVEAU"),
    Watchface(3, "Fitness Circles", "Connect Health", "https://example.com/mock3.png", "POPULAIRE")
)

@Composable
fun WatchfaceStoreScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // App Store Dark Mode background
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 48.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        item {
            Text(
                "Aujourd'hui",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        }

        items(mockWatchfaces) { watchface ->
            AppStoreCard(watchface)
        }
    }
}

@Composable
fun AppStoreCard(watchface: Watchface) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)) // Large corner radius like iOS
            .background(Color(0xFF1C1C1E))
    ) {
        // Tag (Top)
        Text(
            text = watchface.tag,
            color = Color(0xFF007AFF), // Apple Blue
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp)
        )

        // Title
        Text(
            text = watchface.name,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 16.dp)
        )

        // Hero Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = watchface.imageUrl,
                contentDescription = watchface.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Fake watchface for visual until URL is real
            Text("⌚️", fontSize = 100.sp)
        }

        // Footer / Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(watchface.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(watchface.author, color = Color.Gray, fontSize = 13.sp)
            }

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2C2C2E), // Secondary elevated
                    contentColor = Color(0xFF007AFF) // Apple Blue text
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text("OBTENIR", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
