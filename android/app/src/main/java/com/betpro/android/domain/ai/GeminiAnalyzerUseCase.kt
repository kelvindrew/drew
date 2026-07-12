package com.betpro.android.domain.ai

import com.betpro.android.domain.model.SportEvent
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GeminiAnalyzerUseCase @Inject constructor() {

    // Using a placeholder API key. Should be injected securely.
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "MOCK_GEMINI_API_KEY"
    )

    operator fun invoke(event: SportEvent): Flow<String> = flow {
        val prompt = """
            Analyse le match de football suivant en tant qu'expert en paris sportifs.
            Match: ${event.homeTeam} vs ${event.awayTeam}
            Cotes:
            - Victoire ${event.homeTeam}: ${event.odds1}
            - Nul: ${event.oddsX}
            - Victoire ${event.awayTeam}: ${event.odds2}
            - Les deux équipes marquent (Oui): ${event.oddsBttsYes ?: "N/A"}
            - Plus de 2.5 buts: ${event.oddsOver25 ?: "N/A"}

            Donne une analyse concise de 3 phrases maximum sur l'opportunité de pari la plus intéressante.
        """.trimIndent()

        try {
            // Using generateContentStream for reactive flow
            generativeModel.generateContentStream(prompt).collect { chunk ->
                emit(chunk.text ?: "")
            }
        } catch (e: Exception) {
             emit("Erreur lors de l'analyse IA : Impossible de contacter Gemini. (Mode hors-ligne ou clé API invalide)")
        }
    }
}
