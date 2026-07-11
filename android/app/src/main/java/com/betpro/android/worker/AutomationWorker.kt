package com.betpro.android.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.betpro.android.domain.repository.SettingsRepository
import com.betpro.android.domain.repository.SportEventRepository
import com.betpro.android.domain.usecase.CheckStopLossUseCase
import com.betpro.android.domain.usecase.FindDoubleBetOpportunitiesUseCase
import com.betpro.android.util.NotificationUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

@HiltWorker
class AutomationWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sportEventRepository: SportEventRepository,
    private val findDoubleBetOpportunitiesUseCase: FindDoubleBetOpportunitiesUseCase,
    private val checkStopLossUseCase: CheckStopLossUseCase,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Check Stop Loss via DataStore
            val settings = settingsRepository.appSettingsFlow.first()
            val currentBalance = settings.virtualBalance
            val stopLossThreshold = settings.stopLossThreshold

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
                val response = triggerAutoClicker(event1.id, "1", 10.0) // Mocking stake and selection

                if (response != null && response.getBoolean("success")) {
                    val screenshotBase64 = response.optString("screenshotBase64", "")
                    val notificationMessage = if (screenshotBase64.isNotEmpty()) {
                        "Pari double placé sur ${event1.homeTeam} et ${event2.homeTeam}. (Preuve visuelle reçue)"
                    } else {
                        "Pari double placé sur ${event1.homeTeam} et ${event2.homeTeam} (Cote: ${String.format("%.2f", combinedOdds)})"
                    }

                    NotificationUtils.showNotification(
                        appContext,
                        "Pari Automatique Placé",
                        notificationMessage
                    )

                    // Here we would save the screenshotBase64 to Room DB or DataStore to display in StatisticsScreen
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

    private fun triggerAutoClicker(matchId: String, odds: String, stake: Double): JSONObject? {
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
            if (responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseString = reader.readText()
                reader.close()
                JSONObject(responseString)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
