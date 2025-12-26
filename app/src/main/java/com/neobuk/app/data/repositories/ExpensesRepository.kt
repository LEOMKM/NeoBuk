package com.neobuk.app.data.repositories

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
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
data class ExpenseCategoryDTO(
    val id: String? = null,
    @SerialName("business_id") val businessId: String,
    val name: String,
    val icon: String = "MoreHoriz",
    val color: String = "#6B7280",
    @SerialName("is_system") val isSystem: Boolean = false,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class ExpenseDTO(
    val id: String? = null,
    @SerialName("business_id") val businessId: String,
    @SerialName("category_id") val categoryId: String? = null,
    val title: String,
    val amount: Double,
    val description: String? = null,
    @SerialName("payment_method") val paymentMethod: String = "Cash",
    @SerialName("reference_number") val referenceNumber: String? = null,
    @SerialName("receipt_url") val receiptUrl: String? = null,
    @SerialName("expense_date") val expenseDate: String? = null,
    @SerialName("recorded_by") val recordedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

// ============================================
// Domain Models
// ============================================

data class ExpenseCategory(
    val id: String,
    val name: String,
    val icon: String = "MoreHoriz",
    val color: String = "#6B7280",
    val isSystem: Boolean = false,
    val isActive: Boolean = true
)

data class Expense(
    val id: String,
    val categoryId: String?,
    val categoryName: String = "Other",
    val title: String,
    val amount: Double,
    val description: String = "",
    val paymentMethod: String = "Cash",
    val referenceNumber: String? = null,
    val receiptUrl: String? = null,
    val expenseDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

// ============================================
// EXPENSES REPOSITORY
// ============================================

class ExpensesRepository(private val supabaseClient: SupabaseClient) {
    
    private val database = supabaseClient.postgrest
    
    // State holders
    private val _categories = MutableStateFlow<List<ExpenseCategory>>(emptyList())
    val categories: StateFlow<List<ExpenseCategory>> = _categories.asStateFlow()
    
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // ============================================
    // EXPENSE CATEGORIES
    // ============================================
    
    suspend fun fetchCategories(businessId: String): List<ExpenseCategory> {
        return try {
            _isLoading.value = true
            val result = database["expense_categories"]
                .select {
                    filter { eq("business_id", businessId) }
                }
                .decodeList<ExpenseCategoryDTO>()
            
            val categories = result.map { it.toExpenseCategory() }
            _categories.value = categories
            categories
        } catch (e: Exception) {
            // If no categories exist, return empty list (they'll be created on first use)
            emptyList()
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun createCategory(
        businessId: String,
        name: String,
        icon: String = "MoreHoriz",
        color: String = "#6B7280"
    ): Result<ExpenseCategory> {
        return try {
            _isLoading.value = true
            val dto = ExpenseCategoryDTO(
                businessId = businessId,
                name = name,
                icon = icon,
                color = color,
                isSystem = false
            )
            
            val result = database["expense_categories"]
                .insert(dto) { select() }
                .decodeSingle<ExpenseCategoryDTO>()
            
            val category = result.toExpenseCategory()
            _categories.value = _categories.value + category
            Result.success(category)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create category: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun initializeDefaultCategories(businessId: String): Result<Unit> {
        return try {
            // Call the stored function to initialize categories
            val params = buildJsonObject { put("p_business_id", businessId) }
            database.rpc("initialize_expense_categories", params)
            // Refresh categories
            fetchCategories(businessId)
            Result.success(Unit)
        } catch (e: Exception) {
            // If function doesn't exist, create categories manually
            val defaultCategories = listOf(
                Triple("Utilities", "Bolt", "#F59E0B"),
                Triple("Rent", "Home", "#6366F1"),
                Triple("Supplies", "ShoppingCart", "#10B981"),
                Triple("Transport", "LocalShipping", "#3B82F6"),
                Triple("Salary", "People", "#EC4899"),
                Triple("Maintenance", "Build", "#8B5CF6"),
                Triple("Other", "MoreHoriz", "#6B7280")
            )
            
            defaultCategories.forEach { (name, icon, color) ->
                try {
                    createCategory(businessId, name, icon, color)
                } catch (e: Exception) {
                    // Ignore duplicates
                }
            }
            Result.success(Unit)
        }
    }
    
    // ============================================
    // EXPENSES
    // ============================================
    
    suspend fun fetchExpenses(businessId: String, limit: Int = 100): List<Expense> {
        return try {
            _isLoading.value = true
            val result = database["expenses"]
                .select {
                    filter { eq("business_id", businessId) }
                    order("expense_date", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<ExpenseDTO>()
            
            // Map category names
            val categoryMap = _categories.value.associateBy { it.id }
            val expenses = result.map { dto ->
                dto.toExpense(categoryMap[dto.categoryId]?.name ?: "Other")
            }
            _expenses.value = expenses
            expenses
        } catch (e: Exception) {
            emptyList()
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun createExpense(
        businessId: String,
        title: String,
        amount: Double,
        categoryId: String?,
        description: String = "",
        paymentMethod: String = "Cash",
        referenceNumber: String? = null,
        expenseDate: String? = null,
        recordedBy: String? = null
    ): Result<Expense> {
        return try {
            _isLoading.value = true
            val dto = ExpenseDTO(
                businessId = businessId,
                categoryId = categoryId,
                title = title,
                amount = amount,
                description = description.ifBlank { null },
                paymentMethod = paymentMethod,
                referenceNumber = referenceNumber,
                expenseDate = expenseDate,
                recordedBy = recordedBy
            )
            
            val result = database["expenses"]
                .insert(dto) { select() }
                .decodeSingle<ExpenseDTO>()
            
            val categoryName = _categories.value.find { it.id == categoryId }?.name ?: "Other"
            val expense = result.toExpense(categoryName)
            _expenses.value = listOf(expense) + _expenses.value
            Result.success(expense)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to record expense: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateExpense(expense: Expense, businessId: String): Result<Expense> {
        return try {
            _isLoading.value = true
            val updates = mapOf(
                "title" to expense.title,
                "amount" to expense.amount,
                "category_id" to expense.categoryId,
                "description" to expense.description.ifBlank { null },
                "payment_method" to expense.paymentMethod,
                "reference_number" to expense.referenceNumber
            )
            
            database["expenses"]
                .update(updates) {
                    filter { eq("id", expense.id) }
                }
            
            _expenses.value = _expenses.value.map {
                if (it.id == expense.id) expense else it
            }
            Result.success(expense)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update expense: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            database["expenses"]
                .delete {
                    filter { eq("id", expenseId) }
                }
            
            _expenses.value = _expenses.value.filter { it.id != expenseId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete expense: ${e.message}"))
        }
    }
    
    // ============================================
    // ANALYTICS
    // ============================================
    
    fun getTotalExpensesThisMonth(): Double {
        val now = java.util.Calendar.getInstance()
        val startOfMonth = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        return _expenses.value
            .filter { it.expenseDate >= startOfMonth }
            .sumOf { it.amount }
    }
    
    fun getExpensesByCategory(): Map<String, Double> {
        return _expenses.value
            .groupBy { it.categoryName }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }
    
    // ============================================
    // HELPERS
    // ============================================
    
    fun clearData() {
        _categories.value = emptyList()
        _expenses.value = emptyList()
    }
    
    // ============================================
    // DTO CONVERTERS
    // ============================================
    
    private fun ExpenseCategoryDTO.toExpenseCategory(): ExpenseCategory {
        return ExpenseCategory(
            id = this.id ?: "",
            name = this.name,
            icon = this.icon,
            color = this.color,
            isSystem = this.isSystem,
            isActive = this.isActive
        )
    }
    
    private fun ExpenseDTO.toExpense(categoryName: String): Expense {
        return Expense(
            id = this.id ?: "",
            categoryId = this.categoryId,
            categoryName = categoryName,
            title = this.title,
            amount = this.amount,
            description = this.description ?: "",
            paymentMethod = this.paymentMethod,
            referenceNumber = this.referenceNumber,
            receiptUrl = this.receiptUrl,
            expenseDate = parseDate(this.expenseDate),
            createdAt = parseTimestamp(this.createdAt)
        )
    }
    
    private fun parseDate(date: String?): Long {
        return try {
            date?.let {
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    .parse(it)?.time
            } ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
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
