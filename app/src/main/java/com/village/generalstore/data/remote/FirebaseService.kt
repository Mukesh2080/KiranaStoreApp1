package com.village.generalstore.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.snapshots
import com.village.generalstore.domain.model.Order
import com.village.generalstore.domain.model.OrderItem
import com.village.generalstore.domain.model.OrderStatus
import com.village.generalstore.domain.model.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val productsCollection = firestore.collection("products")
    private val ordersCollection = firestore.collection("orders")

    fun getProductsFlow(): Flow<List<Product>> = callbackFlow {
        val listener: ListenerRegistration = productsCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val products = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val name = doc.getString("name") ?: ""
                    val category = doc.getString("category") ?: ""
                    val mrp = doc.getDouble("mrp") ?: 0.0
                    val discountPrice = doc.getDouble("discountPrice") ?: 0.0
                    val stock = doc.getDouble("stock") ?: 0.0
                    val unit = doc.getString("unit") ?: "pcs"
                    val lowStockLimit = doc.getDouble("lowStockLimit") ?: 5.0
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    Product(id, name, category, mrp, discountPrice, stock, unit, lowStockLimit, imageUrl)
                }
                trySend(products)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun upsertProduct(product: Product) {
        val data = hashMapOf(
            "name" to product.name,
            "category" to product.category,
            "mrp" to product.mrp,
            "discountPrice" to product.discountPrice,
            "stock" to product.stock,
            "unit" to product.unit,
            "lowStockLimit" to product.lowStockLimit,
            "imageUrl" to product.imageUrl
        )
        productsCollection.document(product.id).set(data).await()
    }

    suspend fun deleteProduct(productId: String) {
        productsCollection.document(productId).delete().await()
    }

    suspend fun updateStock(productId: String, newStock: Double) {
        productsCollection.document(productId).update("stock", newStock).await()
    }

    fun getOrdersFlow(): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val orders = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val customerName = doc.getString("customerName") ?: ""
                    val customerPhone = doc.getString("customerPhone") ?: ""
                    val totalAmount = doc.getDouble("totalAmount") ?: 0.0
                    val statusStr = doc.getString("status") ?: "PENDING"
                    val status = try { OrderStatus.valueOf(statusStr) } catch (e: Exception) { OrderStatus.PENDING }
                    val isDelivery = doc.getBoolean("isDelivery") ?: false
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                    val itemsRaw = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                    val items = itemsRaw.map { map ->
                        OrderItem(
                            productId = map["productId"] as? String ?: "",
                            name = map["name"] as? String ?: "",
                            price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                            quantity = (map["quantity"] as? Number)?.toDouble() ?: 0.0,
                            unit = map["unit"] as? String ?: ""
                        )
                    }
                    Order(id, customerName, customerPhone, items, totalAmount, status, isDelivery, createdAt)
                }
                trySend(orders.sortedByDescending { it.createdAt })
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun placeOrder(order: Order): String {
        val orderDocRef = ordersCollection.document()
        val itemsList = order.items.map { item ->
            hashMapOf(
                "productId" to item.productId,
                "name" to item.name,
                "price" to item.price,
                "quantity" to item.quantity,
                "unit" to item.unit
            )
        }
        val data = hashMapOf(
            "customerName" to order.customerName,
            "customerPhone" to order.customerPhone,
            "totalAmount" to order.totalAmount,
            "status" to order.status.name,
            "isDelivery" to order.isDelivery,
            "createdAt" to order.createdAt,
            "items" to itemsList
        )
        orderDocRef.set(data).await()
        return orderDocRef.id
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        ordersCollection.document(orderId).update("status", status.name).await()
    }
}
