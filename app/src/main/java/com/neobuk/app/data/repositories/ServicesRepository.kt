package com.neobuk.app.data.repositories

import com.neobuk.app.data.models.CommissionType
import com.neobuk.app.data.models.ServiceDefinition
import com.neobuk.app.data.models.ServiceProvider
import com.neobuk.app.data.models.ServiceRecord
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============================================
// DTOs for Supabase
// ============================================

@Serializable
data class ServiceProviderDTO(
    val id: String? = null,
    @SerialName("business_id") val businessId: String,
    @SerialName("full_name") val fullName: String,
    val role: String = "Service Provider",
    @SerialName("commission_type") val commissionType: String = "PERCENTAGE",
    @SerialName("commission_rate") val commissionRate: Double = 0.0,
    @SerialName("flat_fee") val flatFee: Double = 0.0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class ServiceDefinitionDTO(
    val id: String? = null,
    @SerialName("business_id") val businessId: String,
    val name: String,
    @SerialName("base_price") val basePrice: Double,
    @SerialName("commission_override") val commissionOverride: Double? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class ServiceRecordDTO(
    val id: String? = null,
    @SerialName("business_id") val businessId: String,
    @SerialName("service_name") val serviceName: String,
    @SerialName("service_provider_name") val serviceProviderName: String,
    @SerialName("service_price") val servicePrice: Double,
    @SerialName("commission_rate_used") val commissionRateUsed: Double,
    @SerialName("commission_amount") val commissionAmount: Double,
    @SerialName("business_amount") val businessAmount: Double,
    @SerialName("date_offered") val dateOffered: String? = null,
    @SerialName("recorded_by") val recordedBy: String? = null,
    @SerialName("service_id") val serviceId: String? = null,
    @SerialName("provider_id") val providerId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// ============================================
// SERVICES REPOSITORY
// ============================================

class ServicesRepository(private val supabaseClient: SupabaseClient) {
    
    private val database = supabaseClient.postgrest
    
    // State holders
    private val _serviceProviders = MutableStateFlow<List<ServiceProvider>>(emptyList())
    val serviceProviders: StateFlow<List<ServiceProvider>> = _serviceProviders.asStateFlow()
    
    private val _serviceDefinitions = MutableStateFlow<List<ServiceDefinition>>(emptyList())
    val serviceDefinitions: StateFlow<List<ServiceDefinition>> = _serviceDefinitions.asStateFlow()
    
    private val _serviceRecords = MutableStateFlow<List<ServiceRecord>>(emptyList())
    val serviceRecords: StateFlow<List<ServiceRecord>> = _serviceRecords.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // ============================================
    // SERVICE PROVIDERS (Staff)
    // ============================================
    
    suspend fun fetchServiceProviders(businessId: String): List<ServiceProvider> {
        return try {
            _isLoading.value = true
            val result = database["service_providers"]
                .select {
                    filter { eq("business_id", businessId) }
                }
                .decodeList<ServiceProviderDTO>()
            
            val providers = result.map { it.toServiceProvider() }
            _serviceProviders.value = providers
            providers
        } catch (e: Exception) {
            emptyList()
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun createServiceProvider(
        businessId: String,
        fullName: String,
        role: String,
        commissionType: CommissionType,
        commissionRate: Double,
        flatFee: Double
    ): Result<ServiceProvider> {
        return try {
            _isLoading.value = true
            val dto = ServiceProviderDTO(
                businessId = businessId,
                fullName = fullName,
                role = role,
                commissionType = commissionType.name,
                commissionRate = commissionRate,
                flatFee = flatFee
            )
            
            val result = database["service_providers"]
                .insert(dto) { select() }
                .decodeSingle<ServiceProviderDTO>()
            
            val provider = result.toServiceProvider()
            _serviceProviders.value = _serviceProviders.value + provider
            Result.success(provider)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create staff: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateServiceProvider(provider: ServiceProvider): Result<ServiceProvider> {
        return try {
            _isLoading.value = true
            val updates = mapOf(
                "full_name" to provider.fullName,
                "role" to provider.role,
                "commission_type" to provider.commissionType.name,
                "commission_rate" to provider.commissionRate,
                "flat_fee" to provider.flatFee,
                "is_active" to provider.isActive
            )
            
            database["service_providers"]
                .update(updates) {
                    filter { eq("id", provider.id) }
                }
            
            _serviceProviders.value = _serviceProviders.value.map {
                if (it.id == provider.id) provider else it
            }
            Result.success(provider)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update staff: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun toggleServiceProviderActive(providerId: String, isActive: Boolean): Result<Unit> {
        return try {
            database["service_providers"]
                .update(mapOf("is_active" to isActive)) {
                    filter { eq("id", providerId) }
                }
            
            _serviceProviders.value = _serviceProviders.value.map {
                if (it.id == providerId) it.copy(isActive = isActive) else it
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update staff status: ${e.message}"))
        }
    }
    
    // ============================================
    // SERVICE DEFINITIONS
    // ============================================
    
    suspend fun fetchServiceDefinitions(businessId: String): List<ServiceDefinition> {
        return try {
            _isLoading.value = true
            val result = database["service_definitions"]
                .select {
                    filter { eq("business_id", businessId) }
                }
                .decodeList<ServiceDefinitionDTO>()
            
            val services = result.map { it.toServiceDefinition() }
            _serviceDefinitions.value = services
            services
        } catch (e: Exception) {
            emptyList()
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun createServiceDefinition(
        businessId: String,
        name: String,
        basePrice: Double,
        commissionOverride: Double?
    ): Result<ServiceDefinition> {
        return try {
            _isLoading.value = true
            val dto = ServiceDefinitionDTO(
                businessId = businessId,
                name = name,
                basePrice = basePrice,
                commissionOverride = commissionOverride
            )
            
            val result = database["service_definitions"]
                .insert(dto) { select() }
                .decodeSingle<ServiceDefinitionDTO>()
            
            val service = result.toServiceDefinition()
            _serviceDefinitions.value = _serviceDefinitions.value + service
            Result.success(service)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create service: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun updateServiceDefinition(service: ServiceDefinition): Result<ServiceDefinition> {
        return try {
            _isLoading.value = true
            val updates = buildMap<String, Any?> {
                put("name", service.name)
                put("base_price", service.basePrice)
                put("commission_override", service.commissionOverride)
                put("is_active", service.isActive)
            }
            
            database["service_definitions"]
                .update(updates) {
                    filter { eq("id", service.id) }
                }
            
            _serviceDefinitions.value = _serviceDefinitions.value.map {
                if (it.id == service.id) service else it
            }
            Result.success(service)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update service: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun toggleServiceDefinitionActive(serviceId: String, isActive: Boolean): Result<Unit> {
        return try {
            database["service_definitions"]
                .update(mapOf("is_active" to isActive)) {
                    filter { eq("id", serviceId) }
                }
            
            _serviceDefinitions.value = _serviceDefinitions.value.map {
                if (it.id == serviceId) it.copy(isActive = isActive) else it
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update service status: ${e.message}"))
        }
    }
    
    // ============================================
    // SERVICE RECORDS (Transactions)
    // ============================================
    
    suspend fun fetchServiceRecords(businessId: String, limit: Int = 100): List<ServiceRecord> {
        return try {
            _isLoading.value = true
            val result = database["service_records"]
                .select {
                    filter { eq("business_id", businessId) }
                    order("date_offered", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(limit.toLong())
                }
                .decodeList<ServiceRecordDTO>()
            
            val records = result.map { it.toServiceRecord() }
            _serviceRecords.value = records
            records
        } catch (e: Exception) {
            emptyList()
        } finally {
            _isLoading.value = false
        }
    }
    
    suspend fun createServiceRecord(
        businessId: String,
        service: ServiceDefinition,
        provider: ServiceProvider,
        priceOverride: Double? = null,
        recordedBy: String? = null
    ): Result<ServiceRecord> {
        return try {
            _isLoading.value = true
            
            val finalPrice = priceOverride ?: service.basePrice
            val commissionRate = service.commissionOverride ?: provider.commissionRate
            
            val commissionAmount = when (provider.commissionType) {
                CommissionType.PERCENTAGE -> finalPrice * (commissionRate / 100.0)
                CommissionType.FLAT_FEE -> provider.flatFee
            }
            val businessAmount = finalPrice - commissionAmount
            
            val dto = ServiceRecordDTO(
                businessId = businessId,
                serviceName = service.name,
                serviceProviderName = provider.fullName,
                servicePrice = finalPrice,
                commissionRateUsed = commissionRate,
                commissionAmount = commissionAmount,
                businessAmount = businessAmount,
                recordedBy = recordedBy,
                serviceId = service.id,
                providerId = provider.id
            )
            
            val result = database["service_records"]
                .insert(dto) { select() }
                .decodeSingle<ServiceRecordDTO>()
            
            val record = result.toServiceRecord()
            _serviceRecords.value = listOf(record) + _serviceRecords.value
            Result.success(record)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to record service: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    // ============================================
    // HELPERS
    // ============================================
    
    fun clearData() {
        _serviceProviders.value = emptyList()
        _serviceDefinitions.value = emptyList()
        _serviceRecords.value = emptyList()
    }
    
    // ============================================
    // DTO CONVERTERS
    // ============================================
    
    private fun ServiceProviderDTO.toServiceProvider(): ServiceProvider {
        return ServiceProvider(
            id = this.id ?: "",
            fullName = this.fullName,
            role = this.role,
            commissionType = try {
                CommissionType.valueOf(this.commissionType)
            } catch (e: Exception) {
                CommissionType.PERCENTAGE
            },
            commissionRate = this.commissionRate,
            flatFee = this.flatFee,
            isActive = this.isActive
        )
    }
    
    private fun ServiceDefinitionDTO.toServiceDefinition(): ServiceDefinition {
        return ServiceDefinition(
            id = this.id ?: "",
            name = this.name,
            basePrice = this.basePrice,
            commissionOverride = this.commissionOverride,
            isActive = this.isActive
        )
    }
    
    private fun ServiceRecordDTO.toServiceRecord(): ServiceRecord {
        return ServiceRecord(
            id = this.id ?: "",
            serviceName = this.serviceName,
            serviceProviderName = this.serviceProviderName,
            servicePrice = this.servicePrice,
            commissionRateUsed = this.commissionRateUsed,
            commissionAmount = this.commissionAmount,
            businessAmount = this.businessAmount,
            dateOffered = parseTimestamp(this.dateOffered),
            recordedBy = this.recordedBy,
            serviceId = this.serviceId ?: "",
            providerId = this.providerId ?: ""
        )
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
