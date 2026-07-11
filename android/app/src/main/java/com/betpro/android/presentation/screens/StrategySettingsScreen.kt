package com.betpro.android.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StrategySettingsScreen() {
    var isSimulationMode by remember { mutableStateOf(true) }
    var isAIEngineEnabled by remember { mutableStateOf(true) }
    var baseStake by remember { mutableStateOf("10.0") }
    var stopLoss by remember { mutableStateOf("100.0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Card
        SettingsCard(title = "Mode de Fonctionnement") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (isSimulationMode) "Mode Simulation (Test)" else "Mode Réel (Argent Réel)")
                Switch(checked = isSimulationMode, onCheckedChange = { isSimulationMode = it })
            }
        }

        // AI Engine Card
        SettingsCard(title = "Moteur d'Intelligence Artificielle") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Validation Gemini IA")
                Switch(checked = isAIEngineEnabled, onCheckedChange = { isAIEngineEnabled = it })
            }
            Text("Si activé, l'algorithme demande l'avis de l'IA avant de placer le pari.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Capital Management Card
        SettingsCard(title = "Gestion du Capital (Martingale)") {
            OutlinedTextField(
                value = baseStake,
                onValueChange = { baseStake = it },
                label = { Text("Mise de base ($)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = stopLoss,
                onValueChange = { stopLoss = it },
                label = { Text("Stop-Loss (Arrêt si solde < $)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Allowed Markets Card
        SettingsCard(title = "Marchés Autorisés") {
            MarketCheckbox(label = "Victoire 1 / Victoire 2")
            MarketCheckbox(label = "Double Chance (1X, X2)")
            MarketCheckbox(label = "Les deux équipes marquent (BTTS)")
            MarketCheckbox(label = "Total Plus de 2.5 Buts")
        }
    }
}

@Composable
fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 12.dp))
            content()
        }
    }
}

@Composable
fun MarketCheckbox(label: String) {
    var checked by remember { mutableStateOf(true) }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(checked = checked, onCheckedChange = { checked = it })
        Text(text = label)
    }
}
