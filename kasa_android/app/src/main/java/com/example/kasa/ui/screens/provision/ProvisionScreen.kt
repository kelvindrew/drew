package com.example.kasa.ui.screens.provision

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.model.ProvisionItemModel
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.theme.*
import com.example.kasa.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvisionScreen(
    onNavigateToShoppingList: () -> Unit,
    modifier: Modifier = Modifier
) {
    val household by KasaRepository.currentHousehold.collectAsState()
    val provisions by KasaRepository.provisions.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedItemToEdit by remember { mutableStateOf<ProvisionItemModel?>(null) }

    val currency = household?.currency ?: "USD"
    val tabs = listOf("Tous", "À acheter", "En stock", "Épuisé")

    val filteredList = when (selectedTab) {
        1 -> provisions.filter { it.isPlanned }
        2 -> provisions.filter { it.isBought }
        3 -> provisions.filter { it.isOutOfStock }
        else -> provisions
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = KasaPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter un article")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Action Header: Shopping list banner
            Button(
                onClick = onNavigateToShoppingList,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(KasaDimens.radiusMd),
                colors = ButtonDefaults.buttonColors(containerColor = KasaSecondary)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ouvrir la liste de courses (${provisions.count { it.isPlanned }})",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun article dans cette section.\nAppuyez sur + pour ajouter un article.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredList) { item ->
                        Box(modifier = Modifier.clickable { selectedItemToEdit = item }) {
                            ProvisionItemCard(item = item, currency = currency)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddProvisionDialog(
            currency = currency,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, qty, unit, price, priority, comment ->
                KasaRepository.addProvisionItem(name, qty, unit, price, priority, comment)
                showAddDialog = false
            }
        )
    }

    // Dialog: Edit or Delete Provision Item
    selectedItemToEdit?.let { item ->
        var name by remember { mutableStateOf(item.name) }
        var quantityText by remember { mutableStateOf(item.quantity.toString()) }
        var unit by remember { mutableStateOf(item.unit) }
        var priceText by remember { mutableStateOf(if (item.plannedPrice > 0) item.plannedPrice.toString() else "") }
        var priority by remember { mutableStateOf(item.priority) }
        var status by remember { mutableStateOf(item.status) }
        var comment by remember { mutableStateOf(item.comment) }

        AlertDialog(
            onDismissRequest = { selectedItemToEdit = null },
            title = { Text("Modifier l'article", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom de l'article") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Qté") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unité") },
                            singleLine = true,
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Prix estimé ($currency)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Commentaire / Note (optionnel)") },
                        placeholder = { Text("Ex: Marque bio, remarque...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Status selector
                    Text("Statut", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = status == "planned",
                            onClick = { status = "planned" },
                            label = { Text("À acheter") }
                        )
                        FilterChip(
                            selected = status == "bought",
                            onClick = { status = "bought" },
                            label = { Text("En stock") }
                        )
                        FilterChip(
                            selected = status == "out_of_stock",
                            onClick = { status = "out_of_stock" },
                            label = { Text("Épuisé") }
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            KasaRepository.deleteProvisionItem(item.id)
                            selectedItemToEdit = null
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = KasaError)
                    }
                    Button(
                        onClick = {
                            val qty = quantityText.toIntOrNull() ?: 1
                            val price = priceText.toDoubleOrNull() ?: 0.0
                            KasaRepository.updateProvisionItem(item.id, name.trim(), qty, unit.trim(), price, priority, status, comment.trim())
                            selectedItemToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KasaPrimary)
                    ) {
                        Text("Enregistrer", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedItemToEdit = null }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
fun ProvisionItemCard(
    item: ProvisionItemModel,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(KasaDimens.radiusMd),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.isHighPriority) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = KasaError.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Urgent",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = KasaError,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quantité: ${item.quantity} ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val authorText = buildString {
                    if (item.status == "bought" && !item.purchasedBy.isNullOrBlank()) {
                        append("Acheté par ${item.purchasedBy}")
                    } else if (item.addedBy.isNotBlank()) {
                        append("Ajouté par ${item.addedBy}")
                    }
                    if (!item.lastModifiedBy.isNullOrBlank()) {
                        if (isNotEmpty()) append(" • ")
                        append("✏️ Modifié par ${item.lastModifiedBy}")
                    }
                }
                if (authorText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = authorText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }

                if (item.comment.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "💬 ${item.comment}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(item.plannedPrice, currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = KasaPrimary
                )
                val statusText = when (item.status) {
                    "bought" -> "En stock"
                    "out_of_stock" -> "Épuisé"
                    else -> "À acheter"
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (item.status) {
                        "bought" -> KasaSuccess
                        "out_of_stock" -> KasaError
                        else -> KasaWarning
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProvisionDialog(
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: Int, unit: String, plannedPrice: Double, priority: String, comment: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("pièce") }
    var priceText by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("medium") }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter une provision", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de l'article") },
                    placeholder = { Text("Ex: Sac de riz") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Qté") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unité") },
                        singleLine = true,
                        modifier = Modifier.weight(1.5f)
                    )
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Prix estimé ($currency)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Commentaire / Note (optionnel)") },
                    placeholder = { Text("Ex: Marque spécifique, sans sucre...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toIntOrNull() ?: 1
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), qty, unit.trim(), price, priority, comment.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KasaPrimary)
            ) {
                Text("Ajouter", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
