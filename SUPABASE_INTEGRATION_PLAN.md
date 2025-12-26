# NeoBuk Supabase Integration Plan

> **Strategy**: Supabase-First, Flow-by-Flow Integration  
> **Created**: December 25, 2024  
> **Philosophy**: *"Supabase is the Constitution. Offline is the Write-Ahead Buffer."*

---

## 📋 Executive Summary

This plan outlines the phased integration of Supabase into NeoBuk, following the Supabase-first approach. Each flow will be implemented in isolation, tested online, and then prepared for offline support (Phase 2).

### Current State Analysis
- **Existing Architecture**: Mock data in-memory (ViewModels + Repositories with `MutableStateFlow`)
- **No Backend**: All data is ephemeral and lost on app restart
- **Models Defined**: `User`, `Business`, `Subscription`, `Product`, `ServiceProvider`, `ServiceDefinition`, `ServiceRecord`, `Expense`
- **No Supabase Dependencies**: Fresh integration needed

---

## 🔧 Phase 0: Foundation Setup (Do This First)

### 0.1 Add Supabase Dependencies

**File**: `app/build.gradle.kts`

```kotlin
dependencies {
    // ... existing dependencies
    
    // Supabase Kotlin Client
    implementation(platform("io.github.jan-tennert.supabase:bom:3.1.4"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    
    // Ktor Client (required by Supabase)
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-utils:2.3.12")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

plugins {
    // Add serialization plugin
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24"
}
```

### 0.2 Create Supabase Client Configuration

**New File**: `app/src/main/java/com/neobuk/app/data/SupabaseClient.kt`

```kotlin
package com.neobuk.app.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
    }
}
```

### 0.3 Store Credentials Securely

**File**: `local.properties` (DO NOT COMMIT)

```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key-here
```

**File**: `app/build.gradle.kts` (add to `defaultConfig`)

```kotlin
defaultConfig {
    // ... existing config
    
    // Read from local.properties
    val properties = Properties()
    properties.load(project.rootProject.file("local.properties").inputStream())
    
    buildConfigField("String", "SUPABASE_URL", "\"${properties["SUPABASE_URL"]}\"")
    buildConfigField("String", "SUPABASE_ANON_KEY", "\"${properties["SUPABASE_ANON_KEY"]}\"")
}

buildFeatures {
    compose = true
    buildConfig = true  // Enable BuildConfig generation
}
```

---

## 🗄️ Phase 1: Database Schema (Supabase Dashboard)

### 1.1 Core Tables

Execute these SQL statements in the Supabase SQL Editor:

