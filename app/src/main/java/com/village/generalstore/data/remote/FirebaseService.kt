package com.village.generalstore.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.snapshots
import com.village.generalstore.domain.model.Order
import com.village.generalstore.domain.model.OrderItem
import com.village.generalstore.domain.model.OrderStatus
import com.village.generalstore.domain.model.Product
import com.village.generalstore.domain.model.Store
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
    private val storesCollection = firestore.collection("stores")
    private val productsCollection = firestore.collection("products")
    private val ordersCollection = firestore.collection("orders")
    private val customersCollection = firestore.collection("customers")

    fun getStoresFlow(): Flow<List<Store>> = callbackFlow {
        val listener = storesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val stores = snapshot.documents.mapNotNull { doc ->
                    Store(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        ownerName = doc.getString("ownerName") ?: "",
                        address = doc.getString("address") ?: "",
                        phone = doc.getString("phone") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        passcode = doc.getString("passcode") ?: ""
                    )
                }
                trySend(stores)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun registerStore(store: Store): String {
        val docRef = if (store.id.isEmpty()) storesCollection.document() else storesCollection.document(store.id)
        val data = hashMapOf(
            "name" to store.name,
            "ownerName" to store.ownerName,
            "address" to store.address,
            "phone" to store.phone,
            "imageUrl" to store.imageUrl,
            "passcode" to store.passcode.ifEmpty { (1000..9999).random().toString() },
            "latitude" to store.latitude,
            "longitude" to store.longitude
        )
        docRef.set(data).await()
        return docRef.id
    }

    suspend fun isStoreNameTaken(name: String): Boolean {
        val snapshot = storesCollection.whereEqualTo("name", name).get().await()
        return !snapshot.isEmpty
    }

    suspend fun getStoreByPhone(phone: String): Store? {
        val snapshot = storesCollection.whereEqualTo("phone", phone).limit(1).get().await()
        val doc = snapshot.documents.firstOrNull() ?: return null
        return Store(
            id = doc.id,
            name = doc.getString("name") ?: "",
            ownerName = doc.getString("ownerName") ?: "",
            address = doc.getString("address") ?: "",
            phone = doc.getString("phone") ?: "",
            imageUrl = doc.getString("imageUrl") ?: "",
            passcode = doc.getString("passcode") ?: "",
            latitude = doc.getDouble("latitude"),
            longitude = doc.getDouble("longitude")
        )
    }

    fun getProductsFlow(storeId: String? = null): Flow<List<Product>> = callbackFlow {
        val query = if (storeId != null) {
            productsCollection.whereEqualTo("storeId", storeId)
        } else {
            productsCollection
        }
        
        val listener: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val products = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val sId = doc.getString("storeId") ?: ""
                    val name = doc.getString("name") ?: ""
                    val category = doc.getString("category") ?: ""
                    val mrp = doc.getDouble("mrp") ?: 0.0
                    val discountPrice = doc.getDouble("discountPrice") ?: 0.0
                    val stock = doc.getDouble("stock") ?: 0.0
                    val unit = doc.getString("unit") ?: "pcs"
                    val lowStockLimit = doc.getDouble("lowStockLimit") ?: 5.0
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val barcode = doc.getString("barcode")
                    Product(id, sId, name, category, mrp, discountPrice, stock, unit, lowStockLimit, imageUrl, barcode)
                }
                trySend(products)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun upsertProduct(product: Product) {
        val data = hashMapOf(
            "storeId" to product.storeId,
            "name" to product.name,
            "category" to product.category,
            "mrp" to product.mrp,
            "discountPrice" to product.discountPrice,
            "stock" to product.stock,
            "unit" to product.unit,
            "lowStockLimit" to product.lowStockLimit,
            "imageUrl" to product.imageUrl,
            "barcode" to product.barcode
        )
        productsCollection.document(product.id).set(data).await()
    }

    suspend fun deleteProduct(productId: String) {
        productsCollection.document(productId).delete().await()
    }

    suspend fun updateStock(productId: String, newStock: Double) {
        productsCollection.document(productId).update("stock", newStock).await()
    }

    fun getOrdersFlow(storeId: String? = null): Flow<List<Order>> = callbackFlow {
        val query = if (storeId != null) {
            ordersCollection.whereEqualTo("storeId", storeId)
        } else {
            ordersCollection
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val orders = snapshot.documents.mapNotNull { doc ->
                    val id = doc.id
                    val sId = doc.getString("storeId") ?: ""
                    val cId = doc.getString("customerId") ?: ""
                    val customerName = doc.getString("customerName") ?: ""
                    val customerPhone = doc.getString("customerPhone") ?: ""
                    val totalAmount = doc.getDouble("totalAmount") ?: 0.0
                    val statusStr = doc.getString("status") ?: "PENDING"
                    val status = try { OrderStatus.valueOf(statusStr) } catch (e: Exception) { OrderStatus.PENDING }
                    val isDelivery = doc.getBoolean("isDelivery") ?: false
                    val deliveryAddress = doc.getString("deliveryAddress")
                    val deliveryCharge = doc.getDouble("deliveryCharge") ?: 0.0
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
                    Order(id, sId, cId, customerName, customerPhone, items, totalAmount, status, isDelivery, deliveryAddress, deliveryCharge, createdAt)
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
            "storeId" to order.storeId,
            "customerId" to order.customerId,
            "customerName" to order.customerName,
            "customerPhone" to order.customerPhone,
            "totalAmount" to order.totalAmount,
            "status" to order.status.name,
            "isDelivery" to order.isDelivery,
            "deliveryAddress" to order.deliveryAddress,
            "deliveryCharge" to order.deliveryCharge,
            "createdAt" to order.createdAt,
            "items" to itemsList
        )
        orderDocRef.set(data).await()
        return orderDocRef.id
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        ordersCollection.document(orderId).update("status", status.name).await()
    }

    suspend fun getOrCreateCustomer(name: String, phone: String): String {
        val snapshot = customersCollection.whereEqualTo("phone", phone).get().await()
        return if (snapshot.isEmpty) {
            val doc = customersCollection.document()
            doc.set(hashMapOf("name" to name, "phone" to phone)).await()
            doc.id
        } else {
            snapshot.documents.first().id
        }
    }

    fun getCustomerOrdersFlow(customerId: String): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection.whereEqualTo("customerId", customerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.documents.mapNotNull { doc ->
                        val id = doc.id
                        val sId = doc.getString("storeId") ?: ""
                        val cId = doc.getString("customerId") ?: ""
                        val customerName = doc.getString("customerName") ?: ""
                        val customerPhone = doc.getString("customerPhone") ?: ""
                        val totalAmount = doc.getDouble("totalAmount") ?: 0.0
                        val statusStr = doc.getString("status") ?: "PENDING"
                        val status = try { OrderStatus.valueOf(statusStr) } catch (e: Exception) { OrderStatus.PENDING }
                        val isDelivery = doc.getBoolean("isDelivery") ?: false
                        val deliveryAddress = doc.getString("deliveryAddress")
                        val deliveryCharge = doc.getDouble("deliveryCharge") ?: 0.0
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
                        Order(id, sId, cId, customerName, customerPhone, items, totalAmount, status, isDelivery, deliveryAddress, deliveryCharge, createdAt)
                    }
                    trySend(orders.sortedByDescending { it.createdAt })
                }
            }
        awaitClose { listener.remove() }
    }
}
