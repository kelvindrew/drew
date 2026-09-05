package com.example.kasa.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.model.ExpenseModel
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.theme.KasaDimens
import com.example.kasa.theme.KasaError
import com.example.kasa.theme.KasaPrimary
import com.example.kasa.util.CurrencyFormatter
import com.example.kasa.util.DateFormatter

@Composable
fun ExpensesScreen(
    onNavigateToAddExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    val household by KasaRepository.currentHousehold.collectAsState()
    val expenses by KasaRepository.expenses.collectAsState()
    val categories by KasaRepository.categories.collectAsState()
    val currency = household?.currency ?: "USD"
    val totalSpent = expenses.sumOf { it.amount }

    var selectedExpenseToEdit by remember { mutableStateOf<ExpenseModel?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddExpense,
                containerColor = KasaPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter une dépense")
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

            // Total spent card
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

            Spacer(modifier = Modifier.height(16.dp))

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
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
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
        }
    }

    // Dialog: Edit or Delete Expense
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
}
