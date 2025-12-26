package com.neobuk.app.data.repositories

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ============================================
// DTOs for Supabase
// ============================================

@Serializable
data class SaleDTO(
    val id: String? = null,
    @SerialName("business_id") val businessId: String,
    @SerialName("sale_number") val saleNumber: String? = null,
    @SerialName("sale_type") val saleType: String = "PRODUCT",
    val subtotal: Double = 0.0,
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    @SerialName("tax_amount") val taxAmount: Double = 0.0,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("payment_method") val paymentMethod: String = "Cash",
    @SerialName("payment_reference") val paymentReference: String? = null,
    @SerialName("payment_status") val paymentStatus: String = "PAID",
    @SerialName("amount_paid") val amountPaid: Double? = null,
    @SerialName("change_given") val changeGiven: Double = 0.0,
    @SerialName("customer_name") val customerName: String? = null,
    @SerialName("customer_phone") val customerPhone: String? = null,
    @SerialName("recorded_by") val recordedBy: String? = null,
    @SerialName("sale_date") val saleDate: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class SaleItemDTO(
    val id: String? = null,
    @SerialName("sale_id") val saleId: String,
    @SerialName("item_type") val itemType: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("product_name") val productName: String? = null,
    @SerialName("service_id") val serviceId: String? = null,
    @SerialName("service_name") val serviceName: String? = null,
    @SerialName("provider_id") val providerId: String? = null,
    @SerialName("provider_name") val providerName: String? = null,
    val quantity: Double = 1.0,
    @SerialName("unit_price") val unitPrice: Double,
    val discount: Double = 0.0,
    @SerialName("total_price") val totalPrice: Double,
    @SerialName("unit_cost") val unitCost: Double = 0.0,
    @SerialName("commission_amount") val commissionAmount: Double = 0.0,
    @SerialName("created_at") val createdAt: String? = null
)

// ============================================
// Domain Models
// ============================================

data class Sale(
    val id: String,
    val saleNumber: String,
    val saleType: SaleType = SaleType.PRODUCT,
    val subtotal: Double,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double,
    val paymentMethod: String = "Cash",
    val paymentReference: String? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.PAID,
    val amountPaid: Double,
    val changeGiven: Double = 0.0,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val items: List<SaleItem> = emptyList(),
    val saleDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val itemCount: Int get() = items.size
    val profit: Double get() = items.sumOf { it.profit }
}

data class SaleItem(
    val id: String,
    val saleId: String,
    val itemType: SaleItemType,
    val productId: String? = null,
    val productName: String = "",
    val serviceId: String? = null,
    val serviceName: String = "",
    val providerId: String? = null,
    val providerName: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double,
    val discount: Double = 0.0,
    val totalPrice: Double,
    val unitCost: Double = 0.0,
    val commissionAmount: Double = 0.0
) {
    val displayName: String get() = productName.ifBlank { serviceName }
    val profit: Double get() = if (itemType == SaleItemType.PRODUCT) {
        totalPrice - (unitCost * quantity)
    } else {
        totalPrice - commissionAmount
    }
}

enum class SaleType { PRODUCT, SERVICE, MIXED }
enum class SaleItemType { PRODUCT, SERVICE }
enum class PaymentStatus { PAID, PENDING, PARTIAL }

// Cart item for building a sale
data class CartItem(
    val type: SaleItemType,
    val productId: String? = null,
    val productName: String = "",
    val serviceId: String? = null,
    val serviceName: String = "",
    val providerId: String? = null,
    val providerName: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double,
    val unitCost: Double = 0.0,
    val discount: Double = 0.0
) {
    val totalPrice: Double get() = (quantity * unitPrice) - discount
    val displayName: String get() = productName.ifBlank { serviceName }
}

// ============================================
// SALES REPOSITORY
// ============================================

class SalesRepository(private val supabaseClient: SupabaseClient) {
    
    private val database = supabaseClient.postgrest
    
    // State holders
    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()
    
    private val _todaySales = MutableStateFlow<List<Sale>>(emptyList())
    val todaySales: StateFlow<List<Sale>> = _todaySales.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // ============================================
    // FETCH SALES
    // ============================================
    
    suspend fun fetchSales(businessId: String, limit: Int = 100): List<Sale> {
        return try {
            _isLoading.value = true
            val result = database["sales"]
                .select {
                    filter { eq("business_id", businessId) }
                    order("sale_date", Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<SaleDTO>()
            
            val sales = result.map { it.toSale() }
            _sales.value = sales
            sales
        } catch (e: Exception) {
            emptyList()
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun fetchTodaySales(businessId: String): List<Sale> {
        return try {
            _isLoading.value = true
            
            // Calculate today range in UTC correctly
            val now = LocalDateTime.now()
            val zone = ZoneId.systemDefault()
            val startOfDay = now.with(LocalTime.MIN).atZone(zone).toInstant().toEpochMilli()
            val endOfDay = now.with(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()

            val isoStart = formatIsoDate(startOfDay)
            val isoEnd = formatIsoDate(endOfDay)
            
            android.util.Log.d("SalesRepository", "Fetching today's sales for range: $isoStart to $isoEnd, business: $businessId")
            
            val result = database["sales"]
                .select {
                    filter {
                        eq("business_id", businessId)
                        gte("created_at", isoStart)
                        lte("created_at", isoEnd)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<SaleDTO>()
            
            android.util.Log.d("SalesRepository", "Found ${result.size} sales for today")
            
            val sales = result.map { it.toSale() }
            _todaySales.value = sales
            sales
        } catch (e: Exception) {
            android.util.Log.e("SalesRepository", "Error fetching today's sales: ${e.message}", e)
            emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun fetchSalesForRange(businessId: String, startTimestamp: Long, endTimestamp: Long): List<Sale> {
        return try {
            _isLoading.value = true
            val isoStart = formatIsoDate(startTimestamp)
            val isoEnd = formatIsoDate(endTimestamp)
            
            android.util.Log.d("SalesRepository", "Fetching sales for range: $isoStart to $isoEnd")
            
            val result = database["sales"]
                .select {
                    filter {
                        eq("business_id", businessId)
                        gte("created_at", isoStart)
                        lte("created_at", isoEnd)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<SaleDTO>()
            
            android.util.Log.d("SalesRepository", "Found ${result.size} sales for range")
            
            val sales = result.map { it.toSale() }
            _sales.value = sales
            sales
        } catch (e: Exception) {
            android.util.Log.e("SalesRepository", "Error fetching range sales: ${e.message}", e)
            emptyList()
        } finally {
            _isLoading.value = false
        }
    }

    private fun formatIsoDate(timestamp: Long): String {
        return DateTimeFormatter.ISO_INSTANT
            .format(Instant.ofEpochMilli(timestamp))
    }
    
    suspend fun fetchSaleWithItems(saleId: String): Sale? {
        return try {
            val saleResult = database["sales"]
                .select {
                    filter { eq("id", saleId) }
                }
                .decodeSingleOrNull<SaleDTO>() ?: return null
            
            val itemsResult = database["sale_items"]
                .select {
                    filter { eq("sale_id", saleId) }
                }
                .decodeList<SaleItemDTO>()
            
            saleResult.toSale(itemsResult.map { it.toSaleItem() })
        } catch (e: Exception) {
            null
        }
    }
    
    // ============================================
    // CREATE SALE
    // ============================================
    
    suspend fun createSale(
        businessId: String,
        cartItems: List<CartItem>,
        paymentMethod: String = "Cash",
        paymentReference: String? = null,
        discountAmount: Double = 0.0,
        customerName: String? = null,
        customerPhone: String? = null,
        amountPaid: Double? = null
    ): Result<Sale> {
        return try {
            _isLoading.value = true
            
            // Try using the stored function first
            val itemsJson = buildJsonArray {
                cartItems.forEach { item ->
                    add(buildJsonObject {
                        put("type", if (item.type == SaleItemType.PRODUCT) "PRODUCT" else "SERVICE")
                        item.productId?.let { put("product_id", it) }
                        item.serviceId?.let { put("service_id", it) }
                        item.providerId?.let { put("provider_id", it) }
                        put("quantity", item.quantity)
                        put("unit_price", item.unitPrice)
                        put("discount", item.discount)
                    })
                }
            }
            
            val params = buildJsonObject {
                put("p_business_id", businessId)
                put("p_items", itemsJson)
                put("p_payment_method", paymentMethod)
                paymentReference?.let { put("p_payment_reference", it) }
                customerName?.let { put("p_customer_name", it) }
                customerPhone?.let { put("p_customer_phone", it) }
                put("p_discount_amount", discountAmount)
            }
            
            val result = database.rpc("record_sale", params)
                .decodeSingle<SaleDTO>()
            
            val sale = result.toSale()
            _sales.value = listOf(sale) + _sales.value
            _todaySales.value = listOf(sale) + _todaySales.value
            Result.success(sale)
            
        } catch (e: Exception) {
            // Fallback to manual creation
            try {
                createSaleManually(
                    businessId, cartItems, paymentMethod, paymentReference,
                    discountAmount, customerName, customerPhone, amountPaid
                )
            } catch (fallbackError: Exception) {
                Result.failure(Exception("Failed to create sale: ${fallbackError.message}"))
            }
        } finally {
            _isLoading.value = false
        }
    }
    
    private suspend fun createSaleManually(
        businessId: String,
        cartItems: List<CartItem>,
        paymentMethod: String,
        paymentReference: String?,
        discountAmount: Double,
        customerName: String?,
        customerPhone: String?,
        amountPaid: Double?
    ): Result<Sale> {
        // Calculate totals
        val subtotal = cartItems.sumOf { it.totalPrice }
        val total = subtotal - discountAmount
        val paid = amountPaid ?: total
        val change = if (paid > total) paid - total else 0.0
        
        // Generate sale number
        val datePrefix = java.text.SimpleDateFormat("yyMMdd", java.util.Locale.getDefault())
            .format(java.util.Date())
        val saleNumber = "SL-$datePrefix-${System.currentTimeMillis() % 10000}"
        
        // Determine sale type
        val hasProducts = cartItems.any { it.type == SaleItemType.PRODUCT }
        val hasServices = cartItems.any { it.type == SaleItemType.SERVICE }
        val saleType = when {
            hasProducts && hasServices -> "MIXED"
            hasServices -> "SERVICE"
            else -> "PRODUCT"
        }
        
        // Create sale record
        val saleDto = SaleDTO(
            businessId = businessId,
            saleNumber = saleNumber,
            saleType = saleType,
            subtotal = subtotal,
            discountAmount = discountAmount,
            totalAmount = total,
            paymentMethod = paymentMethod,
            paymentReference = paymentReference,
            paymentStatus = if (paid >= total) "PAID" else "PARTIAL",
            amountPaid = paid,
            changeGiven = change,
            customerName = customerName,
            customerPhone = customerPhone,
            saleDate = Instant.now().toString()
        )
        
        val saleResult = database["sales"]
            .insert(saleDto) { select() }
            .decodeSingle<SaleDTO>()
        
        val saleId = saleResult.id ?: throw Exception("Failed to get sale ID")
        
        // Create sale items
        val saleItems = mutableListOf<SaleItem>()
        cartItems.forEach { item ->
            val itemDto = SaleItemDTO(
                saleId = saleId,
                itemType = if (item.type == SaleItemType.PRODUCT) "PRODUCT" else "SERVICE",
                productId = item.productId,
                productName = item.productName.ifBlank { null },
                serviceId = item.serviceId,
                serviceName = item.serviceName.ifBlank { null },
                providerId = item.providerId,
                providerName = item.providerName.ifBlank { null },
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                discount = item.discount,
                totalPrice = item.totalPrice,
                unitCost = item.unitCost
            )
            
            val itemResult = database["sale_items"]
                .insert(itemDto) { select() }
                .decodeSingle<SaleItemDTO>()
            
            saleItems.add(itemResult.toSaleItem())
        }
        
        val sale = saleResult.toSale(saleItems)
        _sales.value = listOf(sale) + _sales.value
        _todaySales.value = listOf(sale) + _todaySales.value
        return Result.success(sale)
    }
    
    // ============================================
    // QUICK SALE (Single Product)
    // ============================================
    
    suspend fun quickSale(
        businessId: String,
        product: Product,
        quantity: Double = 1.0,
        paymentMethod: String = "Cash"
    ): Result<Sale> {
        val cartItem = CartItem(
            type = SaleItemType.PRODUCT,
            productId = product.id,
            productName = product.name,
            quantity = quantity,
            unitPrice = product.sellingPrice,
            unitCost = product.costPrice
        )
        return createSale(
            businessId = businessId,
            cartItems = listOf(cartItem),
            paymentMethod = paymentMethod
        )
    }
    
    // ============================================
    // ANALYTICS
    // ============================================
    
    fun getTodayTotalSales(): Double {
        return _todaySales.value.sumOf { it.totalAmount }
    }
    
    fun getTodaySalesCount(): Int {
        return _todaySales.value.size
    }
    
    fun getTodayProfit(): Double {
        return _todaySales.value.sumOf { it.profit }
    }
    
    fun getSalesByPaymentMethod(): Map<String, Double> {
        return _todaySales.value
            .groupBy { it.paymentMethod }
            .mapValues { entry -> entry.value.sumOf { it.totalAmount } }
    }
    
    // ============================================
    // HELPERS
    // ============================================
    
    fun clearData() {
        _sales.value = emptyList()
        _todaySales.value = emptyList()
    }
    
    // ============================================
    // DTO CONVERTERS
    // ============================================
    
    private fun SaleDTO.toSale(items: List<SaleItem> = emptyList()): Sale {
        return Sale(
            id = this.id ?: "",
            saleNumber = this.saleNumber ?: "",
            saleType = when (this.saleType) {
                "SERVICE" -> SaleType.SERVICE
                "MIXED" -> SaleType.MIXED
                else -> SaleType.PRODUCT
            },
            subtotal = this.subtotal,
            discountAmount = this.discountAmount,
            taxAmount = this.taxAmount,
            totalAmount = this.totalAmount,
            paymentMethod = this.paymentMethod,
            paymentReference = this.paymentReference,
            paymentStatus = when (this.paymentStatus) {
                "PENDING" -> PaymentStatus.PENDING
                "PARTIAL" -> PaymentStatus.PARTIAL
                else -> PaymentStatus.PAID
            },
            amountPaid = this.amountPaid ?: this.totalAmount,
            changeGiven = this.changeGiven,
            customerName = this.customerName,
            customerPhone = this.customerPhone,
            items = items,
            saleDate = parseTimestamp(this.saleDate),
            createdAt = parseTimestamp(this.createdAt)
        )
    }
    
    private fun SaleItemDTO.toSaleItem(): SaleItem {
        return SaleItem(
            id = this.id ?: "",
            saleId = this.saleId,
            itemType = if (this.itemType == "SERVICE") SaleItemType.SERVICE else SaleItemType.PRODUCT,
            productId = this.productId,
            productName = this.productName ?: "",
            serviceId = this.serviceId,
            serviceName = this.serviceName ?: "",
            providerId = this.providerId,
            providerName = this.providerName ?: "",
            quantity = this.quantity,
            unitPrice = this.unitPrice,
            discount = this.discount,
            totalPrice = this.totalPrice,
            unitCost = this.unitCost,
            commissionAmount = this.commissionAmount
        )
    }
    
    private fun parseTimestamp(timestamp: String?): Long {
        return try {
            timestamp?.let {
                // Handle formats like "2024-12-27 01:23:45+00" or ISO "2024-12-27T01:23:45Z"
                val isoFormatted = if (it.contains(" ") && !it.contains("T")) {
                    it.replace(" ", "T")
                } else it
                kotlinx.datetime.Instant.parse(isoFormatted).toEpochMilliseconds()
            } ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
