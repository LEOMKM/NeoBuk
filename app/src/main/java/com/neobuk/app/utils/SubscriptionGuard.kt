package com.neobuk.app.utils

import com.neobuk.app.data.models.SubscriptionStatus

sealed class GuardResult {
    object Allowed : GuardResult()
    data class Blocked(val reason: String) : GuardResult()
}

object SubscriptionGuard {
    fun checkAccess(status: SubscriptionStatus): GuardResult {
        return when (status) {
            SubscriptionStatus.ACTIVE -> GuardResult.Allowed
            SubscriptionStatus.TRIALING -> GuardResult.Allowed
            SubscriptionStatus.GRACE_PERIOD -> GuardResult.Allowed // Usually allowed during grace
            SubscriptionStatus.LOCKED -> GuardResult.Blocked("Subscription is locked. Please renew to continue.")
            SubscriptionStatus.PAST_DUE -> GuardResult.Blocked("Payment past due. Please update payment method.")
            SubscriptionStatus.CANCELED -> GuardResult.Blocked("Subscription canceled.")
        }
    }
}
