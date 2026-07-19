package com.village.generalstore.domain.model

data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val mrp: Double = 0.0,
    val discountPrice: Double = 0.0,
    val stock: Double = 0.0,
    val unit: String = "pcs",
    val lowStockLimit: Double = 5.0,
    val imageUrl: String = ""
) {
    val savings: Double
        get() = if (mrp > discountPrice) mrp - discountPrice else 0.0
}
