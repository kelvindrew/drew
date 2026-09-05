package com.example.kasa.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.model.CategoryModel
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.theme.*
import com.example.kasa.ui.components.BudgetSummaryCard
import com.example.kasa.ui.components.CategoryCard
import com.example.kasa.ui.components.DailyMealCard
import com.example.kasa.ui.components.DebtSettlementCard
import com.example.kasa.util.CurrencyFormatter
import com.example.kasa.util.DateFormatter

@Composable
fun BudgetScreen(
    onNavigateToProvision: () -> Unit,
    modifier: Modifier = Modifier
) {
    val household by KasaRepository.currentHousehold.collectAsState()
    val budget by KasaRepository.monthlyBudget.collectAsState()
    val categories by KasaRepository.categories.collectAsState()
    val expenses by KasaRepository.expenses.collectAsState()
    val provisions by KasaRepository.provisions.collectAsState()
    val members by KasaRepository.members.collectAsState()
    val currentUser by KasaRepository.currentUser.collectAsState()
    val currentMealPlan by KasaRepository.currentMealPlan.collectAsState()

    var showEditBudgetDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var selectedCategoryToEdit by remember { mutableStateOf<CategoryModel?>(null) }

    val currency = household?.currency ?: "USD"
    val totalSpent = expenses.sumOf { it.amount }
    val plannedProvisions = provisions.filter { it.isPlanned }
    val provisionBudget = categories.find { it.isProvision }?.plannedAmount ?: 0.0
    val provisionSpent = expenses.filter { exp -> categories.find { it.id == exp.categoryId }?.isProvision == true }.sumOf { it.amount }
    val provisionRemaining = provisionBudget - provisionSpent

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Month Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = DateFormatter.currentMonthName().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = household?.name ?: "Mon Foyer",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { showEditBudgetDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifier le budget", tint = KasaPrimary)
                }
            }
        }

        // Budget Summary Card
        item {
            Box(modifier = Modifier.clickable { showEditBudgetDialog = true }) {
                BudgetSummaryCard(
                    totalBudget = budget.totalBudget,
                    totalSpent = totalSpent,
                    currency = currency,
                    lastModifiedBy = budget.lastModifiedBy
                )
            }
        }

        // Équilibre des Comptes & Remboursements (Qui doit à qui)
        item {
            DebtSettlementCard(
                expenses = expenses,
                members = members,
                currentUserId = currentUser?.id ?: "",
                currency = currency
            )
        }

        // Repas du Jour (Menu & RSVP)
        item {
            DailyMealCard(
                mealPlan = currentMealPlan,
                currentUser = currentUser,
                members = members,
                isAdmin = currentUser?.role == "admin"
            )
        }

        // Provisions Quick Access Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToProvision() },
                shape = RoundedCornerShape(KasaDimens.radiusLg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(KasaPrimaryDark, KasaPrimary)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🍚", fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Provisions du foyer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${CurrencyFormatter.formatCompact(provisionRemaining, currency)} restants",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${plannedProvisions.size}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "à acheter",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Categories Title & Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Répartition par catégorie",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { showAddCategoryDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = KasaPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter", color = KasaPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Category Cards List
        if (categories.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(KasaDimens.radiusMd),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Aucune catégorie définie. Cliquez sur \"Ajouter\".", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(categories) { category ->
                val spentInCategory = expenses.filter { it.categoryId == category.id }.sumOf { it.amount }
                Box(modifier = Modifier.clickable { selectedCategoryToEdit = category }) {
                    CategoryCard(
                        category = category,
                        spent = spentInCategory,
                        currency = currency
                    )
                }
            }
        }
    }

    // Dialog: Edit Monthly Budget
    if (showEditBudgetDialog) {
        var budgetText by remember { mutableStateOf(if (budget.totalBudget > 0) budget.totalBudget.toString() else "") }
        AlertDialog(
            onDismissRequest = { showEditBudgetDialog = false },
            title = { Text("Modifier le budget mensuel", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Définissez le montant total alloué pour ce mois ($currency) :", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { budgetText = it },
                        label = { Text("Montant du budget") },
                        placeholder = { Text("Ex: 1200") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = budgetText.toDoubleOrNull() ?: 0.0
                        KasaRepository.updateMonthlyBudget(amount)
                        showEditBudgetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KasaPrimary)
                ) {
                    Text("Enregistrer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBudgetDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog: Add Category
    if (showAddCategoryDialog) {
        var name by remember { mutableStateOf("") }
        var emoji by remember { mutableStateOf("📦") }
        var plannedText by remember { mutableStateOf("") }
        var isProvision by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Ajouter une catégorie", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom de la catégorie") },
                        placeholder = { Text("Ex: Factures, Loisirs...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it },
                        label = { Text("Émoji") },
                        placeholder = { Text("Ex: 🏠, 🚗, 💡") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = plannedText,
                        onValueChange = { plannedText = it },
                        label = { Text("Budget prévu ($currency)") },
                        placeholder = { Text("0.00") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val planned = plannedText.toDoubleOrNull() ?: 0.0
                            KasaRepository.addCategory(name.trim(), emoji.trim(), planned, isProvision)
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KasaPrimary)
                ) {
                    Text("Ajouter", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog: Edit or Delete Category
    selectedCategoryToEdit?.let { cat ->
        var name by remember { mutableStateOf(cat.name) }
        var emoji by remember { mutableStateOf(cat.emoji) }
        var plannedText by remember { mutableStateOf(if (cat.plannedAmount > 0) cat.plannedAmount.toString() else "") }
        var isProvision by remember { mutableStateOf(cat.isProvision) }

        AlertDialog(
            onDismissRequest = { selectedCategoryToEdit = null },
            title = { Text("Modifier la catégorie", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nom de la catégorie") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it },
                        label = { Text("Émoji") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = plannedText,
                        onValueChange = { plannedText = it },
                        label = { Text("Budget prévu ($currency)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            KasaRepository.deleteCategory(cat.id)
                            selectedCategoryToEdit = null
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = KasaError)
                    }
                    Button(
                        onClick = {
                            val planned = plannedText.toDoubleOrNull() ?: 0.0
                            KasaRepository.updateCategory(cat.id, name.trim(), emoji.trim(), planned, isProvision)
                            selectedCategoryToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KasaPrimary)
                    ) {
                        Text("Enregistrer", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCategoryToEdit = null }) {
                    Text("Fermer")
                }
            }
        )
    }
}
