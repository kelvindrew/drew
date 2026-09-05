package com.example.kasa.data.model

import java.util.Date

data class ProvisionItemModel(
    val id: String = "",
    val householdId: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val unit: String = "pièce",
    val plannedPrice: Double = 0.0,
    val actualPrice: Double? = null,
    val status: String = "planned", // "planned", "bought", "out_of_stock"
    val priority: String = "medium", // "high", "medium", "low"
    val comment: String = "",
    val addedBy: String = "",
    val lastModifiedBy: String? = null,
    val lastModifiedAt: Date? = null,
    val purchasedBy: String? = null,
    val purchasedAt: Date? = null,
    val createdAt: Date = Date()
) {
    val isPlanned: Boolean get() = status == "planned"
    val isBought: Boolean get() = status == "bought"
    val isOutOfStock: Boolean get() = status == "out_of_stock"
    val isHighPriority: Boolean get() = priority == "high"
    val variance: Double get() = (actualPrice ?: 0.0) - plannedPrice
    val isOverBudget: Boolean get() = (actualPrice ?: 0.0) > plannedPrice
}
