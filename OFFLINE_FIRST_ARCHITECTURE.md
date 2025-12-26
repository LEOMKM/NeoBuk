# 📓 NeoBuk Offline-First Architecture (Android)

> **Last Updated:** 2025-12-27  
> **Status:** Active Design Document  
> **Priority:** CRITICAL - Non-negotiable architectural principles

---

## 1. Core Principle (Do not violate this)

**NeoBuk must work fully with airplane mode ON.**

### Internet Role
- ✅ **Enhances** the experience
- ❌ **Never blocks** the business
- ❌ **Never causes** data loss

### Critical Functionality Test
If the app cannot perform the following **without internet**, it is broken:
- ✅ Record sales
- ✅ Record service sales
- ✅ Record expenses
- ✅ Compute profit
- ✅ Perform Day Close

---

## 2. Source of Truth Rule

```
Local Database = Primary Source of Truth
Cloud (Supabase) = Sync + Backup
```

### Architecture Principles
- ✅ **Local database** is the primary source of truth
- ✅ **Cloud (Supabase)** is sync + backup
- ✅ **UI always reads** from local DB
- ✅ **Network failures** must be invisible to the user

**This is non-negotiable.**

---

## 3. Storage Choice (Locked)

### ✅ SQLite via Room (Android)

**Why:**
- ✅ ACID guarantees (money data)
- ✅ Fast aggregations (Day Close)
- ✅ Mature tooling
- ✅ Safe migrations

### ❌ Do NOT use
- ❌ SharedPreferences / DataStore (for money)
- ❌ In-memory caches
- ❌ "Temporary" tables for sales

---

## 4. Core Local Tables (Minimum Set)

These must exist locally, **even before login sync**.

### Required Tables
1. **sales** (product sales)
2. **service_sales**
3. **expenses**
4. **day_closure** (Daily Confirmation)
5. **sync_queue** (critical for offline sync)

---

## 5. Writing Data (Offline-First Flow)

**Rule: Write local first. Always.**

### Flow for any action (sale, service, expense):

```kotlin
// ✅ CORRECT FLOW
1. User performs action
2. Write to Room DB
3. Mark is_synced = false
4. Add entry to sync_queue
5. UI updates immediately
6. Sync happens later (background)
```

### ❌ Anti-Pattern
```kotlin
// ❌ NEVER DO THIS
// Never wait for API success before saving locally
try {
    api.createSale(sale) // DON'T WAIT FOR THIS
    db.insert(sale)      // TOO LATE
} catch (e: Exception) {
    // User thinks sale is lost!
}
```

---

## 6. Reading Data (UI Rule)

### UI Must:
- ✅ Observe Room (Flow / LiveData)
- ✅ Never depend on network state
- ✅ Always show latest local truth

### Example Implementation

```kotlin
@Dao
interface SalesDao {
    @Query("SELECT SUM(amount) FROM sales WHERE DATE(created_at) = :today")
    fun getTodaySales(today: String): Flow<Double>
}

// ViewModel
class DashboardViewModel(private val salesDao: SalesDao) : ViewModel() {
    val todaySales: StateFlow<Double> = salesDao
        .getTodaySales(getCurrentDate())
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)
}
```

---

## 7. Day Close Logic (This Is Important)

**Day Close is local computation, not server-driven.**

### Expected Cash Formula (final, locked):

```kotlin
Expected Cash = (cash product sales + cash service sales) − cash expenses
```

**Where:**
- `payment_method = "cash"`
- `Date = closure_date`

### What Day Close does NOT care about:
- ❌ Product vs service distinction
- ❌ Inventory levels
- ❌ Sync status

**If it moved cash today, it counts.**

### Implementation Example

```kotlin
@Query("""
    SELECT 
        (
            (SELECT COALESCE(SUM(amount), 0) FROM sales 
             WHERE payment_method = 'cash' AND DATE(created_at) = :date)
            +
            (SELECT COALESCE(SUM(amount), 0) FROM service_sales 
             WHERE payment_method = 'cash' AND DATE(created_at) = :date)
            -
            (SELECT COALESCE(SUM(amount), 0) FROM expenses 
             WHERE payment_method = 'cash' AND DATE(created_at) = :date)
        ) as expected_cash
""")
suspend fun calculateExpectedCash(date: String): Double
```

---

## 8. Sync Strategy (Simple & Safe)

### Trigger Sync When:
- ✅ App opens
- ✅ Internet becomes available
- ✅ User manually refreshes

### Sync Steps

```kotlin
suspend fun syncData() {
    while (true) {
        // 1. Read oldest item from sync_queue
        val item = syncQueueDao.getOldestPending() ?: break
        
        // 2. Push to Supabase
        when (val result = pushToSupabase(item)) {
            is Success -> {
                // 3. Mark entity is_synced = true
                markAsSynced(item.entityId)
                // 4. Remove queue item
                syncQueueDao.delete(item)
            }
            is Failure -> {
                // 5. Increment retry_count
                syncQueueDao.incrementRetry(item.id)
                // 6. Retry later (with backoff)
                if (item.retryCount >= MAX_RETRIES) {
                    syncQueueDao.markAsFailed(item.id)
                }
                break
            }
        }
    }
}
```

