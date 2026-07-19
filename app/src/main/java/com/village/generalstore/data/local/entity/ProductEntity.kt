package com.village.generalstore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.village.generalstore.domain.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val name: String,
    val category: String,
    val mrp: Double,
    val discountPrice: Double,
    val stock: Double,
    val unit: String,
    val lowStockLimit: Double,
    val imageUrl: String,
    val barcode: String? = null,
    val isSynced: Boolean = true
) {
    fun toDomain(): Product = Product(
        id = id,
        storeId = storeId,
        name = name,
        category = category,
        mrp = mrp,
        discountPrice = discountPrice,
        stock = stock,
        unit = unit,
        lowStockLimit = lowStockLimit,
        imageUrl = imageUrl,
        barcode = barcode
    )
}

fun Product.toEntity(isSynced: Boolean = true): ProductEntity = ProductEntity(
    id = id.ifEmpty { java.util.UUID.randomUUID().toString() },
    storeId = storeId,
    name = name,
    category = category,
    mrp = mrp,
    discountPrice = discountPrice,
    stock = stock,
    unit = unit,
    lowStockLimit = lowStockLimit,
    imageUrl = imageUrl,
    barcode = barcode,
    isSynced = isSynced
)