```sql
-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone TEXT,
    accepted_terms_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================
-- BUSINESSES TABLE
-- ============================================
CREATE TABLE businesses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    business_name TEXT NOT NULL,
    category TEXT NOT NULL CHECK (category IN ('SERVICES', 'PRODUCTS')),
    subtype TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================
-- SUBSCRIPTIONS TABLE
-- ============================================
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID REFERENCES businesses(id) ON DELETE CASCADE,
    plan_type TEXT NOT NULL CHECK (plan_type IN ('FREE_TRIAL', 'MONTHLY', 'YEARLY')),
    price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    currency TEXT NOT NULL DEFAULT 'KES',
    status TEXT NOT NULL CHECK (status IN ('TRIALING', 'ACTIVE', 'PAST_DUE', 'GRACE_PERIOD', 'LOCKED', 'CANCELED')),
    trial_start TIMESTAMPTZ,
    trial_end TIMESTAMPTZ,
    current_period_start TIMESTAMPTZ NOT NULL,
    current_period_end TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================
-- PRODUCTS TABLE (for Product-based businesses)
-- ============================================
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID REFERENCES businesses(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    description TEXT,
    category_id TEXT,
    barcode TEXT,
    unit TEXT NOT NULL DEFAULT 'pcs',
    cost_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    selling_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    quantity DECIMAL(10, 2) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(business_id, barcode)
);

-- ============================================
-- STOCK MOVEMENTS TABLE
-- ============================================
CREATE TABLE stock_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID REFERENCES businesses(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id) ON DELETE CASCADE,
    quantity_change DECIMAL(10, 2) NOT NULL,
    reason TEXT NOT NULL CHECK (reason IN ('MANUAL_ADD', 'SALE', 'ADJUSTMENT', 'RETURN', 'DAMAGE')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================
-- SERVICE PROVIDERS TABLE (Staff)
-- ============================================
CREATE TABLE service_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID REFERENCES businesses(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'Service Provider',
    commission_type TEXT NOT NULL DEFAULT 'PERCENTAGE' CHECK (commission_type IN ('PERCENTAGE', 'FLAT_FEE')),
    commission_rate DECIMAL(5, 2) NOT NULL DEFAULT 0,
    flat_fee DECIMAL(10, 2) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================
-- SERVICE DEFINITIONS TABLE
-- ============================================
CREATE TABLE service_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID REFERENCES businesses(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    commission_override DECIMAL(5, 2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================
-- SERVICE RECORDS TABLE (Transactions)
-- ============================================
CREATE TABLE service_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID REFERENCES businesses(id) ON DELETE CASCADE,
    
    -- Snapshot values (immutable after creation)
    service_name TEXT NOT NULL,
    service_provider_name TEXT NOT NULL,
    service_price DECIMAL(10, 2) NOT NULL,
    commission_rate_used DECIMAL(5, 2) NOT NULL,
    commission_amount DECIMAL(10, 2) NOT NULL,
    business_amount DECIMAL(10, 2) NOT NULL,
    
    -- References (for linking)
    service_id UUID REFERENCES service_definitions(id),
    provider_id UUID REFERENCES service_providers(id),
    
    -- Metadata
    recorded_by TEXT,
    date_offered TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Sync tracking
    sync_status TEXT NOT NULL DEFAULT 'SYNCED' CHECK (sync_status IN ('PENDING', 'SYNCED', 'FAILED'))
);

-- ============================================
-- SALES TABLE
-- ============================================
CREATE TABLE sales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID REFERENCES businesses(id) ON DELETE CASCADE,
    total_amount DECIMAL(10, 2) NOT NULL,
    customer_name TEXT,
    payment_method TEXT NOT NULL DEFAULT 'CASH',
    notes TEXT,
    sale_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Sync tracking
    sync_status TEXT NOT NULL DEFAULT 'SYNCED' CHECK (sync_status IN ('PENDING', 'SYNCED', 'FAILED'))
);

-- ============================================
-- SALE ITEMS TABLE
-- ============================================
CREATE TABLE sale_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sale_id UUID REFERENCES sales(id) ON DELETE CASCADE,
    product_id UUID REFERENCES products(id),
    product_name TEXT NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================
-- EXPENSE CATEGORIES TABLE
-- ============================================
CREATE TABLE expense_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID REFERENCES businesses(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    emoji TEXT NOT NULL DEFAULT '📦',
    color TEXT NOT NULL DEFAULT '#6B7280',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================
-- EXPENSES TABLE
-- ============================================
CREATE TABLE expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID REFERENCES businesses(id) ON DELETE CASCADE,
    amount DECIMAL(10, 2) NOT NULL,
    category_id UUID REFERENCES expense_categories(id),
    category_name TEXT NOT NULL,
    notes TEXT,
    expense_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Sync tracking
    sync_status TEXT NOT NULL DEFAULT 'SYNCED' CHECK (sync_status IN ('PENDING', 'SYNCED', 'FAILED'))
);

-- ============================================
-- DAY CLOSURES TABLE (Funga Siku)
-- ============================================
CREATE TABLE day_closures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID REFERENCES businesses(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    total_sales DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total_expenses DECIMAL(10, 2) NOT NULL DEFAULT 0,
    net DECIMAL(10, 2) NOT NULL DEFAULT 0,
    closed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(business_id, date)
);

-- ============================================
-- PAYMENTS TABLE (Subscription Payments)
-- ============================================
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID REFERENCES subscriptions(id) ON DELETE CASCADE,
    provider TEXT NOT NULL CHECK (provider IN ('MPESA', 'CHAPA', 'PAYSTACK')),
    amount DECIMAL(10, 2) NOT NULL,
    reference TEXT NOT NULL,
    status TEXT NOT NULL CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

### 1.2 Row Level Security (RLS)

```sql
-- Enable RLS on all tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE businesses ENABLE ROW LEVEL SECURITY;
ALTER TABLE subscriptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_movements ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_providers ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_definitions ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE sales ENABLE ROW LEVEL SECURITY;
ALTER TABLE sale_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE expense_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE expenses ENABLE ROW LEVEL SECURITY;
ALTER TABLE day_closures ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;

