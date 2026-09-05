package com.example.kasa.data.model

import java.util.Date

data class UserModel(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val avatar: String = "",
    val role: String = "member", // "admin" or "member"
    val householdId: String = "",
    val trackingEnabled: Boolean = false, // Controlled remotely by Admin!
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String = "",
    val locationUpdatedAt: Date? = null,
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val deviceModel: String = "",
    val deviceStatusUpdatedAt: Date? = null,
    val pingRequestedAt: Long? = null,
    val isHome: Boolean = false,
    val distanceFromHomeMeters: Double? = null,
    val arrivedHomeAt: Date? = null,
    val createdAt: Date = Date()
) {
    val isAdmin: Boolean get() = role == "admin"
    val initials: String get() = if (name.isNotEmpty()) name.take(1).uppercase() else "?"
    val hasLocation: Boolean get() = latitude != null && longitude != null
    val isLowBattery: Boolean get() = batteryLevel <= 15
    val batteryStatusLabel: String get() = when {
        isCharging -> "En charge ⚡ ($batteryLevel%)"
        batteryLevel <= 15 -> "Critique 🪫 ($batteryLevel%)"
        batteryLevel <= 30 -> "Faible 🔋 ($batteryLevel%)"
        else -> "Sur batterie 🔋 ($batteryLevel%)"
    }
}
