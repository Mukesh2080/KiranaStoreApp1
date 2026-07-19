package com.village.generalstore.domain.model

data class CartItem(
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Double,
    val unit: String,
    val stockAvailable: Double
) {
    val totalAmount: Double
        get() = price * quantity
}
