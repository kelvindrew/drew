package com.smartmedia.transfer.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartmedia.transfer.ui.components.GlassCard
import com.smartmedia.transfer.ui.components.NeonButton
import com.smartmedia.transfer.ui.theme.TurquoiseAccent
import com.smartmedia.transfer.ui.theme.NeonGreen

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Smart Media Transfer",
                style = MaterialTheme.typography.displayLarge.copy(color = Color.White),
                modifier = Modifier.padding(top = 32.dp, bottom = 8.dp)
            )
            Text(
                text = "Analysez et transférez vos fichiers",
                style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)),
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        when (val uiState = state) {
            is DashboardUiState.Initial -> {
                item {
                    NeonButton(
                        text = "Lancer le Scanner IA",
                        onClick = { viewModel.startScan() },
                        modifier = Modifier.fillMaxWidth().padding(32.dp)
                    )
                }
            }
            is DashboardUiState.Scanning -> {
                item {
                    CircularProgressIndicator(color = TurquoiseAccent)
                    Text("Analyse en cours...", color = Color.White, modifier = Modifier.padding(16.dp))
                }
            }
            is DashboardUiState.Error -> {
                item {
                    Text("Erreur: ${uiState.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is DashboardUiState.Success -> {
                val totalGB = 128f // Mock total for now
                val usedGB = uiState.result.totalSize.toFloat() / (1024 * 1024 * 1024)

                item {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            StorageRing(
                                usedPercentage = if(totalGB > 0) usedGB / totalGB else 0f,
                                totalSpaceGB = totalGB,
                                modifier = Modifier.fillMaxWidth(0.6f)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            val recGB = String.format("%.2f", uiState.result.recoverableSpace.toFloat() / (1024 * 1024 * 1024))
                            Text(
                                text = "$recGB GB Récupérables",
                                style = MaterialTheme.typography.titleLarge.copy(color = NeonGreen, fontWeight = FontWeight.Bold)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            NeonButton(
                                text = "Transférer vers PC",
                                onClick = { /* TODO Network */ },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recommandations",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }

                items(uiState.result.recommendedFiles) { file ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = TurquoiseAccent,
                                modifier = Modifier.size(40.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .weight(1f)
                            ) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    ),
                                    maxLines = 1
                                )
                                val mbSize = file.size / (1024 * 1024)
                                Text(
                                    text = "$mbSize MB • ${file.category.name}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
