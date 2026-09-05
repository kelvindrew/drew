package com.example.kasa.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

data class AppIconOption(
    val key: String,
    val title: String,
    val subtitle: String,
    val previewEmoji: String,
    val primaryColorHex: String,
    val aliasName: String
)

object AppIconManager {
    private const val TAG = "AppIconManager"

    val ICONS = listOf(
        AppIconOption(
            key = "default",
            title = "Kasa Classique",
            subtitle = "Vert Forêt & Toiture Dorée",
            previewEmoji = "🏡",
            primaryColorHex = "#047857",
            aliasName = "com.example.kasa.MainActivityDefault"
        ),
        AppIconOption(
            key = "gold",
            title = "Or Impérial",
            subtitle = "Bleu Royal & Or Prestige",
            previewEmoji = "👑",
            primaryColorHex = "#F59E0B",
            aliasName = "com.example.kasa.MainActivityGold"
        ),
        AppIconOption(
            key = "emerald",
            title = "Émeraude Pure",
            subtitle = "Menthe Vivifiante & Blanc Épuré",
            previewEmoji = "🌿",
            primaryColorHex = "#10B981",
            aliasName = "com.example.kasa.MainActivityEmerald"
        ),
        AppIconOption(
            key = "dark",
            title = "Nuit Sombre",
            subtitle = "Noir Stealth & Cyan Cyber",
            previewEmoji = "🌙",
            primaryColorHex = "#0F172A",
            aliasName = "com.example.kasa.MainActivityDark"
        ),
        AppIconOption(
            key = "sunset",
            title = "Coucher de Soleil",
            subtitle = "Rubis, Rose Corail & Ambre",
            previewEmoji = "🌅",
            primaryColorHex = "#BE185D",
            aliasName = "com.example.kasa.MainActivitySunset"
        )
    )

    fun getIconDisplayName(key: String): String {
        return ICONS.find { it.key == key }?.title ?: "Kasa Classique"
    }

    fun getIconEmoji(key: String): String {
        return ICONS.find { it.key == key }?.previewEmoji ?: "🏡"
    }

    fun getCurrentIconKey(context: Context): String {
        return try {
            val pm = context.packageManager
            for (option in ICONS) {
                val component = ComponentName(context, option.aliasName)
                val state = pm.getComponentEnabledSetting(component)
                if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                    return option.key
                }
            }
            "default"
        } catch (e: Throwable) {
            Log.e(TAG, "getCurrentIconKey error: ${e.message}", e)
            "default"
        }
    }

    fun applyAppIcon(context: Context, targetKey: String) {
        try {
            val pm = context.packageManager
            val selectedOption = ICONS.find { it.key == targetKey } ?: ICONS.first()
            if (getCurrentIconKey(context) == selectedOption.key) {
                return
            }

            for (option in ICONS) {
                val component = ComponentName(context, option.aliasName)
                val isTarget = (option.key == selectedOption.key)
                val newState = if (isTarget) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }

                val currentState = pm.getComponentEnabledSetting(component)
                if (currentState != newState) {
                    pm.setComponentEnabledSetting(
                        component,
                        newState,
                        PackageManager.DONT_KILL_APP
                    )
                    Log.d(TAG, "Component ${option.aliasName} updated to $newState")
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "applyAppIcon error: ${e.message}", e)
        }
    }
}
