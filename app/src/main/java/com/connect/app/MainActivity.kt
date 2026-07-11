package com.connect.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.connect.ai.SmartCoachAI
import com.connect.ui.DashboardScreen
import com.connect.ui.theme.ConnectPremiumTheme

class MainActivity : ComponentActivity() {
    // Fake API key for demonstration purpose. In a real app, use BuildConfig or DI.
    private val smartCoachAI = SmartCoachAI("fake_gemini_api_key_123")

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

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(aiCoach = aiCoach)
            }
            // Other premium screens (WatchfaceStore, Settings) would be added here
        }
    }
}
