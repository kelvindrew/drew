package com.example.kasa.ui.screens.operations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.model.ExpenseModel
import com.example.kasa.data.model.ProvisionItemModel
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.theme.*
import com.example.kasa.ui.screens.provision.AddProvisionDialog
import com.example.kasa.ui.screens.provision.ProvisionItemCard
import com.example.kasa.util.CurrencyFormatter
import com.example.kasa.util.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationsScreen(
    initialSubTab: Int = 0,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToShoppingList: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMainTab by remember { mutableIntStateOf(initialSubTab) } // 0 = Dépenses, 1 = Provisions

    val household by KasaRepository.currentHousehold.collectAsState()
    val expenses by KasaRepository.expenses.collectAsState()
    val provisions by KasaRepository.provisions.collectAsState()
    val categories by KasaRepository.categories.collectAsState()
    val currency = household?.currency ?: "USD"

    // Sub filter for provisions
    var provisionFilterIndex by remember { mutableIntStateOf(0) }
    val provisionFilters = listOf("Tous", "À acheter", "En stock", "Épuisé")

    // Dialog states
    var showAddProvisionDialog by remember { mutableStateOf(false) }
    var selectedExpenseToEdit by remember { mutableStateOf<ExpenseModel?>(null) }
    var selectedProvisionToEdit by remember { mutableStateOf<ProvisionItemModel?>(null) }

    val totalSpent = expenses.sumOf { it.amount }
    val plannedCount = provisions.count { it.isPlanned }

    val filteredProvisions = when (provisionFilterIndex) {
        1 -> provisions.filter { it.isPlanned }
        2 -> provisions.filter { it.isBought }
        3 -> provisions.filter { it.isOutOfStock }
        else -> provisions
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedMainTab == 0) {
                        onNavigateToAddExpense()
                    } else {
                        showAddProvisionDialog = true
                    }
                },
                containerColor = if (selectedMainTab == 0) KasaPrimary else KasaSecondary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (selectedMainTab == 0) "Ajouter une dépense" else "Ajouter une provision"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Main Segmented Switch
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Option 1: Dépenses
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { selectedMainTab = 0 },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedMainTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (selectedMainTab == 0) 2.dp else 0.dp
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💸", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dépenses",
                                fontWeight = if (selectedMainTab == 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedMainTab == 0) KasaPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Option 2: Provisions
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { selectedMainTab = 1 },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedMainTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (selectedMainTab == 1) 2.dp else 0.dp
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🛒", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Provisions",
                                fontWeight = if (selectedMainTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedMainTab == 1) KasaSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (plannedCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = KasaSecondary
                                ) {
                                    Text(
                                        text = "$plannedCount",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab 1: Dépenses View
            if (selectedMainTab == 0) {
                // Summary Spent Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(KasaDimens.radiusMd),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Dépensé ce mois",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.format(totalSpent, currency),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text("${expenses.size} dépenses", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Historique des dépenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (expenses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucune dépense enregistrée.\nAppuyez sur + pour ajouter une dépense.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(expenses) { expense ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedExpenseToEdit = expense },
                                shape = RoundedCornerShape(KasaDimens.radiusMd),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                RoundedCornerShape(KasaDimens.radiusMd)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(expense.categoryEmoji, fontSize = 20.sp)
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = expense.description.ifEmpty { expense.categoryName },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        val subText = buildString {
                                            append(expense.userName)
                                            append(" • ")
                                            append(DateFormatter.formatDate(expense.date))
                                            if (!expense.lastModifiedByName.isNullOrBlank()) {
                                                append(" • ✏️ Modifié par ")
                                                append(expense.lastModifiedByName)
                                            }
                                        }
                                        Text(
                                            text = subText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 12.sp
                                        )
                                        if (expense.comment.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "💬 ${expense.comment}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = KasaPrimary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = "- ${CurrencyFormatter.format(expense.amount, currency)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 2: Provisions & Stocks View
                Button(
                    onClick = onNavigateToShoppingList,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(KasaDimens.radiusMd),
                    colors = ButtonDefaults.buttonColors(containerColor = KasaSecondary)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Ouvrir la liste de courses ($plannedCount)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Provisions Filter Chips
                PrimaryTabRow(
                    selectedTabIndex = provisionFilterIndex,
                    containerColor = Color.Transparent
                ) {
                    provisionFilters.forEachIndexed { index, title ->
                        Tab(
                            selected = provisionFilterIndex == index,
                            onClick = { provisionFilterIndex = index },
                            text = { Text(title, fontWeight = if (provisionFilterIndex == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredProvisions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun article dans cette section.\nAppuyez sur + pour ajouter un article.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredProvisions) { item ->
                            Box(modifier = Modifier.clickable { selectedProvisionToEdit = item }) {
                                ProvisionItemCard(item = item, currency = currency)
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs: Add Provision
    if (showAddProvisionDialog) {
        AddProvisionDialog(
            currency = currency,
            onDismiss = { showAddProvisionDialog = false },
            onConfirm = { name, qty, unit, price, priority, comment ->
                KasaRepository.addProvisionItem(name, qty, unit, price, priority, comment)
                showAddProvisionDialog = false
            }
        )
    }

    // Dialogs: Edit Expense
    selectedExpenseToEdit?.let { exp ->
        var amountText by remember { mutableStateOf(exp.amount.toString()) }
        var description by remember { mutableStateOf(exp.description) }
        var comment by remember { mutableStateOf(exp.comment) }
        var selectedCategoryId by remember { mutableStateOf(exp.categoryId) }

        AlertDialog(
            onDismissRequest = { selectedExpenseToEdit = null },
            title = { Text("Modifier la dépense", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Montant ($currency)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Commentaire / Note (optionnel)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Catégorie", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategoryId == cat.id,
                                onClick = { selectedCategoryId = cat.id },
                                label = { Text("${cat.emoji} ${cat.name}") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            KasaRepository.deleteExpense(exp.id)
                            selectedExpenseToEdit = null
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = KasaError)
                    }
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: exp.amount
                            KasaRepository.updateExpense(
                                id = exp.id,
                                categoryId = selectedCategoryId,
                                amount = amt,
                                description = description.trim(),
                                comment = comment.trim(),
                                date = exp.date
                            )
                            selectedExpenseToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KasaPrimary)
                    ) {
                        Text("Enregistrer", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedExpenseToEdit = null }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Dialogs: Edit Provision
    selectedProvisionToEdit?.let { item ->
        var name by remember { mutableStateOf(item.name) }
        var quantityText by remember { mutableStateOf(item.quantity.toString()) }
        var unit by remember { mutableStateOf(item.unit) }
        var priceText by remember { mutableStateOf(if (item.plannedPrice > 0) item.plannedPrice.toString() else "") }
        var priority by remember { mutableStateOf(item.priority) }
        var status by remember { mutableStateOf(item.status) }

        AlertDialog(
            onDismissRequest = { selectedProvisionToEdit = null },
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
                            selectedProvisionToEdit = null
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = KasaError)
                    }
                    Button(
                        onClick = {
                            val qty = quantityText.toIntOrNull() ?: 1
                            val price = priceText.toDoubleOrNull() ?: 0.0
                            KasaRepository.updateProvisionItem(item.id, name.trim(), qty, unit.trim(), price, priority, status)
                            selectedProvisionToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KasaPrimary)
                    ) {
                        Text("Enregistrer", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProvisionToEdit = null }) {
                    Text("Fermer")
                }
            }
        )
    }
}
