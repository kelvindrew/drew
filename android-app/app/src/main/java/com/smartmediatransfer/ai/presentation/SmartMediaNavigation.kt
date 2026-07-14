package com.smartmediatransfer.ai.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object Routes {
    const val DASHBOARD = "dashboard"
    const val SCANNER = "scanner"
    const val TRANSFER = "transfer"
    const val FILE_SELECTION = "file_selection"
}

@Composable
fun SmartMediaNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToScanner = { navController.navigate(Routes.SCANNER) },
                onNavigateToTransfer = { navController.navigate(Routes.TRANSFER) },
                onNavigateToFileSelection = { navController.navigate(Routes.FILE_SELECTION) }
            )
        }
        composable(Routes.SCANNER) {
            ScannerScreen(
                onCodeScanned = { scannedCode ->
                    // Here we would handle the scanned code (e.g. initiate connection)
                    // For now, navigate back to Dashboard or to Transfer
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.TRANSFER) {
            TransferScreen()
        }
        composable(Routes.FILE_SELECTION) {
            FileSelectionScreen(
                onFilesAdded = {
                    navController.popBackStack()
                }
            )
        }
    }
}
