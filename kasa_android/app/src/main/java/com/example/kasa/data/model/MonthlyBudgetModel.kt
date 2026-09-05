package com.example.kasa.data.model

import java.util.Date

data class MonthlyBudgetModel(
    val id: String = "",
    val householdId: String = "",
    val monthKey: String = "",
    val totalBudget: Double = 0.0,
    val lastModifiedBy: String = "",
    val lastModifiedAt: Date? = null,
    val createdAt: Date = Date()
)
