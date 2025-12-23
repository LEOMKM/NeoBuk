package com.neobuk.app.data.models

import java.util.UUID

/**
 * Service Provider (Staff who render services)
 * Maintained under More → Staff & Roles
 */
data class ServiceProvider(
    val id: String = UUID.randomUUID().toString(),
    val fullName: String,
    val role: String = "Service Provider",
    val commissionType: CommissionType = CommissionType.PERCENTAGE,
    val commissionRate: Double = 0.0, // e.g., 30.0 for 30%
    val flatFee: Double = 0.0, // For flat fee commission type
    val isActive: Boolean = true
)

enum class CommissionType {
    PERCENTAGE,
    FLAT_FEE
}

/**
 * Service Definition (Services offered by the business)
 * Maintained under Services → Manage Services
 */
data class ServiceDefinition(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val basePrice: Double,
    val commissionOverride: Double? = null, // Optional override for this service
    val isActive: Boolean = true
)

/**
 * Service Record (A service rendered - the transaction)
 * This captures a SNAPSHOT at time of recording for audit safety
 */
data class ServiceRecord(
    val id: String = UUID.randomUUID().toString(),
    
    // Snapshot values (copied, not referenced)
    val serviceName: String,
    val serviceProviderName: String,
    val servicePrice: Double,
    
    // Commission details (locked at time of recording)
    val commissionRateUsed: Double, // The rate that was applied
    val commissionAmount: Double,   // Amount earned by provider
    val businessAmount: Double,     // Amount retained by business
    
    // Metadata
    val dateOffered: Long = System.currentTimeMillis(),
    val recordedBy: String? = null,
    
    // References (for linking, but values are snapshotted above)
    val serviceId: String,
    val providerId: String
) {
    companion object {
        /**
         * Factory method to create a ServiceRecord with automatic commission calculation
         */
        fun create(
            service: ServiceDefinition,
            provider: ServiceProvider,
            priceOverride: Double? = null,
            recordedBy: String? = null
        ): ServiceRecord {
            val finalPrice = priceOverride ?: service.basePrice
            
            // Determine commission rate (service override takes precedence)
            val commissionRate = service.commissionOverride ?: provider.commissionRate
            
            // Calculate commission based on type
            val commissionAmount = when (provider.commissionType) {
                CommissionType.PERCENTAGE -> finalPrice * (commissionRate / 100.0)
                CommissionType.FLAT_FEE -> provider.flatFee
            }
            
            val businessAmount = finalPrice - commissionAmount
            
            return ServiceRecord(
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
        }
    }
}
