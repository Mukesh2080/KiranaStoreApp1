package com.village.generalstore.domain.repository

import com.village.generalstore.domain.model.CartItem
import com.village.generalstore.domain.model.Order
import com.village.generalstore.domain.model.OrderStatus
import com.village.generalstore.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    // Products
    fun getProducts(): Flow<List<Product>>
    fun getProductById(id: String): Flow<Product?>
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
    fun getOrders(): Flow<List<Order>>
    suspend fun placeOrder(customerName: String, customerPhone: String, isDelivery: Boolean): String
    suspend fun updateOrderStatus(orderId: String, status: OrderStatus)
}
