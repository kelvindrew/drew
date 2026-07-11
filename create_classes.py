import os

def write_file(path, content):
    dir_name = os.path.dirname(path)
    if dir_name:
        os.makedirs(dir_name, exist_ok=True)
    with open(path, "w") as f:
        f.write(content.strip() + "\n")

# Module UI: Glassmorphism & Dashboard
write_file("ui/src/main/java/com/connect/ui/Glassmorphism.kt", """
package com.connect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.glassmorphism(): Modifier = composed {
    this
        .clip(RoundedCornerShape(16.dp))
        .background(Color.White.copy(alpha = 0.1f))
        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        .padding(16.dp)
}
""")

write_file("ui/src/main/java/com/connect/ui/DashboardScreen.kt", """
package com.connect.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Smart Coach Dashboard", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.glassmorphism()) {
            Text("Connection: Connected")
            Text("Battery: 85%")
        }
    }
}
""")

# Module AI: SmartCoachAI
write_file("ai/src/main/java/com/connect/ai/SmartCoachAI.kt", """
package com.connect.ai

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SmartCoachAI(private val apiKey: String) {
    private val model = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = apiKey
    )

    fun generateRecommendations(healthData: String): Flow<String> = flow {
        val prompt = "En tant que coach sportif IA, analyse ces données et donne des recommandations: $healthData"
        val response = model.generateContent(prompt)
        emit(response.text ?: "Aucune recommandation générée.")
    }
}
""")

# Module Bluetooth: BleScannerManager
write_file("bluetooth/src/main/java/com/connect/bluetooth/BleScannerManager.kt", """
package com.connect.bluetooth

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log

class BleScannerManager(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val scanner = bluetoothManager.adapter?.bluetoothLeScanner

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.d("BleScanner", "Found device: ${result?.device?.address}")
        }
    }

    fun startScan() {
        // Requires BLUETOOTH_SCAN permission handling in actual implementation
        try {
            scanner?.startScan(scanCallback)
        } catch (e: SecurityException) {
            Log.e("BleScanner", "Missing permissions", e)
        }
    }

    fun stopScan() {
        try {
            scanner?.stopScan(scanCallback)
        } catch (e: SecurityException) {
            Log.e("BleScanner", "Missing permissions", e)
        }
    }
}
""")

# Module Health: HealthConnectManager
write_file("health/src/main/java/com/connect/health/HealthConnectManager.kt", """
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
""")

# Module Notifications: WatchNotificationListenerService
write_file("notifications/src/main/java/com/connect/notifications/WatchNotificationListenerService.kt", """
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
""")

# Module Settings: SettingsDataStore
write_file("settings/src/main/java/com/connect/settings/SettingsDataStore.kt", """
package com.connect.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val isDarkThemeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_THEME] ?: false
        }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME] = enabled
        }
    }
}
""")

# Module Watch: WatchfaceStoreScreen
write_file("watch/src/main/java/com/connect/watch/WatchfaceStoreScreen.kt", """
package com.connect.watch

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun WatchfaceStoreScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Premium Watchfaces")
        Spacer(modifier = Modifier.height(16.dp))
        // Example Watchface Card
        AsyncImage(
            model = "https://example.com/watchface.png",
            contentDescription = "Watchface Preview",
            modifier = Modifier.size(150.dp)
        )
        Text("Author: Connect Premium")
    }
}
""")

# Module Sport: SportTracker
write_file("sport/src/main/java/com/connect/sport/SportTracker.kt", """
package com.connect.sport

// Stub for Google Maps Integration
class SportTracker {
    fun startTracking() {
        // Initialize GPS tracking
    }

    fun getRoutePoints(): List<Pair<Double, Double>> {
        // Returns coordinates for Maps polyline
        return emptyList()
    }
}
""")
