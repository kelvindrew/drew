package com.example.kasa.data.model

import java.util.Date
import java.util.UUID

data class PostItModel(
    val id: String = UUID.randomUUID().toString(),
    val householdId: String = "",
    val text: String = "",
    val colorHex: String = "#FEF08A", // Jaune pastel par défaut (#FEF08A, #FBCFE8, #A7F3D0, #DDD6FE, #BAE6FD)
    val emoji: String = "📌",
    val authorId: String = "",
    val authorName: String = "",
    val createdAt: Date = Date(),
    val isPinned: Boolean = false
)

data class HouseEssentialInfo(
    val wifiSsid: String = "",
    val wifiPassword: String = "",
    val gateCode: String = "",
    val buildingCode: String = "",
    val landlordName: String = "",
    val landlordPhone: String = "",
    val emergencyNotes: String = "",
    val houseRules: List<String> = emptyList(),
    val lastUpdatedBy: String = "",
    val lastUpdatedAt: Date? = null
) {
    val hasWifi: Boolean get() = wifiSsid.isNotBlank()
    val hasLandlord: Boolean get() = landlordName.isNotBlank() || landlordPhone.isNotBlank()
}
