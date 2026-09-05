package com.example.kasa.data.model

import java.util.Date

data class EventModel(
    val id: String = "",
    val householdId: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val startDate: Date = Date(),
    val endDate: Date? = null,
    val category: String = "general", // "school", "health", "birthday", "outing", "chores", "general"
    val categoryEmoji: String = "📌",
    val participantMemberIds: List<String> = emptyList(),
    val createdById: String = "",
    val createdByName: String = "",
    val createdAt: Date = Date()
) {
    val categoryName: String get() = when (category) {
        "school" -> "École / Études"
        "health" -> "Santé / Médical"
        "birthday" -> "Anniversaire / Fête"
        "outing" -> "Sortie / Loisirs"
        "chores" -> "Maison / Bricolage"
        else -> "Général"
    }

    val displayEmoji: String get() = when (category) {
        "school" -> "🎓"
        "health" -> "🩺"
        "birthday" -> "🎂"
        "outing" -> "🍿"
        "chores" -> "🧹"
        else -> categoryEmoji.ifEmpty { "📌" }
    }
}
