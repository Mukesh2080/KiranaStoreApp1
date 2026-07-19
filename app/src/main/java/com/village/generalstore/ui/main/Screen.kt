package com.village.generalstore.ui.main

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Catalog : Screen("catalog")
    object Cart : Screen("cart")
    object SellerDashboard : Screen("seller_dashboard")
    object SellerInventory : Screen("seller_inventory")
}
