package com.example.kasa.data.model

import java.util.Date

data class CategoryModel(
    val id: String = "",
    val householdId: String = "",
    val name: String = "",
    val emoji: String = "📦",
    val plannedAmount: Double = 0.0,
    val colorHex: String = "#10B981",
    val isProvision: Boolean = false,
    val createdAt: Date = Date()
)
