package com.example.kasa.data.model

import java.util.Date

data class AdminNotificationModel(
    val id: String = "",
    val householdId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "general", // "arrival", "low_battery", "location", "info"
    val memberId: String = "",
    val memberName: String = "",
    val createdAt: Date = Date(),
    val isRead: Boolean = false
) {
    val icon: String get() = when (type) {
        "arrival" -> "🏠"
        "low_battery" -> "🪫"
        "location" -> "📍"
        else -> "🔔"
    }
}
