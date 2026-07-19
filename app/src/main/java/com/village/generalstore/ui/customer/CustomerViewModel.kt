package com.village.generalstore.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.village.generalstore.domain.model.CartItem
import com.village.generalstore.domain.model.Product
import com.village.generalstore.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: StoreRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.getProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart calculations
    val cartSummary: StateFlow<CartSummary> = cartItems.map { items ->
        val total = items.sumOf { it.totalAmount }
        // To compute savings, we need to know the original MRP. We've stored that or we can calculate it if we map items.
        // Wait, to calculate savings, we can find the difference between MRP and discounted price.
        // Let's compute savings by referencing the current products list.
        var totalMrp = 0.0
        for (item in items) {
            val matchingProduct = products.value.find { it.id == item.productId }
            val mrpVal = matchingProduct?.mrp ?: item.price
            totalMrp += mrpVal * item.quantity
        }
        val savings = maxOf(0.0, totalMrp - total)
        CartSummary(totalAmount = total, totalSavings = savings, itemCount = items.sumOf { it.quantity })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CartSummary())

    private val _orderState = MutableStateFlow<OrderPlacementState>(OrderPlacementState.Idle)
    val orderState = _orderState.asStateFlow()

    fun addToCart(product: Product, quantity: Double) {
        viewModelScope.launch {
            repository.addToCart(product, quantity)
        }
    }

    fun updateCartQuantity(productId: String, quantity: Double) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, quantity)
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun placeOrder(customerName: String, customerPhone: String, isDelivery: Boolean) {
        if (customerName.isBlank() || customerPhone.isBlank()) {
            _orderState.value = OrderPlacementState.Error("Name and Phone number are required")
            return
        }
        viewModelScope.launch {
            _orderState.value = OrderPlacementState.Loading
            try {
                val orderId = repository.placeOrder(customerName, customerPhone, isDelivery)
                if (orderId.isNotEmpty()) {
                    _orderState.value = OrderPlacementState.Success(orderId)
                } else {
                    _orderState.value = OrderPlacementState.Error("Cart is empty")
                }
            } catch (e: Exception) {
                _orderState.value = OrderPlacementState.Error(e.message ?: "Failed to place order")
            }
        }
    }

    fun resetOrderState() {
        _orderState.value = OrderPlacementState.Idle
    }
}

data class CartSummary(
    val totalAmount: Double = 0.0,
    val totalSavings: Double = 0.0,
    val itemCount: Double = 0.0
)

sealed interface OrderPlacementState {
    data object Idle : OrderPlacementState
    data object Loading : OrderPlacementState
    data class Success(val orderId: String) : OrderPlacementState
    data class Error(val message: String) : OrderPlacementState
}

// Helper extension function to map StateFlow mapping logic
private fun <T, R> StateFlow<T>.map(transform: (T) -> R): StateFlow<R> {
    val mutableState = MutableStateFlow(transform(this.value))
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
        this@map.collect {
            mutableState.value = transform(it)
        }
    }
    return mutableState.asStateFlow()
}
