# Play Store Graphics Guide for NeoBuk

## Required Graphics for Google Play Store

### 1. Feature Graphic (Required)
**Size:** 1024 x 500 px  
**Format:** PNG or JPG (24-bit)  
**Purpose:** Main banner on your Play Store listing

**Content Recommendations:**
```
Left side: NeoBuk branding/logo
Center: "Track Sales • Manage Stock • Grow Your Business"
Right side: Visual icons (chart, products, tasks)

Color: Teal (#00897B)
Style: Professional, clean, minimal
Target: SME owners (dukas, salons)
```

---

### 2. Screenshots (2-8 required, 8 recommended)

**Size:** Varies by device, but aim for:
- **Phone:** 1080 x 1920 px (9:16 ratio)
- Or capture actual screenshots from device

**Recommended Screenshots in Order:**

#### Screenshot 1: Dashboard/Home ⭐ (Most Important)
**Why first:** Shows core value immediately
**What to show:**
- Welcome message: "Karibu, Mama Njeri's Shop"
- AI Insight: "Sales up 12% from last week"
- Three metric cards: Sales, Expenses, Profit
- Net profit explanation
- Weekly performance chart
- Stock running low alert
- Bottom navigation with anchored Home button

**Caption:** "Track everything at a glance"

---

#### Screenshot 2: Sales History
**What to show:**
- Sales summary card with today's metrics
- Transaction list with times and amounts
- Filter chips (Today, Week, Month)
- "Record New Sale" button
- Visual receipt icon

**Caption:** "Record sales in seconds"

---

#### Screenshot 3: Products/Inventory
**What to show:**
- Stock overview stats (Total, Running Low, Out of Stock)
- Product cards with images
- Stock status indicators (Available, Running Low)
- "Add Product" button
- Search and filter options

**Caption:** "Never run out of stock"

---

#### Screenshot 4: Things to Do / Tasks ⭐ (Differentiator)
**What to show:**
- Date strip calendar
- Task cards with:
  - "Restock: Maize Flour - 3 days ago"
  - "Follow up with Supplier"
  - Mark done / Snooze buttons
- "Add Task" button
- Clean, organized layout

**Caption:** "Stay organized with smart reminders"

---

#### Screenshot 5: Reports
**What to show:**
- KPI cards (Revenue, Profit, Growth)
- Sales trend chart (animated bars or line)
- Top products list
- Payment methods donut chart
- Export to PDF/CSV buttons

**Caption:** "Understand your business with clear reports"

---

#### Screenshot 6: Services Screen (For Salon/Service Businesses)
**What to show:**
- Service history
- Record new service interface
- Service definitions list
- Provider assignment

**Caption:** "Perfect for salons and service businesses"

---

#### Screenshot 7: New Sale Flow
**What to show:**
- Clean product selection interface
- Cart with items
- Payment method selector (M-Pesa, Cash, Bank)
- Total calculation
- Receipt generation preview

**Caption:** "Fast checkout, every time"

---

#### Screenshot 8: Net Profit Explanation
**What to show:**
- The info modal from HomeScreen
- Formula: "Selling price - Buying price - Expenses"
- Example calculation
- Clean, educational layout

**Caption:** "Understand your profit clearly"

---

### 3. App Icon (Required)
**Size:** 512 x 512 px  
**Format:** PNG (32-bit with alpha)  
**Purpose:** Shows in Play Store and on device

**Design Recommendations:**
```
Style: Simple, recognizable
Colors: Teal primary, white/contrasting element
Concept ideas:
- Stylized "N" or "NB" 
- Shopping bag with checkmark
- Business growth chart arrow
- Ledger/notebook icon modernized

Must be clear at small sizes!
```

---

### 4. Promo Graphic (Optional but Recommended)
**Size:** 180 x 120 px  
**Format:** PNG or JPG  
**Purpose:** Used in promotional campaigns

---

### 5. Promo Video (Optional - Higher Conversions)
**Length:** 30 seconds max  
**Format:** YouTube link

**Suggested Script:**
```
0-5s:   Problem: "Managing a small business shouldn't be complicated"
5-10s:  Show messy notebooks, confusion
10-15s: Introduce NeoBuk: "Track sales, stock, and profit in one simple app"
15-25s: Quick feature tour (Dashboard → Sales → Tasks)
25-30s: CTA: "Download NeoBuk - Free to start"
```

---

## Screenshot Best Practices

### DO:
✅ Use **real-looking data** (not Lorem Ipsum)
✅ Show **Kenyan context** (KES currency, Swahili names like "Mama Njeri")
✅ Highlight **unique features** (Tasks, Net Profit Info, Services)
✅ Keep UI **clean and uncluttered**
✅ Show **successful states** (not errors)
✅ Use **consistent branding** (Teal color throughout)

