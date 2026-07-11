package com.betpro.android.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.betpro.android.domain.repository.SportEventRepository
import com.betpro.android.domain.usecase.CheckStopLossUseCase
import com.betpro.android.domain.usecase.FindDoubleBetOpportunitiesUseCase
import com.betpro.android.util.NotificationUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@HiltWorker
class AutomationWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sportEventRepository: SportEventRepository,
    private val findDoubleBetOpportunitiesUseCase: FindDoubleBetOpportunitiesUseCase,
    private val checkStopLossUseCase: CheckStopLossUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Check Stop Loss (Mock balance for now)
            val currentBalance = 1250.0
            val stopLossThreshold = 100.0

            if (checkStopLossUseCase(currentBalance, stopLossThreshold)) {
                NotificationUtils.showNotification(
                    appContext,
                    "Stop-Loss Atteint",
                    "Le robot s'est arrêté car le solde est inférieur à $stopLossThreshold$."
                )
                return@withContext Result.failure()
            }

            // 2. Fetch Events
            val events = sportEventRepository.getUpcomingEvents()

            // 3. Find Opportunities
            val opportunities = findDoubleBetOpportunitiesUseCase(events)

            if (opportunities.isNotEmpty()) {
                val bestPair = opportunities.first()
                val event1 = bestPair.first
                val event2 = bestPair.second

                val combinedOdds = event1.odds1 * event2.odds1

                // 4. Trigger Node.js Auto-Clicker via Local API
                val success = triggerAutoClicker(event1.id, "1", 10.0) // Mocking stake and selection

                if (success) {
                    NotificationUtils.showNotification(
                        appContext,
                        "Pari Automatique Placé",
                        "Pari double placé sur ${event1.homeTeam} et ${event2.homeTeam} (Cote: ${String.format("%.2f", combinedOdds)})"
                    )
                } else {
                    NotificationUtils.showNotification(
                        appContext,
                        "Échec du Pari Automatique",
                        "Le robot Node.js n'a pas pu placer le pari."
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun triggerAutoClicker(matchId: String, odds: String, stake: Double): Boolean {
        return try {
            // Assuming Node.js is running locally or on a reachable server
            // For Android emulator pointing to host localhost, use 10.0.2.2
            val url = URL("http://10.0.2.2:3000/api/place-bet")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val jsonPayload = """
                {
                    "matchId": "$matchId",
                    "odds": "$odds",
                    "stake": $stake,
                    "platform": "betika"
                }
            """.trimIndent()

            val outputWriter = OutputStreamWriter(connection.outputStream)
            outputWriter.write(jsonPayload)
            outputWriter.flush()
            outputWriter.close()

            val responseCode = connection.responseCode
            responseCode == 200
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
