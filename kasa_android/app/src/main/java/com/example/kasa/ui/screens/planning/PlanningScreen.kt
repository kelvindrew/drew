package com.example.kasa.ui.screens.planning

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.theme.KasaPrimary

@Composable
fun PlanningScreen(
    initialTab: Int = 0
) {
    var activeTab by rememberSaveable { mutableIntStateOf(initialTab) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    val members by KasaRepository.members.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (activeTab == 0) showAddTaskDialog = true else showAddEventDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = {
                    Text(
                        text = if (activeTab == 0) "Nouvelle Tâche" else "Nouvel Événement",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Segmented Switch Tab Header: Tâches vs Calendrier
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tab 0: Tâches
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        color = if (activeTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (activeTab == 0) 2.dp else 0.dp,
                        onClick = { activeTab = 0 }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🧹 Tâches & Corvées",
                                fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (activeTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Tab 1: Calendrier
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp),
                        color = if (activeTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (activeTab == 1) 2.dp else 0.dp,
                        onClick = { activeTab = 1 }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📆 Calendrier & Agenda",
                                fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (activeTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Body content
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "PlanningTabAnimation"
                ) { tab ->
                    when (tab) {
                        0 -> TasksScreen(onOpenAddTask = { showAddTaskDialog = true })
                        1 -> AgendaScreen(onOpenAddEvent = { showAddEventDialog = true })
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        TaskEditDialog(
            members = members,
            onDismiss = { showAddTaskDialog = false },
            onSave = { newTask ->
                KasaRepository.addTask(
                    title = newTask.title,
                    description = newTask.description,
                    assignedMemberId = newTask.assignedMemberId,
                    assignedMemberName = newTask.assignedMemberName,
                    dueDate = newTask.dueDate,
                    recurrence = newTask.recurrence,
                    points = newTask.points
                )
                showAddTaskDialog = false
            }
        )
    }

    if (showAddEventDialog) {
        EventEditDialog(
            members = members,
            onDismiss = { showAddEventDialog = false },
            onSave = { newEvent ->
                KasaRepository.addEvent(
                    title = newEvent.title,
                    description = newEvent.description,
                    location = newEvent.location,
                    startDate = newEvent.startDate,
                    endDate = newEvent.endDate,
                    category = newEvent.category,
                    categoryEmoji = newEvent.categoryEmoji,
                    participantMemberIds = newEvent.participantMemberIds
                )
                showAddEventDialog = false
            }
        )
    }
}
