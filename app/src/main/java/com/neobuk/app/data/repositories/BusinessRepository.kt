package com.neobuk.app.data.repositories

import com.neobuk.app.data.models.BusinessCategory
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Business
 * Maps to the `businesses` table in Supabase
 */
@Serializable
data class BusinessDTO(
    val id: String? = null,
    @SerialName("owner_user_id") val ownerUserId: String,
    @SerialName("business_name") val businessName: String,
    val category: String,
    val subtype: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Domain model for Business (used in app)
 */
data class Business(
    val id: String,
    val ownerUserId: String,
    val businessName: String,
    val category: BusinessCategory,
    val subtype: String
)

/**
 * Business Repository
 * Handles CRUD operations for businesses in Supabase
 */
class BusinessRepository(private val supabaseClient: SupabaseClient) {
    
    private val database = supabaseClient.postgrest
    
    // Current business state
    private val _currentBusiness = MutableStateFlow<Business?>(null)
    val currentBusiness: StateFlow<Business?> = _currentBusiness.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Create a new business
     * 
     * @param ownerUserId ID of the user who owns this business
     * @param businessName Name of the business
     * @param category SERVICES or PRODUCTS
     * @param subtype Specific type (e.g., "Salon", "Retail Shop")
     * @return Result with created business on success
     */
    suspend fun createBusiness(
        ownerUserId: String,
        businessName: String,
        category: BusinessCategory,
        subtype: String
    ): Result<Business> {
        return try {
            _isLoading.value = true
            
            val dto = BusinessDTO(
                ownerUserId = ownerUserId,
                businessName = businessName,
                category = category.name,
                subtype = subtype
            )
            
            val result = database["businesses"]
                .insert(dto) {
                    select()
                }
                .decodeSingle<BusinessDTO>()
            
            val business = result.toBusiness()
            _currentBusiness.value = business
            
            Result.success(business)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create business: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Fetch the business for a specific user
     * 
     * @param userId ID of the user
     * @return Business if found, null otherwise
     */
    suspend fun fetchBusinessByOwner(userId: String): Business? {
        return try {
            _isLoading.value = true
            
            val result = database["businesses"]
                .select {
                    filter {
                        eq("owner_user_id", userId)
                    }
                }
                .decodeSingleOrNull<BusinessDTO>()
            
            result?.toBusiness()?.also {
                _currentBusiness.value = it
            }
        } catch (e: Exception) {
            null
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Update business details
     * 
     * @param businessId ID of the business to update
     * @param businessName New name (optional)
     * @param subtype New subtype (optional)
     * @return Result with updated business on success
     */
    suspend fun updateBusiness(
        businessId: String,
        businessName: String? = null,
        subtype: String? = null
    ): Result<Business> {
        return try {
            _isLoading.value = true
            
            val updates = buildMap<String, Any> {
                businessName?.let { put("business_name", it) }
                subtype?.let { put("subtype", it) }
            }
            
            if (updates.isEmpty()) {
                return Result.failure(Exception("No updates provided"))
            }
            
            val result = database["businesses"]
                .update(updates) {
                    select()
                    filter {
                        eq("id", businessId)
                    }
                }
                .decodeSingle<BusinessDTO>()
            
            val business = result.toBusiness()
            _currentBusiness.value = business
            
            Result.success(business)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update business: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Clear the current business (on logout)
     */
    fun clearCurrentBusiness() {
        _currentBusiness.value = null
    }
    
    /**
     * Convert DTO to domain model
     */
    private fun BusinessDTO.toBusiness(): Business {
        return Business(
            id = this.id ?: "",
            ownerUserId = this.ownerUserId,
            businessName = this.businessName,
            category = try {
                BusinessCategory.valueOf(this.category)
            } catch (e: Exception) {
                BusinessCategory.SERVICES
            },
            subtype = this.subtype ?: ""
        )
    }
}
