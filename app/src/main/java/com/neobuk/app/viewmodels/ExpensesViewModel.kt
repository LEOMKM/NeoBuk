package com.neobuk.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neobuk.app.data.repositories.Expense
import com.neobuk.app.data.repositories.ExpenseCategory
import com.neobuk.app.data.repositories.ExpensesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel for Expenses functionality
 * Handles expense recording, categorization, and analytics
 */
class ExpensesViewModel(
    private val expensesRepository: ExpensesRepository
) : ViewModel() {
    
    // Current business ID
    private var currentBusinessId: String? = null
    
    // Expose repository state flows
    val categories: StateFlow<List<ExpenseCategory>> = expensesRepository.categories
    val expenses: StateFlow<List<Expense>> = expensesRepository.expenses
    val isLoading: StateFlow<Boolean> = expensesRepository.isLoading
    
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
            // First load categories
            val cats = expensesRepository.fetchCategories(businessId)
            // Initialize default categories if none exist
            if (cats.isEmpty()) {
                expensesRepository.initializeDefaultCategories(businessId)
            }
            // Then load expenses
            expensesRepository.fetchExpenses(businessId)
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
        icon: String = "MoreHoriz",
        color: String = "#6B7280",
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val businessId = currentBusinessId ?: run {
            onError("No business selected")
            return
        }
        
        viewModelScope.launch {
            expensesRepository.createCategory(
                businessId = businessId,
                name = name,
                icon = icon,
                color = color
            ).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to create category") }
            )
        }
    }
    
    // ============================================
    // EXPENSES
    // ============================================
    
    fun createExpense(
        title: String,
        amount: Double,
        categoryId: String?,
        description: String = "",
        paymentMethod: String = "Cash",
        referenceNumber: String? = null,
        expenseDate: Date = Date(),
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val businessId = currentBusinessId ?: run {
            onError("No business selected")
            return
        }
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        viewModelScope.launch {
            expensesRepository.createExpense(
                businessId = businessId,
                title = title,
                amount = amount,
                categoryId = categoryId,
                description = description,
                paymentMethod = paymentMethod,
                referenceNumber = referenceNumber,
                expenseDate = dateFormat.format(expenseDate),
                recordedBy = null // Could pass current user ID
            ).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to record expense") }
            )
        }
    }
    
    fun updateExpense(
        expense: Expense,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val businessId = currentBusinessId ?: run {
            onError("No business selected")
            return
        }
        
        viewModelScope.launch {
            expensesRepository.updateExpense(expense, businessId).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to update expense") }
            )
        }
    }
    
    fun deleteExpense(
        expenseId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            expensesRepository.deleteExpense(expenseId).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to delete expense") }
            )
        }
    }
    
    // ============================================
    // ANALYTICS
    // ============================================
    
    fun getTotalExpensesThisMonth(): Double {
        return expensesRepository.getTotalExpensesThisMonth()
    }
    
    fun getExpensesByCategory(): Map<String, Double> {
        return expensesRepository.getExpensesByCategory()
    }
    
    fun getExpenseCount(): Int {
        return expenses.value.size
    }
    
    // ============================================
    // UTILITY
    // ============================================
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearData() {
        currentBusinessId = null
        expensesRepository.clearData()
    }
    
    // Get category by ID
    fun getCategoryById(categoryId: String?): ExpenseCategory? {
        return categories.value.find { it.id == categoryId }
    }
    
    // Get active categories
    fun getActiveCategories(): List<ExpenseCategory> {
        return categories.value.filter { it.isActive }
    }
}
