package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CartItem
import com.example.data.Inventory
import com.example.data.JungleMallRepository
import com.example.data.Order
import com.example.data.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CheckoutState {
    object Idle : CheckoutState
    object Loading : CheckoutState
    data class Success(val order: Order) : CheckoutState
    data class Error(val message: String) : CheckoutState
}

class JungleMallViewModel(private val repository: JungleMallRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _currentTab = MutableStateFlow("shop") // "shop", "cart", "orders", "profile"
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    // Customer name and phone for billing
    val customerName = MutableStateFlow("")
    val customerPhone = MutableStateFlow("")
    val selectedPaymentMethod = MutableStateFlow("Vodacom M-Pesa")

    // Filtered inventory based on query and category
    val filteredProducts: StateFlow<List<Product>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        Inventory.products.filter { product ->
            val matchesQuery = product.name.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || product.category == category
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Inventory.products
    )

    // Reactive Cart items from Room
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Reactive historic Orders from Room
    val orders: StateFlow<List<Order>> = repository.allOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(product, quantity)
        }
    }

    fun updateCartQuantity(productId: String, qty: Int) {
        viewModelScope.launch {
            repository.updateQuantity(productId, qty)
        }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun selectPaymentMethod(method: String) {
        selectedPaymentMethod.value = method
    }

    fun performCheckout() {
        val name = customerName.value.trim()
        val phone = customerPhone.value.trim()
        val pymt = selectedPaymentMethod.value

        if (name.isEmpty()) {
            _checkoutState.value = CheckoutState.Error("Tafadhali weka jina lako (Please enter your name)")
            return
        }
        if (phone.isEmpty()) {
            _checkoutState.value = CheckoutState.Error("Tafadhali weka namba ya simu (Please enter phone number)")
            return
        }

        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Loading
            try {
                // Simulate network latency/order processing
                kotlinx.coroutines.delay(1200)
                val order = repository.checkout(name, phone, pymt)
                if (order != null) {
                    _checkoutState.value = CheckoutState.Success(order)
                    // Clear fields and switch to orders tab
                    customerName.value = ""
                    customerPhone.value = ""
                } else {
                    _checkoutState.value = CheckoutState.Error("Kifurushi chako kiko wazi (Your cart is empty)")
                }
            } catch (e: Exception) {
                _checkoutState.value = CheckoutState.Error(e.message ?: "Chakula kimeshindwa wekwa (An error occurred)")
            }
        }
    }

    fun resetCheckoutState() {
        _checkoutState.value = CheckoutState.Idle
    }
}

class JungleMallViewModelFactory(private val repository: JungleMallRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JungleMallViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JungleMallViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
