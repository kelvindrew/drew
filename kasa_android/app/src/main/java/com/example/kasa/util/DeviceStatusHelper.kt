package com.example.kasa.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.example.kasa.data.repository.KasaRepository

data class DevicePhoneState(
    val batteryLevel: Int,
    val isCharging: Boolean,
    val deviceModel: String
)

object DeviceStatusHelper {

    fun getPhoneState(context: Context): DevicePhoneState {
        return try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, filter)

            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val batteryLevel = if (level >= 0 && scale > 0) {
                ((level.toFloat() / scale.toFloat()) * 100).toInt()
            } else {
                100
            }

            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val manufacturer = Build.MANUFACTURER?.replaceFirstChar { it.uppercase() } ?: "Android"
            val model = Build.MODEL ?: "Device"
            val deviceModel = "$manufacturer $model"

            DevicePhoneState(
                batteryLevel = batteryLevel,
                isCharging = isCharging,
                deviceModel = deviceModel
            )
        } catch (e: Exception) {
            DevicePhoneState(
                batteryLevel = 100,
                isCharging = false,
                deviceModel = "Android"
            )
        }
    }

    fun syncDeviceStatus(context: Context) {
        try {
            val state = getPhoneState(context)
            KasaRepository.updateDeviceStatus(
                batteryLevel = state.batteryLevel,
                isCharging = state.isCharging,
                deviceModel = state.deviceModel
            )
        } catch (e: Exception) {
            // Ignore failure gracefully
        }
    }
}
