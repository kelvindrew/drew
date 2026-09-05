package com.example.kasa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kasa.data.repository.KasaRepository
import com.example.kasa.ui.screens.auth.CreateHouseholdScreen
import com.example.kasa.ui.screens.auth.JoinHouseholdScreen
import com.example.kasa.ui.screens.auth.NameScreen
import com.example.kasa.ui.screens.expenses.AddExpenseScreen
import com.example.kasa.ui.screens.home.HomeScreen
import com.example.kasa.ui.screens.members.MembersScreen
import com.example.kasa.ui.screens.provision.ShoppingListScreen
import com.example.kasa.ui.screens.settings.SettingsScreen
import com.example.kasa.ui.screens.welcome.WelcomeScreen

@Composable
fun KasaNavGraph() {
    val navController = rememberNavController()
    val startDest = if (KasaRepository.hasConfiguredHousehold()) Screen.Home.route else Screen.Welcome.route

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStarted = { navController.navigate(Screen.Name.route) },
                onJoinHousehold = { navController.navigate(Screen.JoinHousehold.route) }
            )
        }

        composable(Screen.Name.route) {
            NameScreen(
                onContinue = { name ->
                    navController.navigate(Screen.CreateHousehold.createRoute(name))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CreateHousehold.route,
            arguments = listOf(navArgument("userName") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: "Utilisateur"
            CreateHouseholdScreen(
                userName = userName,
                onHouseholdCreated = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.JoinHousehold.route) {
            JoinHouseholdScreen(
                onHouseholdJoined = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(rootNavController = navController)
        }

        composable(Screen.AddExpense.route) {
            AddExpenseScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ShoppingList.route) {
            ShoppingListScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Members.route) {
            MembersScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onResetHousehold = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
