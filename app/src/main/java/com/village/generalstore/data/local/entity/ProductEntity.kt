package com.village.generalstore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.village.generalstore.domain.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val mrp: Double,
    val discountPrice: Double,
    val stock: Double,
    val unit: String,
    val lowStockLimit: Double,
    val imageUrl: String,
    val isSynced: Boolean = true
) {
    fun toDomain(): Product = Product(
        id = id,
        name = name,
        category = category,
        mrp = mrp,
        discountPrice = discountPrice,
        stock = stock,
        unit = unit,
        lowStockLimit = lowStockLimit,
        imageUrl = imageUrl
    )
}

fun Product.toEntity(isSynced: Boolean = true): ProductEntity = ProductEntity(
    id = id.ifEmpty { java.util.UUID.randomUUID().toString() },
    name = name,
    category = category,
    mrp = mrp,
    discountPrice = discountPrice,
    stock = stock,
    unit = unit,
    lowStockLimit = lowStockLimit,
    imageUrl = imageUrl,
    isSynced = isSynced
)
