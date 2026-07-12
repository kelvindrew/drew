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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.betpro.android.presentation.viewmodel.StatisticsViewModel

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val betHistory by viewModel.betHistory.collectAsState()

    var showImageDialog by remember { mutableStateOf(false) }
    var selectedBase64Image by remember { mutableStateOf<String?>(null) }

    // Mock History Data: simulating capital progression over bets (would come from DB too)
    val capitalHistory = listOf(1000f, 990f, 970f, 1010f, 1000f, 1020f, 1010f, 1050f, 1040f, 1080f, 1250f)

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

        if (betHistory.isEmpty()) {
            Text("Aucun pari enregistré pour le moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(betHistory) { bet ->
                    val color = if (bet.status == "Gagné" || bet.status == "Preuve Disponible") Color(0xFF00E676) else Color(0xFFFF5252)
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
                                    Text(bet.matchName, fontWeight = FontWeight.Bold)
                                    Text(bet.status, color = color, style = MaterialTheme.typography.bodySmall)
                                }
                                Text(bet.amountOrOdds, color = color, fontWeight = FontWeight.Bold)
                            }
                            if (!bet.screenshotBase64.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        selectedBase64Image = bet.screenshotBase64
                                        showImageDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("📸 Voir la preuve visuelle")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showImageDialog && selectedBase64Image != null) {
        Dialog(onDismissRequest = { showImageDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    val bitmap = remember(selectedBase64Image) {
                        try {
                            val imageBytes = Base64.decode(selectedBase64Image, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Preuve du Pari",
                            modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 500.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    } else {
                        Text("Erreur lors du chargement de l'image", modifier = Modifier.padding(32.dp))
                    }
                    Button(onClick = { showImageDialog = false }, modifier = Modifier.padding(16.dp)) {
                        Text("Fermer")
                    }
                }
            }
        }
    }
}

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
