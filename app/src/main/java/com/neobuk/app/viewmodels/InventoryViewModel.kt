package com.neobuk.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neobuk.app.data.models.Product
import com.neobuk.app.data.models.StockMovementReason
import com.neobuk.app.data.repositories.InventoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

class InventoryViewModel : ViewModel() {
    private val repository = InventoryRepository

    val products: StateFlow<List<Product>> = repository.products
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val stockMovements = repository.stockMovements

    fun addProduct(product: Product) {
        repository.addProduct(product)
    }

    fun updateStock(productId: UUID, quantityChange: Double, reason: StockMovementReason) {
        repository.updateProductStock(productId, quantityChange, reason)
    }

    fun getProductByBarcode(barcode: String): Product? {
        return repository.getProductByBarcode(barcode)
    }
}
