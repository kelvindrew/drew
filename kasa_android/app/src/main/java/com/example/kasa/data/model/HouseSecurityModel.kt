package com.example.kasa.data.model

import java.util.Date
import java.util.UUID

data class GuestPartyEvent(
    val id: String = UUID.randomUUID().toString(),
    val householdId: String = "",
    val hostUserId: String = "",
    val hostUserName: String = "",
    val guestCount: Int = 2,
    val description: String = "", // ex: "Apéro jeux de société", "Dîner entre amis"
    val startTime: Date = Date(),
    val endTime: Date? = null,
    val isActive: Boolean = true
)

enum class EmergencyType(val title: String, val emoji: String, val defaultMessage: String) {
    WATER_LEAK("Fuite d'eau urgente", "💧", "Attention, fuite d'eau constatée à la maison !"),
    POWER_OUTAGE("Coupure d'électricité", "⚡", "Le disjoncteur a sauté / coupure de courant générale."),
    LOCKED_OUT("Clé oubliée / Bloqué dehors", "🔑", "Je suis bloqué devant la porte sans clés, quelqu'un est à la maison ?"),
    HELP_RETURN("SOS Retour / Besoin d'aide", "🚨", "J'ai un problème sur le trajet du retour, voici ma position !");

    val label: String get() = title
}

data class EmergencyAlertModel(
    val id: String = UUID.randomUUID().toString(),
    val householdId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val type: EmergencyType = EmergencyType.HELP_RETURN,
    val message: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Date = Date()
)
