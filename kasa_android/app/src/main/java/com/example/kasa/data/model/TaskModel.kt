package com.example.kasa.data.model

import java.util.Date

data class TaskModel(
    val id: String = "",
    val householdId: String = "",
    val title: String = "",
    val description: String = "",
    val assignedMemberId: String = "",
    val assignedMemberName: String = "",
    val dueDate: Date? = null,
    val recurrence: String = "none", // "none", "daily", "weekly", "monthly"
    val status: String = "todo", // "todo", "in_progress", "done"
    val points: Int = 10,
    val completedAt: Date? = null,
    val completedByMemberId: String = "",
    val completedByMemberName: String = "",
    val createdAt: Date = Date()
) {
    val isDone: Boolean get() = status == "done"
    val recurrenceLabel: String get() = when (recurrence) {
        "daily" -> "Tous les jours"
        "weekly" -> "Chaque semaine"
        "monthly" -> "Chaque mois"
        else -> "Ponctuel"
    }
}
