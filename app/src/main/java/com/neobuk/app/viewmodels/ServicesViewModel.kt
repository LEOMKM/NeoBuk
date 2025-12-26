package com.neobuk.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neobuk.app.data.models.CommissionType
import com.neobuk.app.data.models.ServiceDefinition
import com.neobuk.app.data.models.ServiceProvider
import com.neobuk.app.data.models.ServiceRecord
import com.neobuk.app.data.repositories.ServicesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Services functionality
 * Handles service providers, service definitions, and service records
 */
class ServicesViewModel(
    private val servicesRepository: ServicesRepository
) : ViewModel() {
    
    // Current business ID (set after login)
    private var currentBusinessId: String? = null
    
    // Expose repository state flows
    val serviceProviders: StateFlow<List<ServiceProvider>> = servicesRepository.serviceProviders
    val serviceDefinitions: StateFlow<List<ServiceDefinition>> = servicesRepository.serviceDefinitions
    val serviceRecords: StateFlow<List<ServiceRecord>> = servicesRepository.serviceRecords
    val isLoading: StateFlow<Boolean> = servicesRepository.isLoading
    
    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // ============================================
    // INITIALIZATION
    // ============================================
    
    fun setBusinessId(businessId: String) {
        currentBusinessId = businessId
        loadAllData()
    }
    
    private fun loadAllData() {
        val businessId = currentBusinessId ?: return
        viewModelScope.launch {
            servicesRepository.fetchServiceProviders(businessId)
            servicesRepository.fetchServiceDefinitions(businessId)
            servicesRepository.fetchServiceRecords(businessId)
        }
    }
    
    fun refreshData() {
        loadAllData()
    }
    
    // ============================================
    // SERVICE PROVIDERS (Staff)
    // ============================================
    
    fun createServiceProvider(
        fullName: String,
        role: String = "Service Provider",
        commissionType: CommissionType = CommissionType.PERCENTAGE,
        commissionRate: Double = 0.0,
        flatFee: Double = 0.0,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val businessId = currentBusinessId ?: run {
            onError("No business selected")
            return
        }
        
        viewModelScope.launch {
            servicesRepository.createServiceProvider(
                businessId = businessId,
                fullName = fullName,
                role = role,
                commissionType = commissionType,
                commissionRate = commissionRate,
                flatFee = flatFee
            ).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to create staff") }
            )
        }
    }
    
    fun updateServiceProvider(
        provider: ServiceProvider,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            servicesRepository.updateServiceProvider(provider).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to update staff") }
            )
        }
    }
    
    fun toggleServiceProviderActive(
        providerId: String,
        isActive: Boolean,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            servicesRepository.toggleServiceProviderActive(providerId, isActive).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to update staff status") }
            )
        }
    }
    
    // ============================================
    // SERVICE DEFINITIONS
    // ============================================
    
    fun createServiceDefinition(
        name: String,
        basePrice: Double,
        commissionOverride: Double? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val businessId = currentBusinessId ?: run {
            onError("No business selected")
            return
        }
        
        viewModelScope.launch {
            servicesRepository.createServiceDefinition(
                businessId = businessId,
                name = name,
                basePrice = basePrice,
                commissionOverride = commissionOverride
            ).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to create service") }
            )
        }
    }
    
    fun updateServiceDefinition(
        service: ServiceDefinition,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            servicesRepository.updateServiceDefinition(service).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to update service") }
            )
        }
    }
    
    fun toggleServiceDefinitionActive(
        serviceId: String,
        isActive: Boolean,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            servicesRepository.toggleServiceDefinitionActive(serviceId, isActive).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to update service status") }
            )
        }
    }
    
    // ============================================
    // SERVICE RECORDS (Transactions)
    // ============================================
    
    fun recordService(
        service: ServiceDefinition,
        provider: ServiceProvider,
        priceOverride: Double? = null,
        recordedBy: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val businessId = currentBusinessId ?: run {
            onError("No business selected")
            return
        }
        
        viewModelScope.launch {
            servicesRepository.createServiceRecord(
                businessId = businessId,
                service = service,
                provider = provider,
                priceOverride = priceOverride,
                recordedBy = recordedBy
            ).fold(
                onSuccess = { onSuccess() },
                onFailure = { onError(it.message ?: "Failed to record service") }
            )
        }
    }
    
    // ============================================
    // UTILITY
    // ============================================
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearData() {
        currentBusinessId = null
        servicesRepository.clearData()
    }
    
    // Get active providers only
    fun getActiveProviders(): List<ServiceProvider> {
        return serviceProviders.value.filter { it.isActive }
    }
    
    // Get active services only
    fun getActiveServices(): List<ServiceDefinition> {
        return serviceDefinitions.value.filter { it.isActive }
    }
    
    // Calculate today's revenue
    fun getTodayRevenue(): Double {
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        return serviceRecords.value
            .filter { it.dateOffered >= today }
            .sumOf { it.servicePrice }
    }
    
    // Calculate today's commission
    fun getTodayCommission(): Double {
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        return serviceRecords.value
            .filter { it.dateOffered >= today }
            .sumOf { it.commissionAmount }
    }
}
