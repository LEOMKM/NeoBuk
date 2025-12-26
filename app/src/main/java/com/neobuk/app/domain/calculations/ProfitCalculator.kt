package com.neobuk.app.domain.calculations

/**
 * pure Kotlin domain logic for Profit calculations.
 * Centralizing the formulas ensures consistency between Home, Reports, and Closure screens.
 */
object ProfitCalculator {

    /**
     * Calculates Gross Profit.
     * Formula: Total Sales - Cost of Goods Sold (COGS)
     * Note: COGS should include commission amounts.
     */
    fun grossProfit(
        totalSales: Double,
        costOfSales: Double
    ): Double {
        return totalSales - costOfSales
    }

    /**
     * Calculates Net Profit.
     * Formula: Gross Profit - Total Expenses
     */
    fun netProfit(
        grossProfit: Double,
        totalExpenses: Double
    ): Double {
        return grossProfit - totalExpenses
    }

    /**
     * Calculates Net Profit Margin (Percentage).
     * Formula: (Net Profit / Total Sales) * 100
     */
    fun netProfitMargin(
        netProfit: Double,
        totalSales: Double
    ): Double {
        if (totalSales == 0.0) return 0.0
        return (netProfit / totalSales) * 100
    }
}
