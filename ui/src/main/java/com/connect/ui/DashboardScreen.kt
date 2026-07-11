package com.connect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val aiRecommendation by viewModel.aiRecommendation.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 20.dp) // Large top padding typical of iOS Large Titles
    ) {
        // iOS Large Title Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "Résumé",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            // Profile / Watch Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2C2E)),
                contentAlignment = Alignment.Center
            ) {
                Text("JD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // AI Coaching Alert (iOS Notification Style)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphism()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✨", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "SIRI COACH",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                aiRecommendation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "TENDANCES",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Apple Fitness Style Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AppleStatCard(
                modifier = Modifier.weight(1f),
                title = "Bouger",
                value = "450",
                unit = "kcal",
                iconColor = Color(0xFFFF2D55) // Apple Pink/Red
            )
            AppleStatCard(
                modifier = Modifier.weight(1f),
                title = "M'entraîner",
                value = "30",
                unit = "MIN",
                iconColor = Color(0xFF34C759) // Apple Green
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AppleStatCard(
                modifier = Modifier.weight(1f),
                title = "Me lever",
                value = "8",
                unit = "h",
                iconColor = Color(0xFF00C7BE) // Apple Cyan
            )
            AppleStatCard(
                modifier = Modifier.weight(1f),
                title = "BPM",
                value = "72",
                unit = "Moy",
                iconColor = Color(0xFFFF3B30) // Red
            )
        }
    }
}

@Composable
fun AppleStatCard(modifier: Modifier = Modifier, title: String, value: String, unit: String, iconColor: Color) {
    Column(
        modifier = modifier.appleCardStyle(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(8.dp).clip(CircleShape).background(iconColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                title.uppercase(),
                color = iconColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                unit,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}
