package com.example.kasa.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.model.DebtTransfer
import com.example.kasa.data.model.ExpenseModel
import com.example.kasa.data.model.UserModel
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.theme.*
import com.example.kasa.util.CurrencyFormatter
import com.example.kasa.util.DebtCalculator

@Composable
fun DebtSettlementCard(
    expenses: List<ExpenseModel>,
    members: List<UserModel>,
    currentUserId: String,
    currency: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var transferToSettle by remember { mutableStateOf<DebtTransfer?>(null) }

    val debtSummary = remember(expenses, members) {
        DebtCalculator.calculate(expenses, members)
    }

    val myBalance = debtSummary.balances.find { it.userId == currentUserId }
    val myTransfersToPay = debtSummary.transfers.filter { it.fromUserId == currentUserId }
    val myTransfersToReceive = debtSummary.transfers.filter { it.toUserId == currentUserId }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row with Title and Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ThemedIconBadge(size = 38.dp) {
                        Text("⚖️", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Équilibre des Comptes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Part équitable : ${CurrencyFormatter.format(debtSummary.fairSharePerPerson, currency)} / pers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Détails"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // My Personal Status Pill
            if (myBalance != null) {
                val statusColor = when {
                    myBalance.isCreditor -> KasaSuccess
                    myBalance.isDebtor -> Color(0xFFF59E0B)
                    else -> MaterialTheme.colorScheme.primary
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                myBalance.isCreditor -> "On vous doit au total"
                                myBalance.isDebtor -> "Vous devez au total"
                                else -> "Vos comptes sont équilibrés"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )

                        Text(
                            text = when {
                                myBalance.isCreditor -> "+ ${CurrencyFormatter.format(myBalance.netBalance, currency)}"
                                myBalance.isDebtor -> "- ${CurrencyFormatter.format(kotlin.math.abs(myBalance.netBalance), currency)}"
                                else -> "0.00 $currency"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            // Expanded Section: Details of Simplified Transfers
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 6.dp))

                    Text(
                        text = "Remboursements simplifiés (${debtSummary.transfers.size})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    if (debtSummary.transfers.isEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = KasaSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Toutes les dépenses sont équilibrées !",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        debtSummary.transfers.forEach { transfer ->
                            val isMyDebt = transfer.fromUserId == currentUserId
                            val isMyCredit = transfer.toUserId == currentUserId

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = transfer.fromUserName,
                                            fontWeight = if (isMyDebt) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isMyDebt) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = transfer.toUserName,
                                            fontWeight = if (isMyCredit) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isMyCredit) KasaSuccess else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = CurrencyFormatter.format(transfer.amount, currency),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { transferToSettle = transfer },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text("Régler", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Confirmation de Remboursement
    if (transferToSettle != null) {
        val transfer = transferToSettle!!
        AlertDialog(
            onDismissRequest = { transferToSettle = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🤝", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmer le règlement", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Voulez-vous marquer le virement de ${CurrencyFormatter.format(transfer.amount, currency)} de ${transfer.fromUserName} vers ${transfer.toUserName} comme effectué ?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val provCat = KasaRepository.categories.value.firstOrNull()
                        KasaRepository.addExpense(
                            amount = transfer.amount,
                            description = "🤝 [Remboursement] ${transfer.fromUserName} a remboursé ${transfer.toUserName}",
                            categoryId = provCat?.id ?: "cat_general",
                            comment = "Règlement de dette simplifié"
                        )
                        transferToSettle = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirmer le règlement", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transferToSettle = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}
