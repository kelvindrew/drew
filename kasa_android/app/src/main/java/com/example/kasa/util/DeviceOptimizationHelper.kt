package com.example.kasa.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

object DeviceOptimizationHelper {

    val isXiaomi: Boolean
        get() {
            val m = Build.MANUFACTURER.lowercase()
            val b = Build.BRAND.lowercase()
            return m.contains("xiaomi") || m.contains("redmi") || b.contains("poco")
        }

    val isSamsung: Boolean
        get() {
            val m = Build.MANUFACTURER.lowercase()
            val b = Build.BRAND.lowercase()
            return m.contains("samsung") || b.contains("samsung")
        }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        } catch (e: Exception) {
            true
        }
    }

    fun openBatteryOptimizationSettings(context: Context) {
        // 1. Try Xiaomi Autostart if on MIUI / HyperOS
        if (isXiaomi) {
            try {
                val intent = Intent().apply {
                    component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // Fallback to standard
            }
        }

        // 2. Try standard Ignore Battery Optimizations intent
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return
        } catch (e: Exception) {
            // Fallback to app details
        }

        // 3. Fallback to App Info screen
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
