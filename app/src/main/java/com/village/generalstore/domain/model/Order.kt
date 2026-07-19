package com.village.generalstore.domain.model

data class Order(
    val id: String = "",
    val storeId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val isDelivery: Boolean = false,
    val deliveryAddress: String? = null,
    val deliveryCharge: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Double = 0.0,
    val unit: String = ""
)

enum class OrderStatus {
    PENDING,
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED
}
