package com.example.kasa.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.service.AlertCategory
import com.example.kasa.service.KasaNotificationManager
import com.example.kasa.theme.KasaPrimary
import com.example.kasa.theme.KasaSecondary
import com.example.kasa.theme.KasaTheme
import com.example.kasa.ui.components.ThemedActionIconButton
import com.example.kasa.ui.components.ThemedNavIcon
import com.example.kasa.ui.navigation.BottomBarTab
import com.example.kasa.ui.navigation.Screen
import com.example.kasa.ui.screens.budget.BudgetScreen
import com.example.kasa.ui.screens.chat.ChatScreen
import com.example.kasa.ui.screens.operations.OperationsScreen
import com.example.kasa.ui.screens.statistics.StatisticsScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    rootNavController: NavHostController
) {
    var selectedTab by rememberSaveable { mutableStateOf(BottomBarTab.BUDGET) }
    var operationsInitialSubTab by remember { mutableIntStateOf(0) } // 0 = Dépenses, 1 = Provisions

    val household by KasaRepository.currentHousehold.collectAsState()
    val currentUser by KasaRepository.currentUser.collectAsState()
    val postIts by KasaRepository.postIts.collectAsState()
    val houseEssentials by KasaRepository.houseEssentials.collectAsState()
    val currentAlert by KasaNotificationManager.currentAlert.collectAsState()
    val unreadMessages by KasaNotificationManager.unreadMessagesCount.collectAsState()
    val unreadModifications by KasaNotificationManager.unreadModificationsCount.collectAsState()

    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showCorkboardDialog by remember { mutableStateOf(false) }
    var showWifiQrDialog by remember { mutableStateOf(false) }

    val isImeVisible = WindowInsets.isImeVisible

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("🏡 ${household?.name ?: "Mon Foyer"}", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KasaTheme.colors.surface,
                    titleContentColor = KasaTheme.colors.onSurface
                ),
                actions = {
                    // SOS / Emergency Alert
                    ThemedActionIconButton(onClick = { showEmergencyDialog = true }) {
                        Text("🚨", fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(4.dp))

                    // Tableau de Liège Virtuel
                    Box {
                        ThemedActionIconButton(onClick = { showCorkboardDialog = true }) {
                            Text("📌", fontSize = 16.sp)
                        }
                        if (postIts.isNotEmpty()) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp)
                            ) {
                                Text("${postIts.size}", fontSize = 9.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))

                    ThemedActionIconButton(onClick = { rootNavController.navigate(Screen.Members.route) }) {
                        Icon(Icons.Default.Group, contentDescription = "Membres", modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    ThemedActionIconButton(onClick = { rootNavController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Paramètres", modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        bottomBar = {
            if (!isImeVisible || selectedTab != BottomBarTab.CHAT) {
                Surface(
                    shape = KasaTheme.shapes.bottomNavShape,
                    color = KasaTheme.colors.surface,
                    shadowElevation = KasaTheme.shadows.floatingElevation,
                    border = BorderStroke(0.5.dp, KasaTheme.colors.border)
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        contentColor = KasaTheme.colors.onSurface
                    ) {
                        BottomBarTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (tab == BottomBarTab.ACTIVITIES) {
                                        operationsInitialSubTab = 0
                                        KasaNotificationManager.markModificationsAsRead()
                                    } else if (tab == BottomBarTab.CHAT) {
                                        KasaNotificationManager.markMessagesAsRead()
                                    }
                                    selectedTab = tab
                                },
                                icon = {
                                    val badgeComposable: (@Composable BoxScope.() -> Unit)? = when (tab) {
                                        BottomBarTab.CHAT -> if (unreadMessages > 0) {
                                            {
                                                Badge(
                                                    containerColor = MaterialTheme.colorScheme.error,
                                                    contentColor = Color.White
                                                ) {
                                                    Text("$unreadMessages")
                                                }
                                            }
                                        } else null

                                        BottomBarTab.ACTIVITIES -> if (unreadModifications > 0) {
                                            {
                                                Badge(containerColor = KasaSecondary)
                                            }
                                        } else null

                                        else -> null
                                    }

                                    ThemedNavIcon(
                                        isSelected = isSelected,
                                        badge = badgeComposable
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                label = { Text(tab.title) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Transparent,
                                    selectedTextColor = KasaTheme.colors.primary,
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor = KasaTheme.colors.onSurfaceVariant,
                                    unselectedTextColor = KasaTheme.colors.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                BottomBarTab.BUDGET -> {
                    BudgetScreen(
                        onNavigateToProvision = {
                            operationsInitialSubTab = 1
                            selectedTab = BottomBarTab.ACTIVITIES
                        }
                    )
                }
                BottomBarTab.ACTIVITIES -> {
                    OperationsScreen(
                        initialSubTab = operationsInitialSubTab,
                        onNavigateToAddExpense = {
                            rootNavController.navigate(Screen.AddExpense.route)
                        },
                        onNavigateToShoppingList = {
                            rootNavController.navigate(Screen.ShoppingList.route)
                        }
                    )
                }
                BottomBarTab.CHAT -> {
                    ChatScreen()
                }
                BottomBarTab.STATS -> {
                    StatisticsScreen()
                }
            }

            // Top Floating In-App Alert Banner
            AnimatedVisibility(
                visible = currentAlert != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                currentAlert?.let { alert ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                when (alert.category) {
                                    AlertCategory.MESSAGE -> {
                                        selectedTab = BottomBarTab.CHAT
                                        KasaNotificationManager.markMessagesAsRead()
                                    }
                                    AlertCategory.MODIFICATION -> {
                                        selectedTab = BottomBarTab.ACTIVITIES
                                        KasaNotificationManager.markModificationsAsRead()
                                    }
                                    AlertCategory.BUDGET_EXCEEDED -> {
                                        selectedTab = BottomBarTab.BUDGET
                                    }
                                    else -> {}
                                }
                                KasaNotificationManager.dismissCurrentAlert()
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = BorderStroke(1.dp, KasaPrimary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = alert.emoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alert.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = alert.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { KasaNotificationManager.dismissCurrentAlert() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Fermer",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEmergencyDialog) {
        com.example.kasa.ui.components.EmergencyAlertDialog(
            currentUser = currentUser,
            onDismiss = { showEmergencyDialog = false }
        )
    }

    if (showCorkboardDialog) {
        com.example.kasa.ui.components.CorkboardDialog(
            postIts = postIts,
            houseEssentials = houseEssentials,
            currentUser = currentUser,
            isAdmin = currentUser?.isAdmin == true,
            onDismiss = { showCorkboardDialog = false },
            onOpenWifiQr = {
                showCorkboardDialog = false
                showWifiQrDialog = true
            }
        )
    }

    if (showWifiQrDialog) {
        com.example.kasa.ui.components.WifiQrDialog(
            wifiSsid = houseEssentials.wifiSsid,
            wifiPassword = houseEssentials.wifiPassword,
            onDismiss = { showWifiQrDialog = false }
        )
    }
}