package com.neobuk.app.data.repositories

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// ============================================
// DTOs for Supabase
// ============================================

@Serializable
data class ProductCategoryDTO(
    val id: String? = null,
    @SerialName("business_id") val businessId: String,
    val name: String,
    val description: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class ProductDTO(
    val id: String? = null,
    @SerialName("business_id") val businessId: String,
    @SerialName("category_id") val categoryId: String? = null,
    val name: String,
    val description: String? = null,
    val barcode: String? = null,
    val sku: String? = null,
    val unit: String = "pcs",
    @SerialName("cost_price") val costPrice: Double = 0.0,
    @SerialName("selling_price") val sellingPrice: Double,
    val quantity: Double = 0.0,
    @SerialName("low_stock_threshold") val lowStockThreshold: Double = 5.0,
    @SerialName("track_inventory") val trackInventory: Boolean = true,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class StockMovementDTO(
    val id: String? = null,
    @SerialName("business_id") val businessId: String,
    @SerialName("product_id") val productId: String,
    @SerialName("quantity_change") val quantityChange: Double,
    val reason: String,
    val notes: String? = null,
    @SerialName("balance_after") val balanceAfter: Double? = null,
    @SerialName("reference_id") val referenceId: String? = null,
    @SerialName("reference_type") val referenceType: String? = null,
    @SerialName("recorded_by") val recordedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// ============================================
// Domain Models
// ============================================

data class ProductCategory(
    val id: String,
    val name: String,
    val description: String = "",
    val isActive: Boolean = true
)

data class Product(
    val id: String,
    val categoryId: String?,
    val categoryName: String = "",
    val name: String,
    val description: String = "",
    val barcode: String? = null,
    val sku: String? = null,
    val unit: String = "pcs",
    val costPrice: Double = 0.0,
    val sellingPrice: Double,
    val quantity: Double = 0.0,
    val lowStockThreshold: Double = 5.0,
    val trackInventory: Boolean = true,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean get() = quantity <= lowStockThreshold
    val profit: Double get() = sellingPrice - costPrice
    val profitMargin: Double get() = if (sellingPrice > 0) (profit / sellingPrice) * 100 else 0.0
}

data class StockMovement(
    val id: String,
    val productId: String,
    val productName: String = "",
    val quantityChange: Double,
    val reason: String,
    val notes: String = "",
    val balanceAfter: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class StockMovementReason(val displayName: String) {
    MANUAL_ADD("Stock Added"),
    SALE("Sold"),
    ADJUSTMENT("Adjusted"),
    RETURN("Returned"),
    DAMAGE("Damaged")
}

// ============================================
// PRODUCTS REPOSITORY
// ============================================

class ProductsRepository(private val supabaseClient: SupabaseClient) {
    
    private val database = supabaseClient.postgrest
    
    // State holders
    private val _categories = MutableStateFlow<List<ProductCategory>>(emptyList())
    val categories: StateFlow<List<ProductCategory>> = _categories.asStateFlow()
    
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()
    
    private val _stockMovements = MutableStateFlow<List<StockMovement>>(emptyList())
    val stockMovements: StateFlow<List<StockMovement>> = _stockMovements.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // ============================================
    // PRODUCT CATEGORIES
    // ============================================
    
    suspend fun fetchCategories(businessId: String): List<ProductCategory> {
        return try {
            _isLoading.value = true
            val result = database["product_categories"]
                .select {
                    filter { eq("business_id", businessId) }
                }
                .decodeList<ProductCategoryDTO>()
            
            val categories = result.map { it.toProductCategory() }
            _categories.value = categories
            categories
        } catch (e: Exception) {
            emptyList()
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun createCategory(
        businessId: String,
        name: String,
        description: String = ""
    ): Result<ProductCategory> {
        return try {
            _isLoading.value = true
            val dto = ProductCategoryDTO(
                businessId = businessId,
                name = name,
                description = description.ifBlank { null }
            )
            
            val result = database["product_categories"]
                .insert(dto) { select() }
                .decodeSingle<ProductCategoryDTO>()
            
            val category = result.toProductCategory()
            _categories.value = _categories.value + category
            Result.success(category)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create category: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    // ============================================
    // PRODUCTS
    // ============================================
    
    suspend fun fetchProducts(businessId: String): List<Product> {
        return try {
            _isLoading.value = true
            val result = database["products"]
                .select {
                    filter { eq("business_id", businessId) }
                    order("name", Order.ASCENDING)
                }
                .decodeList<ProductDTO>()
            
            // Map category names
            val categoryMap = _categories.value.associateBy { it.id }
            val products = result.map { dto ->
                dto.toProduct(categoryMap[dto.categoryId]?.name ?: "")
            }
            _products.value = products
            products
        } catch (e: Exception) {
            emptyList()
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun createProduct(
        businessId: String,
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
        trackInventory: Boolean = true
    ): Result<Product> {
        return try {
            _isLoading.value = true
            val dto = ProductDTO(
                businessId = businessId,
                categoryId = categoryId,
                name = name,
                description = description.ifBlank { null },
                barcode = barcode?.ifBlank { null },
                sku = sku?.ifBlank { null },
                unit = unit,
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                quantity = quantity,
                lowStockThreshold = lowStockThreshold,
                trackInventory = trackInventory
            )
            
            val result = database["products"]
                .insert(dto) { select() }
                .decodeSingle<ProductDTO>()
            
            val categoryName = _categories.value.find { it.id == categoryId }?.name ?: ""
            val product = result.toProduct(categoryName)
            _products.value = _products.value + product
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create product: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateProduct(product: Product, businessId: String): Result<Product> {
        return try {
            _isLoading.value = true
            val updates = mapOf(
                "name" to product.name,
                "description" to product.description.ifBlank { null },
                "category_id" to product.categoryId,
                "barcode" to product.barcode,
                "sku" to product.sku,
                "unit" to product.unit,
                "cost_price" to product.costPrice,
                "selling_price" to product.sellingPrice,
                "low_stock_threshold" to product.lowStockThreshold,
                "track_inventory" to product.trackInventory,
                "is_active" to product.isActive
            )
            
            database["products"]
                .update(updates) {
                    filter { eq("id", product.id) }
                }
            
            _products.value = _products.value.map {
                if (it.id == product.id) product else it
            }
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update product: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            database["products"]
                .delete {
                    filter { eq("id", productId) }
                }
            
            _products.value = _products.value.filter { it.id != productId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete product: ${e.message}"))
        }
    }
    
    // ============================================
    // STOCK MANAGEMENT
    // ============================================
    
    suspend fun updateStock(
        businessId: String,
        productId: String,
        quantityChange: Double,
        reason: com.neobuk.app.data.models.StockMovementReason,
        notes: String = ""
    ): Result<Product> {
        return try {
            _isLoading.value = true
            
            // Use the stored function for atomic stock update
            val params = buildJsonObject {
                put("p_product_id", productId)
                put("p_quantity_change", quantityChange)
                put("p_reason", reason.name)
                put("p_notes", notes.ifBlank { null })
            }
            
            val result = database.rpc("update_product_stock", params)
                .decodeSingle<ProductDTO>()
            
            val categoryName = _categories.value.find { it.id == result.categoryId }?.name ?: ""
            val updatedProduct = result.toProduct(categoryName)
            
            _products.value = _products.value.map {
                if (it.id == productId) updatedProduct else it
            }
            
            Result.success(updatedProduct)
        } catch (e: Exception) {
            // Fallback to manual update if stored function doesn't exist
            try {
                val currentProduct = _products.value.find { it.id == productId }
                    ?: return Result.failure(Exception("Product not found"))
                
                val newQuantity = currentProduct.quantity + quantityChange
                if (currentProduct.trackInventory && newQuantity < 0) {
                    return Result.failure(Exception("Insufficient stock"))
                }
                
                // Update product quantity
                database["products"]
                    .update(mapOf("quantity" to newQuantity)) {
                        filter { eq("id", productId) }
                    }
                
                // Record movement
                val movementDto = StockMovementDTO(
                    businessId = businessId,
                    productId = productId,
                    quantityChange = quantityChange,
                    reason = reason.name,
                    notes = notes.ifBlank { null },
                    balanceAfter = newQuantity
                )
                
                database["stock_movements"].insert(movementDto)
                
                val updatedProduct = currentProduct.copy(quantity = newQuantity)
                _products.value = _products.value.map {
                    if (it.id == productId) updatedProduct else it
                }
                
                Result.success(updatedProduct)
            } catch (fallbackError: Exception) {
                Result.failure(Exception("Failed to update stock: ${fallbackError.message}"))
            }
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun fetchStockMovements(businessId: String, productId: String? = null, limit: Int = 50): List<StockMovement> {
        return try {
            val result = database["stock_movements"]
                .select {
                    filter { eq("business_id", businessId) }
                    productId?.let { filter { eq("product_id", it) } }
                    order("created_at", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<StockMovementDTO>()
            
            val productMap = _products.value.associateBy { it.id }
            val movements = result.map { dto ->
                dto.toStockMovement(productMap[dto.productId]?.name ?: "Unknown Product")
            }
            _stockMovements.value = movements
            movements
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // ============================================
    // PRODUCT LOOKUP
    // ============================================
    
    suspend fun findByBarcode(businessId: String, barcode: String): Product? {
        return try {
            val result = database["products"]
                .select {
                    filter {
                        eq("business_id", businessId)
                        eq("barcode", barcode)
                    }
                }
                .decodeSingleOrNull<ProductDTO>()
            
            result?.let {
                val categoryName = _categories.value.find { cat -> cat.id == it.categoryId }?.name ?: ""
                it.toProduct(categoryName)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // ============================================
    // ANALYTICS
    // ============================================
    
    fun getTotalInventoryValue(): Double {
        return _products.value.sumOf { it.quantity * it.costPrice }
    }
    
    fun getTotalRetailValue(): Double {
        return _products.value.sumOf { it.quantity * it.sellingPrice }
    }
    
    fun getLowStockProducts(): List<Product> {
        return _products.value.filter { it.isLowStock && it.trackInventory }
    }
    
    fun getOutOfStockProducts(): List<Product> {
        return _products.value.filter { it.quantity <= 0 && it.trackInventory }
    }
    
    // ============================================
    // HELPERS
    // ============================================
    
    fun clearData() {
        _categories.value = emptyList()
        _products.value = emptyList()
        _stockMovements.value = emptyList()
    }
    
    // ============================================
    // DTO CONVERTERS
    // ============================================
    
    private fun ProductCategoryDTO.toProductCategory(): ProductCategory {
        return ProductCategory(
            id = this.id ?: "",
            name = this.name,
            description = this.description ?: "",
            isActive = this.isActive
        )
    }
    
    private fun ProductDTO.toProduct(categoryName: String): Product {
        return Product(
            id = this.id ?: "",
            categoryId = this.categoryId,
            categoryName = categoryName,
            name = this.name,
            description = this.description ?: "",
            barcode = this.barcode,
            sku = this.sku,
            unit = this.unit,
            costPrice = this.costPrice,
            sellingPrice = this.sellingPrice,
            quantity = this.quantity,
            lowStockThreshold = this.lowStockThreshold,
            trackInventory = this.trackInventory,
            isActive = this.isActive,
            createdAt = parseTimestamp(this.createdAt)
        )
    }
    
    private fun StockMovementDTO.toStockMovement(productName: String): StockMovement {
        return StockMovement(
            id = this.id ?: "",
            productId = this.productId,
            productName = productName,
            quantityChange = this.quantityChange,
            reason = this.reason,
            notes = this.notes ?: "",
            balanceAfter = this.balanceAfter ?: 0.0,
            createdAt = parseTimestamp(this.createdAt)
        )
    }
    
    private fun parseTimestamp(timestamp: String?): Long {
        return try {
            timestamp?.let {
                kotlinx.datetime.Instant.parse(it).toEpochMilliseconds()
            } ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
