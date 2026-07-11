package com.connect.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.connect.ai.SmartCoachAI
import com.connect.bluetooth.ui.DeviceScannerScreen
import com.connect.ui.DashboardScreen
import com.connect.ui.theme.ConnectPremiumTheme
import com.connect.watch.WatchfaceStoreScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var smartCoachAI: SmartCoachAI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConnectPremiumTheme {
                ConnectPremiumApp(smartCoachAI)
            }
        }
    }
}

@Composable
fun ConnectPremiumApp(aiCoach: SmartCoachAI) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    icon = { Text("🏠") },
                    label = { Text("Accueil") },
                    selected = currentRoute == "dashboard",
                    onClick = { navController.navigate("dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF64FFDA),
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color(0xFF0F172A)
                    )
                )
                NavigationBarItem(
                    icon = { Text("⌚") },
                    label = { Text("Boutique") },
                    selected = currentRoute == "store",
                    onClick = { navController.navigate("store") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF64FFDA),
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color(0xFF0F172A)
                    )
                )
                NavigationBarItem(
                    icon = { Text("📡") },
                    label = { Text("Radar") },
                    selected = currentRoute == "scanner",
                    onClick = { navController.navigate("scanner") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF64FFDA),
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color(0xFF0F172A)
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
                DashboardScreen(aiCoach = aiCoach)
            }
            composable("store") {
                WatchfaceStoreScreen()
            }
            composable("scanner") {
                DeviceScannerScreen()
            }
        }
    }
}
