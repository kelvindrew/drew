package com.connect.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SmartCoachAI(private val apiKey: String) {
    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = apiKey
    )

    private val chat = model.startChat()

    fun generateRecommendations(healthData: String): Flow<String> = flow {
        val prompt = "En tant que coach sportif IA de haut niveau, analyse ces données et donne une brève recommandation très concise (1 phrase) : $healthData"
        try {
            val response = model.generateContent(prompt)
            emit(response.text ?: "Aucune recommandation générée.")
        } catch (e: Exception) {
            emit("Impossible de générer la recommandation (Clé API de test).")
        }
    }

    suspend fun sendMessage(message: String): String {
        return try {
            val response = chat.sendMessage(message)
            response.text ?: "Désolé, je n'ai pas pu générer de réponse."
        } catch (e: Exception) {
            "Erreur de connexion au serveur AI (Clé de test)."
        }
    }
}
