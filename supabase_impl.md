# Supabase Implementation & Sync Strategy

## ✅ 1. Supabase = Source of Truth (Do this first)

Supabase MUST define the authoritative "Constitution" of the application before any offline code is written.

**Supabase should define:**
- Final table structures
- Constraints & Relationships
- IDs (UUIDs)
- Row-Level Security (RLS) rules
- Subscription enforcement logic
- Idempotency expectations

**Why this matters:**
- Offline sync depends entirely on a stable server truth.
- If your backend is shaky, offline mode will amplify bugs exponentially.
- You only want to build the sync layer once.

---

## ❌ What NOT to do
- **Do not** build offline DB schema first and “hope it maps later.”
- **Do not** prototype sync without finalized, frozen server tables.
- **Do not** let client-side models diverge from server models.

*Divergence is how "sync hell" happens.*

---

## 🧠 The Right Mental Model (Lock this in)
- **Supabase is Authoritative.** 
- **Offline Storage is a Write-Ahead Buffer.**
- Offline mode should mirror Supabase, not invent its own reality.

---

## 🚀 Recommended Implementation Sequence

### Step 1 — Finalize Supabase Schema (NOW)
Focus ONLY on these entities:
- `sales`
- `expenses`
- `services_offered`
- `day_closures`
- `subscriptions` (read-only offline)
- `businesses`

**Key requirements:**
- **UUID Primary Keys**: (Must allow client-generated IDs).
- **Metadata**: `created_at`, `synced_at`.
- **Soft Delete**: Use status fields rather than destructive deletes.
- **RLS Rules**: Fully locked and tested.

### Step 2 — Build ONLINE-ONLY Flows
Before touching the offline layer:
- Push **Sale → Supabase**
- Push **Expense → Supabase**
- Push **Service → Supabase**

**Confirm:**
- Reports are 100% accurate.
- Subscription rules are enforced by the server.
- No duplicate records are created.

### Step 3 — Add Offline Layer as a Thin Wrapper
Introduce **Room / SQLite** tables that are identical mirrors of Supabase:
- Same IDs
- Same fields
- Same enums

**The Flow:**
1. Write to local DB.
2. Mark `sync_status = PENDING`.
3. UI returns success immediately (Reassuring "Saves first" UX).
4. Background worker is triggered to push to Supabase.

### Step 4 — Add Sync Worker
- Reads `PENDING` local records.
- POSTs to Supabase.
- **On success**: Mark as `synced`.
- **On failure**: Keep local data and retry later automatically.
- **Idempotency**: Supabase must accept the same UUID twice but "do nothing" on conflict.

---

## 💳 Subscription Logic (Offline Detail)
**The Correct Rule:**
1. Cache the subscription status locally during the last successful sync.
2. **If status = ACTIVE or TRIAL**: Allow offline writes.
3. **If status = LOCKED**: Block even offline writes.
4. Supabase still performs the final enforcement upon sync.

---

## 📊 Why Supabase-First Beats Offline-First

| Approach | Result |
| :--- | :--- |
| **Offline-first without backend** | Fast demo, painful full-rewrite later. |
| **Supabase-first then offline** | Stable, scalable, investor-safe foundation. |
| **Trying both together** | Bugs, delays, and high frustration. |

---

## 🗣️ Straight Talk & Recommendation
1. **Lock Supabase schema this week.**
2. Get the online-only app fully correct and bug-free.
3. Add the offline capture layer in 3–5 focused days.
4. Test with airplane mode + bad network conditions.
5. **Ship beta.**

**Clear Recommendation:**
Yes — start with **Supabase first.**
Offline sync is an enhancement layer, not the foundation.
