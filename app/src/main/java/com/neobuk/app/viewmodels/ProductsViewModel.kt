package com.neobuk.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neobuk.app.data.repositories.Product
import com.neobuk.app.data.repositories.ProductCategory
import com.neobuk.app.data.repositories.ProductsRepository
import com.neobuk.app.data.repositories.StockMovement
import com.neobuk.app.data.models.StockMovementReason
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Products/Inventory functionality
 * Handles product CRUD, stock management, and barcode scanning
 */
class ProductsViewModel(
    private val productsRepository: ProductsRepository
) : ViewModel() {
    
    // Current business ID
    private var currentBusinessId: String? = null
    
    // Expose repository state flows
    val categories: StateFlow<List<ProductCategory>> = productsRepository.categories
    val products: StateFlow<List<Product>> = productsRepository.products
    val stockMovements: StateFlow<List<StockMovement>> = productsRepository.stockMovements
    val isLoading: StateFlow<Boolean> = productsRepository.isLoading
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // ============================================
    // INITIALIZATION
    // ============================================
    
    fun setBusinessId(businessId: String) {
        if (currentBusinessId == businessId) return
        currentBusinessId = businessId
        loadAllData()
    }
    
    private fun loadAllData() {
        val businessId = currentBusinessId ?: return
        viewModelScope.launch {
            // First load categories, then products
            productsRepository.fetchCategories(businessId)
            productsRepository.fetchProducts(businessId)
        }
    }
    
    fun refreshData() {
        loadAllData()
    }
    
    // ============================================
    // CATEGORIES
    // ============================================
    
    fun createCategory(
        name: String,
        description: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val businessId = currentBusinessId ?: run {
            onError("No business selected")
            return
        }
        
        viewModelScope.launch {
            productsRepository.createCategory(
                businessId = businessId,
                name = name,
                description = description
            ).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to create category") }
            )
        }
    }
    
    // ============================================
    // PRODUCTS
    // ============================================
    
    fun createProduct(
        name: String,
        description: String = "",
        categoryId: String? = null,
        barcode: String? = null,
        sku: String? = null,
        unit: String = "pcs",
        costPrice: Double = 0.0,
        sellingPrice: Double,
        quantity: Double = 0.0,
        lowStockThreshold: Double = 5.0,
        trackInventory: Boolean = true,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val businessId = currentBusinessId ?: run {
            onError("No business selected")
            return
        }
        
        viewModelScope.launch {
            productsRepository.createProduct(
                businessId = businessId,
                name = name,
                description = description,
                categoryId = categoryId,
                barcode = barcode,
                sku = sku,
                unit = unit,
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                quantity = quantity,
                lowStockThreshold = lowStockThreshold,
                trackInventory = trackInventory
            ).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to create product") }
            )
        }
    }
    
    fun updateProduct(
        product: Product,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val businessId = currentBusinessId ?: run {
            onError("No business selected")
            return
        }
        
        viewModelScope.launch {
            productsRepository.updateProduct(product, businessId).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to update product") }
            )
        }
    }
    
    fun deleteProduct(
        productId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            productsRepository.deleteProduct(productId).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to delete product") }
            )
        }
    }
    
    // ============================================
    // STOCK MANAGEMENT
    // ============================================
    
    fun addStock(
        productId: String,
        quantity: Double,
        notes: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        updateStock(productId, quantity, StockMovementReason.MANUAL_ADD, notes, onSuccess, onError)
    }
    
    fun removeStock(
        productId: String,
        quantity: Double,
        reason: StockMovementReason = StockMovementReason.ADJUSTMENT,
        notes: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        updateStock(productId, -quantity, reason, notes, onSuccess, onError)
    }
    
    fun recordSale(
        productId: String,
        quantity: Double,
        notes: String = "",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        updateStock(productId, -quantity, StockMovementReason.SALE, notes, onSuccess, onError)
    }
    
    private fun updateStock(
        productId: String,
        quantityChange: Double,
        reason: StockMovementReason,
        notes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val businessId = currentBusinessId ?: run {
            onError("No business selected")
            return
        }
        
        viewModelScope.launch {
            productsRepository.updateStock(
                businessId = businessId,
                productId = productId,
                quantityChange = quantityChange,
                reason = reason,
                notes = notes
            ).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to update stock") }
            )
        }
    }
    
    fun loadStockMovements(productId: String? = null) {
        val businessId = currentBusinessId ?: return
        viewModelScope.launch {
            productsRepository.fetchStockMovements(businessId, productId)
        }
    }
    
    // ============================================
    // BARCODE LOOKUP
    // ============================================
    
    fun findProductByBarcode(
        barcode: String,
        onFound: (Product) -> Unit,
        onNotFound: () -> Unit
    ) {
        val businessId = currentBusinessId ?: run {
            onNotFound()
            return
        }
        
        viewModelScope.launch {
            val product = productsRepository.findByBarcode(businessId, barcode)
            if (product != null) {
                onFound(product)
            } else {
                onNotFound()
            }
        }
    }
    
    // ============================================
    // ANALYTICS
    // ============================================
    
    fun getTotalInventoryValue(): Double {
        return productsRepository.getTotalInventoryValue()
    }
    
    fun getTotalRetailValue(): Double {
        return productsRepository.getTotalRetailValue()
    }
    
    fun getLowStockProducts(): List<Product> {
        return productsRepository.getLowStockProducts()
    }
    
    fun getOutOfStockProducts(): List<Product> {
        return productsRepository.getOutOfStockProducts()
    }
    
    fun getProductCount(): Int {
        return products.value.size
    }
    
    fun getLowStockCount(): Int {
        return getLowStockProducts().size
    }
    
    // ============================================
    // UTILITY
    // ============================================
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearData() {
        currentBusinessId = null
        productsRepository.clearData()
    }
    
    fun getProductById(productId: String): Product? {
        return products.value.find { it.id == productId }
    }
    
    fun getActiveProducts(): List<Product> {
        return products.value.filter { it.isActive }
    }
    
    fun getActiveCategories(): List<ProductCategory> {
        return categories.value.filter { it.isActive }
    }
}
