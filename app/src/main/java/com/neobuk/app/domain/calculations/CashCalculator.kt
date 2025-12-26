package com.neobuk.app.domain.calculations

/**
 * pure Kotlin domain logic for Cash calculations.
 * Used for Day Closure and reconciliation.
 */
object CashCalculator {

    /**
     * Calculates the Expected Cash amount.
     * Formula: Total Cash Sales - Total Cash Expenses
     */
    fun expectedCash(
        cashSales: Double,
        cashExpenses: Double
    ): Double {
        return cashSales - cashExpenses
    }

    /**
     * Calculates the Discrepancy (difference between physical cash and system record).
     * Formula: Actual Cash - Expected Cash
     * Negative value means cash shortage.
     * Positive value means cash surplus.
     */
    fun discrepancy(
        expectedCash: Double,
        actualCash: Double
    ): Double {
        return actualCash - expectedCash
    }
}
