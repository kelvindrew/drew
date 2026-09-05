package com.example.kasa.data.model

import java.util.Date

data class ExpenseModel(
    val id: String = "",
    val householdId: String = "",
    val userId: String = "",
    val userName: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val categoryEmoji: String = "📦",
    val amount: Double = 0.0,
    val description: String = "",
    val comment: String = "",
    val lastModifiedByName: String? = null,
    val lastModifiedAt: Date? = null,
    val date: Date = Date(),
    val createdAt: Date = Date()
)
