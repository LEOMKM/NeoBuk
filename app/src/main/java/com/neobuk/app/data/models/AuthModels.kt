package com.neobuk.app.data.models

import java.util.Date

/**
 * Clean data models for the Signup flow and Auth domain.
 */

data class User(
    val id: String,
    val fullName: String,
    val email: String,     // primary contact
    val phone: String,     // optional or alternative
    val passwordHash: String, // In real app, never store plain text
    val acceptedTermsAt: Date,
    val createdAt: Date = Date()
)

data class Business(
    val id: String,
    val ownerUserId: String,
    val businessName: String,
    val category: BusinessCategory,
    val subtype: String, // e.g. "Salon", "Kinyozi", "Retail"
    val createdAt: Date = Date()
)

enum class BusinessCategory {
    SERVICES,
    PRODUCTS
}

data class Subscription(
    val id: String,
    val businessId: String,
    val planType: PlanType,
    val price: Double,
    val currency: String = "KES",
    val status: SubscriptionStatus,
    val trialStart: Date? = null,
    val trialEnd: Date? = null, // Logic: +1 month if trial
    val currentPeriodStart: Date,
    val currentPeriodEnd: Date
)

enum class PlanType {
    FREE_TRIAL,
    MONTHLY,
    YEARLY
}

enum class SubscriptionStatus {
    TRIALING,
    ACTIVE,
    PAST_DUE, // Failed payment on active sub
    GRACE_PERIOD, // Trial finished, 3-5 days buffer
    LOCKED, // Trial/Sub fully expired, access restricted
    CANCELED
}

data class Payment(
    val id: String,
    val subscriptionId: String,
    val provider: PaymentProvider, // M-PESA, CHAPA, PAYSTACK
    val amount: Double,
    val reference: String,
    val status: PaymentStatus,
    val paidAt: Date? = null
)

enum class PaymentProvider {
    MPESA,
    CHAPA,
    PAYSTACK
}

enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED
}
