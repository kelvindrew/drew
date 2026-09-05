package com.example.kasa.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.model.HouseEssentialInfo
import com.example.kasa.data.model.PostItModel
import com.example.kasa.data.model.UserModel
import com.example.kasa.data.repository.KasaRepository
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CorkboardDialog(
    postIts: List<PostItModel>,
    houseEssentials: HouseEssentialInfo,
    currentUser: UserModel?,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onOpenWifiQr: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentUserId = currentUser?.id.orEmpty()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Post-it, 1: Infos Maison

    // Post-it add form state
    var showAddPostIt by remember { mutableStateOf(false) }
    var newPostItText by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#FEF08A") }
    var selectedEmoji by remember { mutableStateOf("📌") }
    var isPinned by remember { mutableStateOf(false) }

    // Edit Essentials form state
    var showEditEssentials by remember { mutableStateOf(false) }
    var editSsid by remember(houseEssentials) { mutableStateOf(houseEssentials.wifiSsid) }
    var editPassword by remember(houseEssentials) { mutableStateOf(houseEssentials.wifiPassword) }
    var editGateCode by remember(houseEssentials) { mutableStateOf(houseEssentials.gateCode) }
    var editBuildingCode by remember(houseEssentials) { mutableStateOf(houseEssentials.buildingCode) }
    var editLandlordName by remember(houseEssentials) { mutableStateOf(houseEssentials.landlordName) }
    var editLandlordPhone by remember(houseEssentials) { mutableStateOf(houseEssentials.landlordPhone) }
    var editEmergencyNotes by remember(houseEssentials) { mutableStateOf(houseEssentials.emergencyNotes) }
    var newRuleText by remember { mutableStateOf("") }

    val postItColors = listOf(
        "#FEF08A" to "Jaune",
        "#FBCFE8" to "Rose",
        "#A7F3D0" to "Menthe",
        "#DDD6FE" to "Lavande",
        "#FED7AA" to "Pêche"
    )

    val postItEmojis = listOf("📌", "💡", "⚠️", "🧹", "🍕", "🎉", "🔑", "❤️")
    val dateFormatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ThemedIconBadge(size = 38.dp) {
                            Text("📌", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tableau de la Maison",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Post-it & Infos Essentielles",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Post-it (${postIts.size})", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Infos Maison", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
            ) {
                if (selectedTab == 0) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AnimatedVisibility(
                            visible = !showAddPostIt,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            OutlinedButton(
                                onClick = { showAddPostIt = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Épingler un nouveau post-it")
                            }
                        }

                        AnimatedVisibility(
                            visible = showAddPostIt,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Nouveau Post-it",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = newPostItText,
                                        onValueChange = { newPostItText = it },
                                        placeholder = { Text("Message du post-it...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 4,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Couleur :", style = MaterialTheme.typography.labelSmall)
                                        postItColors.forEach { (colorCode, _) ->
                                            val c = try { Color(android.graphics.Color.parseColor(colorCode)) } catch (e: Exception) { Color.Yellow }
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(c)
                                                    .clickable { selectedColorHex = colorCode }
                                                    .then(
                                                        if (selectedColorHex == colorCode) {
                                                            Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                                        } else Modifier
                                                    )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            postItEmojis.take(5).forEach { em ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(28.dp)
                                                        .clip(CircleShape)
                                                        .background(if (selectedEmoji == em) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                                        .clickable { selectedEmoji = em },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(em, fontSize = 14.sp)
                                                }
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { isPinned = !isPinned }
                                        ) {
                                            Checkbox(
                                                checked = isPinned,
                                                onCheckedChange = { isPinned = it },
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Épingler", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = { showAddPostIt = false }) {
                                            Text("Annuler")
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                if (newPostItText.isNotBlank()) {
                                                    KasaRepository.addPostIt(
                                                        text = newPostItText.trim(),
                                                        colorHex = selectedColorHex,
                                                        emoji = selectedEmoji,
                                                        isPinned = isPinned
                                                    )
                                                    newPostItText = ""
                                                    showAddPostIt = false
                                                    Toast.makeText(context, "Post-it épinglé !", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Épingler")
                                        }
                                    }
                                }
                            }
                        }

                        if (postIts.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📋", fontSize = 40.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Aucun post-it sur le tableau",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Laissez un mot doux, un rappel de ménage ou une remarque sympa à vos colocataires !",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(postIts, key = { it.id }) { postIt ->
                                    val bgCardColor = try {
                                        Color(android.graphics.Color.parseColor(postIt.colorHex))
                                    } catch (e: Exception) {
                                        Color(0xFFFEF08A)
                                    }

                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = bgCardColor),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(postIt.emoji, fontSize = 18.sp)
                                                    if (postIt.isPinned) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("📌", fontSize = 12.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = postIt.authorName,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1E293B)
                                                    )
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = dateFormatter.format(postIt.createdAt),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFF64748B)
                                                    )

                                                    IconButton(
                                                        onClick = { KasaRepository.togglePostItPin(postIt.id) },
                                                        modifier = Modifier.size(26.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (postIt.isPinned) Icons.Default.PushPin else Icons.Default.VerticalAlignTop,
                                                            contentDescription = "Épingler",
                                                            tint = Color(0xFF334155),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }

                                                    if (postIt.authorId == currentUserId || isAdmin) {
                                                        IconButton(
                                                            onClick = { KasaRepository.deletePostIt(postIt.id) },
                                                            modifier = Modifier.size(26.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Supprimer",
                                                                tint = Color(0xFFDC2626),
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            Text(
                                                text = postIt.text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF0F172A),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("📡", fontSize = 20.sp)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Wi-Fi du Foyer",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = if (houseEssentials.wifiSsid.isNotBlank()) houseEssentials.wifiSsid else "Non configuré",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Button(
                                            onClick = onOpenWifiQr,
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("QR Code", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🔑 Codes d'Accès",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Code Portail / Entrée", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = if (houseEssentials.gateCode.isNotBlank()) houseEssentials.gateCode else "Non renseigné",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        if (houseEssentials.gateCode.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(houseEssentials.gateCode))
                                                    Toast.makeText(context, "Code copié !", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copier", modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Digicode Immeuble", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = if (houseEssentials.buildingCode.isNotBlank()) houseEssentials.buildingCode else "Non renseigné",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        if (houseEssentials.buildingCode.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(houseEssentials.buildingCode))
                                                    Toast.makeText(context, "Digicode copié !", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copier", modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "📞 Contact Propriétaire / Agence",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (houseEssentials.landlordName.isNotBlank()) houseEssentials.landlordName else "Non renseigné",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (houseEssentials.landlordPhone.isNotBlank()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = houseEssentials.landlordPhone,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            IconButton(
                                                onClick = {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${houseEssentials.landlordPhone}"))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        clipboardManager.setText(AnnotatedString(houseEssentials.landlordPhone))
                                                        Toast.makeText(context, "Numéro copié !", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = "Appeler", modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (houseEssentials.emergencyNotes.isNotBlank()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "🚨 Consignes Urgentes (Disjoncteur, Eau...)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = houseEssentials.emergencyNotes,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "📜 Règles de Vie du Foyer",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    if (houseEssentials.houseRules.isEmpty()) {
                                        Text(
                                            text = "Aucune règle spécifique définie. Respect, calme et bonne humeur !",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        houseEssentials.houseRules.forEachIndexed { idx, rule ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("• ", fontWeight = FontWeight.Bold)
                                                Text(
                                                    text = rule,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (isAdmin) {
                                                    IconButton(
                                                        onClick = {
                                                            val updatedRules = houseEssentials.houseRules.toMutableList()
                                                            updatedRules.removeAt(idx)
                                                            KasaRepository.updateHouseEssentials(houseEssentials.copy(houseRules = updatedRules))
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = "Supprimer règle", modifier = Modifier.size(12.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (isAdmin) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            OutlinedTextField(
                                                value = newRuleText,
                                                onValueChange = { newRuleText = it },
                                                placeholder = { Text("Nouvelle règle...", fontSize = 12.sp) },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            IconButton(
                                                onClick = {
                                                    if (newRuleText.isNotBlank()) {
                                                        val updatedRules = houseEssentials.houseRules + newRuleText.trim()
                                                        KasaRepository.updateHouseEssentials(houseEssentials.copy(houseRules = updatedRules))
                                                        newRuleText = ""
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Ajouter")
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedButton(
                                onClick = { showEditEssentials = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Modifier les informations du foyer")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )

    if (showEditEssentials) {
        AlertDialog(
            onDismissRequest = { showEditEssentials = false },
            title = {
                Text("Modifier les Infos de la Maison", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = editSsid,
                            onValueChange = { editSsid = it },
                            label = { Text("Nom Wi-Fi (SSID)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editPassword,
                            onValueChange = { editPassword = it },
                            label = { Text("Mot de passe Wi-Fi") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editGateCode,
                            onValueChange = { editGateCode = it },
                            label = { Text("Code Portail / Entrée") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editBuildingCode,
                            onValueChange = { editBuildingCode = it },
                            label = { Text("Digicode Immeuble") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editLandlordName,
                            onValueChange = { editLandlordName = it },
                            label = { Text("Nom Propriétaire / Agence") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editLandlordPhone,
                            onValueChange = { editLandlordPhone = it },
                            label = { Text("Téléphone Propriétaire") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editEmergencyNotes,
                            onValueChange = { editEmergencyNotes = it },
                            label = { Text("Consignes d'urgence") },
                            placeholder = { Text("Ex: Vanne sous l'évier, disjoncteur...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = houseEssentials.copy(
                            wifiSsid = editSsid.trim(),
                            wifiPassword = editPassword.trim(),
                            gateCode = editGateCode.trim(),
                            buildingCode = editBuildingCode.trim(),
                            landlordName = editLandlordName.trim(),
                            landlordPhone = editLandlordPhone.trim(),
                            emergencyNotes = editEmergencyNotes.trim()
                        )
                        KasaRepository.updateHouseEssentials(updated)
                        showEditEssentials = false
                        Toast.makeText(context, "Informations mises à jour !", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditEssentials = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
