package com.mulykap.clone.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mulykap.clone.ui.screens.MainScreen
import com.mulykap.clone.ui.screens.BookingScreen
import com.mulykap.clone.ui.screens.PackageScreen

@Composable
fun MulykapNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "main"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("main") {
            MainScreen(
                onNavigateToBooking = { navController.navigate("booking") },
                onNavigateToPackages = { navController.navigate("packages") }
            )
        }
        composable("booking") {
            BookingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("packages") {
            PackageScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
