package com.betpro.app.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.betpro.app.BuildConfig
import com.betpro.app.data.BackendApiService
import com.betpro.app.data.OddsApiService
import com.betpro.app.data.PlaceBetRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ArbitrageWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val oddsApi: OddsApiService,
    private val backendApi: BackendApiService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val amountStr = inputData.getString("BET_AMOUNT") ?: "10"
        val amount = amountStr.toDoubleOrNull() ?: 10.0

        try {
            // Check Odds
            val apiKey = BuildConfig.ODDS_API_KEY
            if (apiKey.isEmpty()) return Result.failure()

            val events = oddsApi.getUpcomingOdds(apiKey)

            // Simplified Dropping Odds Logic:
            // In a real scenario, we'd compare historical odds in Room DB vs current odds.
            // For this MVP "dingue", we simulate finding a dropping odd if any match exists.
            val targetEvent = events.firstOrNull { it.bookmakers.isNotEmpty() }

            if (targetEvent != null) {
                // Found a temporal arbitrage opportunity!
                sendNotification("Faille Temporelle Trouvée !", "Match: \${targetEvent.home_team} vs \${targetEvent.away_team}. Pari en cours: \$amount$")

                // Trigger backend
                val request = PlaceBetRequest(
                    eventId = targetEvent.id,
                    amount = amount,
                    odds = 2.0, // Example odds
                    bookmaker = "betika"
                )

                val token = "Bearer \${BuildConfig.BACKEND_TOKEN}"
                val response = backendApi.placeBet(token, request)

                if (response.success) {
                    sendNotification("Pari Placé !", "L'arbitrage temporel a réussi.")
                    return Result.success()
                } else {
                    sendNotification("Erreur de Pari", "Le bot a échoué: \${response.error}")
                    return Result.retry()
                }
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "arbitrage_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Arbitrage Notifications", NotificationManager.IMPORTANCE_HIGH)
            val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(appContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            NotificationManagerCompat.from(appContext).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission missing on API 33+
        }
    }
}
