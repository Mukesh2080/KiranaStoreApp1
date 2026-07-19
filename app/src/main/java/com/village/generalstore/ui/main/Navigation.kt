package com.village.generalstore.ui.main

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.village.generalstore.ui.customer.CartScreen
import com.village.generalstore.ui.customer.CatalogScreen
import com.village.generalstore.ui.customer.CustomerViewModel
import com.village.generalstore.ui.seller.DashboardScreen
import com.village.generalstore.ui.seller.InventoryScreen
import com.village.generalstore.ui.seller.SellerViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToCustomer = { navController.navigate(Screen.Catalog.route) },
                onNavigateToSeller = { navController.navigate(Screen.SellerDashboard.route) }
            )
        }

        composable(Screen.Catalog.route) {
            val customerViewModel: CustomerViewModel = hiltViewModel()
            CatalogScreen(
                viewModel = customerViewModel,
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Cart.route) {
            val customerViewModel: CustomerViewModel = hiltViewModel()
            CartScreen(
                viewModel = customerViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOrderSuccessFinished = {
                    navController.popBackStack(Screen.Catalog.route, inclusive = false)
                }
            )
        }

        composable(Screen.SellerDashboard.route) {
            val sellerViewModel: SellerViewModel = hiltViewModel()
            DashboardScreen(
                viewModel = sellerViewModel,
                onNavigateToInventory = { navController.navigate(Screen.SellerInventory.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SellerInventory.route) {
            val sellerViewModel: SellerViewModel = hiltViewModel()
            InventoryScreen(
                viewModel = sellerViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
