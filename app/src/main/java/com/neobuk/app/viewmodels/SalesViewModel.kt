package com.neobuk.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neobuk.app.data.repositories.CartItem
import com.neobuk.app.data.repositories.Product
import com.neobuk.app.data.repositories.Sale
import com.neobuk.app.data.repositories.SaleItemType
import com.neobuk.app.data.repositories.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Sales functionality
 * Handles cart management, sale recording, and sales analytics
 */
class SalesViewModel(
    private val salesRepository: SalesRepository
) : ViewModel() {
    
    // Current business ID
    private var currentBusinessId: String? = null
    
    // Expose repository state flows
    val sales: StateFlow<List<Sale>> = salesRepository.sales
    val todaySales: StateFlow<List<Sale>> = salesRepository.todaySales
    val isLoading: StateFlow<Boolean> = salesRepository.isLoading
    
    // Cart state
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()
    
    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal: StateFlow<Double> = _cartTotal.asStateFlow()
    
    private val _cartDiscount = MutableStateFlow(0.0)
    val cartDiscount: StateFlow<Double> = _cartDiscount.asStateFlow()
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // ============================================
    // INITIALIZATION
    // ============================================
    
    fun setBusinessId(businessId: String) {
        if (currentBusinessId == businessId) return
        currentBusinessId = businessId
        loadData()
    }
    
    private fun loadData() {
        val businessId = currentBusinessId ?: return
        viewModelScope.launch {
            salesRepository.fetchTodaySales(businessId)
            salesRepository.fetchSales(businessId, limit = 50)
        }
    }
    
    fun refreshData() {
        loadData()
    }
    
    // ============================================
    // CART MANAGEMENT
    // ============================================
    
    fun addProductToCart(product: Product, quantity: Double = 1.0) {
        val existingIndex = _cartItems.value.indexOfFirst {
            it.type == SaleItemType.PRODUCT && it.productId == product.id
        }
        
        if (existingIndex >= 0) {
            // Update existing item quantity
            val existing = _cartItems.value[existingIndex]
            val updated = existing.copy(quantity = existing.quantity + quantity)
            _cartItems.value = _cartItems.value.toMutableList().apply {
                set(existingIndex, updated)
            }
        } else {
            // Add new item
            val cartItem = CartItem(
                type = SaleItemType.PRODUCT,
                productId = product.id,
                productName = product.name,
                quantity = quantity,
                unitPrice = product.sellingPrice,
                unitCost = product.costPrice
            )
            _cartItems.value = _cartItems.value + cartItem
        }
        
        updateCartTotal()
    }
    
    fun addServiceToCart(
        serviceId: String,
        serviceName: String,
        providerId: String,
        providerName: String,
        price: Double
    ) {
        val cartItem = CartItem(
            type = SaleItemType.SERVICE,
            serviceId = serviceId,
            serviceName = serviceName,
            providerId = providerId,
            providerName = providerName,
            quantity = 1.0,
            unitPrice = price
        )
        _cartItems.value = _cartItems.value + cartItem
        updateCartTotal()
    }
    
    fun updateCartItemQuantity(index: Int, quantity: Double) {
        if (index < 0 || index >= _cartItems.value.size) return
        
        if (quantity <= 0) {
            removeCartItem(index)
        } else {
            val updated = _cartItems.value[index].copy(quantity = quantity)
            _cartItems.value = _cartItems.value.toMutableList().apply {
                set(index, updated)
            }
            updateCartTotal()
        }
    }
    
    fun removeCartItem(index: Int) {
        if (index < 0 || index >= _cartItems.value.size) return
        _cartItems.value = _cartItems.value.toMutableList().apply {
            removeAt(index)
        }
        updateCartTotal()
    }
    
    fun clearCart() {
        _cartItems.value = emptyList()
        _cartDiscount.value = 0.0
        updateCartTotal()
    }
    
    fun setCartDiscount(discount: Double) {
        _cartDiscount.value = discount.coerceAtLeast(0.0)
        updateCartTotal()
    }
    
    private fun updateCartTotal() {
        val subtotal = _cartItems.value.sumOf { it.totalPrice }
        _cartTotal.value = (subtotal - _cartDiscount.value).coerceAtLeast(0.0)
    }
    
    // ============================================
    // CHECKOUT / SALE CREATION
    // ============================================
    
    fun checkout(
        paymentMethod: String = "Cash",
        paymentReference: String? = null,
        customerName: String? = null,
        customerPhone: String? = null,
        amountPaid: Double? = null,
        onSuccess: (Sale) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val businessId = currentBusinessId ?: run {
            onError("No business selected")
            return
        }
        
        if (_cartItems.value.isEmpty()) {
            onError("Cart is empty")
            return
        }
        
        viewModelScope.launch {
            salesRepository.createSale(
                businessId = businessId,
                cartItems = _cartItems.value,
                paymentMethod = paymentMethod,
                paymentReference = paymentReference,
                discountAmount = _cartDiscount.value,
                customerName = customerName,
                customerPhone = customerPhone,
                amountPaid = amountPaid
            ).fold(
                onSuccess = { sale ->
                    clearCart()
                    onSuccess(sale)
                },
                onFailure = { onError(it.message ?: "Failed to complete sale") }
            )
        }
    }
    
    fun quickSale(
        product: Product,
        quantity: Double = 1.0,
        paymentMethod: String = "Cash",
        onSuccess: (Sale) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val businessId = currentBusinessId ?: run {
            onError("No business selected")
            return
        }
        
        viewModelScope.launch {
            salesRepository.quickSale(
                businessId = businessId,
                product = product,
                quantity = quantity,
                paymentMethod = paymentMethod
            ).fold(
                onSuccess = { onSuccess(it) },
                onFailure = { onError(it.message ?: "Failed to complete sale") }
            )
        }
    }
    
    // ============================================
    // FETCH SALE DETAILS
    // ============================================
    
    fun getSaleDetails(
        saleId: String,
        onResult: (Sale?) -> Unit
    ) {
        viewModelScope.launch {
            val sale = salesRepository.fetchSaleWithItems(saleId)
            onResult(sale)
        }
    }
    
    // ============================================
    // ANALYTICS
    // ============================================
    
    fun getTodayTotalSales(): Double {
        return salesRepository.getTodayTotalSales()
    }
    
    fun getTodaySalesCount(): Int {
        return salesRepository.getTodaySalesCount()
    }
    
    fun getTodayProfit(): Double {
        return salesRepository.getTodayProfit()
    }
    
    fun getSalesByPaymentMethod(): Map<String, Double> {
        return salesRepository.getSalesByPaymentMethod()
    }
    
    // Calculated properties
    val cartSubtotal: Double get() = _cartItems.value.sumOf { it.totalPrice }
    val cartItemCount: Int get() = _cartItems.value.size
    val cartQuantityCount: Double get() = _cartItems.value.sumOf { it.quantity }
    val hasItems: Boolean get() = _cartItems.value.isNotEmpty()
    
    // ============================================
    // UTILITY
    // ============================================
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearData() {
        currentBusinessId = null
        clearCart()
        salesRepository.clearData()
    }
}
