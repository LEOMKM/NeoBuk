package com.neobuk.app.data.models

import java.util.UUID

data class Product(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val categoryId: String,
    val barcode: String, // UNIQUE
    val unit: String, // e.g., "pcs", "kg", "litres"
    val costPrice: Double,
    val sellingPrice: Double,
    val quantity: Double = 0.0,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class StockMovement(
    val id: UUID = UUID.randomUUID(),
    val productId: UUID,
    val quantityChange: Double, // +10, -2.5, etc.
    val reason: StockMovementReason, // manual_add, sale, adjustment
    val createdAt: Long = System.currentTimeMillis()
)

enum class StockMovementReason {
    MANUAL_ADD,
    SALE,
    ADJUSTMENT,
    RETURN,
    DAMAGE
}
