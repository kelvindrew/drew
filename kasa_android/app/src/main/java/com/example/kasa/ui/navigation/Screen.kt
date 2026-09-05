package com.example.kasa.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Name : Screen("name")
    data object CreateHousehold : Screen("create_household/{userName}") {
        fun createRoute(userName: String) = "create_household/$userName"
    }
    data object JoinHousehold : Screen("join_household")

    // Home tabs
    data object Home : Screen("home")
    data object Budget : Screen("budget")
    data object Provision : Screen("provision")
    data object ShoppingList : Screen("shopping_list")
    data object Expenses : Screen("expenses")
    data object AddExpense : Screen("add_expense")
    data object Chat : Screen("chat")
    data object Planning : Screen("planning")
    data object Statistics : Screen("statistics")
    data object Members : Screen("members")
    data object Settings : Screen("settings")
}

enum class BottomBarTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    BUDGET("budget", "Budget", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    ACTIVITIES("activities", "Dépenses", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong),
    CHAT("chat", "Salon", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    STATS("statistics", "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart)
}
