package com.example.kasa.ui.screens.members

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.kasa.data.model.UserModel
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.service.FamilyLocationManager
import com.example.kasa.service.FamilyLocationService
import com.example.kasa.theme.*
import com.example.kasa.util.DateFormatter

@Composable
fun MembersScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val household by KasaRepository.currentHousehold.collectAsState()
    val currentUser by KasaRepository.currentUser.collectAsState()
    val members by KasaRepository.members.collectAsState()
    val adminNotifications by KasaRepository.adminNotifications.collectAsState()

    val isAdmin = currentUser?.isAdmin == true

    var hasLocationPermission by remember {
        mutableStateOf(FamilyLocationManager.hasLocationPermission(context))
    }
    var isGpsHardwareEnabled by remember {
        mutableStateOf(FamilyLocationManager.isLocationEnabled(context))
    }

    var isSettingHomeLocation by remember { mutableStateOf(false) }
    var showEditHomeLocationDialog by remember { mutableStateOf(false) }
    var showAllNotifications by remember { mutableStateOf(false) }
    var actionFeedback by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = fineGranted || coarseGranted
        if (hasLocationPermission) {
            FamilyLocationManager.forceInstantLocationUpdate(context) { loc ->
                if (loc != null) {
                    actionFeedback = "📍 Position GPS synchronisée avec succès !"
                }
            }
            if (currentUser?.trackingEnabled == true) {
                FamilyLocationService.start(context)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Top Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛡️ Radar & Sécurité",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (isAdmin) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "👑 ADMIN",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Text(
                    text = if (isAdmin) "Localisation automatique, alertes batterie et détection d'arrivée à la maison" else "Protection et partage de position avec le foyer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Home Location Setup Card (For Geofencing)
        if (isAdmin) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (household?.hasHomeLocation == true) KasaSuccess.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (household?.hasHomeLocation == true) KasaSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏠", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Emplacement de la Maison",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (household?.hasHomeLocation == true) {
                                            household?.homeAddress.orEmpty().ifEmpty { "Domicile Familial" }
                                        } else {
                                            "Non configuré (définir l'adresse)"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (household?.hasHomeLocation == true) KasaSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (household?.hasHomeLocation == true && household?.homeLatitude != null && household?.homeLongitude != null) {
                                IconButton(
                                    onClick = {
                                        FamilyLocationManager.openInGoogleMaps(
                                            context,
                                            household!!.homeLatitude!!,
                                            household!!.homeLongitude!!,
                                            household?.homeAddress.orEmpty().ifEmpty { "Maison" }
                                        )
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(KasaSuccess.copy(alpha = 0.15f))
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = "Voir sur Maps", tint = KasaSuccess, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        if (household?.hasHomeLocation == true && household?.homeLatitude != null && household?.homeLongitude != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "📍 Coordonnées : ${String.format("%.4f", household?.homeLatitude)}, ${String.format("%.4f", household?.homeLongitude)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // GPS Auto-set Button
                            Button(
                                onClick = {
                                    if (hasLocationPermission) {
                                        if (!FamilyLocationManager.isLocationEnabled(context)) {
                                            actionFeedback = "⚠️ Le service GPS est désactivé. Veuillez l'activer dans les paramètres."
                                            FamilyLocationManager.openLocationSettings(context)
                                            return@Button
                                        }
                                        isSettingHomeLocation = true
                                        FamilyLocationManager.forceInstantLocationUpdate(context) { loc ->
                                            if (loc != null) {
                                                val resolved = FamilyLocationManager.resolveAddress(context, loc.latitude, loc.longitude)
                                                val addr = if (resolved.isNotBlank()) resolved else "Maison (${String.format("%.4f", loc.latitude)}, ${String.format("%.4f", loc.longitude)})"
                                                KasaRepository.setHouseholdHomeLocation(
                                                    loc.latitude,
                                                    loc.longitude,
                                                    addr
                                                )
                                                actionFeedback = "🏠 Emplacement de la maison enregistré : $addr"
                                            } else {
                                                actionFeedback = "⚠️ Impossible d'obtenir la position GPS actuelle."
                                            }
                                            isSettingHomeLocation = false
                                        }
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1.2f).height(42.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isSettingHomeLocation) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                                } else {
                                    Icon(
                                        Icons.Default.MyLocation,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (household?.hasHomeLocation == true) "Position GPS" else "Définir par GPS",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Manual Edit Button
                            OutlinedButton(
                                onClick = { showEditHomeLocationDialog = true },
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Modifier", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Non-Admin Member: Location Status & Share Button Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📍", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Ma Géolocalisation & Sécurité",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (household?.hasHomeLocation == true) {
                                        if (currentUser?.isHome == true) "🏠 Vous êtes à la maison" else "🚶 À ${String.format("%.1f", (currentUser?.distanceFromHomeMeters ?: 0.0) / 1000.0)} km de la maison"
                                    } else {
                                        "Position partagée avec l'administrateur du foyer"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (currentUser?.isHome == true) KasaSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (hasLocationPermission) {
                                    FamilyLocationManager.forceInstantLocationUpdate(context) { loc ->
                                        actionFeedback = if (loc != null) {
                                            "📍 Position GPS partagée avec succès avec le foyer !"
                                        } else {
                                            "⚠️ Impossible d'obtenir la position GPS."
                                        }
                                    }
                                } else {
                                    permissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.ShareLocation, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Partager ma position avec le Foyer 📍", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Admin Notifications & Safety Alerts Card (Admin Only)
        if (isAdmin) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (adminNotifications.any { !it.isRead }) KasaWarning.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (adminNotifications.isNotEmpty()) KasaWarning.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🔔", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Alertes Administrateur",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        val unreadCount = adminNotifications.count { !it.isRead }
                                        if (unreadCount > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = KasaError
                                            ) {
                                                Text(
                                                    text = "$unreadCount",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = if (adminNotifications.isNotEmpty()) "${adminNotifications.size} alerte(s) envoyée(s) à l'Admin uniquement" else "Aucune alerte récente",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (adminNotifications.isNotEmpty()) {
                                TextButton(
                                    onClick = { KasaRepository.clearAllAdminNotifications() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Effacer", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        if (adminNotifications.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val displayedNotifs = if (showAllNotifications) adminNotifications else adminNotifications.take(3)

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                displayedNotifs.forEach { notif ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = when (notif.type) {
                                            "low_battery" -> KasaError.copy(alpha = 0.08f)
                                            "arrival" -> KasaSuccess.copy(alpha = 0.08f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        },
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            when (notif.type) {
                                                "low_battery" -> KasaError.copy(alpha = 0.3f)
                                                "arrival" -> KasaSuccess.copy(alpha = 0.3f)
                                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                            }
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(notif.icon, fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = notif.title,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = when (notif.type) {
                                                            "low_battery" -> KasaError
                                                            "arrival" -> KasaSuccess
                                                            else -> MaterialTheme.colorScheme.onSurface
                                                        }
                                                    )
                                                    Text(
                                                        text = DateFormatter.formatTime(notif.createdAt),
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = notif.message,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }

                                if (adminNotifications.size > 3) {
                                    TextButton(
                                        onClick = { showAllNotifications = !showAllNotifications },
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    ) {
                                        Text(
                                            text = if (showAllNotifications) "Afficher moins" else "Voir les ${adminNotifications.size} alertes",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Permission Banner (if not yet granted)
        if (!hasLocationPermission) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KasaWarning.copy(alpha = 0.12f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, KasaWarning.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOff, contentDescription = null, tint = KasaWarning)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Autorisez le GPS pour synchroniser automatiquement la position",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KasaWarning),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Activer", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        actionFeedback?.let { msg ->
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Section Title: Family Members Radar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Membres du Foyer (${members.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (isAdmin) {
                    FilledTonalButton(
                        onClick = {
                            KasaRepository.requestAllMembersLocationPing()
                            actionFeedback = "📡 Signal de localisation envoyé à tous les membres !"
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Actualiser tous", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // List of Member Cards with Live GPS Tracking, Arrival & Low Battery Alerts
        items(members, key = { it.id }) { member ->
            val isCurrent = member.id == currentUser?.id
            val isLowBat = member.isLowBattery

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(if (isLowBat) 4.dp else 2.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLowBat) KasaError.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isLowBat) 2.dp else if (member.trackingEnabled) 1.5.dp else 1.dp,
                    color = if (isLowBat) KasaError else if (member.trackingEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Low Battery Warning Banner
                    if (isLowBat) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = KasaError,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.BatteryAlert, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🪫 ATTENTION : BATTERIE CRITIQUE (${member.batteryLevel}%)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Header: Avatar, Name, Role & Battery
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.initials,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = member.name + if (isCurrent) " (Moi)" else "",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (member.isAdmin) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Star, contentDescription = "Admin", tint = KasaWarning, modifier = Modifier.size(15.dp))
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Home Presence Badge directly next to Name
                                if (member.isHome) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = KasaSuccess.copy(alpha = 0.18f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, KasaSuccess.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🏠", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "À la maison",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = KasaSuccess,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                } else if (member.distanceFromHomeMeters != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🚶", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${String.format("%.1f", member.distanceFromHomeMeters!! / 1000.0)} km",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("📍", fontSize = 10.sp)
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "Dehors",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = if (member.isAdmin) "Parent / Administrateur" else "Enfant / Membre",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Battery Indicator with Charging ⚡
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when {
                                member.isCharging -> KasaSuccess.copy(alpha = 0.18f)
                                member.batteryLevel > 50 -> KasaSuccess.copy(alpha = 0.15f)
                                member.batteryLevel > 20 -> KasaWarning.copy(alpha = 0.15f)
                                else -> KasaError.copy(alpha = 0.15f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (member.isCharging) Icons.Default.BatteryChargingFull else if (isLowBat) Icons.Default.BatteryAlert else Icons.Default.BatteryChargingFull,
                                    contentDescription = null,
                                    tint = when {
                                        member.isCharging -> KasaSuccess
                                        member.batteryLevel > 50 -> KasaSuccess
                                        member.batteryLevel > 20 -> KasaWarning
                                        else -> KasaError
                                    },
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (member.isCharging) "${member.batteryLevel}% ⚡" else "${member.batteryLevel}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = when {
                                        member.isCharging -> KasaSuccess
                                        member.batteryLevel > 50 -> KasaSuccess
                                        member.batteryLevel > 20 -> KasaWarning
                                        else -> KasaError
                                    }
                                )
                            }
                        }
                    }

                    // Phone Device Info & State
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Smartphone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (member.deviceModel.isNotBlank()) member.deviceModel else "Smartphone Android",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = member.batteryStatusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    member.isCharging -> KasaSuccess
                                    member.batteryLevel <= 15 -> KasaError
                                    member.batteryLevel <= 30 -> KasaWarning
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Location Live Display Box with Arrival Status
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (member.isHome) KasaSuccess.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (member.isHome) KasaSuccess else if (member.trackingEnabled && member.hasLocation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (member.isHome) {
                                            "🏠 À la Maison" + (member.arrivedHomeAt?.let { " (depuis ${DateFormatter.formatTime(it)})" } ?: "")
                                        } else if (member.hasLocation) {
                                            member.locationName.ifEmpty { "Position GPS active" }
                                        } else {
                                            "📍 Position en attente..."
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (member.isHome) KasaSuccess else if (member.hasLocation) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                member.locationUpdatedAt?.let { dt ->
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Mis à jour à ${DateFormatter.formatTime(dt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            // Google Maps Button
                            if (member.hasLocation && member.latitude != null && member.longitude != null) {
                                IconButton(
                                    onClick = {
                                        FamilyLocationManager.openInGoogleMaps(context, member.latitude, member.longitude, member.name)
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = "Itinéraire Maps", tint = Color.White, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Remote Control Panel (Visible by Admin or Self)
                    if (isAdmin || isCurrent) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (member.trackingEnabled) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                                        contentDescription = null,
                                        tint = if (member.trackingEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isAdmin && !isCurrent) "Suivi à distance" else "Localisation automatique",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (member.trackingEnabled) "Synchronisation en continu" else "Désactivé",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (member.trackingEnabled) KasaSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Direct Ping Button (Always available for admin on other member cards)
                                    if (isAdmin && !isCurrent) {
                                        FilledTonalButton(
                                            onClick = {
                                                KasaRepository.requestMemberLocationPing(member.id)
                                                actionFeedback = "Signal GPS envoyé à ${member.name} !"
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.MyLocation,
                                                contentDescription = "Demander position",
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Demander position", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    // Switch controlled remotely by Admin
                                    Switch(
                                        checked = member.trackingEnabled,
                                        onCheckedChange = { isEnabled ->
                                            KasaRepository.setMemberTrackingRemote(member.id, isEnabled)
                                            actionFeedback = if (isEnabled) {
                                                "Localisation automatique activée à distance pour ${member.name}"
                                            } else {
                                                "Localisation désactivée pour ${member.name}"
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Household Invite Code Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        household?.let {
                            clipboard.nativeClipboard.setPrimaryClip(
                                android.content.ClipData.newPlainText("invite_code", it.formattedInviteCode)
                            )
                            actionFeedback = "Code d'invitation copié !"
                        }
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Code d'invitation du Foyer", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = household?.formattedInviteCode ?: "UNK-CODE",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                    }
                    IconButton(
                        onClick = {
                            household?.let {
                                clipboard.nativeClipboard.setPrimaryClip(
                                    android.content.ClipData.newPlainText("invite_code", it.formattedInviteCode)
                                )
                                actionFeedback = "Code d'invitation copié !"
                            }
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    // Dialog for Admin to enter or edit Home Location manually
    if (showEditHomeLocationDialog) {
        var addressText by remember { mutableStateOf(household?.homeAddress.orEmpty().ifEmpty { "Domicile Familial" }) }
        var latText by remember { mutableStateOf(household?.homeLatitude?.toString().orEmpty()) }
        var lngText by remember { mutableStateOf(household?.homeLongitude?.toString().orEmpty()) }
        var isFetchingGps by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showEditHomeLocationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏠", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Emplacement de la Maison", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Définissez le nom/l'adresse ou les coordonnées GPS de la maison pour activer la détection d'arrivée des membres.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = addressText,
                        onValueChange = { addressText = it },
                        label = { Text("Nom ou Adresse du domicile") },
                        placeholder = { Text("Ex: 12 Rue de la Paix, Kinshasa") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = latText,
                            onValueChange = { latText = it },
                            label = { Text("Latitude") },
                            placeholder = { Text("-4.3214") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lngText,
                            onValueChange = { lngText = it },
                            label = { Text("Longitude") },
                            placeholder = { Text("15.3125") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (hasLocationPermission) {
                                isFetchingGps = true
                                FamilyLocationManager.forceInstantLocationUpdate(context) { loc ->
                                    if (loc != null) {
                                        latText = loc.latitude.toString()
                                        lngText = loc.longitude.toString()
                                        val resolved = FamilyLocationManager.resolveAddress(context, loc.latitude, loc.longitude)
                                        addressText = if (resolved.isNotBlank()) resolved else "Maison (${String.format("%.4f", loc.latitude)}, ${String.format("%.4f", loc.longitude)})"
                                    }
                                    isFetchingGps = false
                                }
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isFetchingGps) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Acquisition GPS & Adresse...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Remplir avec ma position GPS", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        var lat = latText.toDoubleOrNull()
                        var lng = lngText.toDoubleOrNull()
                        if ((lat == null || lng == null) && addressText.isNotBlank()) {
                            val coords = FamilyLocationManager.resolveCoordinates(context, addressText.trim())
                            if (coords != null) {
                                lat = coords.first
                                lng = coords.second
                            }
                        }
                        if (lat != null && lng != null) {
                            KasaRepository.setHouseholdHomeLocation(
                                lat = lat,
                                lng = lng,
                                address = addressText.trim().ifEmpty { "Domicile Familial" }
                            )
                            actionFeedback = "🏠 Emplacement de la maison enregistré avec succès !"
                            showEditHomeLocationDialog = false
                        } else {
                            actionFeedback = "⚠️ Coordonnées GPS introuvables pour cette adresse."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Enregistrer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditHomeLocationDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
