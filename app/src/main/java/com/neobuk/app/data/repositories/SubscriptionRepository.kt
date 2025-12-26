package com.neobuk.app.data.repositories

import com.neobuk.app.data.models.PlanType
import com.neobuk.app.data.models.SubscriptionStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for Subscription
 * Maps to the `subscriptions` table in Supabase
 */
@Serializable
data class SubscriptionDTO(
    val id: String? = null,
    @SerialName("business_id") val businessId: String,
    @SerialName("plan_type") val planType: String,
    val price: Double = 0.0,
    val currency: String = "KES",
    val status: String = "TRIALING",
    @SerialName("trial_start") val trialStart: String? = null,
    @SerialName("trial_end") val trialEnd: String? = null,
    @SerialName("current_period_start") val currentPeriodStart: String,
    @SerialName("current_period_end") val currentPeriodEnd: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

/**
 * Domain model for Subscription (used in app)
 */
data class SubscriptionInfo(
    val id: String,
    val businessId: String,
    val planType: PlanType,
    val price: Double,
    val currency: String,
    val status: SubscriptionStatus,
    val trialStart: Instant?,
    val trialEnd: Instant?,
    val currentPeriodStart: Instant,
    val currentPeriodEnd: Instant
)

/**
 * Subscription Repository
 * Handles subscription creation, fetching, and status management
 */
class SubscriptionRepository(private val supabaseClient: SupabaseClient) {
    
    private val database = supabaseClient.postgrest
    
    // Current subscription state
    private val _currentSubscription = MutableStateFlow<SubscriptionInfo?>(null)
    val currentSubscription: StateFlow<SubscriptionInfo?> = _currentSubscription.asStateFlow()
    
    // Computed effective status (considering trial expiry)
    private val _effectiveStatus = MutableStateFlow(SubscriptionStatus.TRIALING)
    val effectiveStatus: StateFlow<SubscriptionStatus> = _effectiveStatus.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Create a new subscription (trial or paid)
     * 
     * @param businessId ID of the business
     * @param planType FREE_TRIAL, MONTHLY, or YEARLY
     * @return Result with created subscription on success
     */
    suspend fun createSubscription(
        businessId: String,
        planType: PlanType
    ): Result<SubscriptionInfo> {
        return try {
            _isLoading.value = true
            
            val now = Clock.System.now()
            val periodEnd = calculatePeriodEnd(now, planType)
            
            val dto = SubscriptionDTO(
                businessId = businessId,
                planType = planType.name,
                price = when (planType) {
                    PlanType.FREE_TRIAL -> 0.0
                    PlanType.MONTHLY -> 249.0
                    PlanType.YEARLY -> 2490.0
                },
                status = if (planType == PlanType.FREE_TRIAL) "TRIALING" else "ACTIVE",
                trialStart = if (planType == PlanType.FREE_TRIAL) now.toString() else null,
                trialEnd = if (planType == PlanType.FREE_TRIAL) periodEnd.toString() else null,
                currentPeriodStart = now.toString(),
                currentPeriodEnd = periodEnd.toString()
            )
            
            val result = database["subscriptions"]
                .insert(dto) {
                    select()
                }
                .decodeSingle<SubscriptionDTO>()
            
            val subscription = result.toSubscriptionInfo()
            _currentSubscription.value = subscription
            updateEffectiveStatus(subscription)
            
            Result.success(subscription)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create subscription: ${e.message}"))
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Fetch subscription for a specific business
     * 
     * @param businessId ID of the business
     * @return Subscription if found, null otherwise
     */
    suspend fun fetchSubscriptionByBusiness(businessId: String): SubscriptionInfo? {
        return try {
            _isLoading.value = true
            
            val result = database["subscriptions"]
                .select {
                    filter {
                        eq("business_id", businessId)
                    }
                }
                .decodeSingleOrNull<SubscriptionDTO>()
            
            result?.toSubscriptionInfo()?.also {
                _currentSubscription.value = it
                updateEffectiveStatus(it)
            }
        } catch (e: Exception) {
            null
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Calculate period end date based on plan type
     */
    private fun calculatePeriodEnd(start: Instant, planType: PlanType): Instant {
        val startDate = start.toLocalDateTime(TimeZone.currentSystemDefault())
        
        val endDate = when (planType) {
            PlanType.FREE_TRIAL -> startDate.date.plus(30, DateTimeUnit.DAY)
            PlanType.MONTHLY -> startDate.date.plus(1, DateTimeUnit.MONTH)
            PlanType.YEARLY -> startDate.date.plus(1, DateTimeUnit.YEAR)
        }
        
        return endDate.atStartOfDayIn(TimeZone.currentSystemDefault())
    }
    
    /**
     * Update the effective subscription status
     * This takes into account trial expiry and grace periods
     */
    private fun updateEffectiveStatus(subscription: SubscriptionInfo) {
        val now = Clock.System.now()
        
        val effectiveStatus = when (subscription.status) {
            SubscriptionStatus.TRIALING -> {
                val trialEnd = subscription.trialEnd
                if (trialEnd != null) {
                    when {
                        now < trialEnd -> SubscriptionStatus.TRIALING
                        now < trialEnd.plus(5, DateTimeUnit.DAY, TimeZone.currentSystemDefault()) -> 
                            SubscriptionStatus.GRACE_PERIOD
                        else -> SubscriptionStatus.LOCKED
                    }
                } else {
                    SubscriptionStatus.TRIALING
                }
            }
            SubscriptionStatus.ACTIVE -> {
                if (now > subscription.currentPeriodEnd) {
                    SubscriptionStatus.PAST_DUE
                } else {
                    SubscriptionStatus.ACTIVE
                }
            }
            else -> subscription.status
        }
        
        _effectiveStatus.value = effectiveStatus
    }
    
    /**
     * Check if user can perform actions (not locked)
     */
    fun canPerformActions(): Boolean {
        return _effectiveStatus.value != SubscriptionStatus.LOCKED
    }
    
    /**
     * Check if user is in trial
     */
    fun isTrialing(): Boolean {
        return _effectiveStatus.value == SubscriptionStatus.TRIALING
    }
    
    /**
     * Get days remaining in trial
     */
    fun getTrialDaysRemaining(): Int {
        val subscription = _currentSubscription.value ?: return 0
        val trialEnd = subscription.trialEnd ?: return 0
        val now = Clock.System.now()
        
        if (now >= trialEnd) return 0
        
        val remaining = trialEnd - now
        return remaining.inWholeDays.toInt()
    }
    
    /**
     * Clear subscription state (on logout)
     */
    fun clearCurrentSubscription() {
        _currentSubscription.value = null
        _effectiveStatus.value = SubscriptionStatus.TRIALING
    }
    
    /**
     * Convert DTO to domain model
     */
    private fun SubscriptionDTO.toSubscriptionInfo(): SubscriptionInfo {
        return SubscriptionInfo(
            id = this.id ?: "",
            businessId = this.businessId,
            planType = try {
                PlanType.valueOf(this.planType)
            } catch (e: Exception) {
                PlanType.FREE_TRIAL
            },
            price = this.price,
            currency = this.currency,
            status = try {
                SubscriptionStatus.valueOf(this.status)
            } catch (e: Exception) {
                SubscriptionStatus.TRIALING
            },
            trialStart = this.trialStart?.let { Instant.parse(it) },
            trialEnd = this.trialEnd?.let { Instant.parse(it) },
            currentPeriodStart = Instant.parse(this.currentPeriodStart),
            currentPeriodEnd = Instant.parse(this.currentPeriodEnd)
        )
    }
}
