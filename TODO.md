# NeoBuk Project TODOs

## Strategic & Technical Roadmap

### 🟡 High Priority (Post-V1 Launch)
- [ ] **Offline Capture + Deferred Sync (Kenyan Market V1)**:
    - [ ] **Core Three Offline**: Enable Record Sale, Expense, and Service Offered to work without internet.
    - [ ] **Offline UX**: Implement the "Saved · Will sync when online" confirmation and subtle header sync indicator (🟡).
    - [ ] **Data Integrity**: Implement Local-First Write with UUIDs and Append-Only Sync (no remote updates or deletions).
    - [ ] **Subscription Logic**: Allow offline writes only if the last subscription check was valid. 
- [ ] **Local-First Database Strategy**: Integrate Room/SQLite for local-first operations and WorkManager for reliable background syncing to Supabase.
- [ ] **Data Export Improvements**: Enhance the "Export anytime" feature to generate professional CSV/Excel reports for owner use.

### 🔵 Features & UX
- [ ] **Custom Expense Categories**: Allow owners to define their own expense categories under the "More" tab.
- [ ] **Commission Automation**: Automated payout summaries for staff based on recorded service commissions.
- [ ] **Hybrid Business Support**: Toggle to enable both Products and Services for a single business entity.

### 🟢 Trust & Security
- [ ] **Audit Trail**: Basic logging of record creations and edits for transparency.
- [ ] **Privacy Dashboard**: A visible "Security & Privacy" screen in Settings summarizing data ownership and security protocols.
