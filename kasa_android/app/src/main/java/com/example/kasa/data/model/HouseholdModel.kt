package com.example.kasa.data.model

import java.util.Date

data class HouseholdModel(
    val id: String = "",
    val name: String = "",
    val inviteCode: String = "",
    val currency: String = "USD",
    val secondaryCurrency: String = "CDF",
    val exchangeRate: Double = 2250.0,
    val ownerId: String = "",
    val homeLatitude: Double? = null,
    val homeLongitude: Double? = null,
    val homeAddress: String = "",
    val memberIds: List<String> = emptyList(),
    val monthlyBudget: Double = 0.0,
    val createdAt: Date = Date(),
    val appIcon: String = "default",
    val homeRadiusMeters: Double = 150.0
) {
    val formattedInviteCode: String get() = "UNK-$inviteCode"
    val hasHomeLocation: Boolean get() = homeLatitude != null && homeLongitude != null
    fun isOwner(userId: String): Boolean = ownerId == userId

    fun isWithinHome(lat: Double, lng: Double): Boolean {
        if (!hasHomeLocation || homeLatitude == null || homeLongitude == null) return false
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat, lng, homeLatitude, homeLongitude, results)
        return results[0] <= homeRadiusMeters
    }
}
