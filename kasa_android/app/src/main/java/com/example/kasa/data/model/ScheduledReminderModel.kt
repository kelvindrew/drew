package com.example.kasa.data.model

import java.util.Date
import java.util.UUID

data class ScheduledReminderModel(
    val id: String = UUID.randomUUID().toString(),
    val householdId: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val message: String = "",
    val triggerTimestamp: Long = System.currentTimeMillis() + 3600000L, // dans 1 heure par défaut
    val isSent: Boolean = false,
    val createdAt: Date = Date()
) {
    val isDue: Boolean get() = !isSent && System.currentTimeMillis() >= triggerTimestamp
}
