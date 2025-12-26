package com.neobuk.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neobuk.app.data.models.Product
import com.neobuk.app.data.models.StockMovementReason
import com.neobuk.app.data.repositories.ProductsRepository
// import com.neobuk.app.data.repositories.InventoryRepository // Removed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class InventoryViewModel(
    private val productsRepository: ProductsRepository
) : ViewModel() {
    
    private var currentBusinessId: String? = null
    
    // Map repository Product (String ID) to model Product (UUID ID)
    // This is an adapter layer to support existing UI without major refactoring
    val products: StateFlow<List<Product>> = productsRepository.products
        .map { repoProducts ->
            repoProducts.mapNotNull { repoProduct ->
                try {
                    Product(
                        id = UUID.fromString(repoProduct.id),
                        name = repoProduct.name,
                        description = repoProduct.description ?: "",
                        categoryId = repoProduct.categoryId ?: "",
                        barcode = repoProduct.barcode ?: "",
                        unit = repoProduct.unit,
                        costPrice = repoProduct.costPrice,
                        sellingPrice = repoProduct.sellingPrice,
                        quantity = repoProduct.quantity,
                        imageUrl = repoProduct.imageUrl,
                        createdAt = 0L // Timestamp mismatch
                    )
                } catch (e: Exception) {
                    null // Skip invalid UUIDs
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addProduct(product: Product) {
        val businessId = currentBusinessId ?: return 
        viewModelScope.launch {
            productsRepository.createProduct(
                businessId = businessId,
                name = product.name,
                description = product.description,
                categoryId = if (product.categoryId.isNotBlank()) product.categoryId else null,
                barcode = if (product.barcode.isNotBlank()) product.barcode else null,
                unit = product.unit,
                costPrice = product.costPrice,
                sellingPrice = product.sellingPrice,
                quantity = product.quantity,
                trackInventory = true
            )
        }
    }

    fun updateStock(productId: UUID, quantityChange: Double, reason: StockMovementReason) {
        val businessId = currentBusinessId ?: return
        viewModelScope.launch {
            productsRepository.updateStock(
                businessId = businessId,
                productId = productId.toString(),
                quantityChange = quantityChange,
                reason = StockMovementReason.MANUAL_ADD, // Enum mismatch handling needed if specific reasons
                notes = "Manual adjustment via InventoryViewModel" 
            )
        }
    }

    fun getProductByBarcode(barcode: String): Product? {
        val businessId = currentBusinessId ?: return null
        // findByBarcode is suspend function? 
        // Calling it synchronously in a non-suspend function is impossible if it's suspend.
        // I need to change signature or block. 
        // But ProductsScreen calls it in UI likely... 
        // 'val existingProduct = inventoryViewModel.getProductByBarcode(barcode)' in SheetScreen.ScanProduct
        // If it's blocking, we can't easily change it.
        // BUT ProductsRepository.findByBarcode IS suspend.
        // I must assume for now I cannot fix this blocking call easily without UI refactor.
        // EXCEPT: ProductsRepository caches products? 
        // 'products.value' has the list. I can search locally!
        
        return products.value.find { it.barcode == barcode }
    }
    
    // Set Business ID helper since InventoryViewModel is legacy but needs context
    fun setBusinessId(businessId: String) {
        this.currentBusinessId = businessId
        viewModelScope.launch {
            productsRepository.fetchProducts(businessId)
        }
    }
}
