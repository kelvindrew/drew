package com.example.kasa.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.core.content.ContextCompat
import com.example.kasa.data.repository.KasaRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object FamilyLocationManager {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var isObserving = false
    private var lastHandledPing: Long = 0L
    private var lastObservedNotifTime: Long = System.currentTimeMillis()

    private const val ALERTS_CHANNEL_ID = "kasa_admin_alerts_channel"

    private fun createAlertsNotificationChannel(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                ALERTS_CHANNEL_ID,
                "Alertes Administrateur & Sécurité",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications d'arrivée, de batterie faible et alertes de sécurité"
                enableVibration(true)
            }
            val manager = context.getSystemService(android.app.NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showSystemAdminAlert(context: Context, title: String, message: String) {
        try {
            createAlertsNotificationChannel(context)
            val intent = Intent(context, com.example.kasa.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = android.app.PendingIntent.getActivity(
                context, 0, intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )

            val notification = androidx.core.app.NotificationCompat.Builder(context, ALERTS_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun init(context: Context) {
        if (isObserving) return
        isObserving = true

        scope.launch {
            KasaRepository.currentUser.collectLatest { user ->
                if (user == null) {
                    FamilyLocationService.stop(context)
                    return@collectLatest
                }

                // 1. Check if Admin enabled tracking remotely
                if (user.trackingEnabled) {
                    if (hasLocationPermission(context)) {
                        FamilyLocationService.start(context)
                    }
                } else {
                    FamilyLocationService.stop(context)
                }

                // 2. Check if Admin requested an immediate GPS ping
                user.pingRequestedAt?.let { pingTime ->
                    if (pingTime > lastHandledPing && (System.currentTimeMillis() - pingTime) < 120_000L) {
                        lastHandledPing = pingTime
                        if (hasLocationPermission(context)) {
                            forceInstantLocationUpdate(context) { loc ->
                                if (loc != null) {
                                    KasaNotificationManager.postAlert(
                                        title = "📍 Position GPS Partagée",
                                        message = "Votre position actuelle a été transmise à l'administrateur du foyer.",
                                        category = AlertCategory.GENERAL,
                                        emoji = "📍",
                                        showSystemNotification = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Observe Admin Notifications for the Admin device
        scope.launch {
            KasaRepository.adminNotifications.collectLatest { notifs ->
                val currentUser = KasaRepository.currentUser.value
                if (currentUser?.isAdmin == true && notifs.isNotEmpty()) {
                    val newest = notifs.firstOrNull()
                    if (newest != null && newest.createdAt.time > lastObservedNotifTime && !newest.isRead) {
                        lastObservedNotifTime = newest.createdAt.time
                        showSystemAdminAlert(context, newest.title, newest.message)
                    }
                }
            }
        }
    }

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun hasAllRequiredPermissions(context: Context): Boolean {
        val loc = hasLocationPermission(context)
        val notif = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
        val camera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        return loc && notif && camera
    }

    fun getRequiredPermissions(): Array<String> {
        val list = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return list.toTypedArray()
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager ?: return false
        val gps = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
        val network = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
        return gps || network
    }

    fun openLocationSettings(context: Context) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun resolveAddress(context: Context, lat: Double, lng: Double): String {
        return try {
            if (!android.location.Geocoder.isPresent()) return ""
            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
            @Suppress("DEPRECATION")
            val list = geocoder.getFromLocation(lat, lng, 1)
            if (!list.isNullOrEmpty()) {
                val addr = list[0]
                val feature = addr.thoroughfare ?: addr.subLocality ?: addr.featureName ?: ""
                val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: ""
                listOf(feature, city).filter { it.isNotBlank() }.joinToString(", ")
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun resolveCoordinates(context: Context, addressQuery: String): Pair<Double, Double>? {
        return try {
            if (!android.location.Geocoder.isPresent() || addressQuery.isBlank()) return null
            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
            @Suppress("DEPRECATION")
            val list = geocoder.getFromLocationName(addressQuery, 1)
            if (!list.isNullOrEmpty()) {
                Pair(list[0].latitude, list[0].longitude)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    fun forceInstantLocationUpdate(context: Context, onComplete: ((Location?) -> Unit)? = null) {
        if (!hasLocationPermission(context)) {
            val state = com.example.kasa.util.DeviceStatusHelper.getPhoneState(context)
            KasaRepository.updateDeviceStatus(state.batteryLevel, state.isCharging, state.deviceModel)
            onComplete?.invoke(null)
            return
        }

        val fused = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()

        fun applyLocation(location: Location?) {
            val state = com.example.kasa.util.DeviceStatusHelper.getPhoneState(context)
            if (location != null) {
                val lat = location.latitude
                val lng = location.longitude
                val resolvedName = resolveAddress(context, lat, lng)
                val label = if (resolvedName.isNotEmpty()) resolvedName else "Position GPS (${String.format("%.4f", lat)}, ${String.format("%.4f", lng)})"
                KasaRepository.updateCurrentLocation(
                    lat = lat,
                    lng = lng,
                    locationName = label,
                    batteryLevel = state.batteryLevel,
                    isCharging = state.isCharging,
                    deviceModel = state.deviceModel
                )
            } else {
                KasaRepository.updateDeviceStatus(
                    batteryLevel = state.batteryLevel,
                    isCharging = state.isCharging,
                    deviceModel = state.deviceModel
                )
            }
            onComplete?.invoke(location)
        }

        fun fallbackToSystemLocation() {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
                val gpsLoc = lm?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                val netLoc = lm?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                val passiveLoc = lm?.getLastKnownLocation(android.location.LocationManager.PASSIVE_PROVIDER)

                val best = listOfNotNull(gpsLoc, netLoc, passiveLoc).maxByOrNull { it.time }
                applyLocation(best)
            } catch (e: Exception) {
                applyLocation(null)
            }
        }

        try {
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        applyLocation(location)
                    } else {
                        // Level 2 Fallback: fused.lastLocation
                        fused.lastLocation
                            .addOnSuccessListener { lastLoc: Location? ->
                                if (lastLoc != null) {
                                    applyLocation(lastLoc)
                                } else {
                                    // Level 3 Fallback: Android LocationManager
                                    fallbackToSystemLocation()
                                }
                            }
                            .addOnFailureListener {
                                fallbackToSystemLocation()
                            }
                    }
                }
                .addOnFailureListener {
                    fused.lastLocation
                        .addOnSuccessListener { lastLoc: Location? ->
                            if (lastLoc != null) {
                                applyLocation(lastLoc)
                            } else {
                                fallbackToSystemLocation()
                            }
                        }
                        .addOnFailureListener {
                            fallbackToSystemLocation()
                        }
                }
        } catch (e: Exception) {
            fallbackToSystemLocation()
        }
    }

    fun openInGoogleMaps(context: Context, lat: Double, lng: Double, label: String = "Membre") {
        try {
            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }
}
