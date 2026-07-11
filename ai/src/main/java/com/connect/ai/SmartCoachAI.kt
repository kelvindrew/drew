package com.connect.ai

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SmartCoachAI(private val apiKey: String) {
    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = apiKey
    )

    fun generateRecommendations(healthData: String): Flow<String> = flow {
        val prompt = "En tant que coach sportif IA, analyse ces données et donne des recommandations: $healthData"
        val response = model.generateContent(prompt)
        emit(response.text ?: "Aucune recommandation générée.")
    }
}
