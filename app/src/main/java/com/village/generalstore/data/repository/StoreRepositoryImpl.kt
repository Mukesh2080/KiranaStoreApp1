package com.village.generalstore.data.repository

import com.village.generalstore.data.local.dao.CartDao
import com.village.generalstore.data.local.dao.ProductDao
import com.village.generalstore.data.local.entity.CartItemEntity
import com.village.generalstore.data.local.entity.toEntity
import com.village.generalstore.data.remote.FirebaseService
import com.village.generalstore.domain.model.CartItem
import com.village.generalstore.domain.model.Order
import com.village.generalstore.domain.model.OrderItem
import com.village.generalstore.domain.model.OrderStatus
import com.village.generalstore.domain.model.Product
import com.village.generalstore.domain.repository.StoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val cartDao: CartDao,
    private val firebaseService: FirebaseService
) : StoreRepository {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        // Start synchronized listener for products from Firebase Firestore to local Room cache
        repositoryScope.launch {
            try {
                firebaseService.getProductsFlow().collect { remoteProducts ->
                    val entities = remoteProducts.map { it.toEntity(isSynced = true) }
                    productDao.insertProducts(entities)
                }
            } catch (e: Exception) {
                // Handle offline or database errors gracefully (Room remains as local data)
                e.printStackTrace()
            }
        }
    }

    // Products
    override fun getProducts(): Flow<List<Product>> {
        return productDao.getProducts().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getProductById(id: String): Flow<Product?> {
        return productDao.getProductById(id).map { it?.toDomain() }.flowOn(Dispatchers.IO)
    }

    override suspend fun upsertProduct(product: Product) {
        val productEntity = product.toEntity(isSynced = false)
        // 1. Write locally
        productDao.insertOrUpdateProduct(productEntity)

        // 2. Try remote sync
        try {
            val domainProduct = product.copy(id = productEntity.id)
            firebaseService.upsertProduct(domainProduct)
            // 3. Mark synced
            productDao.insertOrUpdateProduct(productEntity.copy(id = productEntity.id, isSynced = true))
        } catch (e: Exception) {
            e.printStackTrace()
            // Kept as unsynced in local DB, will require sync service when network resumes
        }
    }

    override suspend fun deleteProduct(productId: String) {
        // 1. Delete locally
        productDao.deleteProduct(productId)
        // 2. Delete remotely
        try {
            firebaseService.deleteProduct(productId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun updateStock(productId: String, newStock: Double) {
        // 1. Update locally
        productDao.updateStock(productId, newStock)
        // 2. Update remotely
        try {
            firebaseService.updateStock(productId, newStock)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Cart
    override fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getCartItems().map { entities ->
            entities.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun addToCart(product: Product, quantity: Double) {
        val existing = cartDao.getCartItemsOneShot().find { it.productId == product.id }
        val newQuantity = (existing?.quantity ?: 0.0) + quantity
        
        if (newQuantity <= 0) {
            cartDao.deleteCartItem(product.id)
        } else {
            val cartItemEntity = CartItemEntity(
                productId = product.id,
                name = product.name,
                price = product.discountPrice,
                quantity = newQuantity,
                unit = product.unit,
                stockAvailable = product.stock
            )
            cartDao.insertOrUpdateCartItem(cartItemEntity)
        }
    }

    override suspend fun updateCartQuantity(productId: String, quantity: Double) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(productId)
        } else {
            cartDao.updateQuantity(productId, quantity)
        }
    }

    override suspend fun removeFromCart(productId: String) {
        cartDao.deleteCartItem(productId)
    }

    override suspend fun clearCart() {
        cartDao.clearCart()
    }

    // Orders
    override fun getOrders(): Flow<List<Order>> {
        return firebaseService.getOrdersFlow().flowOn(Dispatchers.IO)
    }

    override suspend fun placeOrder(customerName: String, customerPhone: String, isDelivery: Boolean): String {
        val cartItems = cartDao.getCartItemsOneShot()
        if (cartItems.isEmpty()) return ""

        val totalAmount = cartItems.sumOf { it.price * it.quantity }
        val orderItems = cartItems.map {
            OrderItem(
                productId = it.productId,
                name = it.name,
                price = it.price,
                quantity = it.quantity,
                unit = it.unit
            )
        }

        val order = Order(
            id = UUID.randomUUID().toString(),
            customerName = customerName,
            customerPhone = customerPhone,
            items = orderItems,
            totalAmount = totalAmount,
            status = OrderStatus.PENDING,
            isDelivery = isDelivery,
            createdAt = System.currentTimeMillis()
        )

        // 1. Submit order to Firebase
        val orderId = firebaseService.placeOrder(order)

        // 2. Update stock of products locally and remotely
        for (item in cartItems) {
            val productEntity = productDao.getProductByIdOneShot(item.productId)
            if (productEntity != null) {
                val updatedStock = maxOf(0.0, productEntity.stock - item.quantity)
                updateStock(item.productId, updatedStock)
            }
        }

        // 3. Clear cart
        cartDao.clearCart()

        return orderId
    }

    override suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        firebaseService.updateOrderStatus(orderId, status)
    }
}
