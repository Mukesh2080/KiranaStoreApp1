package com.village.generalstore.ui.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.village.generalstore.domain.model.Order
import com.village.generalstore.domain.model.OrderStatus
import com.village.generalstore.domain.model.Product
import com.village.generalstore.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerViewModel @Inject constructor(
    private val repository: StoreRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.getProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<Order>> = repository.getOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(SellerUiState())
    val uiState = _uiState.asStateFlow()

    // Dashboard metrics combining orders
    val dashboardMetrics = orders.map { orderList ->
        val completedOrders = orderList.filter { it.status == OrderStatus.COMPLETED }
        val pendingOrders = orderList.filter { it.status == OrderStatus.PENDING }
        val revenue = completedOrders.sumOf { it.totalAmount }
        DashboardMetrics(
            totalRevenue = revenue,
            completedOrdersCount = completedOrders.size,
            pendingOrdersCount = pendingOrders.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    fun addOrUpdateProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.upsertProduct(product)
                _uiState.value = _uiState.value.copy(successMessage = "Product saved successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to save product")
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(productId)
                _uiState.value = _uiState.value.copy(successMessage = "Product deleted successfully")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to delete product")
            }
        }
    }

    fun updateStock(productId: String, stockChange: Double) {
        viewModelScope.launch {
            try {
                val currentProduct = products.value.find { it.id == productId }
                if (currentProduct != null) {
                    val newStock = maxOf(0.0, currentProduct.stock + stockChange)
                    repository.updateStock(productId, newStock)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to update stock")
            }
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, status)
                _uiState.value = _uiState.value.copy(successMessage = "Order status updated to ${status.name}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to update order status")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, errorMessage = null)
    }
}

data class SellerUiState(
    val successMessage: String? = null,
    val errorMessage: String? = null
)

data class DashboardMetrics(
    val totalRevenue: Double = 0.0,
    val completedOrdersCount: Int = 0,
    val pendingOrdersCount: Int = 0
)

// Helper extension function to map StateFlow mapping logic
private fun <T, R> StateFlow<T>.map(transform: (T) -> R): StateFlow<R> {
    val mutableState = MutableStateFlow(transform(this.value))
    CoroutineScope(Dispatchers.Default).launch {
        this@map.collect {
            mutableState.value = transform(it)
        }
    }
    return mutableState.asStateFlow()
}
