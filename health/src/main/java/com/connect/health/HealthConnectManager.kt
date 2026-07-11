package com.connect.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient

class HealthConnectManager(private val context: Context) {
    fun getClient(): HealthConnectClient? {
        return if (HealthConnectClient.isProviderAvailable(context)) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }

    // Read Steps, HeartRate etc. would be implemented here
}
