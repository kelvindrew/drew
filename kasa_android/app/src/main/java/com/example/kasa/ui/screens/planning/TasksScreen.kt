package com.example.kasa.ui.screens.planning

import androidx.compose.animation.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.model.TaskModel
import com.example.kasa.data.model.UserModel
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.theme.*
import com.example.kasa.util.DateFormatter
import java.util.*

@Composable
fun TasksScreen(
    onOpenAddTask: () -> Unit
) {
    val tasks by KasaRepository.tasks.collectAsState()
    val members by KasaRepository.members.collectAsState()
    val currentUser by KasaRepository.currentUser.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, MINE, TODO, DONE
    var editingTask by remember { mutableStateOf<TaskModel?>(null) }

    val filteredTasks = remember(tasks, selectedFilter, currentUser) {
        when (selectedFilter) {
            "MINE" -> tasks.filter { it.assignedMemberId == currentUser?.id }
            "TODO" -> tasks.filter { it.status != "done" }
            "DONE" -> tasks.filter { it.status == "done" }
            else -> tasks
        }
    }

    val todoCount = tasks.count { it.status != "done" }
    val doneCount = tasks.count { it.status == "done" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Summary & Stats Bar
        Surface(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$todoCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("À faire", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$doneCount",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = KasaSuccess
                    )
                    Text("Terminées", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider(modifier = Modifier.height(24.dp).width(1.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val totalPoints = tasks.filter { it.status == "done" }.sumOf { it.points }
                    Text(
                        text = "⭐ $totalPoints",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = KasaWarning
                    )
                    Text("Points gagnés", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filters = listOf(
                "ALL" to "Toutes (${tasks.size})",
                "MINE" to "Mes tâches",
                "TODO" to "À faire ($todoCount)",
                "DONE" to "Terminées ($doneCount)"
            )
            items(filters) { (key, label) ->
                val isSelected = selectedFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = key },
                    label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tasks List
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🧹", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedFilter == "DONE") "Aucune tâche terminée pour l'instant" else "Aucune tâche dans cette catégorie",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onOpenAddTask,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ajouter une tâche")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onToggle = { KasaRepository.toggleTaskStatus(task.id) },
                        onEdit = { editingTask = task },
                        onDelete = { KasaRepository.deleteTask(task.id) }
                    )
                }
            }
        }
    }

    editingTask?.let { task ->
        TaskEditDialog(
            task = task,
            members = members,
            onDismiss = { editingTask = null },
            onSave = { updated ->
                KasaRepository.updateTask(updated)
                editingTask = null
            }
        )
    }
}

@Composable
fun TaskCard(
    task: TaskModel,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (task.isDone) KasaSuccess.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox Icon Button
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (task.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (task.isDone) KasaSuccess else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Task Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Tags: Assigned Member, Recurrence, Points
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task.assignedMemberName.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "👤 ${task.assignedMemberName}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (task.recurrence != "none") {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "🔄 ${task.recurrenceLabel}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = KasaWarning.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "+${task.points} pts",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = KasaWarning
                        )
                    }
                }
            }

            // Options: Edit / Delete Menu
            var menuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", modifier = Modifier.size(18.dp))
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Modifier") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Supprimer", color = KasaError) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = KasaError) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskEditDialog(
    task: TaskModel? = null,
    members: List<UserModel>,
    onDismiss: () -> Unit,
    onSave: (TaskModel) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var selectedMemberId by remember { mutableStateOf(task?.assignedMemberId ?: "") }
    var selectedMemberName by remember { mutableStateOf(task?.assignedMemberName ?: "") }
    var recurrence by remember { mutableStateOf(task?.recurrence ?: "none") }
    var points by remember { mutableIntStateOf(task?.points ?: 10) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (task == null) "🧹 Nouvelle Tâche" else "✏️ Modifier la Tâche", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre de la tâche *") },
                    placeholder = { Text("ex: Faire la vaisselle, Sortir les poubelles") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Consignes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                // Assign to member dropdown / selector
                Text("Attribuer à :", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = selectedMemberId.isEmpty(),
                            onClick = {
                                selectedMemberId = ""
                                selectedMemberName = "Tous les membres"
                            },
                            label = { Text("👥 Tous") }
                        )
                    }
                    items(members) { member ->
                        val isSelected = selectedMemberId == member.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedMemberId = member.id
                                selectedMemberName = member.name
                            },
                            label = { Text(member.name) }
                        )
                    }
                }

                // Recurrence Selector
                Text("Récurrence :", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                val recurrences = listOf("none" to "Ponctuel", "daily" to "Quotidien", "weekly" to "Hebdomadaire")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    recurrences.forEach { (key, label) ->
                        FilterChip(
                            selected = recurrence == key,
                            onClick = { recurrence = key },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                // Points Selector
                Text("Points de récompense :", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                val pointOptions = listOf(5, 10, 20, 50)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    pointOptions.forEach { pt ->
                        FilterChip(
                            selected = points == pt,
                            onClick = { points = pt },
                            label = { Text("+$pt pts", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val toSave = task?.copy(
                            title = title.trim(),
                            description = description.trim(),
                            assignedMemberId = selectedMemberId,
                            assignedMemberName = selectedMemberName.ifEmpty { "Tous les membres" },
                            recurrence = recurrence,
                            points = points
                        ) ?: TaskModel(
                            id = UUID.randomUUID().toString(),
                            householdId = "",
                            title = title.trim(),
                            description = description.trim(),
                            assignedMemberId = selectedMemberId,
                            assignedMemberName = selectedMemberName.ifEmpty { "Tous les membres" },
                            recurrence = recurrence,
                            points = points,
                            createdAt = Date()
                        )
                        onSave(toSave)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