### Never:
- ❌ Block UI
- ❌ Show sync errors to user (unless critical)
- ❌ Retry endlessly (cap retries at 5)

---

## 9. Conflict Resolution Rule

**NeoBuk is single-owner per business (V1).**

### Conflict Strategy:
- ✅ **Last write wins**
- ✅ Based on `updated_at` timestamp
- ✅ Server accepts client timestamps
- ❌ No merge UI
- ❌ No conflict screens

### Implementation

```kotlin
// Client sends timestamp
data class SaleDTO(
    val id: String,
    val amount: Double,
    val created_at: Long,
    val updated_at: Long // Client generates this
)

// Server accepts if newer
if (clientSale.updated_at > serverSale.updated_at) {
    updateSale(clientSale)
}
```

---

## 10. Network Awareness (UX, not logic)

### Show Subtle Indicators Only:
- ✅ "Syncing…" (small badge)
- ✅ "Saved offline" (brief toast)
- ✅ "All data synced" (checkmark)

### Never Show:
- ❌ Errors during sales
- ❌ Toasts that scare users
- ❌ Blocking dialogs

**NeoBuk must feel calm.**

---

## 11. Login & Offline Reality

### V1 Rule:
- ✅ Once logged in once → app works offline
- ✅ Business ID cached locally
- ✅ No forced re-auth on network loss

### Token Expiry Handling:
- ✅ Allow grace period
- ✅ Re-auth silently when online
- ❌ Never block data access

### Implementation

```kotlin
// Store auth state locally
@Entity(tableName = "auth_state")
data class AuthState(
    @PrimaryKey val id: Int = 1,
    val userId: String,
    val businessId: String,
    val token: String,
    val lastSynced: Long
)

// Check auth gracefully
suspend fun ensureAuth() {
    val authState = authDao.getAuthState()
    if (isOnline() && authState.isExpired()) {
        try {
            refreshToken()
        } catch (e: Exception) {
            // Continue with offline mode
            // Don't block the user
        }
    }
}
```

---

## 12. Backups & Restore (V2)

### Backup Strategy:
- ✅ Local DB backed up to Supabase
- ✅ Restore pulls full dataset
- ✅ Device loss should not mean data loss
- ✅ Local DB remains the active DB after restore

---

## 13. Testing Scenarios (Must Test)

**You are not done until these pass:**

- [ ] ✅ Airplane mode → record sale → close day
- [ ] ✅ Kill app → reopen → data intact
- [ ] ✅ Switch phones → restore → totals correct
- [ ] ✅ Record service sale only → Day Close reflects it
- [ ] ✅ Cash ≠ expected → discrepancy shown calmly
- [ ] ✅ Partial sync failure → app still usable

### Test Implementation

```kotlin
@Test
fun `record sale in airplane mode and verify day close`() = runTest {
    // 1. Disable network
    networkSimulator.setAirplaneMode(true)
    
    // 2. Record sale
    val sale = Sale(amount = 1000.0, paymentMethod = "cash")
    salesRepository.createSale(sale)
    
    // 3. Verify local DB
    val todaySales = salesDao.getTodaySalesSync()
    assertEquals(1000.0, todaySales)
    
    // 4. Close day
    val dayClose = dayCloseRepository.closeDayLocally()
    assertEquals(1000.0, dayClose.expectedCash)
    
    // 5. Verify no network calls were made
    verify(exactly = 0) { supabaseClient.any() }
}
```

---

## 14. One Rule to Keep Forever

> **Offline is not a feature.**  
> **It is the default state.**

If a feature assumes internet, it must be redesigned.

---

## 15. Final Mental Model

```
┌─────────────────────────────────────────┐
│  SQLite (Room) = Business Memory        │
│  ↓                                      │
│  Supabase = Safety Net                  │
│  ↓                                      │
│  UI = Reflection of Local Truth         │
│  ↓                                      │
│  Sync = Background Hygiene              │
└─────────────────────────────────────────┘
```

### Core Beliefs
- ✅ **SQLite** = business memory
- ✅ **Supabase** = safety net
- ✅ **UI** = reflection of local truth
- ✅ **Sync** = background hygiene

---

## 16. Implementation Checklist

### Phase 1: Core Local Storage
- [ ] Set up Room database
- [ ] Create sales, service_sales, expenses tables
- [ ] Create sync_queue table
- [ ] Implement offline-first write pattern
- [ ] Implement local Day Close calculation

### Phase 2: Sync Engine
- [ ] Build sync queue processor
- [ ] Implement retry logic with backoff
- [ ] Add network state listener
- [ ] Test sync recovery scenarios

### Phase 3: UX Polish
- [ ] Add subtle sync indicators
- [ ] Implement offline-mode grace
- [ ] Test and refine user messaging
- [ ] Ensure no blocking errors

### Phase 4: Validation
- [ ] Run all offline test scenarios
- [ ] Test on real devices
- [ ] Verify Day Close accuracy
- [ ] Load test with large datasets

---

## Success Criteria

**NeoBuk succeeds because it never lies, even offline.**

The app is considered successful when:
1. ✅ A user can run their entire business day without internet
2. ✅ No data is ever lost due to network issues  
3. ✅ Day Close calculations are always accurate
4. ✅ Sync happens transparently in the background
5. ✅ The user never worries about connectivity

---

**End of Document**
