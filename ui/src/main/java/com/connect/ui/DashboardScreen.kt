package com.connect.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val aiRecommendation by viewModel.aiRecommendation.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Force black background as per design
            .verticalScroll(scrollState)
            .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 20.dp)
    ) {
        // "Large Title"
        Text(
            "Large Title",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Résumé",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
            // Profile Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFEFEF)), // Yellowish tint from mockup? Let's use image
                contentAlignment = Alignment.Center
            ) {
                // We'll simulate the avatar image with a generic shape or Coil for now
                // Fallback text if no image
                Text("👨", fontSize = 24.sp)
            }
        }

        // Siri Coach Glass Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphism()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mockup shows Siri glowing icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFF8888FF), Color(0xFFFF55AA), Color(0xFF55DDFF)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Siri Coach",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Vous avez bien récupéré aujourd'hui. Une séance de fractionné est conseillée.", // Mock text from image
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2x2 Grid stats
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AppleStatCard(
                modifier = Modifier.weight(1f).height(120.dp),
                title = "Bouger",
                value = "450",
                unit = "kcal",
                bgColor = Color(0xFFFF4B6E) // Pinkish red
            )
            AppleStatCard(
                modifier = Modifier.weight(1f).height(120.dp),
                title = "M'entraîner",
                value = "30",
                unit = "MIN",
                bgColor = Color(0xFF5DDF6A) // Light green
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AppleStatCard(
                modifier = Modifier.weight(1f).height(120.dp),
                title = "Me lever",
                value = "8",
                unit = "h",
                bgColor = Color(0xFF1CB5E0) // Cyan
            )
            AppleStatCard(
                modifier = Modifier.weight(1f).height(120.dp),
                title = "BPM",
                value = "72",
                unit = "Moy",
                bgColor = Color(0xFFFF3B30) // Red
            )
        }
    }
}

@Composable
fun AppleStatCard(modifier: Modifier = Modifier, title: String, value: String, unit: String, bgColor: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            title,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                unit,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}
