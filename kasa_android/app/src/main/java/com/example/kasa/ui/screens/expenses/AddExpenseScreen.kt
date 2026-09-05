package com.example.kasa.ui.screens.expenses

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.theme.*
import com.example.kasa.util.ReceiptScanner
import com.example.kasa.util.ScannedReceipt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val household by KasaRepository.currentHousehold.collectAsState()
    val categories by KasaRepository.categories.collectAsState()
    val currency = household?.currency ?: "USD"

    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    // Scanner States
    var isScanning by remember { mutableStateOf(false) }
    var scannedImageUri by remember { mutableStateOf<Uri?>(null) }
    var scannedReceiptInfo by remember { mutableStateOf<ScannedReceipt?>(null) }
    var scanErrorMessage by remember { mutableStateOf<String?>(null) }
    var showRawTextDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scannedImageUri = uri
            isScanning = true
            scanErrorMessage = null
            ReceiptScanner.scanReceipt(
                context = context,
                imageUri = uri,
                onSuccess = { receipt ->
                    isScanning = false
                    scannedReceiptInfo = receipt

                    // Auto-fill amount if detected
                    receipt.totalAmount?.let { total ->
                        amountText = String.format(java.util.Locale.US, "%.2f", total)
                    }

                    // Auto-fill merchant / description
                    receipt.merchantName?.let { merchant ->
                        if (description.isBlank()) {
                            description = merchant
                        }
                    }

                    // Auto-match category
                    receipt.suggestedCategoryType?.let { catType ->
                        val matched = categories.find { cat ->
                            when (catType) {
                                "provisions" -> cat.isProvision || cat.name.contains("Alimentation", ignoreCase = true) || cat.name.contains("Courses", ignoreCase = true)
                                "health" -> cat.name.contains("Santé", ignoreCase = true) || cat.name.contains("Médical", ignoreCase = true)
                                "transport" -> cat.name.contains("Transport", ignoreCase = true) || cat.name.contains("Carburant", ignoreCase = true)
                                "restaurant" -> cat.name.contains("Restaurant", ignoreCase = true) || cat.name.contains("Sortie", ignoreCase = true)
                                "bills" -> cat.name.contains("Facture", ignoreCase = true) || cat.name.contains("Logement", ignoreCase = true)
                                else -> false
                            }
                        }
                        if (matched != null) {
                            selectedCategoryId = matched.id
                        }
                    }
                },
                onError = { e ->
                    isScanning = false
                    scanErrorMessage = "Impossible de lire le ticket : ${e.localizedMessage ?: "Erreur inconnue"}"
                }
            )
        }
    }

    val isValid = amountText.toDoubleOrNull() != null && (amountText.toDoubleOrNull() ?: 0.0) > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter une dépense", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 🧾 Smart Receipt Scanner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (scannedReceiptInfo != null) KasaSuccess.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🧾", fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Scanner un Ticket de Caisse",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Remplissage automatique par IA",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scanner", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        // Scanning Progress Bar
                        if (isScanning) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🔍 Analyse intelligente du ticket en cours...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Success Result Banner
                        scannedReceiptInfo?.let { receipt ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = KasaSuccess.copy(alpha = 0.12f)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "✅ Ticket détecté : ${receipt.merchantName ?: "Marchand"}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = KasaSuccess
                                        )
                                        if (receipt.totalAmount != null) {
                                            Text(
                                                text = "Montant extrait : ${receipt.totalAmount} $currency",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    TextButton(onClick = { showRawTextDialog = true }) {
                                        Text("Voir texte", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Error Banner
                        scanErrorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "⚠️ $err",
                                fontSize = 12.sp,
                                color = KasaError
                            )
                        }
                    }
                }

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Montant ($currency) *") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(KasaDimens.radiusMd),
                    leadingIcon = {
                        Text(
                            text = if (currency == "EUR") "€" else if (currency == "USD") "$" else currency,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                )

                // Description Field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Motif *") },
                    placeholder = { Text("Ex: Carrefour, Supermarché, Facture...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(KasaDimens.radiusMd)
                )

                // Comment Field
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Commentaire / Note (optionnel)") },
                    placeholder = { Text("Ex: Payé par carte, note personnelle...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(KasaDimens.radiusMd)
                )

                // Category Selector
                Text("Sélectionner la catégorie", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategoryId == cat.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text("${cat.emoji} ${cat.name}", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (isValid) {
                        KasaRepository.addExpense(
                            categoryId = selectedCategoryId,
                            amount = amount,
                            description = description.trim(),
                            comment = comment.trim()
                        )
                        onBack()
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .height(54.dp),
                shape = RoundedCornerShape(KasaDimens.radiusMd),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enregistrer la dépense", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    if (showRawTextDialog && scannedReceiptInfo != null) {
        AlertDialog(
            onDismissRequest = { showRawTextDialog = false },
            title = { Text("🧾 Texte extrait du ticket", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = scannedReceiptInfo?.rawText ?: "",
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showRawTextDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }
}
