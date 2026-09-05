package com.example.kasa.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.model.MealPlanModel
import com.example.kasa.data.model.UserModel
import com.example.kasa.theme.KasaPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminMealDialog(
    currentPlan: MealPlanModel,
    members: List<UserModel>,
    onDismiss: () -> Unit,
    onSave: (lunch: String, lunchDetails: String, dinner: String, dinnerDetails: String, chefId: String?, chefName: String?, note: String) -> Unit
) {
    var lunchTitle by remember { mutableStateOf(currentPlan.lunchTitle) }
    var dinnerTitle by remember { mutableStateOf(currentPlan.dinnerTitle) }
    var specialNote by remember { mutableStateOf(currentPlan.specialNote) }
    var selectedChefId by remember { mutableStateOf(currentPlan.chefUserId) }
    var selectedChefName by remember { mutableStateOf(currentPlan.chefUserName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ThemedIconBadge(size = 36.dp) {
                    Text("👨‍🍳", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Repas du Jour", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Espace Administrateur", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Suggestions issues du bloc-notes des membres
                val mealNotes by com.example.kasa.data.repository.KasaRepository.mealNotes.collectAsState()
                val activeNotes = remember(mealNotes) { mealNotes.sortedByDescending { it.upvoteCount } }
                if (activeNotes.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💡 Idées du bloc-notes (${activeNotes.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Toucher pour choisir",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(activeNotes.size) { index ->
                                    val note = activeNotes[index]
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable {
                                                dinnerTitle = note.mealTitle
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("❤️ ${note.upvoteCount}", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(note.mealTitle, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Dinner (Repas du Soir)
                OutlinedTextField(
                    value = dinnerTitle,
                    onValueChange = { dinnerTitle = it },
                    label = { Text("🍽️ Repas du soir (Dîner)") },
                    placeholder = { Text("Ex: Lasagnes maison, Salade...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Lunch (Repas du Midi)
                OutlinedTextField(
                    value = lunchTitle,
                    onValueChange = { lunchTitle = it },
                    label = { Text("☀️ Repas du midi (Déjeuner)") },
                    placeholder = { Text("Ex: Pâtes pesto, Sandwich...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Chef du jour Section with Random Wheel / Casino Button
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👨‍🍳 Chef du jour", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                        // Tirage au sort button
                        FilledTonalButton(
                            onClick = {
                                if (members.isNotEmpty()) {
                                    val randomMember = members.random()
                                    selectedChefId = randomMember.id
                                    selectedChefName = randomMember.name
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tirage au sort", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Chips of members to pick
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        members.forEach { m ->
                            val isSelected = selectedChefName.equals(m.name, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedChefId = null
                                        selectedChefName = null
                                    } else {
                                        selectedChefId = m.id
                                        selectedChefName = m.name
                                    }
                                },
                                label = { Text(m.name, fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                // Special Note
                OutlinedTextField(
                    value = specialNote,
                    onValueChange = { specialNote = it },
                    label = { Text("📝 Note ou consigne") },
                    placeholder = { Text("Ex: Chacun apporte son dessert, 20h pile...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        lunchTitle,
                        "",
                        dinnerTitle,
                        "",
                        selectedChefId,
                        selectedChefName,
                        specialNote
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enregistrer le menu", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
