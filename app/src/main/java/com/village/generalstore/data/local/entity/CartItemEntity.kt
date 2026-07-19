package com.village.generalstore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.village.generalstore.domain.model.CartItem

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val name: String,
    val price: Double,
    val quantity: Double,
    val unit: String,
    val stockAvailable: Double
) {
    fun toDomain(): CartItem = CartItem(
        productId = productId,
        name = name,
        price = price,
        quantity = quantity,
        unit = unit,
        stockAvailable = stockAvailable
    )
}
