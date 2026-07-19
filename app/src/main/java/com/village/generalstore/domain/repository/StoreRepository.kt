package com.village.generalstore.domain.repository

import com.village.generalstore.domain.model.CartItem
import com.village.generalstore.domain.model.Order
import com.village.generalstore.domain.model.OrderStatus
import com.village.generalstore.domain.model.Product
import com.village.generalstore.domain.model.Store
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    // Stores
    fun getStores(): Flow<List<Store>>
    suspend fun getStoreById(id: String): Store?

    // Products
    fun getProducts(storeId: String? = null): Flow<List<Product>>
    fun getProductById(id: String): Flow<Product?>
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun upsertProduct(product: Product)
    suspend fun deleteProduct(productId: String)
    suspend fun updateStock(productId: String, newStock: Double)

    // Cart
    fun getCartItems(): Flow<List<CartItem>>
    suspend fun addToCart(product: Product, quantity: Double)
    suspend fun updateCartQuantity(productId: String, quantity: Double)
    suspend fun removeFromCart(productId: String)
    suspend fun clearCart()

    // Orders
    fun getOrders(storeId: String? = null): Flow<List<Order>>
    suspend fun placeOrder(storeId: String, customerName: String, customerPhone: String, isDelivery: Boolean): String
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus)
}
