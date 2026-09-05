package com.example.kasa.data.model

import java.util.Date
import java.util.UUID

data class MealNoteModel(
    val id: String = UUID.randomUUID().toString(),
    val householdId: String = "",
    val mealTitle: String = "",
    val noteDetails: String = "",
    val suggestedByUserId: String = "",
    val suggestedByUserName: String = "",
    val createdAt: Date = Date(),
    val upvoterUserIds: List<String> = emptyList(),
    val status: String = "ACTIVE" // ACTIVE, PLANNED, ARCHIVED
) {
    val upvoteCount: Int get() = upvoterUserIds.size
    fun isUpvotedBy(userId: String): Boolean = upvoterUserIds.contains(userId)
}
