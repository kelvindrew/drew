package com.example.kasa.data.model

import java.util.Date

data class MealPlanModel(
    val id: String = "meal_today",
    val householdId: String = "",
    val date: String = "", // YYYY-MM-DD
    val lunchTitle: String = "",
    val lunchDetails: String = "",
    val dinnerTitle: String = "",
    val dinnerDetails: String = "",
    val chefUserId: String? = null,
    val chefUserName: String? = null,
    val specialNote: String = "",
    val attendeesDinnerYes: List<String> = emptyList(), // user names
    val attendeesDinnerNo: List<String> = emptyList(), // user names
    val lastModifiedByName: String = "",
    val lastModifiedAt: Date = Date()
) {
    val hasLunch: Boolean get() = lunchTitle.isNotBlank()
    val hasDinner: Boolean get() = dinnerTitle.isNotBlank()
    val hasMeal: Boolean get() = hasLunch || hasDinner
}
