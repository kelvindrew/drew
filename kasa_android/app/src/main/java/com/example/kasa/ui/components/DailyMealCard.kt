package com.example.kasa.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.model.MealPlanModel
import com.example.kasa.data.model.UserModel
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.theme.KasaPrimary
import com.example.kasa.theme.KasaSuccess

@Composable
fun DailyMealCard(
    mealPlan: MealPlanModel,
    currentUser: UserModel?,
    members: List<UserModel>,
    isAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    var showAdminDialog by remember { mutableStateOf(false) }
    var showMealNotesDialog by remember { mutableStateOf(false) }
    val mealNotes by KasaRepository.mealNotes.collectAsState()

    val myName = currentUser?.name.orEmpty()
    val isEatingTonight = mealPlan.attendeesDinnerYes.contains(myName)
    val isNotEatingTonight = mealPlan.attendeesDinnerNo.contains(myName)

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
            // Header: Title + Admin Edit Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ThemedIconBadge(size = 38.dp) {
                        Text("🍽️", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Repas du Jour",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!mealPlan.chefUserName.isNullOrBlank()) {
                            Text(
                                text = "Chef du jour : 👨‍🍳 ${mealPlan.chefUserName}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Menu du foyer",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isAdmin) {
                    IconButton(
                        onClick = { showAdminDialog = true },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Gérer les repas",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Meal Titles Container
            if (mealPlan.hasMeal) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (mealPlan.dinnerTitle.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌙", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Ce soir (Dîner)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = mealPlan.dinnerTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (mealPlan.lunchTitle.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("☀️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Ce midi (Déjeuner)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = mealPlan.lunchTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (mealPlan.specialNote.isNotBlank()) {
                        Text(
                            text = "💡 ${mealPlan.specialNote}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAdmin) "Aucun repas planifié. Cliquez sur ✏️ pour définir le menu."
                            else "Aucun repas défini pour aujourd'hui.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(10.dp))

            // Dinner RSVP Buttons
            Text(
                text = "Vous mangez à la maison ce soir ?",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Yes Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isEatingTonight) KasaSuccess.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, if (isEatingTonight) KasaSuccess else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (myName.isNotBlank()) {
                                KasaRepository.toggleDinnerAttendance(myName, true)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🙋 Oui", fontWeight = if (isEatingTonight) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (isEatingTonight) KasaSuccess else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${mealPlan.attendeesDinnerYes.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEatingTonight) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // No Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isNotEatingTonight) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, if (isNotEatingTonight) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (myName.isNotBlank()) {
                                KasaRepository.toggleDinnerAttendance(myName, false)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🙅 Pas ce soir", fontWeight = if (isNotEatingTonight) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (isNotEatingTonight) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${mealPlan.attendeesDinnerNo.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNotEatingTonight) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Names of confirmed diners
            if (mealPlan.attendeesDinnerYes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "À table (${mealPlan.attendeesDinnerYes.size}) : ${mealPlan.attendeesDinnerYes.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = KasaSuccess,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Une envie particulière ?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                TextButton(
                    onClick = { showMealNotesDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "📝 Bloc-notes (${mealNotes.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    if (showAdminDialog) {
        AdminMealDialog(
            currentPlan = mealPlan,
            members = members,
            onDismiss = { showAdminDialog = false },
            onSave = { lunch, lunchDet, dinner, dinnerDet, chefId, chefName, note ->
                KasaRepository.updateMealPlan(
                    lunchTitle = lunch,
                    lunchDetails = lunchDet,
                    dinnerTitle = dinner,
                    dinnerDetails = dinnerDet,
                    chefUserId = chefId,
                    chefUserName = chefName,
                    specialNote = note
                )
                showAdminDialog = false
            }
        )
    }

    if (showMealNotesDialog) {
        MealNotesDialog(
            notes = mealNotes,
            currentUser = currentUser,
            isAdmin = isAdmin,
            onDismiss = { showMealNotesDialog = false }
        )
    }
}
