package com.example.kasa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.service.FamilyLocationManager
import com.example.kasa.theme.KASATheme
import com.example.kasa.theme.ThemeManager
import com.example.kasa.ui.navigation.KasaNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            KasaRepository.init(this)
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "KasaRepository init error", e)
        }
        try {
            ThemeManager.init(this)
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "ThemeManager init error", e)
        }
        try {
            FamilyLocationManager.init(this)
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "FamilyLocationManager init error", e)
        }
        try {
            val hh = KasaRepository.currentHousehold.value
            if (hh != null && hh.appIcon.isNotBlank()) {
                com.example.kasa.util.AppIconManager.applyAppIcon(this, hh.appIcon)
            }
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "AppIconManager init error", e)
        }

        // Request all essential runtime permissions on first open
        val missingPermissions = mutableListOf<String>()
        if (!FamilyLocationManager.hasLocationPermission(this)) {
            missingPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            missingPermissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            missingPermissions.add(android.Manifest.permission.CAMERA)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                missingPermissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (missingPermissions.isNotEmpty()) {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                101
            )
        }

        enableEdgeToEdge()
        setContent {
            val themeMode by ThemeManager.themeMode.collectAsState()
            val aestheticTheme by ThemeManager.aestheticTheme.collectAsState()
            val cornerStyle by ThemeManager.cornerStyle.collectAsState()

            KASATheme(
                themeMode = themeMode,
                aestheticTheme = aestheticTheme,
                cornerStyle = cornerStyle
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    KasaNavGraph()
                    com.example.kasa.ui.components.PermissionOnboardingDialog()
                }
            }
        }
        com.example.kasa.util.DeviceStatusHelper.syncDeviceStatus(this)
    }

    override fun onResume() {
        super.onResume()
        com.example.kasa.util.DeviceStatusHelper.syncDeviceStatus(this)
    }
}
