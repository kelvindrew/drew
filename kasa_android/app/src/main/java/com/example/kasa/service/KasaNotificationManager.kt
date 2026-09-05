package com.example.kasa.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.kasa.MainActivity
import com.example.kasa.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

enum class AlertCategory {
    MESSAGE,
    MODIFICATION,
    BUDGET_EXCEEDED,
    GENERAL,
    URGENCE
}

data class InAppAlert(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val category: AlertCategory = AlertCategory.GENERAL,
    val emoji: String = "🔔",
    val timestamp: Long = System.currentTimeMillis()
)

object KasaNotificationManager {
    private const val CHANNEL_ID = "kasa_household_alerts"
    private const val CHANNEL_NAME = "Alertes & Activités du Foyer"
    private var appContext: Context? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var autoDismissJob: Job? = null

    // Current active In-App Alert banner
    private val _currentAlert = MutableStateFlow<InAppAlert?>(null)
    val currentAlert: StateFlow<InAppAlert?> = _currentAlert.asStateFlow()

    // Unread messages count for badge
    private val _unreadMessagesCount = MutableStateFlow(0)
    val unreadMessagesCount: StateFlow<Int> = _unreadMessagesCount.asStateFlow()

    // Unread modifications count for badge
    private val _unreadModificationsCount = MutableStateFlow(0)
    val unreadModificationsCount: StateFlow<Int> = _unreadModificationsCount.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        createNotificationChannel(context)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour nouveaux messages, modifications de budget, dépenses et provisions"
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    fun postAlert(
        title: String,
        message: String,
        category: AlertCategory = AlertCategory.GENERAL,
        emoji: String = "🔔",
        showSystemNotification: Boolean = true
    ) {
        val alert = InAppAlert(
            title = title,
            message = message,
            category = category,
            emoji = emoji
        )

        // 1. Show In-App Banner
        _currentAlert.value = alert
        autoDismissJob?.cancel()
        autoDismissJob = scope.launch {
            delay(4000)
            if (_currentAlert.value?.id == alert.id) {
                _currentAlert.value = null
            }
        }

        // Increment badges
        if (category == AlertCategory.MESSAGE) {
            _unreadMessagesCount.value += 1
        } else if (category == AlertCategory.MODIFICATION) {
            _unreadModificationsCount.value += 1
        }

        // 2. Show Android System Status Bar Notification
        if (showSystemNotification) {
            appContext?.let { ctx ->
                showSystemNotification(ctx, title, message)
            }
        }
    }

    fun dismissCurrentAlert() {
        autoDismissJob?.cancel()
        _currentAlert.value = null
    }

    fun markMessagesAsRead() {
        _unreadMessagesCount.value = 0
    }

    fun markModificationsAsRead() {
        _unreadModificationsCount.value = 0
    }

    private fun showSystemNotification(context: Context, title: String, message: String) {
        try {
            createNotificationChannel(context)
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                (System.currentTimeMillis() % 100000).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.notify((System.currentTimeMillis() % 100000).toInt(), notification)
        } catch (e: Exception) {
            // Ignore if notification permission not yet granted
        }
    }
}