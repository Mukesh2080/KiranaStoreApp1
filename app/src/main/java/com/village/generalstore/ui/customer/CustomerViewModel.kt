package com.village.generalstore.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.village.generalstore.domain.model.CartItem
import com.village.generalstore.domain.model.Order
import com.village.generalstore.domain.model.Product
import com.village.generalstore.domain.model.Store
import com.village.generalstore.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repository: StoreRepository,
    private val sharedPreferences: android.content.SharedPreferences
) : ViewModel() {

    private val _selectedStoreId = MutableStateFlow<String?>(null)
    val selectedStoreId = _selectedStoreId.asStateFlow()

    private val _currentCustomerId = MutableStateFlow<String?>(sharedPreferences.getString("customer_id", null))
    val currentCustomerId = _currentCustomerId.asStateFlow()

    val stores: StateFlow<List<Store>> = repository.getStores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<Product>> = _selectedStoreId.flatMapLatest { storeId ->
        repository.getProducts(storeId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val customerOrders: StateFlow<List<Order>> = _currentCustomerId.flatMapLatest { customerId ->
        if (customerId == null) flowOf(emptyList())
        else repository.getCustomerOrders(customerId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectStore(storeId: String) {
        _selectedStoreId.value = storeId
    }

    fun loginCustomer(name: String, phone: String) {
        viewModelScope.launch {
            val id = repository.getOrCreateCustomer(name, phone)
            _currentCustomerId.value = id
            sharedPreferences.edit().putString("customer_id", id).apply()
        }
    }

    private val _isDelivery = MutableStateFlow(false)
    val isDelivery = _isDelivery.asStateFlow()

    private val _deliveryAddress = MutableStateFlow("")
    val deliveryAddress = _deliveryAddress.asStateFlow()

    fun setDelivery(enabled: Boolean) {
        _isDelivery.value = enabled
    }

    fun setAddress(address: String) {
        _deliveryAddress.value = address
    }

    // Cart calculations
    val cartSummary: StateFlow<CartSummary> = combine(cartItems, _isDelivery) { items, delivery ->
        val itemsTotal = items.sumOf { it.totalAmount }
        
        var totalMrp = 0.0
        for (item in items) {
            val matchingProduct = products.value.find { it.id == item.productId }
            val mrpVal = matchingProduct?.mrp ?: item.price
            totalMrp += mrpVal * item.quantity
        }
        val savings = maxOf(0.0, totalMrp - itemsTotal)
        
        val deliveryCharge = if (delivery && itemsTotal < 500.0 && itemsTotal > 0) 40.0 else 0.0
        
        CartSummary(
            totalAmount = itemsTotal,
            totalSavings = savings,
            itemCount = items.sumOf { it.quantity },
            deliveryCharge = deliveryCharge
        )
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

    fun placeOrder(customerName: String, customerPhone: String) {
        val storeId = _selectedStoreId.value ?: return
        val delivery = _isDelivery.value
        val address = _deliveryAddress.value

        if (customerName.isBlank() || customerPhone.isBlank()) {
            _orderState.value = OrderPlacementState.Error("Name and Phone number are required")
            return
        }
        
        if (delivery && address.isBlank()) {
            _orderState.value = OrderPlacementState.Error("Delivery address is required for home delivery")
            return
        }

        viewModelScope.launch {
            _orderState.value = OrderPlacementState.Loading
            try {
                // 1. Get or create customer ID
                val customerId = repository.getOrCreateCustomer(customerName, customerPhone)
                _currentCustomerId.value = customerId
                sharedPreferences.edit().putString("customer_id", customerId).apply()
                
                // 2. Place order with customerId
                val orderId = repository.placeOrder(
                    storeId = storeId,
                    customerId = customerId,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    isDelivery = delivery,
                    deliveryAddress = if (delivery) address else null
                )
                if (orderId.isNotEmpty()) {
                    _orderState.value = OrderPlacementState.Success(orderId)
                    // Reset delivery state for next time
                    _isDelivery.value = false
                    _deliveryAddress.value = ""
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
    val itemCount: Double = 0.0,
    val deliveryCharge: Double = 0.0
)

sealed interface OrderPlacementState {
    data object Idle : OrderPlacementState
    data object Loading : OrderPlacementState
    data class Success(val orderId: String) : OrderPlacementState
    data class Error(val message: String) : OrderPlacementState
}

