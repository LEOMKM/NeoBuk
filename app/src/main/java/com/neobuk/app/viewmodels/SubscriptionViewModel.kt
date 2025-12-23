package com.neobuk.app.viewmodels

import androidx.lifecycle.ViewModel
import com.neobuk.app.data.models.PlanType
import com.neobuk.app.data.models.Subscription
import com.neobuk.app.data.models.SubscriptionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar
import java.util.Date
import java.util.UUID

class SubscriptionViewModel : ViewModel() {

    private val _subscription = MutableStateFlow<Subscription?>(null)
    val subscription: StateFlow<Subscription?> = _subscription.asStateFlow()

    private val _status = MutableStateFlow(SubscriptionStatus.TRIALING)
    val status: StateFlow<SubscriptionStatus> = _status.asStateFlow()

    init {
        // Initialize with a default Trial Active subscription
        initializeTrial()
    }

    private fun initializeTrial() {
        val today = Date()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 30) // 30 day trial
        val trialEnd = cal.time

        val mockSub = Subscription(
            id = UUID.randomUUID().toString(),
            businessId = "mock-business-id",
            planType = PlanType.FREE_TRIAL,
            price = 0.0,
            status = SubscriptionStatus.TRIALING,
            trialStart = today,
            trialEnd = trialEnd,
            currentPeriodStart = today,
            currentPeriodEnd = trialEnd
        )
        _subscription.value = mockSub
        _status.value = SubscriptionStatus.TRIALING
    }

    // --- Dev Tools for Testing ---

    fun simulateActiveTrial() {
        initializeTrial()
    }

    fun simulateGracePeriod() {
        // Trial ended 2 days ago
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -2)
        val end = cal.time
        
        val sub = _subscription.value?.copy(
            status = SubscriptionStatus.GRACE_PERIOD,
            trialEnd = end,
            currentPeriodEnd = end
        )
        if (sub != null) {
            _subscription.value = sub
            _status.value = SubscriptionStatus.GRACE_PERIOD
        }
    }

    fun simulateLocked() {
        // Trial ended 10 days ago
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -10)
        val end = cal.time

        val sub = _subscription.value?.copy(
            status = SubscriptionStatus.LOCKED,
            trialEnd = end,
            currentPeriodEnd = end
        )
        if (sub != null) {
            _subscription.value = sub
            _status.value = SubscriptionStatus.LOCKED
        }
    }

    fun upgradeToActive() {
        val today = Date()
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 1)
        val end = cal.time

        val sub = _subscription.value?.copy(
            planType = PlanType.MONTHLY,
            status = SubscriptionStatus.ACTIVE,
            price = 249.0,
            trialEnd = null, // Trial over or converted
            currentPeriodStart = today,
            currentPeriodEnd = end
        )
        if (sub != null) {
            _subscription.value = sub
            _status.value = SubscriptionStatus.ACTIVE
        }
    }

    fun upgradeToYearly() {
        val today = Date()
        val cal = Calendar.getInstance()
        cal.add(Calendar.YEAR, 1)
        val end = cal.time

        val sub = _subscription.value?.copy(
            planType = PlanType.YEARLY,
            status = SubscriptionStatus.ACTIVE,
            price = 2490.0, // Example: 249 * 10 (Save 2 months)
            trialEnd = null,
            currentPeriodStart = today,
            currentPeriodEnd = end
        )
        if (sub != null) {
            _subscription.value = sub
            _status.value = SubscriptionStatus.ACTIVE
        }
    }
}
