package com.betpro.android.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatisticsScreen() {
    // Mock History Data: simulating capital progression over bets
    val capitalHistory = listOf(1000f, 990f, 970f, 1010f, 1000f, 1020f, 1010f, 1050f, 1040f, 1080f, 1250f)

    // Mock Bet History
    val recentBets = listOf(
        BetHistoryItem("Real Madrid vs Man City", "Gagné", "+ 40.00 $", Color(0xFF00E676)),
        BetHistoryItem("Arsenal vs Chelsea", "Perdu", "- 10.00 $", Color(0xFFFF5252)),
        BetHistoryItem("PSG vs Bayern", "Gagné", "+ 20.00 $", Color(0xFF00E676)),
        BetHistoryItem("Barcelone vs Juventus", "Perdu", "- 10.00 $", Color(0xFFFF5252))
    )

    // Mock Base64 Image string for demonstration
    // In reality, this comes from DataStore/Room populated by AutomationWorker
    val mockScreenshotBase64 = ""

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Évolution du Capital", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            CapitalChart(data = capitalHistory, modifier = Modifier.fillMaxSize().padding(16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Historique des Paris", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(recentBets) { bet ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(bet.match, fontWeight = FontWeight.Bold)
                                Text(bet.status, color = bet.color, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(bet.amount, color = bet.color, fontWeight = FontWeight.Bold)
                        }
                        if (bet.status == "Gagné" || bet.status == "Preuve Disponible") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { /* Open full screen image using Base64 */ }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.primary)) {
                                Text("📸 Voir la preuve visuelle")
                            }
                        }
                    }
                }
            }
        }
    }
}

data class BetHistoryItem(val match: String, val status: String, val amount: String, val color: Color)

@Composable
fun CapitalChart(data: List<Float>, modifier: Modifier = Modifier) {
    if (data.isEmpty()) return

    val maxVal = data.maxOrNull() ?: 1f
    val minVal = data.minOrNull() ?: 0f
    val range = maxVal - minVal

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (data.size - 1).coerceAtLeast(1)

        val path = Path()
        data.forEachIndexed { index, value ->
            val x = index * stepX
            // Normalize value to height (invert Y because Canvas 0,0 is top-left)
            // Prevent division by zero if all values are equal
            val normalizedY = if (range > 0f) (value - minVal) / range else 0.5f
            val y = height - (normalizedY * height)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = Color(0xFF00E676),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
