package com.example.kasa.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.theme.KasaDimens
import com.example.kasa.theme.KasaError
import com.example.kasa.theme.KasaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinHouseholdScreen(
    onHouseholdJoined: () -> Unit,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isValid = code.trim().isNotBlank() && userName.trim().isNotBlank() && !isLoading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rejoindre un foyer", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isLoading) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Entrez le code d'invitation",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Demandez le code à l'administrateur du foyer pour rejoindre et synchroniser toutes les données.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(28.dp))

                OutlinedTextField(
                    value = userName,
                    onValueChange = {
                        userName = it
                        errorMessage = null
                    },
                    label = { Text("Votre prénom ou nom") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(KasaDimens.radiusMd)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it.uppercase()
                        errorMessage = null
                    },
                    label = { Text("Code d'invitation") },
                    placeholder = { Text("Ex: UNK-8F3A2B") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(KasaDimens.radiusMd)
                )

                errorMessage?.let { err ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = err,
                        color = KasaError,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Button(
                onClick = {
                    if (isValid) {
                        isLoading = true
                        errorMessage = null
                        KasaRepository.joinHousehold(
                            code = code,
                            userName = userName.trim(),
                            onSuccess = {
                                isLoading = false
                                onHouseholdJoined()
                            },
                            onError = { err ->
                                isLoading = false
                                errorMessage = err
                            }
                        )
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(KasaDimens.radiusMd),
                colors = ButtonDefaults.buttonColors(containerColor = KasaPrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Recherche du foyer...", fontWeight = FontWeight.Bold, color = Color.White)
                } else {
                    Text(text = "Rejoindre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