### DON'T:
❌ Show empty states in screenshots
❌ Use placeholder text
❌ Include unrealistic data (KES 999,999,999)
❌ Show ads or watermarks
❌ Use low-resolution images
❌ Include device frames (Google adds them)

---

## How to Capture Screenshots

### Option 1: Actual Device Screenshots (Best)
1. Run `./gradlew installDebug` on device
2. Navigate to each screen
3. Take screenshot (Power + Volume Down)
4. Screenshots saved to device Pictures folder

### Option 2: Android Studio Emulator
1. Run app in emulator
2. Click camera icon in emulator controls
3. Screenshots saved to project directory

### Option 3: Professional Tools
- **Figma/Sketch:** Design high-fidelity mockups
- **Screenly:** Add device frames and captions
- **AppMockUp:** Professional Play Store screenshot generator

---

## Screenshot Requirements Checklist

| Requirement | Spec | Status |
|-------------|------|--------|
| Minimum screenshots | 2 | ⬜ |
| Recommended screenshots | 8 | ⬜ |
| Resolution (Phone) | 1080x1920 px | ⬜ |
| Format | PNG or JPG | ⬜ |
| File size | Max 8MB each | ⬜ |
| Feature Graphic | 1024x500 px | ⬜ |
| App Icon | 512x512 px | ⬜ |

---

## Caption Formula

For each screenshot, use this structure:
```
[Action Verb] + [Benefit]

Examples:
✅ "Track sales in real-time"
✅ "Never miss a restock reminder"
✅ "Understand your profit instantly"

Avoid:
❌ "Sales Screen"
❌ "This is the dashboard where you can see things"
```

---

## Localization (Future)

When you add Swahili support, you'll need separate screenshots for:
- English (en-US)
- Swahili (sw-KE)

Play Store allows up to 8 screenshots per locale.

---

## Testing Your Screenshots

Before uploading, ask:
1. **Does it pass the 3-second test?** (Can someone understand the feature in 3 seconds?)
2. **Is the value clear?** (Not just "what" but "why")
3. **Does it match your brand?** (Teal, professional, trustworthy)
4. **Is text readable on mobile?** (Important details not too small)

---

## Screenshot Sequencing Strategy

**Order matters!** Most users only see the first 2-3 screenshots.

**Recommended Order:**
1. 🏆 Dashboard (Shows completeness)
2. 💰 Sales (Core value)
3. 📋 Tasks (Unique differentiator)
4. 📦 Products (Expected feature)
5. 📊 Reports (Professional credibility)
6. ✂️ Services (Expands market)
7. 🧾 Receipt (Trust signal)
8. 📖 Education (Net profit explanation)

---

## Tools & Resources

### Design Tools:
- **Figma:** Free, best for UI mockups
- **Canva:** Quick graphics and banners
- **Photoshop/GIMP:** Professional editing

### Screenshot Enhancers:
- **AppMockUp:** Adds device frames + captions
- **Previewed:** Beautiful app screenshots
- **Smartmockups:** Quick mockup generator

### Color Palette (For Consistency):
```
Primary Teal:    #00897B
Cyan Accent:     #00BCD4
Success Green:   #4CAF50
Warning Orange:  #F59E0B
Surface:         #FAFAFA
Text:            #1A1A1A
```

---

## What to Do Right Now

1. ✅ **Build and run your app** (`./gradlew installDebug`)
2. ✅ **Navigate to each key screen**
3. ✅ **Take actual screenshots** (8 total)
4. ⬜ **Edit if needed** (crop, optimize)
5. ⬜ **Add captions** (in Play Store Console)
6. ⬜ **Create feature graphic** (Canva template available)
7. ⬜ **Upload to Play Store draft listing**

---

## Example Screenshot Captions (Copy-Paste Ready)

```
1. Track sales, expenses, and profit — all in one simple dashboard
2. Record sales in seconds, view history anytime
3. Never run out of stock with smart reminders
4. Stay organized with Things to Do
5. Understand your business with clear reports and charts
6. Perfect for salons, spas, and service businesses
7. Fast checkout with M-Pesa, Cash, or Bank transfers
8. Clear explanations for every number
```

---

## Pro Tips

💡 **Tip 1:** Users scroll fast — put your best screenshot first
💡 **Tip 2:** Show results, not features ("Profit up 12%" not "Profit tracking")
💡 **Tip 3:** Use real Kenyan business names for credibility
💡 **Tip 4:** Screenshots convert better than videos for SME apps
💡 **Tip 5:** Update screenshots quarterly as app improves

---

## Final Note

Your app already has premium features. Make sure your screenshots **show that quality**.

Don't undersell with generic screenshots. Show:
- Professional UI ✅
- Real data ✅
- Unique features (Tasks!) ✅
- Kenyan context ✅

**Next Step:** Build the app, capture 8 screens, upload to Play Store Console draft.

---

**Need Help?**
If you want me to generate mockups once image generation is available, or create caption variations, just ask!
