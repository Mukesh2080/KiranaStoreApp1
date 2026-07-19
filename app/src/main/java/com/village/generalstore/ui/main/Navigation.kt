package com.village.generalstore.ui.main

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.village.generalstore.ui.customer.CartScreen
import com.village.generalstore.ui.customer.CatalogScreen
import com.village.generalstore.ui.customer.CustomerViewModel
import com.village.generalstore.ui.customer.OrderTrackingScreen
import com.village.generalstore.ui.customer.StoreListScreen
import com.village.generalstore.ui.seller.DashboardScreen
import com.village.generalstore.ui.seller.InventoryScreen
import com.village.generalstore.ui.seller.SellerViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Define a single CustomerViewModel to be shared across all customer screens
    val customerViewModel: CustomerViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToCustomer = { navController.navigate(Screen.StoreList.route) },
                onNavigateToSeller = { storeId -> 
                    navController.navigate(Screen.SellerDashboard.createRoute(storeId)) 
                }
            )
        }

        composable(Screen.StoreList.route) {
            StoreListScreen(
                viewModel = customerViewModel,
                onStoreSelected = { storeId ->
                    customerViewModel.selectStore(storeId)
                    navController.navigate(Screen.Catalog.createRoute(storeId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.OrderTracking.route) {
            OrderTrackingScreen(
                viewModel = customerViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Catalog.route,
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            customerViewModel.selectStore(storeId)
            
            CatalogScreen(
                viewModel = customerViewModel,
                onNavigateToCart = { navController.navigate(Screen.Cart.createRoute(storeId)) },
                onNavigateToTracking = { navController.navigate(Screen.OrderTracking.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Cart.route,
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            customerViewModel.selectStore(storeId)

            CartScreen(
                viewModel = customerViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOrderSuccessFinished = {
                    navController.popBackStack(Screen.Catalog.createRoute(storeId), inclusive = false)
                }
            )
        }

        composable(
            route = Screen.SellerDashboard.route,
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            val sellerViewModel: SellerViewModel = hiltViewModel()
            sellerViewModel.setStoreId(storeId)

            DashboardScreen(
                viewModel = sellerViewModel,
                onNavigateToInventory = { navController.navigate(Screen.SellerInventory.createRoute(storeId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SellerInventory.route,
            arguments = listOf(navArgument("storeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            val sellerViewModel: SellerViewModel = hiltViewModel()
            sellerViewModel.setStoreId(storeId)

            InventoryScreen(
                viewModel = sellerViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