-- Users can only access their own data
CREATE POLICY "Users can view own data" ON users
    FOR ALL USING (auth.uid() = id);

-- Businesses owned by user
CREATE POLICY "Users can manage own businesses" ON businesses
    FOR ALL USING (owner_user_id = auth.uid());

-- Data scoped to user's business
CREATE POLICY "Business data access" ON products
    FOR ALL USING (business_id IN (SELECT id FROM businesses WHERE owner_user_id = auth.uid()));

CREATE POLICY "Business data access" ON subscriptions
    FOR ALL USING (business_id IN (SELECT id FROM businesses WHERE owner_user_id = auth.uid()));

CREATE POLICY "Business data access" ON service_providers
    FOR ALL USING (business_id IN (SELECT id FROM businesses WHERE owner_user_id = auth.uid()));

CREATE POLICY "Business data access" ON service_definitions
    FOR ALL USING (business_id IN (SELECT id FROM businesses WHERE owner_user_id = auth.uid()));

CREATE POLICY "Business data access" ON service_records
    FOR ALL USING (business_id IN (SELECT id FROM businesses WHERE owner_user_id = auth.uid()));

CREATE POLICY "Business data access" ON sales
    FOR ALL USING (business_id IN (SELECT id FROM businesses WHERE owner_user_id = auth.uid()));

CREATE POLICY "Business data access" ON sale_items
    FOR ALL USING (sale_id IN (
        SELECT id FROM sales WHERE business_id IN (
            SELECT id FROM businesses WHERE owner_user_id = auth.uid()
        )
    ));

CREATE POLICY "Business data access" ON expense_categories
    FOR ALL USING (business_id IN (SELECT id FROM businesses WHERE owner_user_id = auth.uid()));

CREATE POLICY "Business data access" ON expenses
    FOR ALL USING (business_id IN (SELECT id FROM businesses WHERE owner_user_id = auth.uid()));

CREATE POLICY "Business data access" ON day_closures
    FOR ALL USING (business_id IN (SELECT id FROM businesses WHERE owner_user_id = auth.uid()));

CREATE POLICY "Business data access" ON stock_movements
    FOR ALL USING (business_id IN (SELECT id FROM businesses WHERE owner_user_id = auth.uid()));

CREATE POLICY "Business data access" ON payments
    FOR ALL USING (subscription_id IN (
        SELECT id FROM subscriptions WHERE business_id IN (
            SELECT id FROM businesses WHERE owner_user_id = auth.uid()
        )
    ));
