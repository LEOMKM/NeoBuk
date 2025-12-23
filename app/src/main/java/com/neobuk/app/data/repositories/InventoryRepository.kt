package com.neobuk.app.data.repositories

import com.neobuk.app.data.models.Product
import com.neobuk.app.data.models.StockMovement
import com.neobuk.app.data.models.StockMovementReason
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

object InventoryRepository {
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _stockMovements = MutableStateFlow<List<StockMovement>>(emptyList())
    val stockMovements: StateFlow<List<StockMovement>> = _stockMovements.asStateFlow()

    init {
        // Initialize with some mock data
        val mockProducts = listOf(
            Product(
                id = UUID.randomUUID(),
                name = "Fresh Milk 500ml",
                description = "Daily fresh milk",
                categoryId = "dairy",
                barcode = "5012345678900",
                unit = "Packet",
                costPrice = 45.0,
                sellingPrice = 60.0,
                quantity = 15.0,
                createdAt = System.currentTimeMillis()
            ),
            Product(
                id = UUID.randomUUID(),
                name = "Bread 400g",
                description = "Whole wheat bread",
                categoryId = "bakery",
                barcode = "5012345678901",
                unit = "Loaf",
                costPrice = 50.0,
                sellingPrice = 65.0,
                quantity = 5.0,
                createdAt = System.currentTimeMillis()
            ),
            Product(
                id = UUID.randomUUID(),
                name = "Sugar 1kg",
                description = "Premium sugar",
                categoryId = "pantry",
                barcode = "5012345678902",
                unit = "Kg",
                costPrice = 180.0,
                sellingPrice = 220.0,
                quantity = 0.0, // Out of stock
                createdAt = System.currentTimeMillis()
            )
        )
        _products.value = mockProducts
    }

    fun addProduct(product: Product) {
        _products.update { current ->
            current + product
        }
        // Log initial stock movement
        if (product.quantity > 0) {
            addStockMovement(
                StockMovement(
                    id = UUID.randomUUID(),
                    productId = product.id,
                    quantityChange = product.quantity,
                    reason = StockMovementReason.MANUAL_ADD,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateProductStock(productId: UUID, quantityChange: Double, reason: StockMovementReason) {
        _products.update { current ->
            current.map { product ->
                if (product.id == productId) {
                    product.copy(quantity = product.quantity + quantityChange)
                } else {
                    product
                }
            }
        }
        addStockMovement(
            StockMovement(
                id = UUID.randomUUID(),
                productId = productId,
                quantityChange = quantityChange,
                reason = reason,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    private fun addStockMovement(movement: StockMovement) {
        _stockMovements.update { current ->
            current + movement
        }
    }

    fun getProductByBarcode(barcode: String): Product? {
        return _products.value.find { it.barcode == barcode }
    }
}
