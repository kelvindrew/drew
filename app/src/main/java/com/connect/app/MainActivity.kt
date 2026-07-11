package com.connect.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.connect.bluetooth.ui.DeviceScannerScreen
import com.connect.settings.SettingsScreen
import com.connect.settings.SettingsDataStore
import com.connect.ui.DashboardScreen
import com.connect.ui.theme.ConnectPremiumTheme
import com.connect.watch.WatchfaceStoreScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isDarkTheme by settingsDataStore.isDarkThemeFlow.collectAsState(initial = true)

            ConnectPremiumTheme(darkTheme = isDarkTheme) {
                ConnectPremiumApp()
            }
        }
    }
}

@Composable
fun ConnectPremiumApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    icon = { Text("🏠") },
                    label = { Text("Accueil") },
                    selected = currentRoute == "dashboard",
                    onClick = { navController.navigate("dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.background
                    )
                )
                NavigationBarItem(
                    icon = { Text("⌚") },
                    label = { Text("Boutique") },
                    selected = currentRoute == "store",
                    onClick = { navController.navigate("store") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.background
                    )
                )
                NavigationBarItem(
                    icon = { Text("📡") },
                    label = { Text("Radar") },
                    selected = currentRoute == "scanner",
                    onClick = { navController.navigate("scanner") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.background
                    )
                )
                NavigationBarItem(
                    icon = { Text("⚙️") },
                    label = { Text("Réglages") },
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen()
            }
            composable("store") {
                WatchfaceStoreScreen()
            }
            composable("scanner") {
                DeviceScannerScreen()
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}