```

### 1.3 Indexes for Performance

```sql
-- Performance indexes
CREATE INDEX idx_products_business ON products(business_id);
CREATE INDEX idx_products_barcode ON products(business_id, barcode);
CREATE INDEX idx_sales_business_date ON sales(business_id, sale_date DESC);
CREATE INDEX idx_expenses_business_date ON expenses(business_id, expense_date DESC);
CREATE INDEX idx_service_records_business_date ON service_records(business_id, date_offered DESC);
CREATE INDEX idx_day_closures_business_date ON day_closures(business_id, date DESC);
```

---

## 🚀 Phase 2: Flow-by-Flow Integration

### Integration Order (Priority-Based)

| Priority | Flow | Screens Affected | Complexity | Dependencies |
|:--------:|:-----|:-----------------|:-----------|:-------------|
| 1 | **Authentication** | `LoginScreen`, `SignupScreen` | Medium | None |
| 2 | **Business Setup** | `SignupScreen` (Step 2) | Low | Auth |
| 3 | **Subscription** | `SubscriptionScreen`, `SubscriptionLockedScreen` | Medium | Auth, Business |
| 4 | **Services Flow** | `ServicesScreen`, `ManageServicesScreen` | High | Auth, Business |
| 5 | **Expenses Flow** | `ExpensesScreen`, `AllExpensesScreen` | Medium | Auth, Business |
| 6 | **Products Flow** | `ProductsScreen` | Medium | Auth, Business |
| 7 | **Sales Flow** | `NewSaleScreen`, `SalesHistoryScreen` | High | Auth, Business, Products |
| 8 | **Reports** | `ReportsScreen` | Medium | All Transactions |
| 9 | **Day Closure** | `HomeScreen` (Funga Siku) | Low | Sales, Expenses |
| 10 | **Dashboard** | `HomeScreen` | Low | All Data |

---

## 🔐 Flow 1: Authentication

### What This Flow Covers
- User registration (email + password)
- User login
- Session management
- Terms acceptance tracking

### Files to Create/Modify

#### 1.1 New: `data/repositories/AuthRepository.kt`

```kotlin
package com.neobuk.app.data.repositories

import com.neobuk.app.data.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthResult {
    data class Success(val userId: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}

object AuthRepository {
    private val supabase = SupabaseClient.client
    
    private val _authState = MutableStateFlow<AuthResult?>(null)
    val authState: StateFlow<AuthResult?> = _authState.asStateFlow()
    
    val currentUserId: String?
        get() = supabase.auth.currentUserOrNull()?.id
    
    suspend fun signUp(email: String, password: String, fullName: String): AuthResult {
        return try {
            _authState.value = AuthResult.Loading
            
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", fullName)
                }
            }
            
            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId != null) {
                // Insert into users table
                supabase.postgrest["users"].insert(mapOf(
                    "id" to userId,
                    "full_name" to fullName,
                    "email" to email
                ))
                
                AuthResult.Success(userId).also { _authState.value = it }
            } else {
                AuthResult.Error("Signup failed").also { _authState.value = it }
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error").also { _authState.value = it }
        }
    }
    
    suspend fun login(email: String, password: String): AuthResult {
        return try {
            _authState.value = AuthResult.Loading
            
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId != null) {
                AuthResult.Success(userId).also { _authState.value = it }
            } else {
                AuthResult.Error("Login failed").also { _authState.value = it }
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error").also { _authState.value = it }
        }
    }
    
    suspend fun logout() {
        supabase.auth.signOut()
        _authState.value = null
    }
    
    suspend fun checkSession(): Boolean {
        return supabase.auth.currentUserOrNull() != null
    }
}
```

#### 1.2 New: `viewmodels/AuthViewModel.kt`

```kotlin
package com.neobuk.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neobuk.app.data.repositories.AuthRepository
import com.neobuk.app.data.repositories.AuthResult
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    
    val authState: StateFlow<AuthResult?> = AuthRepository.authState
    
    fun signUp(email: String, password: String, fullName: String, onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            val result = AuthRepository.signUp(email, password, fullName)
            onResult(result)
        }
    }
    
    fun login(email: String, password: String, onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            val result = AuthRepository.login(email, password)
            onResult(result)
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            AuthRepository.logout()
        }
    }
    
    fun checkSession(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(AuthRepository.checkSession())
        }
    }
}
```

#### 1.3 Modify: `ui/screens/auth/LoginScreen.kt`

- Wire up to `AuthViewModel`
- Add loading states
- Handle error messages
- Navigate on success

#### 1.4 Modify: `ui/screens/auth/SignupScreen.kt`

- Wire up Step 1 to `AuthViewModel.signUp()`
- Save terms acceptance timestamp
- Handle errors gracefully

### Testing Checklist
- [ ] User can sign up with email/password
- [ ] User appears in Supabase Auth dashboard
- [ ] User record created in `users` table
- [ ] User can log in with credentials
- [ ] Session persists across app restarts
- [ ] Logout clears session

---

## 🏢 Flow 2: Business Setup

### What This Flow Covers
- Create business record after signup
- Business type selection (Services/Products)
- Link business to user

### Files to Create/Modify

#### 2.1 New: `data/repositories/BusinessRepository.kt`

```kotlin
package com.neobuk.app.data.repositories

