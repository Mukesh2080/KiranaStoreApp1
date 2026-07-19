package com.village.generalstore.di

import android.content.Context
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.village.generalstore.data.local.dao.CartDao
import com.village.generalstore.data.local.dao.ProductDao
import com.village.generalstore.data.local.db.StoreDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideStoreDatabase(
        @ApplicationContext context: Context
    ): StoreDatabase {
        return Room.databaseBuilder(
            context,
            StoreDatabase::class.java,
            "store_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideProductDao(db: StoreDatabase): ProductDao = db.productDao()

    @Provides
    fun provideCartDao(db: StoreDatabase): CartDao = db.cartDao()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): android.content.SharedPreferences {
        return context.getSharedPreferences("kirana_store_prefs", Context.MODE_PRIVATE)
    }
}
