package com.smartmediatransfer.ai.data.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransferForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val CHANNEL_ID = "SmartMediaTransferChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("Connexion en cours...", 0, 100)
        startForeground(1, notification)

        serviceScope.launch {
            // Placeholder: TLS Connection and sync logic starts here
            // It will fetch tasks from TransferDao and execute Handshake -> Resumable transfer
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // We don't use bound services for this case
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Transferts Smart Media",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Affiche la progression des transferts en arrière-plan"
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(content: String, progress: Int, maxProgress: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Smart Media Transfer AI")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_upload) // Placeholder
            .setProgress(maxProgress, progress, false)
            .setOngoing(true)
            .build()
    }
}
