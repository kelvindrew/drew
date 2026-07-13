package com.connect.watch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
    Watchface(1, "OLED Minimal", "Xiaomi A...", "https://example.com/mock1.png", "ÉDITORIAL"),
    Watchface(2, "Chronograph Pro", "Rolex Style", "https://example.com/mock2.png", "NOUVEAU")
)

@Composable
fun WatchfaceStoreScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // App Store Dark Mode background
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 48.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    "Cadrans",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    "(Watchfaces Store)",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    "Aujourd'hui",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )
            }
        }

        items(mockWatchfaces) { watchface ->
            AppStoreCard(watchface)
        }
    }
}

@Composable
fun AppStoreCard(watchface: Watchface) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF2C3E50)) // Deep blue background as seen in mockup
    ) {
        // Tag (Top Left)
        Box(
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF3498DB))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = watchface.tag,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        // Title (Top Rightish)
        Text(
            text = watchface.name,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 16.dp, end = 16.dp).align(Alignment.TopEnd)
        )

        // Mock Watch image (Center)
        Box(
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Using a simple square/circle box with an X to simulate the minimalist analog watch in mockup
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.Black)
            ) {
                 // Simulated hands of the clock
                 Box(modifier = Modifier.width(2.dp).height(40.dp).background(Color.White).align(Alignment.Center).offset(y = (-20).dp))
                 Box(modifier = Modifier.width(40.dp).height(2.dp).background(Color.White).align(Alignment.Center).offset(x = 20.dp, y = (-20).dp))
            }
        }

        // Footer / Action (Bottom)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mock Avatar
                Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color.LightGray), contentAlignment = Alignment.Center) {
                    Text("👨", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(watchface.author, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
            }

            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("OBTENIR", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