import com.neobuk.app.data.SupabaseClient
import com.neobuk.app.data.models.Business
import com.neobuk.app.data.models.BusinessCategory
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class BusinessDTO(
    val id: String? = null,
    val owner_user_id: String,
    val business_name: String,
    val category: String,
    val subtype: String? = null
)

object BusinessRepository {
    private val supabase = SupabaseClient.client
    
    private val _currentBusiness = MutableStateFlow<Business?>(null)
    val currentBusiness: StateFlow<Business?> = _currentBusiness.asStateFlow()
    
    suspend fun createBusiness(
        ownerUserId: String,
        businessName: String,
        category: BusinessCategory,
        subtype: String?
    ): Result<Business> {
        return try {
            val dto = BusinessDTO(
                owner_user_id = ownerUserId,
                business_name = businessName,
                category = category.name,
                subtype = subtype
            )
            
            val result = supabase.postgrest["businesses"]
                .insert(dto)
                .decodeSingle<BusinessDTO>()
            
            val business = Business(
                id = result.id!!,
                ownerUserId = result.owner_user_id,
                businessName = result.business_name,
                category = BusinessCategory.valueOf(result.category),
                subtype = result.subtype ?: ""
            )
            
            _currentBusiness.value = business
            Result.success(business)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun fetchUserBusiness(userId: String): Business? {
        return try {
            val result = supabase.postgrest["businesses"]
                .select {
                    filter { eq("owner_user_id", userId) }
                }
                .decodeSingleOrNull<BusinessDTO>()
            
            result?.let {
                Business(
                    id = it.id!!,
                    ownerUserId = it.owner_user_id,
                    businessName = it.business_name,
                    category = BusinessCategory.valueOf(it.category),
                    subtype = it.subtype ?: ""
                )
            }.also { _currentBusiness.value = it }
        } catch (e: Exception) {
            null
        }
    }
}
```

#### 2.2 Modify: `ui/screens/auth/SignupScreen.kt` (BusinessSetupStep)

- Call `BusinessRepository.createBusiness()` on Step 2 completion
- Handle errors and show feedback

### Testing Checklist
- [ ] Business created after Step 2 completion
- [ ] Business linked to authenticated user
- [ ] Business type (Services/Products) correctly saved
- [ ] Business record visible in Supabase dashboard

---

## 💳 Flow 3: Subscription Management

### What This Flow Covers
- Create trial subscription on signup
- Check subscription status
- Display correct UI based on status
- Handle grace period and locked states

### Files to Create/Modify

#### 3.1 New: `data/repositories/SubscriptionRepository.kt`

```kotlin
package com.neobuk.app.data.repositories

import com.neobuk.app.data.SupabaseClient
import com.neobuk.app.data.models.*
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.*
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionDTO(
    val id: String? = null,
    val business_id: String,
    val plan_type: String,
    val price: Double,
    val currency: String = "KES",
    val status: String,
    val trial_start: String? = null,
    val trial_end: String? = null,
    val current_period_start: String,
    val current_period_end: String
)

object SubscriptionRepository {
    private val supabase = SupabaseClient.client
    
    private val _subscription = MutableStateFlow<Subscription?>(null)
    val subscription: StateFlow<Subscription?> = _subscription.asStateFlow()
    
    private val _status = MutableStateFlow(SubscriptionStatus.TRIALING)
    val status: StateFlow<SubscriptionStatus> = _status.asStateFlow()
    
    suspend fun createTrialSubscription(businessId: String): Result<Subscription> {
        return try {
            val now = Clock.System.now()
            val trialEnd = now.plus(30, DateTimeUnit.DAY, TimeZone.currentSystemDefault())
            
            val dto = SubscriptionDTO(
                business_id = businessId,
                plan_type = PlanType.FREE_TRIAL.name,
                price = 0.0,
                status = SubscriptionStatus.TRIALING.name,
                trial_start = now.toString(),
                trial_end = trialEnd.toString(),
                current_period_start = now.toString(),
                current_period_end = trialEnd.toString()
            )
            
            val result = supabase.postgrest["subscriptions"]
                .insert(dto)
                .decodeSingle<SubscriptionDTO>()
            
            val subscription = result.toSubscription()
            _subscription.value = subscription
            _status.value = subscription.status
            
            Result.success(subscription)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun fetchSubscription(businessId: String): Subscription? {
        return try {
            val result = supabase.postgrest["subscriptions"]
                .select {
                    filter { eq("business_id", businessId) }
                }
                .decodeSingleOrNull<SubscriptionDTO>()
            
            result?.toSubscription()?.also {
                _subscription.value = it
                _status.value = computeEffectiveStatus(it)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun computeEffectiveStatus(subscription: Subscription): SubscriptionStatus {
        val now = Date()
        
        return when (subscription.status) {
            SubscriptionStatus.TRIALING -> {
                subscription.trialEnd?.let { trialEnd ->
                    when {
                        now.before(trialEnd) -> SubscriptionStatus.TRIALING
                        now.after(Date(trialEnd.time + 5 * 24 * 60 * 60 * 1000)) -> SubscriptionStatus.LOCKED
                        else -> SubscriptionStatus.GRACE_PERIOD
                    }
                } ?: SubscriptionStatus.TRIALING
            }
            else -> subscription.status
        }
    }
    
    private fun SubscriptionDTO.toSubscription(): Subscription {
        // Convert DTO to domain model
        // ... implementation
    }
}
```

#### 3.2 Modify: `viewmodels/SubscriptionViewModel.kt`

- Replace mock data with `SubscriptionRepository` calls
- Implement `fetchSubscription()` on init
- Keep `simulate*()` methods for testing (dev only)

#### 3.3 Modify: `ui/screens/SubscriptionScreen.kt`

- Show real subscription data
- Handle upgrade flows

### Testing Checklist
- [ ] Trial subscription created on signup
- [ ] Subscription status correctly computed
- [ ] Grace period triggers after trial end
- [ ] Locked status blocks actions after grace period
- [ ] UI reflects correct subscription state

---

## 🛠️ Flow 4: Services Flow (For Service Businesses)

### What This Flow Covers
- Manage service definitions
- Manage service providers (staff)
- Record service transactions
- View service history

### Files to Create/Modify

#### 4.1 New: `data/repositories/ServicesRepository.kt`

```kotlin
package com.neobuk.app.data.repositories

import com.neobuk.app.data.SupabaseClient
import com.neobuk.app.data.models.*
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ServicesRepository {
    private val supabase = SupabaseClient.client
    
    // --- Service Definitions ---
    private val _serviceDefinitions = MutableStateFlow<List<ServiceDefinition>>(emptyList())
    val serviceDefinitions: StateFlow<List<ServiceDefinition>> = _serviceDefinitions.asStateFlow()
    
    suspend fun fetchServiceDefinitions(businessId: String) {
        try {
            val results = supabase.postgrest["service_definitions"]
                .select {
                    filter { eq("business_id", businessId) }
                }
                .decodeList<ServiceDefinitionDTO>()
            
            _serviceDefinitions.value = results.map { it.toServiceDefinition() }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    suspend fun createServiceDefinition(businessId: String, service: ServiceDefinition): Result<ServiceDefinition>
    suspend fun updateServiceDefinition(service: ServiceDefinition): Result<ServiceDefinition>
    suspend fun toggleServiceActive(serviceId: String): Result<Unit>
    
    // --- Service Providers (Staff) ---
    private val _serviceProviders = MutableStateFlow<List<ServiceProvider>>(emptyList())
    val serviceProviders: StateFlow<List<ServiceProvider>> = _serviceProviders.asStateFlow()
    
    suspend fun fetchServiceProviders(businessId: String)
    suspend fun createServiceProvider(businessId: String, provider: ServiceProvider): Result<ServiceProvider>
    suspend fun updateServiceProvider(provider: ServiceProvider): Result<ServiceProvider>
    suspend fun toggleProviderActive(providerId: String): Result<Unit>
    
    // --- Service Records (Transactions) ---
    private val _serviceRecords = MutableStateFlow<List<ServiceRecord>>(emptyList())
    val serviceRecords: StateFlow<List<ServiceRecord>> = _serviceRecords.asStateFlow()
    
    suspend fun fetchServiceRecords(businessId: String, limit: Int = 50)
    suspend fun createServiceRecord(businessId: String, record: ServiceRecord): Result<ServiceRecord>
}
```

#### 4.2 New: `viewmodels/ServicesViewModel.kt`

```kotlin
package com.neobuk.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neobuk.app.data.repositories.BusinessRepository
import com.neobuk.app.data.repositories.ServicesRepository
import com.neobuk.app.data.models.*
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServicesViewModel : ViewModel() {
    
    val serviceDefinitions: StateFlow<List<ServiceDefinition>> = ServicesRepository.serviceDefinitions
    val serviceProviders: StateFlow<List<ServiceProvider>> = ServicesRepository.serviceProviders
    val serviceRecords: StateFlow<List<ServiceRecord>> = ServicesRepository.serviceRecords
    
    private val businessId: String?
        get() = BusinessRepository.currentBusiness.value?.id
    
    init {
        refresh()
    }
    
    fun refresh() {
        viewModelScope.launch {
            businessId?.let { id ->
                ServicesRepository.fetchServiceDefinitions(id)
                ServicesRepository.fetchServiceProviders(id)
                ServicesRepository.fetchServiceRecords(id)
            }
        }
    }
    
    fun addService(service: ServiceDefinition) { /* ... */ }
    fun updateService(service: ServiceDefinition) { /* ... */ }
    fun toggleServiceActive(serviceId: String) { /* ... */ }
    
    fun addProvider(provider: ServiceProvider) { /* ... */ }
    fun updateProvider(provider: ServiceProvider) { /* ... */ }
    fun toggleProviderActive(providerId: String) { /* ... */ }
    
    fun recordService(record: ServiceRecord) { /* ... */ }
}
```

#### 4.3 Modify: `ui/screens/ServicesScreen.kt`

- Accept `ServicesViewModel` as parameter
- Replace hardcoded data with ViewModel state
- Wire up add/edit/record actions

#### 4.4 Modify: `ui/screens/ManageServicesScreen.kt`

- Accept `ServicesViewModel` as parameter
- Wire up CRUD operations for services and staff

### Testing Checklist
- [ ] Services can be created/edited
- [ ] Staff members can be created/edited
- [ ] Service records correctly calculate commission
- [ ] History shows all past service transactions
- [ ] Data persists across app restarts

---

## 💸 Flow 5: Expenses Flow

### What This Flow Covers
- Record expenses with categories
- View expense history
- Category breakdown

### Files to Create

#### 5.1 New: `data/repositories/ExpensesRepository.kt`
#### 5.2 New: `viewmodels/ExpensesViewModel.kt`
#### 5.3 Modify: `ui/screens/ExpensesScreen.kt`

**Pattern**: Same structure as Services Flow

### Testing Checklist
- [ ] Expenses can be created
- [ ] Expenses appear in history
- [ ] Category breakdown is accurate
- [ ] Date range filtering works

---

## 📦 Flow 6: Products Flow

### What This Flow Covers
- Manage product catalog
- Track inventory levels
- Stock movements

### Files to Create

#### 6.1 Modify: `data/repositories/InventoryRepository.kt` → Supabase integration
#### 6.2 Modify: `viewmodels/InventoryViewModel.kt`
#### 6.3 Modify: `ui/screens/ProductsScreen.kt`

### Testing Checklist
- [ ] Products can be added with barcode
- [ ] Stock levels update correctly
- [ ] Stock movements are logged
- [ ] Barcode scanning finds correct product

---

## 🛒 Flow 7: Sales Flow

### What This Flow Covers
- Create sales from products
- Cart management
- Payment method selection
- Sales history

### Files to Create

#### 7.1 New: `data/repositories/SalesRepository.kt`
#### 7.2 New: `viewmodels/SalesViewModel.kt`
#### 7.3 Modify: `ui/screens/sales/NewSaleScreen.kt`
#### 7.4 Modify: `ui/screens/SalesHistoryScreen.kt`

### Testing Checklist
- [ ] Sale can be completed with items
- [ ] Product stock decreases on sale
- [ ] Sale appears in history
- [ ] Total calculations are accurate

---

## 📊 Flow 8: Reports

### What This Flow Covers
- Aggregate data from sales, expenses, services
- Generate KPIs
- Export functionality

### Files to Modify

#### 8.1 Modify: `ui/screens/ReportsScreen.kt`

- Fetch aggregated data from repositories
- Compute KPIs client-side (or via Supabase functions)

### Testing Checklist
- [ ] Sales totals match actual sales
- [ ] Expense totals match actual expenses
- [ ] Top products/services calculated correctly
- [ ] PDF/CSV export works

---

## 🌙 Flow 9: Day Closure (Funga Siku)

### What This Flow Covers
- End-of-day summary
- Create closure record
- Prevent duplicate closures

### Files to Create

#### 9.1 New: `data/repositories/DayClosureRepository.kt`
#### 9.2 Integrate: `ui/screens/HomeScreen.kt`

### Testing Checklist
- [ ] Day closure shows correct totals
- [ ] Closure record created successfully
- [ ] Cannot close same day twice
- [ ] UI shows closure status

---

## 🏠 Flow 10: Dashboard (Home Screen)

### What This Flow Covers
- Real-time KPIs (Today's Sales, Expenses, Profit)
- Weekly performance chart
- Quick actions

### Files to Modify

#### 10.1 Modify: `ui/screens/HomeScreen.kt`

- Fetch today's metrics from repositories
- Compute weekly trends

### Testing Checklist
- [ ] Today's metrics are accurate
- [ ] Weekly chart shows real data
- [ ] Data updates in real-time

---

## 📅 Implementation Timeline

| Week | Flows | Deliverable |
|:----:|:------|:------------|
| **1** | Phase 0 + Flow 1-2 | Auth + Business Setup working |
| **2** | Flow 3-4 | Subscription + Services Flow |
| **3** | Flow 5-6 | Expenses + Products Flow |
| **4** | Flow 7 | Sales Flow (most complex) |
| **5** | Flow 8-10 | Reports, Day Closure, Dashboard |
| **6** | Testing + Polish | End-to-end testing, bug fixes |

---

## ✅ Pre-Integration Checklist

Before starting each flow:

- [ ] Schema tables exist in Supabase
- [ ] RLS policies are active and tested
- [ ] DTO serialization models created
- [ ] Repository with CRUD operations
- [ ] ViewModel connecting Repository to UI
- [ ] UI wired to ViewModel state

---

## 🔒 Security Reminders

1. **Never expose Supabase Service Role Key** in mobile app
2. **Always use Anon Key** with proper RLS
3. **Validate on server side** (RLS + triggers)
4. **UUID generation** happens client-side (for offline support later)
5. **Sensitive data** (passwords) never stored in app models

---

## 🚧 Phase 2: Offline Support (Future)

After all flows are online-only and stable:

1. Add Room database mirroring Supabase tables
2. Implement `sync_status` column tracking
3. Add WorkManager for background sync
4. Implement conflict resolution
5. Add network state detection
6. Implement retry with exponential backoff

---

## 📝 Notes

- Each flow should be **merged independently** after testing
- Keep **mock data** available for UI development (behind feature flag)
- Document **API response structures** as you implement
- Use **Kotlin Serialization** consistently

---

*This plan was generated based on the current NeoBuk codebase analysis. Adjust timelines based on team capacity.*
