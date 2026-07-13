package com.connect.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.connect.bluetooth.ui.DeviceScannerScreen
import com.connect.settings.SettingsScreen
import com.connect.settings.SettingsDataStore
import com.connect.ui.DashboardScreen
import com.connect.ui.CoachChatScreen
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
                containerColor = Color(0xFF1C1C1E).copy(alpha = 0.95f),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    icon = { Text("􀎟", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp)) },
                    label = { Text("Résumé", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp)) },
                    selected = currentRoute == "dashboard",
                    onClick = { navController.navigate("dashboard") { launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007AFF),
                        selectedTextColor = Color(0xFF007AFF),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    icon = { Text("􀺽", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp)) },
                    label = { Text("Cadrans", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp)) },
                    selected = currentRoute == "store",
                    onClick = { navController.navigate("store") { launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007AFF),
                        selectedTextColor = Color(0xFF007AFF),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    icon = { Text("􀌨", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp)) },
                    label = { Text("Coach", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp)) },
                    selected = currentRoute == "coach_chat",
                    onClick = { navController.navigate("coach_chat") { launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007AFF),
                        selectedTextColor = Color(0xFF007AFF),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    icon = { Text("􀤆", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp)) },
                    label = { Text("Jumelage", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp)) },
                    selected = currentRoute == "scanner",
                    onClick = { navController.navigate("scanner") { launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007AFF),
                        selectedTextColor = Color(0xFF007AFF),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    icon = { Text("􀍟", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp)) },
                    label = { Text("Réglages", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp)) },
                    selected = currentRoute == "settings",
                    onClick = { navController.navigate("settings") { launchSingleTop = true } },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF007AFF),
                        selectedTextColor = Color(0xFF007AFF),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            NavHost(
                navController = navController,
                startDestination = "dashboard",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("dashboard") { DashboardScreen() }
                composable("store") { WatchfaceStoreScreen() }
                composable("coach_chat") { CoachChatScreen() }
                composable("scanner") { DeviceScannerScreen() }
                composable("settings") { SettingsScreen() }
            }
        }
    }
}
