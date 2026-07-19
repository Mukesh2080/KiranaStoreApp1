package com.village.generalstore.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.village.generalstore.data.local.dao.CartDao
import com.village.generalstore.data.local.dao.ProductDao
import com.village.generalstore.data.local.entity.CartItemEntity
import com.village.generalstore.data.local.entity.ProductEntity

@Database(entities = [ProductEntity::class, CartItemEntity::class], version = 3, exportSchema = false)
abstract class StoreDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
}
