# NeoBuk – App Journey & Product Specification

## 1. Product Overview

NeoBuk is a **mobile-first business management app for SMEs**, designed for clarity, speed, and low cognitive load on small Android devices.

The app helps business owners:
- Record **sales and expenses**
- Manage **products or services** (depending on business type)
- Track **performance through reports and dashboard insights**
- Operate with minimal setup and daily friction

Target users are **non-technical SME owners** operating retail or service businesses in real-world, high-pressure environments.

### 1.1 Why NeoBuk Wins (The Competitive Edge)
Unlike international competitors like **QuickBooks** or **Zoho** which assume stable high-speed internet, NeoBuk is built for the Kenyan reality:
- **Competitors**: Fail loudly when offline, leading to work stoppage and immediate loss of trust.
- **NeoBuk**: Saves first, syncs later. It **never loses data** due to connectivity drops. This reliability creates the word-of-mouth trust required to win the market.

---

## 2. Core Design Principles

- **Action-first UX** (do work immediately)
- **Low cognitive load** on small screens (5.8–6.4")
- **Clear separation of concerns**
- **Predictable navigation**
- **No data loss on subscription expiry**
- **Offline reliability** (works when internet is unreliable)

---

## 3. App Navigation Structure

### Bottom Navigation (5 tabs)

1. **Products** (`ProductsScreen.kt`)
2. **Services** (`ServicesScreen.kt`)
3. **Home** (`HomeScreen.kt`)
4. **Reports** (`ReportsScreen.kt`)
5. **More** (`MoreScreen.kt`)

The center **Home** tab represents daily work.

---

## 4. Home (Dashboard & Actions)

### Purpose
Home is the central command center, combining **quick actions** with **high-level insights**.

### Home contains:
- **Hero Section**: Welcome banner + AI-driven insights (e.g., "Sales up 12%").
- **Quick Actions**:
  - **➕ Sale** (Button)
  - **➖ Expense** (Button)
- **Key Metrics (KPIs)**:
  - Today's Sales
  - Expenses
  - Profit
- **Weekly Performance**: A simplified bar chart showing 7-day trends.

*Note: While "Low cognitive load" is a principle, high-value visual summaries are included to motivate the owner.*

---

## 5. Products (Product-based Businesses)

Enabled when business type = `products`.

### Features
- Product list
- Add / edit product
- Pricing
- Inventory / stock levels
- Barcode generation & scanning

### Data captured per product
- Name
- SKU / barcode
- Price
- Stock quantity
- Active / inactive

**Implementation:** `ProductsScreen.kt`, `InventoryViewModel.kt`

---

## 6. Services (Service-based Businesses)

Enabled when business type = `services`.

Services are split into **setup** and **recording**.

---

### 6.1 Service Setup (Unified)

Managed under **More → Manage Services** (`ManageServicesScreen.kt`).
This screen uses a **Tabbed Interface**:

#### Tab 1: Services
Definitions of what is sold.
- Service name
- Base price
- Optional commission override
- Active / inactive
- **Add/Edit**: Via bottom sheet.

#### Tab 2: Staff
Service providers who perform the work.
- Full name
- Role (e.g., Barber, Stylist)
- Commission rate (%)
- Active / inactive
- **Add/Edit**: Via bottom sheet.

*Note: Previous spec separated these into different menu items. They are now consolidated for easier management.*

---

### 6.2 Recording a Service Offered

Purpose: record **actual work done**, not configuration.

#### Entry points
- Services → Record Service
- Home → Sale → Service

#### Flow
1. Select service (price auto-fills)
2. Select service provider (from maintained list)
3. Confirm & save

---

#### What is recorded (snapshot)
- Service name
- Service provider name
- Service price
- Commission rate used
- Commission earned
- Amount due to business
- Date & time offered

Values are copied to preserve historical accuracy.

---

#### Service History View
Each entry shows:
- Service name
- Provider name
- Date
- Price
- Commission earned
- Business amount

**Implementation:** `ServicesScreen.kt`

---

## 7. Sales Flow

Sales can include:
- Products
- Services
- (Hybrid later)

Sales are recorded from **Home → Sale**.

Each sale records:
- Items/services
- Total amount
- Date & time
- Linked product/service snapshots

**Implementation:** `NewSaleScreen.kt`

---

## 8. Expenses Flow

Recorded from **Home → Expense**.

### Expense capture
- Amount
- Category
- Notes (optional)
- Date

Expense categories are managed under **More**.

**Implementation:** `ExpensesScreen.kt`

---

## 9. Reports

Reports are **read-only**.

### Includes:
- Sales summary
- Expense summary
- Net performance
- Product performance
- Service & provider performance

No data editing from Reports.

**Implementation:** `ReportsScreen.kt`

---

## 10. More (Admin & Setup)

Low-frequency actions live here.

Includes:
- Business profile
- Subscription
- Service providers
- Expense categories
- Customers (future)
- Staff & roles
- Settings
- Support
- Legal / Terms

**Implementation:** `MoreScreen.kt`

---

## 11. Signup & Onboarding Journey

### Screen 1: Account Basics
- Full name
- Email or phone
- Password
- Confirm password
- Accept Terms & Conditions

### Screen 2: Business Setup
- Business name
- Business type:
  - **Services** (Salon, Kinyozi, Laundry, etc.)
  - **Products** (Shop, Spare parts, Retail, etc.)

This selection drives feature availability.

### Screen 3: Subscription Plan
- Free trial: **1 month**
- Monthly: **KES 249**
- Yearly: **KES 2,490**

User may start trial without payment.

### Screen 4: Payment (if plan selected)
Providers:
- M-Pesa
- Paystack

### Screen 5: Success
- Business ready
- Redirect to Home

**Implementation:** `SignupScreen.kt` (Multi-step Wizard)

---

## 12. Subscription & Trial Lifecycle

### Trial States
1. Trial Active
2. Trial Ending Soon (last 7 days)
3. Trial Expired (grace period)
4. Trial Locked

### Trial Ending Soon UX
- Soft banner on Home
- Informational tone
- CTA: Choose Plan

### Trial Expired (Grace Period)
- View-only access
- Cannot add sales or expenses
- Clear upgrade prompts

### Trial Locked
- Access limited to:
  - Subscription
  - Support
  - Logout

User data is NEVER deleted.

**Implementation:** `SubscriptionViewModel.kt`, `SubscriptionScreen.kt`, `SubscriptionLockedScreen.kt`

---

## 13. Pricing Model

- Monthly: **KES 249**
- Yearly: **KES 2,490**
- Free trial: **1 month**
- Currency: KES

Subscriptions are tied to **Business**, not User.

---

## 14. Data Model (High Level)

User
└── Business
  └── Subscription
  └── Payments
    ├── Products
    ├── Services
    ├── Service Providers
    ├── Sales
    └── Expenses

**Implementation:** `AuthModels.kt`

---

## 15. Key UX Rules (Non-negotiable)

- Home stays **action-first** (transactions + key insights)
- No free-text service providers
- No surprise lockouts
- No data deletion on expiry
- Read-only access before full lock
- Clear, calm language everywhere

---

## 16. Business Type Feature Gating

Business type selected during onboarding controls feature availability.

| Feature | Products Business | Services Business |
| :--- | :--- | :--- |
| **Products Tab** | ✅ Enabled | ❌ Hidden |
| **Services Tab** | ❌ Hidden | ✅ Enabled |
| **Inventory** | ✅ Enabled | ❌ Hidden |
| **Staff & Commissions** | ❌ Hidden | ✅ Enabled |
| **Manage Services** | ❌ Hidden | ✅ Enabled |

*Hybrid support is deferred to a later phase.*

---

## 17. Technical Gap Analysis (Current Code vs Spec)

| Feature | Spec | Current Implementation | Action Required |
| :--- | :--- | :--- | :--- |
| **Home Screen** | **Dashboard** (Charts & KPIs) | **Matches Spec** | None. |
| **Manage Services** | **Unified Tabbed UI** | **Matches Spec** | None. |
| **Inventory** | Visible on Home in Spec? (Implicitly No) | **Inventory Status** widget on Home | **Remove Inventory widget** from Home to match "Low cognitive load". (Pending) |

---

## 18. Next Possible Extensions

- Hybrid businesses (Products + Services)
- Staff payouts
- Customer management
- Multi-branch support
- Web admin dashboard
- AI insights (later)

---

## 19. Offline-First Capability (V1)

**"Works even when the internet is unreliable."** (Core UX Promise)

This is a critical differentiator for the Kenyan market. NeoBuk (V1) focuses on **Offline Capture + Deferred Sync**, which covers 80–90% of real SME pain.

### 19.1 What MUST work offline (The Core Three)
Only these transactions are enabled offline. Everything else can be read-only or disabled:
1.  **Record Sale**
2.  **Record Expense**
3.  **Record Service Offered**

### 19.2 Recommended Offline UX (Exact Behavior)
1.  **Immediacy**: When recording something offline, save it to local storage immediately.
2.  **Reassurance**: Show a small, calm confirmation: *“Saved · Will sync when online”*.
3.  **Subtle Indicator**: A tiny dot or icon (e.g., 🟡) in the header or Home screen that is only visible when items are pending sync. Avoid banners or modals.
4.  **Auto-Sync**: When connection returns, auto-sync silently in the background.
5.  **Failure Handling**: If sync fails, keep data locally and show: *“Some items couldn’t sync. We’ll retry.”* No manual retry buttons yet; automatic retries are sufficient.

### 19.3 Non-negotiable Data Rules
These rules ensure data integrity and prevent conflicts:
1.  **Local-First Write**: Every transaction is written to the local database first. Server sync is always secondary.
2.  **Immutable IDs**: Generate UUIDs locally. The server MUST accept client-generated IDs to prevent duplicates.
3.  **Append-Only Sync**: Never “update” remote records during sync. Only create. Any corrections are handled later as new adjustment transactions.

### 19.4 Subscription + Offline
- If the subscription was **valid** at the last check, allow offline writes.
- If the subscription is **locked**, block even offline writes.
- This prevents abuse without punishing honest users during unexpected outages.

---

## 20. Funga Siku (The Daily Ritual)

### 20.1 What “Day Close” Really Is
Day Close (renamed to **Funga Siku** in the UI) is not bookkeeping; it is a simple question asked once per day: **“Are you done for today?”**
When the owner taps "**Funga Siku**", the app:
- Summarizes today’s activity
- Locks today as “reviewed” (psychological bookmark)
- Gives psychological closure
*No journals, no balances carried forward, no complex accounting vocabulary.*

### 20.2 Why Day Close Wins in Kenya
Matches the existing habit of counting cash, checking M-Pesa, and asking *“Leo tumefanya aje?”* (How did we do today?).
- **Retention**: Builds a daily habit.
- **Trust**: Solidifies the feeling that "my numbers are final".

### 20.3 What Day Close IS NOT
To maintain its power, it MUST NOT be:
- ❌ Mandatory
- ❌ Blocking future days
- ❌ A financial period lock or tax action
It is a **choice**, not a gate.

### 20.4 Exact Funga Siku UX (V1)
- **Entry Points**: 1. Home Screen (evening prompt) 2. Reports → Today.
- **Prompt Timing**: After 6pm or when opening the app at night.
- **Copy**: *“Funga Siku? Review today’s sales and expenses.”*
- **Action Buttons**: `Funga Siku` (Primary), `Later` (Neutral).

### 20.5 Review Screen & Feedback
Before confirming, show a single summary:
- **Metrics**: Total Sales, Total Expenses, Net (Sales – Expenses).
- **Metadata**: Number of sales, number of expenses.
- **Action**: Tap `Confirm Funga Siku` to finish.

### 20.6 Technical Implementation & Data Rule
- **Action**: Create a `day_closure` record (see Section 20.8).
- **Late Entries**: Real life happens. If a sale is added after closure, allow it but show a subtle indicator: *“1 item added after day close”*. **Do not** reopen the day automatically.
- **Reports**: Days with closure get a small ✓ or badge; others remain neutral. No "shame" language for missing days.

### 20.7 Comparison: NeoBuk vs Global Tools
- **Competitors**: Think in periods, ledgers, and feature parity.
- **NeoBuk**: Thinks in days and human routine. **Ritual > Feature.**

### 20.8 Data Model
**Table**: `day_closures`
- `id` (UUID)
- `business_id` (UUID)
- `date` (YYYY-MM-DD)
- `total_sales` (Double)
- `total_expenses` (Double)
- `net` (Double)
- `closed_at` (Timestamp)
*No foreign keys to individual transactions needed in V1.*

---

## 21. Future Roadmap & Strategic Priorities

### 21.1 Offline Excellence (Phase 2)
- **Offline Capture + Deferred Sync**: The primary technical priority. This covers 80–90% of real SME pain by allowing business to continue during outages and syncing everything once back online.
- **Local-First Database**: Move from API-heavy to local-first (e.g., Room + WorkManager) to ensure 0ms latency on all captures.
- **Manual Sync Trigger**: Simple "Sync Now" button in Settings if an owner wants immediate confirmation.

### 21.2 Financial Trust Signals
- **Audit Log**: Visible history of changes to transactions to prevent employee theft.
- **Encrypted Backups**: Reassure owners that data is stored in the cloud but only accessible by them.

### 21.3 Hybrid Capability
- Support for businesses that provide both services and sell physical products (e.g., a salon selling hair products).

---

## 22. Internal Analytics & Monitoring

To ensure data-driven growth and avoid "flying blind," the following core metrics must be trackable via an internal dashboard before public launch:

### 22.1 Operational Metrics
- **DAU (Daily Active Businesses)**: Count of unique businesses recording at least one transaction per day.
- **Transaction Density**: Average number of sales/expenses recorded per active business.
- **Feature Adoption**: % of active users utilizing the "Day Close" ritual.

### 22.2 Business Health Metrics
- **Conversion Rate**: Trial-to-paid subscription conversion percentage.
- **Churn Rate**: Frequency of businesses becoming "Locked" without upgrading.
- **Support Volume**: Number of "One-tap support" queries per business.

### 22.3 Technical Performance
- **Sync Reliability**: Ratio of **Offline Saves** vs. **Online Saves** to monitor real-world connectivity impact.
- **Sync Latency**: Time taken for a deferred record to reach Supabase once online.
