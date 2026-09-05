package com.example.kasa.ui.screens.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import com.example.kasa.ui.components.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.service.FamilyLocationManager
import com.example.kasa.theme.*
import com.example.kasa.util.AppIconManager
import com.example.kasa.util.DeviceOptimizationHelper

@Composable
fun SettingsScreen(
    onResetHousehold: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val household by KasaRepository.currentHousehold.collectAsState()
    val currentUser by KasaRepository.currentUser.collectAsState()
    val members by KasaRepository.members.collectAsState()
    val mealPlan by KasaRepository.currentMealPlan.collectAsState()

    val currentAestheticTheme by ThemeManager.aestheticTheme.collectAsState()
    val themeMode by ThemeManager.themeMode.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    var pushNotifications by remember { mutableStateOf(true) }
    var budgetAlerts by remember { mutableStateOf(true) }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEditHouseholdDialog by remember { mutableStateOf(false) }
    var showEditHomeLocationDialog by remember { mutableStateOf(false) }
    var showAppIconDialog by remember { mutableStateOf(false) }
    var showAdminMealDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Paramètres",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Profile Section Card (Clickable to Edit Name)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showEditProfileDialog = true },
            shape = RoundedCornerShape(KasaDimens.radiusMd),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(25.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            currentUser?.initials ?: "U",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = currentUser?.name ?: "Mon Profil",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (currentUser?.isAdmin == true) "Administrateur" else "Membre",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = MaterialTheme.colorScheme.primary)
            }
        }
        // ==========================================
        // 🎨 PERSONNALISATION DU THÈME (BOUTON UNIQUE)
        // ==========================================
        Text(
            text = "🎨 Personnalisation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        val nextTheme = remember(currentAestheticTheme) { ThemeManager.getNextAestheticTheme() }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val applied = ThemeManager.cycleAestheticTheme()
                    android.widget.Toast.makeText(
                        context,
                        "Thème : ${applied.previewEmoji} ${applied.title}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    ThemedIconBadge(size = 44.dp) {
                        Text(currentAestheticTheme.previewEmoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = currentAestheticTheme.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cliquez pour passer à ${nextTheme.previewEmoji} ${nextTheme.title}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = {
                        val applied = ThemeManager.cycleAestheticTheme()
                        android.widget.Toast.makeText(
                            context,
                            "Thème : ${applied.previewEmoji} ${applied.title}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Changer ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Household Settings Card
        Text("Foyer & Devises", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showEditHouseholdDialog = true },
            shape = RoundedCornerShape(KasaDimens.radiusMd),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = household?.name ?: "Mon Foyer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Devise: ${household?.currency ?: "USD"} • Code: ${household?.formattedInviteCode ?: "UNK-CODE"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Admin Home Location Settings Card
        if (currentUser?.isAdmin == true) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEditHomeLocationDialog = true },
                shape = RoundedCornerShape(KasaDimens.radiusMd),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("🏠", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Emplacement de la Maison",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (household?.hasHomeLocation == true) {
                                    household?.homeAddress.orEmpty().ifEmpty { "Domicile Familial (Configuré)" }
                                } else {
                                    "Non configuré (cliquez pour définir)"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (household?.hasHomeLocation == true) KasaSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(Icons.Default.EditLocationAlt, contentDescription = "Modifier", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Admin Remote App Icon Settings Card
            val currentAppIcon = household?.appIcon ?: "default"
            val iconTitle = AppIconManager.getIconDisplayName(currentAppIcon)
            val iconEmoji = AppIconManager.getIconEmoji(currentAppIcon)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAppIconDialog = true },
                shape = RoundedCornerShape(KasaDimens.radiusMd),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(iconEmoji, fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Icône de l'application (Foyer)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$iconTitle • Contrôle à distance",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Text(
                        text = "Changer",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Admin Meal Plan Card
            val mealDescription = when {
                mealPlan.dinnerTitle.isNotBlank() -> "Ce soir : ${mealPlan.dinnerTitle}${if (!mealPlan.chefUserName.isNullOrBlank()) " (Chef : ${mealPlan.chefUserName})" else ""}"
                mealPlan.lunchTitle.isNotBlank() -> "Ce midi : ${mealPlan.lunchTitle}"
                else -> "Non planifié (cliquez pour définir)"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAdminMealDialog = true },
                shape = RoundedCornerShape(KasaDimens.radiusMd),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🍽️", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Repas du Jour & Chef",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mealDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (mealPlan.hasMeal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (mealPlan.hasMeal) FontWeight.Medium else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Text(
                        text = "Gérer",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Text("Préférences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(KasaDimens.radiusMd),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsSwitchRow(
                    icon = Icons.Default.Notifications,
                    title = "Notifications push",
                    checked = pushNotifications,
                    onCheckedChange = { pushNotifications = it }
                )

                HorizontalDivider()

                SettingsSwitchRow(
                    icon = Icons.Default.Warning,
                    title = "Alertes dépassement budget",
                    checked = budgetAlerts,
                    onCheckedChange = { budgetAlerts = it }
                )
            }
        }

        // 📱 Device Compatibility & Background Optimization (Xiaomi / Samsung / Android)
        val isOptIgnoring = remember { mutableStateOf(DeviceOptimizationHelper.isIgnoringBatteryOptimizations(context)) }
        val deviceBrand = when {
            DeviceOptimizationHelper.isXiaomi -> "Xiaomi (HyperOS / MIUI)"
            DeviceOptimizationHelper.isSamsung -> "Samsung (One UI)"
            else -> "Android"
        }

        Text("Compatibilité Système & Arrière-plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(KasaDimens.radiusMd),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("⚡", fontSize = 20.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Optimisation Batterie : $deviceBrand",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Permet aux alertes de sécurité et à la synchronisation en arrière-plan de ne pas être stoppées par le gestionnaire d'énergie.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = {
                        DeviceOptimizationHelper.openBatteryOptimizationSettings(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(KasaDimens.radiusMd)
                ) {
                    Icon(Icons.Default.BatteryChargingFull, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOptIgnoring.value) "Arrière-plan sans restriction (Gérer)" else "Désactiver les restrictions d'arrière-plan",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Text("À propos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(KasaDimens.radiusMd),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Unknown Budget v1.0.0", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Application native Android de gestion de budget et provisions pour les foyers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedButton(
            onClick = {
                KasaRepository.resetHousehold()
                onResetHousehold?.invoke()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(KasaDimens.radiusMd),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = KasaError)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = KasaError)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Changer ou quitter le foyer", fontWeight = FontWeight.SemiBold, color = KasaError)
        }
    }

    // Dialog: Edit Profile Name
    if (showEditProfileDialog) {
        var name by remember { mutableStateOf(currentUser?.name ?: "") }
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Modifier votre profil", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Votre prénom ou nom") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            KasaRepository.updateUserProfile(name.trim())
                            showEditProfileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Enregistrer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog: Admin Meal Plan
    if (showAdminMealDialog) {
        AdminMealDialog(
            currentPlan = mealPlan,
            members = members,
            onDismiss = { showAdminMealDialog = false },
            onSave = { lunch, lunchDet, dinner, dinnerDet, chefId, chefName, note ->
                KasaRepository.updateMealPlan(
                    lunchTitle = lunch,
                    lunchDetails = lunchDet,
                    dinnerTitle = dinner,
                    dinnerDetails = dinnerDet,
                    chefUserId = chefId,
                    chefUserName = chefName,
                    specialNote = note
                )
                showAdminMealDialog = false
            }
        )
    }

    // Dialog: Edit Household Info
    if (showEditHouseholdDialog) {
        var hName by remember { mutableStateOf(household?.name ?: "") }
        var currency by remember { mutableStateOf(household?.currency ?: "USD") }
        var secCurrency by remember { mutableStateOf(household?.secondaryCurrency ?: "CDF") }
        var rateText by remember { mutableStateOf((household?.exchangeRate ?: 2250.0).toString()) }

        val currencies = listOf("USD", "EUR", "CDF", "FCFA")

        AlertDialog(
            onDismissRequest = { showEditHouseholdDialog = false },
            title = { Text("Modifier le foyer", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = hName,
                        onValueChange = { hName = it },
                        label = { Text("Nom du foyer") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Devise principale", style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        currencies.forEach { curr ->
                            FilterChip(
                                selected = currency == curr,
                                onClick = { currency = curr },
                                label = { Text(curr) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text("Taux de change (1 $currency = X $secCurrency)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (hName.isNotBlank()) {
                            val rate = rateText.toDoubleOrNull() ?: 2250.0
                            KasaRepository.updateHousehold(hName.trim(), currency, secCurrency, rate)
                            showEditHouseholdDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Enregistrer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditHouseholdDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showEditHomeLocationDialog) {
        val dlgContext = LocalContext.current
        var addressText by remember { mutableStateOf(household?.homeAddress.orEmpty().ifEmpty { "Domicile Familial" }) }
        var latText by remember { mutableStateOf(household?.homeLatitude?.toString().orEmpty()) }
        var lngText by remember { mutableStateOf(household?.homeLongitude?.toString().orEmpty()) }
        var isFetchingGps by remember { mutableStateOf(false) }

        val permLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                isFetchingGps = true
                FamilyLocationManager.forceInstantLocationUpdate(dlgContext) { loc ->
                    if (loc != null) {
                        latText = loc.latitude.toString()
                        lngText = loc.longitude.toString()
                        if (addressText.isBlank() || addressText == "Domicile Familial") {
                            addressText = "Maison (${String.format("%.4f", loc.latitude)}, ${String.format("%.4f", loc.longitude)})"
                        }
                    }
                    isFetchingGps = false
                }
            }
        }

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
                        text = "Définissez le nom/l'adresse ou les coordonnées GPS de la maison pour activer le géorepérage du foyer.",
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
                            if (FamilyLocationManager.hasLocationPermission(dlgContext)) {
                                isFetchingGps = true
                                FamilyLocationManager.forceInstantLocationUpdate(dlgContext) { loc ->
                                    if (loc != null) {
                                        latText = loc.latitude.toString()
                                        lngText = loc.longitude.toString()
                                        val resolved = FamilyLocationManager.resolveAddress(dlgContext, loc.latitude, loc.longitude)
                                        addressText = if (resolved.isNotBlank()) resolved else "Maison (${String.format("%.4f", loc.latitude)}, ${String.format("%.4f", loc.longitude)})"
                                    }
                                    isFetchingGps = false
                                }
                            } else {
                                permLauncher.launch(
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
                            val coords = FamilyLocationManager.resolveCoordinates(dlgContext, addressText.trim())
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
                            showEditHomeLocationDialog = false
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

    if (showAppIconDialog) {
        var selectedIconKey by remember { mutableStateOf(household?.appIcon ?: "default") }

        AlertDialog(
            onDismissRequest = { showAppIconDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎨", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Icône de l'Application", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "En tant qu'administrateur, choisissez l'icône de l'application. Elle s'appliquera automatiquement et à distance sur les téléphones de tous les membres du foyer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    AppIconManager.ICONS.forEach { iconOption ->
                        val isSelected = selectedIconKey == iconOption.key
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedIconKey = iconOption.key },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(iconOption.previewEmoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = iconOption.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = iconOption.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Sélectionné",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        KasaRepository.updateHouseholdAppIcon(selectedIconKey)
                        showAppIconDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Appliquer à tous les membres 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppIconDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        )
    }
}
