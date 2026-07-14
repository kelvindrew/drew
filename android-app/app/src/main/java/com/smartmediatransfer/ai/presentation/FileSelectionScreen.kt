package com.smartmediatransfer.ai.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartmediatransfer.ai.presentation.theme.ElectricBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSelectionScreen(
    onFilesAdded: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sélection de Fichiers", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFilesAdded,
                containerColor = ElectricBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Files", tint = androidx.compose.ui.graphics.Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Aucun fichier sélectionné",
                color = androidx.compose.ui.graphics.Color.Gray,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Appuyez sur le bouton + pour ajouter des médias à la file d'attente.",
                color = androidx.compose.ui.graphics.Color.Gray,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
