package com.neobuk.app.domain.calculations

import com.neobuk.app.data.repositories.CartItem
import com.neobuk.app.data.repositories.Sale
import java.util.Calendar

/**
 * Domain logic for Sales calculations and analytics.
 */
object SalesCalculator {

    /**
     * Calculates the subtotal of a list of cart items.
     */
    fun calculateSubtotal(items: List<CartItem>): Double {
        return items.sumOf { it.totalPrice }
    }

    /**
     * Calculates the final total after discount.
     */
    fun calculateTotal(subtotal: Double, discount: Double): Double {
        return (subtotal - discount).coerceAtLeast(0.0)
    }

    /**
     * Groups sales into 4-hour intervals for charting.
     * Intervals: 6AM-10AM, 10AM-12PM, 12PM-4PM, 4PM-6PM, 6PM-8PM, 8PM-10PM, Others
     */
    fun distributeHourlySales(sales: List<Sale>): List<Double> {
        val hourlySales = mutableListOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // 7 intervals
        
        sales.forEach { sale ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = sale.saleDate
            }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            
            val intervalIndex = when (hour) {
                in 6..9 -> 0    // 6AM-10AM
                in 10..11 -> 1  // 10AM-12PM
                in 12..15 -> 2  // 12PM-4PM
                in 16..17 -> 3  // 4PM-6PM
                in 18..19 -> 4  // 6PM-8PM
                in 20..21 -> 5  // 8PM-10PM
                in 22..23, in 0..5 -> 6  // 10PM-6AM
                else -> 2 // Fallback/Default
            }
            
            if (intervalIndex in hourlySales.indices) {
                hourlySales[intervalIndex] += sale.totalAmount
            }
        }
        
        return hourlySales
    }
}
