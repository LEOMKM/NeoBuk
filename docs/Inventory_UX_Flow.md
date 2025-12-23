# Inventory UX Flow (Step-by-Step)

## 1. Flow A: Add New Product

### Screen 1: Add Product
**Fields:**
- Product name
- Category
- Cost price
- Selling price
- Unit (pcs, kg, litres)
- Initial quantity

**CTA: Generate barcode**

### Barcode Generation Strategy
**Option A: Backend-generated barcode (Recommended)**
- Frontend requests `/products/generate-barcode`
- Backend generates a unique numeric code (e.g., `893245001278`)
- **Benefits:** No collisions, Central control, Easy to scale.

### Screen 2: Barcode Preview
- Show barcode visually
- Show barcode number
- **Options:** Print, Save, Continue

**Handling Multiple Products:**
If multiple products are added, show cards:
- Product A | [Barcode Image] | 893245001278
- Product B | [Barcode Image] | 893245001279

**CTA: Submit products**
- **Backend Action:**
    1. Create product record.
    2. Create initial stock movement (+quantity).

---

## 2. Flow B: Adding stock for EXISTING product (Barcode Scan)

### Screen: Scan or Add Stock
1. Camera opens.
2. User scans barcode.

### Case 1: Barcode FOUND
- **Backend:** Returns product details.
- **Display:**
    - Product name
    - Current stock
- **Input:** Quantity to add
- **CTA:** Add Stock
- **Backend Action:** Insert stock movement (+quantity). 
- **Benefit:** Fast, No duplicates, No re-entry.

### Case 2: Barcode NOT FOUND
- **Display:** "Product not found. Add as new product?"
- **Buttons:**
    - ➕ Add New Product
    - ❌ Cancel
- **If Proceed:**
    - Pre-fill barcode field with the scanned code.
    - Redirect to **Add Product** screen.
- **Benefit:** Avoids duplicate barcodes and confusion.
