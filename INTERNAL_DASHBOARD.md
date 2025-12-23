# Internal Admin Dashboard Specification

**"If you can't see these, you're flying blind."**

This dashboard is for internal use to monitor the health, growth, and technical reliability of NeoBuk businesses.

## 📊 Core Metrics (The Big 5)

### 1. DAU (Daily Active Businesses)
- **Definition**: Number of unique `business_id`s that have recorded a Sale, Expense, or Service in the last 24 hours.
- **Why**: Real-time pulse of business adoption.

### 2. Transaction Volume per Business
- **Definition**: Average and individual transaction counts (Sales/Expenses) per business.
- **Why**: Identifies "Power Users" vs. businesses struggling with onboarding.

### 3. Ritual Engagement (% Funga Siku)
- **Definition**: (Businesses with `day_closure` record / Total active businesses).
- **Why**: Measures the success of our "Closure Ritual" strategy. High engagement here = high retention.

### 4. Conversion Funnel (Trial → Paid)
- **Definition**: Percentage of businesses that move from the 30-day trial to a Monthly or Yearly paid plan.
- **Why**: Validates product-market fit and pricing strategy.

### 5. Sync Performance (Offline vs Online)
- **Definition**: Ratio of records created with `synced_at > created_at` (Offline) vs. `synced_at approx created_at` (Online).
- **Why**: Critical technical health check. Shows how often NeoBuk is "saving the day" during network outages.

## 🛠️ Data Sources
- Authoritative Data: **Supabase**
- Monitoring Views: PostgreSQL materialized views for real-time aggregation.

## 🚀 Future Dashboard Extensions
- **M-Pesa Success Rate**: Monitoring payment gateway reliability.
- **Support Response Time**: Tracking "One-tap support" queries.
- **Churn Alert**: Identifying businesses that haven't recorded a sale in 3+ days.
