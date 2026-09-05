package com.example.kasa.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.model.ScheduledReminderModel
import com.example.kasa.data.model.UserModel
import com.example.kasa.data.repository.KasaRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduledReminderDialog(
    reminders: List<ScheduledReminderModel>,
    currentUser: UserModel?,
    isAdmin: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var reminderMessage by remember { mutableStateOf("") }
    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    val currentUserId = currentUser?.id.orEmpty()
    val dateTimeFormatter = remember { SimpleDateFormat("dd MMM à HH:mm", Locale.getDefault()) }

    val activeReminders = remember(reminders) {
        reminders.filter { !it.isSent }.sortedBy { it.triggerTimestamp }
    }

    // Helper to calculate preset timestamps
    fun getPresetTimestamp(presetIdx: Int): Long {
        val cal = Calendar.getInstance()
        when (presetIdx) {
            0 -> {
                // Dans 1 heure
                cal.add(Calendar.HOUR_OF_DAY, 1)
            }
            1 -> {
                // Ce soir à 20h (ou demain à 20h si déjà passé)
                if (cal.get(Calendar.HOUR_OF_DAY) >= 20) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                cal.set(Calendar.HOUR_OF_DAY, 20)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
            }
            2 -> {
                // Demain matin à 09h
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 9)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
            }
            3 -> {
                // Demain soir à 20h
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 20)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
            }
            4 -> {
                // Dans 2 jours
                cal.add(Calendar.DAY_OF_YEAR, 2)
                cal.set(Calendar.HOUR_OF_DAY, 12)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
            }
        }
        return cal.timeInMillis
    }

    val presets = listOf(
        "Dans 1h",
        "Ce soir 20h",
        "Demain 9h",
        "Demain 20h",
        "Dans 2j"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ThemedIconBadge(size = 38.dp) {
                        Text("⏰", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Rappels Programmés",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Diffusion automatique dans le chat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                // Saisie du message
                OutlinedTextField(
                    value = reminderMessage,
                    onValueChange = { reminderMessage = it },
                    placeholder = { Text("Ex: Sortir les poubelles, payer le gaz...") },
                    label = { Text("Message du rappel") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Quand déclencher le rappel ?",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Presets flow row
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presets.forEachIndexed { index, label ->
                        val isSelected = selectedPresetIndex == index
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPresetIndex = index },
                            label = { Text(label, fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Prévu pour : ${dateTimeFormatter.format(getPresetTimestamp(selectedPresetIndex))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (reminderMessage.isNotBlank()) {
                            val triggerTs = getPresetTimestamp(selectedPresetIndex)
                            KasaRepository.addScheduledReminder(reminderMessage.trim(), triggerTs)
                            reminderMessage = ""
                            Toast.makeText(context, "Rappel programmé !", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = reminderMessage.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddAlarm, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Programmer le rappel")
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rappels en attente (${activeReminders.size}) :",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (activeReminders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun rappel en attente",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(activeReminders, key = { it.id }) { reminder ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = reminder.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "🔔 ${dateTimeFormatter.format(reminder.triggerTimestamp)} • Par ${reminder.creatorName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (reminder.creatorId == currentUserId || isAdmin) {
                                        IconButton(
                                            onClick = {
                                                KasaRepository.deleteScheduledReminder(reminder.id)
                                                Toast.makeText(context, "Rappel supprimé", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Supprimer",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
