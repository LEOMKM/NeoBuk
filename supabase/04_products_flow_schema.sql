-- ============================================
-- NEOBUK PRODUCTS FLOW SCHEMA
-- Flow 6: Product/Inventory Management
-- ============================================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- PRODUCT CATEGORIES TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS product_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(business_id, name)
);

-- ============================================
-- PRODUCTS TABLE
-- ============================================

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    category_id UUID REFERENCES product_categories(id) ON DELETE SET NULL,
    
    -- Product details
    name VARCHAR(255) NOT NULL,
    description TEXT,
    barcode VARCHAR(100),  -- Can be empty for products without barcode
    sku VARCHAR(100),  -- Stock Keeping Unit
    unit VARCHAR(50) DEFAULT 'pcs',  -- pcs, kg, litres, etc.
    
    -- Pricing
    cost_price DECIMAL(12, 2) NOT NULL DEFAULT 0 CHECK (cost_price >= 0),
    selling_price DECIMAL(12, 2) NOT NULL CHECK (selling_price >= 0),
    
    -- Stock info
    quantity DECIMAL(12, 3) NOT NULL DEFAULT 0,  -- Supports decimals for kg, litres
    low_stock_threshold DECIMAL(12, 3) DEFAULT 5,
    
    -- Settings
    track_inventory BOOLEAN DEFAULT true,
    is_active BOOLEAN DEFAULT true,
    
    -- Metadata
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    -- Unique barcode per business (allowing null for products without barcode)
    UNIQUE(business_id, barcode)
);

-- ============================================
-- STOCK MOVEMENTS TABLE
-- ============================================
-- Track all stock changes for audit trail

CREATE TABLE IF NOT EXISTS stock_movements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    
    -- Movement details
    quantity_change DECIMAL(12, 3) NOT NULL,  -- Positive for additions, negative for deductions
    reason VARCHAR(50) NOT NULL,  -- MANUAL_ADD, SALE, ADJUSTMENT, RETURN, DAMAGE
    notes TEXT,
    
    -- Balance after movement
    balance_after DECIMAL(12, 3),
    
    -- Reference to related records (e.g., sale_id if sold)
    reference_id UUID,
    reference_type VARCHAR(50),  -- 'sale', 'purchase', 'adjustment'
    
    -- Who made the change
    recorded_by UUID REFERENCES auth.users(id),
    
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX IF NOT EXISTS idx_product_categories_business ON product_categories(business_id);
CREATE INDEX IF NOT EXISTS idx_products_business ON products(business_id);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_barcode ON products(business_id, barcode);
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(business_id, sku);
CREATE INDEX IF NOT EXISTS idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_business ON stock_movements(business_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_created ON stock_movements(created_at DESC);

-- ============================================
-- ROW LEVEL SECURITY
-- ============================================

ALTER TABLE product_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_movements ENABLE ROW LEVEL SECURITY;

-- Drop existing policies
DROP POLICY IF EXISTS "Users can view their business product categories" ON product_categories;
DROP POLICY IF EXISTS "Users can insert product categories for their business" ON product_categories;
DROP POLICY IF EXISTS "Users can update their business product categories" ON product_categories;
DROP POLICY IF EXISTS "Users can delete their business product categories" ON product_categories;

DROP POLICY IF EXISTS "Users can view their business products" ON products;
DROP POLICY IF EXISTS "Users can insert products for their business" ON products;
DROP POLICY IF EXISTS "Users can update their business products" ON products;
DROP POLICY IF EXISTS "Users can delete their business products" ON products;

DROP POLICY IF EXISTS "Users can view their business stock movements" ON stock_movements;
DROP POLICY IF EXISTS "Users can insert stock movements for their business" ON stock_movements;

-- Product Categories Policies
CREATE POLICY "Users can view their business product categories"
    ON product_categories FOR SELECT
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can insert product categories for their business"
    ON product_categories FOR INSERT
    WITH CHECK (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can update their business product categories"
    ON product_categories FOR UPDATE
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can delete their business product categories"
    ON product_categories FOR DELETE
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

-- Products Policies
CREATE POLICY "Users can view their business products"
    ON products FOR SELECT
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can insert products for their business"
    ON products FOR INSERT
    WITH CHECK (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can update their business products"
    ON products FOR UPDATE
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can delete their business products"
    ON products FOR DELETE
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

-- Stock Movements Policies
CREATE POLICY "Users can view their business stock movements"
    ON stock_movements FOR SELECT
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can insert stock movements for their business"
    ON stock_movements FOR INSERT
    WITH CHECK (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

-- ============================================
-- TRIGGERS
-- ============================================

-- Update timestamp trigger
DROP TRIGGER IF EXISTS update_product_categories_updated_at ON product_categories;
CREATE TRIGGER update_product_categories_updated_at
    BEFORE UPDATE ON product_categories
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_products_updated_at ON products;
CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- FUNCTION: Update Stock with Automatic Movement Recording
-- ============================================

CREATE OR REPLACE FUNCTION update_product_stock(
    p_product_id UUID,
    p_quantity_change DECIMAL,
    p_reason VARCHAR,
    p_notes TEXT DEFAULT NULL,
    p_reference_id UUID DEFAULT NULL,
    p_reference_type VARCHAR DEFAULT NULL
)
RETURNS products AS $$
DECLARE
    v_product products;
    v_business_id UUID;
    v_new_quantity DECIMAL;
BEGIN
    -- Get current product and lock the row
    SELECT * INTO v_product FROM products WHERE id = p_product_id FOR UPDATE;
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Product not found';
    END IF;
    
    v_business_id := v_product.business_id;
    v_new_quantity := v_product.quantity + p_quantity_change;
    
    -- Prevent negative stock if tracking inventory
    IF v_product.track_inventory AND v_new_quantity < 0 THEN
        RAISE EXCEPTION 'Insufficient stock. Current: %, Requested: %', v_product.quantity, ABS(p_quantity_change);
    END IF;
    
    -- Update product quantity
    UPDATE products SET quantity = v_new_quantity WHERE id = p_product_id;
    
    -- Record the movement
    INSERT INTO stock_movements (
        business_id, product_id, quantity_change, reason, notes, 
        balance_after, reference_id, reference_type, recorded_by
    ) VALUES (
        v_business_id, p_product_id, p_quantity_change, p_reason, p_notes,
        v_new_quantity, p_reference_id, p_reference_type, auth.uid()
    );
    
    -- Return updated product
    SELECT * INTO v_product FROM products WHERE id = p_product_id;
    RETURN v_product;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
