package com.connect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Bonjour,",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Text(
                    "Premium User",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            // Fake Watch Icon/Status
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("⌚", fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Smart Coach Glassmorphism Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphism()
        ) {
            Text(
                "🧠 Smart Coach AI",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                aiRecommendation,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Statistiques du jour",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Health Stats Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "BPM",
                value = "72",
                unit = "bpm",
                gradient = listOf(Color(0xFFFF5252), Color(0xFFFF1744))
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Pas",
                value = "8,500",
                unit = "pas",
                gradient = listOf(Color(0xFF448AFF), Color(0xFF2979FF))
            )
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, unit: String, gradient: List<Color>) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradient))
            .padding(20.dp)
    ) {
        Text(title, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(unit, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}
