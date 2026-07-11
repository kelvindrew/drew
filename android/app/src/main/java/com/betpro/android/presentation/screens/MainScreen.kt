package com.betpro.android.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.betpro.android.domain.model.SportEvent
import com.betpro.android.presentation.components.MatchCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    // Mock data for UI presentation
    val events = listOf(
        SportEvent("1", "Real Madrid", "Manchester City", System.currentTimeMillis(), 2.65, 3.40, 2.50, 1.65, 1.80),
        SportEvent("2", "Arsenal", "Chelsea", System.currentTimeMillis(), 1.85, 3.60, 4.20, 1.90, 2.10)
    )

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Upcoming", "Live", "Settings")

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var betSlipCount by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BETPRO", fontWeight = FontWeight.Bold) },
                actions = {
                    Text(
                        text = "$ 1,250.00",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (betSlipCount > 0) {
                FloatingActionButton(
                    onClick = { showBottomSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text("Slip ($betSlipCount)", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(events) { event ->
                            MatchCard(
                                event = event,
                                onAiClick = { /* Show AI Analysis */ },
                                onOddsClick = { _, _, _ ->
                                    betSlipCount++
                                }
                            )
                        }
                    }
                }
                2 -> {
                    StrategySettingsScreen()
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Panier Interactif", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Sélections : $betSlipCount")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { /* Simulate Bet */ }, modifier = Modifier.fillMaxWidth()) {
                        Text("Simuler / Placer le pari")
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
