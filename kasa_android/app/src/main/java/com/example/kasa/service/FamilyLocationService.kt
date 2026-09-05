package com.example.kasa.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.kasa.R
import com.example.kasa.data.repository.KasaRepository
import com.google.android.gms.location.*

class FamilyLocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    override fun onCreate() {
        super.onCreate()
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            createNotificationChannel()
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopLocationUpdates()
            stopSelf()
            return START_NOT_STICKY
        }

        // Verify permission before attempting to start foreground service on Android 14+
        if (FamilyLocationManager.hasLocationPermission(this)) {
            try {
                startForegroundServiceNotification()
                startLocationUpdates()
                isRunning = true
            } catch (e: Exception) {
                stopSelf()
                return START_NOT_STICKY
            }
        } else {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Protection Familiale & Sécurité",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Synchronisation de la position pour le foyer familial"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startForegroundServiceNotification() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Protection Familiale Active")
            .setContentText("Localisation sécurisée partagée avec le foyer")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            INTERVAL_MILLIS
        )
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MILLIS)
            .setMinUpdateDistanceMeters(25f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location: Location = result.lastLocation ?: return
                handleNewLocation(location)
            }
        }

        try {
            locationCallback?.let { cb ->
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    cb,
                    Looper.getMainLooper()
                )
            }
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    private fun handleNewLocation(location: Location) {
        val state = com.example.kasa.util.DeviceStatusHelper.getPhoneState(this)
        val lat = location.latitude
        val lng = location.longitude
        val label = "Position GPS (${String.format("%.4f", lat)}, ${String.format("%.4f", lng)})"
        KasaRepository.updateCurrentLocation(
            lat = lat,
            lng = lng,
            locationName = label,
            batteryLevel = state.batteryLevel,
            isCharging = state.isCharging,
            deviceModel = state.deviceModel
        )
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }

    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "kasa_family_safety_channel"
        const val NOTIFICATION_ID = 9901
        const val ACTION_STOP_SERVICE = "com.example.kasa.ACTION_STOP_SERVICE"

        private var isRunning = false

        private const val INTERVAL_MILLIS = 3 * 60 * 1000L // 3 minutes
        private const val FASTEST_INTERVAL_MILLIS = 60 * 1000L // 1 minute

        fun getBatteryLevel(context: Context): Int {
            return try {
                val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = context.registerReceiver(null, filter)
                val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) {
                    ((level.toFloat() / scale.toFloat()) * 100).toInt()
                } else 100
            } catch (e: Exception) {
                100
            }
        }

        fun start(context: Context) {
            if (!FamilyLocationManager.hasLocationPermission(context)) return
            try {
                val intent = Intent(context, FamilyLocationService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                // Ignore if in background restriction
            }
        }

        fun stop(context: Context) {
            try {
                context.stopService(Intent(context, FamilyLocationService::class.java))
                isRunning = false
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
