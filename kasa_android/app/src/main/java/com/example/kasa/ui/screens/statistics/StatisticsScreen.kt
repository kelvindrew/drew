package com.example.kasa.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.theme.*
import com.example.kasa.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier
) {
    val household by KasaRepository.currentHousehold.collectAsState()
    val budget by KasaRepository.monthlyBudget.collectAsState()
    val categories by KasaRepository.categories.collectAsState()
    val expenses by KasaRepository.expenses.collectAsState()

    var selectedExpenseToEdit by remember { mutableStateOf<com.example.kasa.data.model.ExpenseModel?>(null) }

    val currency = household?.currency ?: "USD"
    val totalBudget = budget.totalBudget
    val totalSpent = expenses.sumOf { it.amount }
    val balance = totalBudget - totalSpent
    val isOverBudget = totalBudget > 0.0 && totalSpent > totalBudget
    val budgetConsumptionPercent = if (totalBudget > 0.0) {
        ((totalSpent / totalBudget) * 100).toInt()
    } else 0
    val progressFraction = if (totalBudget > 0.0) {
        (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f)
    } else 0f

    val paletteColors = listOf(
        KasaPrimary, KasaSecondary, KasaAccent, KasaWarning,
        Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFF3B82F6),
        Color(0xFFF97316), Color(0xFF14B8A6), Color(0xFFEAB308)
    )

    // Dynamic category breakdown built directly from actual expenses
    val categoryBreakdown = expenses.groupBy { it.categoryName.ifEmpty { "Autre" } }
        .map { (name, exps) ->
            val emoji = exps.firstOrNull()?.categoryEmoji?.ifEmpty { "📦" } ?: "📦"
            val sum = exps.sumOf { it.amount }
            Triple(name, emoji, sum)
        }
        .sortedByDescending { it.third }

    // Dynamic member breakdown built directly from actual expenses
    val memberBreakdown = expenses.groupBy { it.userName.ifEmpty { "Membre" } }
        .map { (user, exps) ->
            val sum = exps.sumOf { it.amount }
            user to sum
        }
        .sortedByDescending { it.second }

    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.FRENCH)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Title & Context
        item {
            Column {
                Text(
                    text = "Statistiques & Analyses",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${household?.name ?: "Mon Foyer"} • Suivi des dépenses réelles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 3 Key Financial Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Budget",
                    value = CurrencyFormatter.format(totalBudget, currency),
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.AccountBalanceWallet,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Dépenses",
                    value = CurrencyFormatter.format(totalSpent, currency),
                    color = if (isOverBudget) KasaWarning else KasaSecondary,
                    icon = Icons.Default.ReceiptLong,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = if (isOverBudget) "Dépassement" else "Reste",
                    value = CurrencyFormatter.format(if (isOverBudget) totalSpent - totalBudget else balance.coerceAtLeast(0.0), currency),
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else KasaPrimary,
                    icon = if (isOverBudget) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Budget Consumption Gauge Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(KasaDimens.radiusLg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isOverBudget) MaterialTheme.colorScheme.error.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Consommation du Budget",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isOverBudget) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "$budgetConsumptionPercent%",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (totalBudget <= 0.0) {
                            "💡 Aucun budget défini. Cliquez sur l'onglet Budget pour en fixer un."
                        } else if (isOverBudget) {
                            "⚠️ Attention : Le budget est dépassé de ${CurrencyFormatter.format(totalSpent - totalBudget, currency)} !"
                        } else {
                            "✅ Vous avez consommé $budgetConsumptionPercent% de votre budget. Il reste ${CurrencyFormatter.format(balance, currency)}."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Category Breakdown (Donut Chart & List)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(KasaDimens.radiusLg),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Répartition des Dépenses Effectuées",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${expenses.size} dépense(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (totalSpent <= 0.0) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("📊", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Aucune dépense effectuée",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Ajoutez vos dépenses dans l'onglet Dépenses",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        // Donut Chart Canvas
                        Box(
                            modifier = Modifier.size(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                var startAngle = -90f
                                val strokeWidth = 28f

                                categoryBreakdown.forEachIndexed { index, item ->
                                    val catSpent = item.third
                                    val sweepAngle = (catSpent / totalSpent * 360).toFloat()
                                    if (sweepAngle > 0f) {
                                        val color = paletteColors[index % paletteColors.size]
                                        drawArc(
                                            color = color,
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                        )
                                        startAngle += sweepAngle
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = CurrencyFormatter.formatCompact(totalSpent, currency),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Category rows with exact sums
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            categoryBreakdown.forEachIndexed { index, item ->
                                val (name, emoji, catSpent) = item
                                val percentage = if (totalSpent > 0) ((catSpent / totalSpent) * 100).toInt() else 0
                                val color = paletteColors[index % paletteColors.size]

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(color, RoundedCornerShape(3.dp))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$emoji $name",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "${CurrencyFormatter.format(catSpent, currency)} ($percentage%)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Member Spending Breakdown
        if (memberBreakdown.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(KasaDimens.radiusLg),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "👥 Dépenses par Membre du Foyer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        memberBreakdown.forEachIndexed { index, (userName, sum) ->
                            val percent = if (totalSpent > 0) ((sum / totalSpent) * 100).toInt() else 0
                            val userFraction = if (totalSpent > 0) (sum / totalSpent).toFloat().coerceIn(0f, 1f) else 0f
                            val color = paletteColors[index % paletteColors.size]

                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(color.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = null,
                                                tint = color,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = userName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = "${CurrencyFormatter.format(sum, currency)} ($percent%)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { userFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = color,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Expenses Activity
        if (expenses.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(KasaDimens.radiusLg),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🧾 Dernières Dépenses Enregistrées",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        expenses.take(6).forEach { exp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedExpenseToEdit = exp }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(exp.categoryEmoji.ifEmpty { "📦" }, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = exp.description.ifEmpty { exp.categoryName },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        val sub = buildString {
                                            append(exp.userName)
                                            append(" • ")
                                            append(dateFormat.format(exp.date))
                                            if (!exp.lastModifiedByName.isNullOrBlank()) {
                                                append(" • ✏️ ${exp.lastModifiedByName}")
                                            }
                                        }
                                        Text(
                                            text = sub,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (exp.comment.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "💬 ${exp.comment}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = KasaPrimary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "-${CurrencyFormatter.format(exp.amount, currency)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }
    }

    // Dialog: Edit or Delete Expense directly from Statistics
    selectedExpenseToEdit?.let { exp ->
        var amountText by remember { mutableStateOf(exp.amount.toString()) }
        var description by remember { mutableStateOf(exp.description) }
        var comment by remember { mutableStateOf(exp.comment) }
        var selectedCategoryId by remember { mutableStateOf(exp.categoryId) }

        AlertDialog(
            onDismissRequest = { selectedExpenseToEdit = null },
            title = { Text("Modifier ou Supprimer", fontWeight = FontWeight.Bold) },
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
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
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

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(KasaDimens.radiusMd),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                if (icon != null) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
