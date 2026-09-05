package com.example.kasa.ui.screens.provision

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kasa.data.model.ProvisionItemModel
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.theme.*
import com.example.kasa.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onBack: () -> Unit
) {
    val household by KasaRepository.currentHousehold.collectAsState()
    val provisions by KasaRepository.provisions.collectAsState()
    val plannedItems = provisions.filter { it.isPlanned }
    val currency = household?.currency ?: "USD"

    // Express In-Store Mode state
    var isExpressMode by remember { mutableStateOf(true) }

    // Map of items currently placed in the physical cart -> Pair(isInCart: Boolean, customPrice: Double)
    val cartItems = remember { mutableStateMapOf<String, Double>() }

    // Priority filter (Tous, Urgent, Normal, Faible)
    var selectedPriorityFilter by remember { mutableStateOf("Tous") }

    // Quick add in-aisle state
    var quickItemName by remember { mutableStateOf("") }

    // Checkout dialog
    var showCheckoutDialog by remember { mutableStateOf(false) }

    // Single item edit dialog
    var itemToEditPrice by remember { mutableStateOf<ProvisionItemModel?>(null) }
    var singleItemPriceText by remember { mutableStateOf("") }

    // Filtered items
    val filteredPlannedItems = plannedItems.filter { item ->
        when (selectedPriorityFilter) {
            "Urgent" -> item.priority == "high"
            "Normal" -> item.priority == "normal"
            "Faible" -> item.priority == "low"
            else -> true
        }
    }

    // Calculations
    val inCartCount = cartItems.keys.count { id -> plannedItems.any { it.id == id } }
    val totalPlannedCount = plannedItems.size
    val progress = if (totalPlannedCount > 0) inCartCount.toFloat() / totalPlannedCount else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 350),
        label = "cart_progress"
    )

    // Live Cart Total
    val cartTotalPrice = cartItems.entries.sumOf { (id, price) ->
        if (plannedItems.any { it.id == id }) price else 0.0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🛒 Mode Course Express", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = KasaSecondary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "EN MAGASIN",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KasaSecondaryDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp
                                )
                            }
                        }
                        Text(
                            text = "${household?.name ?: "Mon Foyer"} • $totalPlannedCount article(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { isExpressMode = !isExpressMode }) {
                        Icon(
                            imageVector = if (isExpressMode) Icons.Default.Bolt else Icons.Default.List,
                            contentDescription = "Basculer affichage",
                            tint = if (isExpressMode) KasaSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (plannedItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 10.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Quick Add Bar while walking aisles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = quickItemName,
                                onValueChange = { quickItemName = it },
                                placeholder = { Text("Ajouter un article au vol...", fontSize = 13.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = KasaSecondary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (quickItemName.isNotBlank()) {
                                        KasaRepository.addProvisionItem(
                                            name = quickItemName.trim(),
                                            quantity = 1,
                                            unit = "pièce",
                                            plannedPrice = 0.0,
                                            priority = "normal"
                                        )
                                        quickItemName = ""
                                    }
                                },
                                enabled = quickItemName.isNotBlank(),
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(if (quickItemName.isNotBlank()) KasaSecondary else MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Ajouter",
                                    tint = if (quickItemName.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Checkout Action Button
                        val hasCartItems = inCartCount > 0
                        Button(
                            onClick = { showCheckoutDialog = true },
                            enabled = hasCartItems,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(if (hasCartItems) 4.dp else 0.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KasaPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.ShoppingCartCheckout,
                                        contentDescription = null,
                                        tint = if (hasCartItems) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (hasCartItems) "Passer en caisse ($inCartCount)" else "Caddie vide (touchez un article)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (hasCartItems) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (hasCartItems) {
                                    Text(
                                        text = CurrencyFormatter.format(cartTotalPrice, currency),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (plannedItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(KasaSuccess.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎉", fontSize = 38.sp)
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Tout est acheté !",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Le foyer est bien approvisionné. Ajoutez de nouveaux articles si besoin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))

                // Cart Live Meter Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Caddie estimé",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.format(cartTotalPrice, currency),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KasaPrimaryDark
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (inCartCount == totalPlannedCount) KasaSuccess.copy(alpha = 0.15f) else KasaSecondary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$inCartCount / $totalPlannedCount article(s)",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (inCartCount == totalPlannedCount) KasaSuccess else KasaSecondaryDark,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (inCartCount == totalPlannedCount) KasaSuccess else KasaPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Priority Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Tous", "Urgent", "Normal", "Faible").forEach { filter ->
                        val isSelected = selectedPriorityFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPriorityFilter = filter },
                            label = {
                                Text(
                                    text = when (filter) {
                                        "Urgent" -> "🔥 Urgent"
                                        "Normal" -> "⚡ Normal"
                                        "Faible" -> "🕒 Faible"
                                        else -> "Tous"
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Items list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filteredPlannedItems, key = { it.id }) { item ->
                        val isInCart = cartItems.containsKey(item.id)
                        val itemPrice = cartItems[item.id] ?: item.plannedPrice

                        val itemBgColor = if (isInCart) {
                            KasaPrimary.copy(alpha = 0.08f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }

                        val itemBorderColor = if (isInCart) {
                            KasaPrimary.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, itemBorderColor, RoundedCornerShape(16.dp))
                                .clickable {
                                    if (isInCart) {
                                        cartItems.remove(item.id)
                                    } else {
                                        cartItems[item.id] = item.plannedPrice
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = itemBgColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Large Tactile Checkbox
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isInCart) KasaPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                        .border(
                                            width = if (isInCart) 0.dp else 1.5.dp,
                                            color = if (isInCart) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isInCart) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Dans le caddie",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                // Item Details
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isInCart) FontWeight.Normal else FontWeight.Bold,
                                            textDecoration = if (isInCart) TextDecoration.LineThrough else TextDecoration.None,
                                            color = if (isInCart) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f) else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 16.sp
                                        )

                                        if (item.priority == "high" && !isInCart) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = KasaError.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "🔥 URGENT",
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = KasaError,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "${item.quantity} ${item.unit} • ${CurrencyFormatter.format(itemPrice, currency)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isInCart) KasaPrimaryDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isInCart) FontWeight.SemiBold else FontWeight.Normal
                                    )

                                    val authorText = buildString {
                                        if (item.addedBy.isNotBlank()) {
                                            append("Ajouté par ${item.addedBy}")
                                        }
                                        if (!item.lastModifiedBy.isNullOrBlank()) {
                                            if (isNotEmpty()) append(" • ")
                                            append("✏️ Modifié par ${item.lastModifiedBy}")
                                        }
                                    }
                                    if (authorText.isNotBlank()) {
                                        Text(
                                            text = authorText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (item.comment.isNotBlank()) {
                                        Text(
                                            text = "💬 ${item.comment}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = KasaPrimary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                // Quick Edit Price Button
                                IconButton(
                                    onClick = {
                                        itemToEditPrice = item
                                        singleItemPriceText = itemPrice.toString()
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Ajuster le prix",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Edit single item price in cart
    itemToEditPrice?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToEditPrice = null },
            title = { Text("Ajuster le prix : ${item.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Indiquez le prix exact constaté en rayon :", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = singleItemPriceText,
                        onValueChange = { singleItemPriceText = it },
                        label = { Text("Prix en rayon ($currency)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newPrice = singleItemPriceText.toDoubleOrNull() ?: item.plannedPrice
                        cartItems[item.id] = newPrice
                        itemToEditPrice = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KasaPrimary)
                ) {
                    Text("Appliquer", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToEditPrice = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Modal: Checkout & Finish Shopping Trip
    if (showCheckoutDialog) {
        val selectedPairs = plannedItems.filter { cartItems.containsKey(it.id) }.map { item ->
            Pair(item, cartItems[item.id] ?: item.plannedPrice)
        }
        var totalPaidText by remember { mutableStateOf(cartTotalPrice.toString()) }
        var checkoutComment by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏁", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Validation du passage en caisse", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Vous avez coché ${selectedPairs.size} article(s) dans votre caddie :",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            selectedPairs.forEach { (item, price) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("• ${item.name} (${item.quantity} ${item.unit})", fontSize = 13.sp)
                                    Text(CurrencyFormatter.format(price, currency), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = totalPaidText,
                        onValueChange = { totalPaidText = it },
                        label = { Text("Montant total du ticket de caisse ($currency)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = checkoutComment,
                        onValueChange = { checkoutComment = it },
                        label = { Text("Commentaire / Remarque (optionnel)") },
                        placeholder = { Text("Ex: Facture #123, courses hebdo...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "✨ Une dépense globale sera automatiquement enregistrée dans le budget et notifiée dans le Salon !",
                        style = MaterialTheme.typography.bodySmall,
                        color = KasaPrimaryDark,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val totalPaid = totalPaidText.toDoubleOrNull() ?: cartTotalPrice
                        KasaRepository.batchConfirmPurchases(selectedPairs, checkoutComment.trim())
                        cartItems.clear()
                        showCheckoutDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KasaPrimary)
                ) {
                    Text("Confirmer & Clôturer", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCheckoutDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
