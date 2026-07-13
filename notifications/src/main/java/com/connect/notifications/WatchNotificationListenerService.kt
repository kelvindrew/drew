package com.connect.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class WatchNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val packageName = sbn?.packageName
        val title = sbn?.notification?.extras?.getString("android.title")
        Log.d("NotificationListener", "New notification from $packageName: $title")
        // Logic to forward to BLE Watch
    }
}
