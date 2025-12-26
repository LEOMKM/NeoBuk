-- ============================================
-- NEOBUK SALES FLOW SCHEMA
-- Flow 7: Sales and Transaction Management
-- ============================================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- SALES TABLE
-- ============================================
-- Main sales/transaction records

CREATE TABLE IF NOT EXISTS sales (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    
    -- Sale details
    sale_number VARCHAR(50),  -- Auto-generated sale reference number
    sale_type VARCHAR(20) DEFAULT 'PRODUCT',  -- PRODUCT, SERVICE, MIXED
    
    -- Amounts
    subtotal DECIMAL(12, 2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12, 2) DEFAULT 0,
    tax_amount DECIMAL(12, 2) DEFAULT 0,
    total_amount DECIMAL(12, 2) NOT NULL,
    
    -- Payment
    payment_method VARCHAR(50) DEFAULT 'Cash',  -- Cash, M-PESA, Card, Bank Transfer
    payment_reference VARCHAR(100),  -- M-PESA code, card reference, etc.
    payment_status VARCHAR(20) DEFAULT 'PAID',  -- PAID, PENDING, PARTIAL
    amount_paid DECIMAL(12, 2),
    change_given DECIMAL(12, 2) DEFAULT 0,
    
    -- Customer info (optional)
    customer_name VARCHAR(255),
    customer_phone VARCHAR(20),
    
    -- Staff info
    recorded_by UUID REFERENCES auth.users(id),
    
    -- Timestamps
    sale_date TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- SALE ITEMS TABLE
-- ============================================
-- Individual items in a sale (products or services)

CREATE TABLE IF NOT EXISTS sale_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sale_id UUID NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    
    -- Item type
    item_type VARCHAR(20) NOT NULL,  -- PRODUCT or SERVICE
    
    -- Product reference (for product sales)
    product_id UUID REFERENCES products(id) ON DELETE SET NULL,
    product_name VARCHAR(255),  -- Stored for history even if product deleted
    
    -- Service reference (for service sales)
    service_id UUID REFERENCES service_definitions(id) ON DELETE SET NULL,
    service_name VARCHAR(255),  -- Stored for history
    provider_id UUID REFERENCES service_providers(id) ON DELETE SET NULL,
    provider_name VARCHAR(255),  -- Stored for history
    
    -- Pricing
    quantity DECIMAL(12, 3) NOT NULL DEFAULT 1,
    unit_price DECIMAL(12, 2) NOT NULL,
    discount DECIMAL(12, 2) DEFAULT 0,
    total_price DECIMAL(12, 2) NOT NULL,
    
    -- For product items - cost for profit calculation
    unit_cost DECIMAL(12, 2) DEFAULT 0,
    
    -- For service items - commission
    commission_amount DECIMAL(12, 2) DEFAULT 0,
    
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX IF NOT EXISTS idx_sales_business ON sales(business_id);
CREATE INDEX IF NOT EXISTS idx_sales_date ON sales(sale_date DESC);
CREATE INDEX IF NOT EXISTS idx_sales_business_date ON sales(business_id, sale_date DESC);
CREATE INDEX IF NOT EXISTS idx_sales_number ON sales(business_id, sale_number);
CREATE INDEX IF NOT EXISTS idx_sale_items_sale ON sale_items(sale_id);
CREATE INDEX IF NOT EXISTS idx_sale_items_product ON sale_items(product_id);
CREATE INDEX IF NOT EXISTS idx_sale_items_service ON sale_items(service_id);

-- ============================================
-- ROW LEVEL SECURITY
-- ============================================

ALTER TABLE sales ENABLE ROW LEVEL SECURITY;
ALTER TABLE sale_items ENABLE ROW LEVEL SECURITY;

-- Drop existing policies
DROP POLICY IF EXISTS "Users can view their business sales" ON sales;
DROP POLICY IF EXISTS "Users can insert sales for their business" ON sales;
DROP POLICY IF EXISTS "Users can update their business sales" ON sales;
DROP POLICY IF EXISTS "Users can delete their business sales" ON sales;

DROP POLICY IF EXISTS "Users can view their business sale items" ON sale_items;
DROP POLICY IF EXISTS "Users can insert sale items for their business" ON sale_items;

-- Sales Policies
CREATE POLICY "Users can view their business sales"
    ON sales FOR SELECT
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can insert sales for their business"
    ON sales FOR INSERT
    WITH CHECK (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can update their business sales"
    ON sales FOR UPDATE
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can delete their business sales"
    ON sales FOR DELETE
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

-- Sale Items Policies (access through parent sale)
CREATE POLICY "Users can view their business sale items"
    ON sale_items FOR SELECT
    USING (sale_id IN (
        SELECT s.id FROM sales s
        JOIN businesses b ON s.business_id = b.id
        WHERE b.owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can insert sale items for their business"
    ON sale_items FOR INSERT
    WITH CHECK (sale_id IN (
        SELECT s.id FROM sales s
        JOIN businesses b ON s.business_id = b.id
        WHERE b.owner_user_id = auth.uid()
    ));

-- ============================================
-- TRIGGERS
-- ============================================

-- Update timestamp trigger
DROP TRIGGER IF EXISTS update_sales_updated_at ON sales;
CREATE TRIGGER update_sales_updated_at
    BEFORE UPDATE ON sales
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- FUNCTION: Generate Sale Number
-- ============================================

CREATE OR REPLACE FUNCTION generate_sale_number(p_business_id UUID)
RETURNS VARCHAR AS $$
DECLARE
    v_count INTEGER;
    v_date_prefix VARCHAR;
BEGIN
    -- Get current date as prefix (YYMMDD)
    v_date_prefix := TO_CHAR(NOW(), 'YYMMDD');
    
    -- Count today's sales for this business
    SELECT COUNT(*) + 1 INTO v_count
    FROM sales
    WHERE business_id = p_business_id
    AND DATE(sale_date) = CURRENT_DATE;
    
    -- Return formatted sale number: SL-YYMMDD-XXXX
    RETURN 'SL-' || v_date_prefix || '-' || LPAD(v_count::TEXT, 4, '0');
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- FUNCTION: Record Complete Sale with Stock Deduction
-- ============================================

CREATE OR REPLACE FUNCTION record_sale(
    p_business_id UUID,
    p_items JSONB,  -- Array of items: [{type, product_id, service_id, provider_id, quantity, unit_price, discount}]
    p_payment_method VARCHAR DEFAULT 'Cash',
    p_payment_reference VARCHAR DEFAULT NULL,
    p_customer_name VARCHAR DEFAULT NULL,
    p_customer_phone VARCHAR DEFAULT NULL,
    p_discount_amount DECIMAL DEFAULT 0
)
RETURNS sales AS $$
DECLARE
    v_sale sales;
    v_sale_number VARCHAR;
    v_subtotal DECIMAL := 0;
    v_total DECIMAL;
    v_item JSONB;
    v_item_type VARCHAR;
    v_product products;
    v_service service_definitions;
    v_provider service_providers;
    v_quantity DECIMAL;
    v_unit_price DECIMAL;
    v_item_discount DECIMAL;
    v_item_total DECIMAL;
    v_commission DECIMAL := 0;
BEGIN
    -- Generate sale number
    v_sale_number := generate_sale_number(p_business_id);
    
    -- Calculate subtotal from items
    FOR v_item IN SELECT * FROM jsonb_array_elements(p_items)
    LOOP
        v_quantity := COALESCE((v_item->>'quantity')::DECIMAL, 1);
        v_unit_price := (v_item->>'unit_price')::DECIMAL;
        v_item_discount := COALESCE((v_item->>'discount')::DECIMAL, 0);
        v_subtotal := v_subtotal + (v_quantity * v_unit_price) - v_item_discount;
    END LOOP;
    
    -- Calculate total
    v_total := v_subtotal - COALESCE(p_discount_amount, 0);
    
    -- Create the sale record
    INSERT INTO sales (
        business_id, sale_number, sale_type, subtotal, discount_amount,
        total_amount, payment_method, payment_reference, payment_status,
        amount_paid, customer_name, customer_phone, recorded_by
    ) VALUES (
        p_business_id, v_sale_number, 'MIXED', v_subtotal, p_discount_amount,
        v_total, p_payment_method, p_payment_reference, 'PAID',
        v_total, p_customer_name, p_customer_phone, auth.uid()
    )
    RETURNING * INTO v_sale;
    
    -- Process each item
    FOR v_item IN SELECT * FROM jsonb_array_elements(p_items)
    LOOP
        v_item_type := v_item->>'type';
        v_quantity := COALESCE((v_item->>'quantity')::DECIMAL, 1);
        v_unit_price := (v_item->>'unit_price')::DECIMAL;
        v_item_discount := COALESCE((v_item->>'discount')::DECIMAL, 0);
        v_item_total := (v_quantity * v_unit_price) - v_item_discount;
        
        IF v_item_type = 'PRODUCT' THEN
            -- Get product details
            SELECT * INTO v_product FROM products WHERE id = (v_item->>'product_id')::UUID;
            
            -- Insert sale item
            INSERT INTO sale_items (
                sale_id, item_type, product_id, product_name,
                quantity, unit_price, discount, total_price, unit_cost
            ) VALUES (
                v_sale.id, 'PRODUCT', v_product.id, v_product.name,
                v_quantity, v_unit_price, v_item_discount, v_item_total, v_product.cost_price
            );
            
            -- Deduct stock
            IF v_product.track_inventory THEN
                PERFORM update_product_stock(
                    v_product.id, -v_quantity, 'SALE', 
                    'Sale: ' || v_sale_number, v_sale.id, 'sale'
                );
            END IF;
            
        ELSIF v_item_type = 'SERVICE' THEN
            -- Get service and provider details
            SELECT * INTO v_service FROM service_definitions WHERE id = (v_item->>'service_id')::UUID;
            SELECT * INTO v_provider FROM service_providers WHERE id = (v_item->>'provider_id')::UUID;
            
            -- Calculate commission
            IF v_provider.commission_type = 'PERCENTAGE' THEN
                v_commission := v_item_total * (v_provider.commission_rate / 100);
            ELSE
                v_commission := v_provider.flat_fee;
            END IF;
            
            -- Insert sale item
            INSERT INTO sale_items (
                sale_id, item_type, service_id, service_name,
                provider_id, provider_name, quantity, unit_price, 
                discount, total_price, commission_amount
            ) VALUES (
                v_sale.id, 'SERVICE', v_service.id, v_service.name,
                v_provider.id, v_provider.full_name, v_quantity, v_unit_price,
                v_item_discount, v_item_total, v_commission
            );
        END IF;
    END LOOP;
    
    RETURN v_sale;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
