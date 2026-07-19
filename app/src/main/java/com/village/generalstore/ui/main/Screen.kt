package com.village.generalstore.ui.main

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object StoreList : Screen("store_list")
    object Catalog : Screen("catalog/{storeId}") {
        fun createRoute(storeId: String) = "catalog/$storeId"
    }
    object Cart : Screen("cart/{storeId}") {
        fun createRoute(storeId: String) = "cart/$storeId"
    }
    object SellerDashboard : Screen("seller_dashboard/{storeId}") {
        fun createRoute(storeId: String) = "seller_dashboard/$storeId"
    }
    object SellerInventory : Screen("seller_inventory/{storeId}") {
        fun createRoute(storeId: String) = "seller_inventory/$storeId"
    }
}
