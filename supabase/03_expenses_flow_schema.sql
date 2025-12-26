-- ============================================
-- NEOBUK EXPENSES FLOW SCHEMA
-- Flow 5: Expense tracking and categorization
-- ============================================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- EXPENSE CATEGORIES TABLE
-- ============================================
-- Pre-defined categories for expenses

CREATE TABLE IF NOT EXISTS expense_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(50) DEFAULT 'MoreHoriz',
    color VARCHAR(20) DEFAULT '#6B7280',
    is_system BOOLEAN DEFAULT false,  -- System categories can't be deleted
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(business_id, name)
);

-- ============================================
-- EXPENSES TABLE
-- ============================================
-- Individual expense records

CREATE TABLE IF NOT EXISTS expenses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    category_id UUID REFERENCES expense_categories(id) ON DELETE SET NULL,
    
    -- Expense details
    title VARCHAR(255) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL CHECK (amount >= 0),
    description TEXT,
    
    -- Payment info
    payment_method VARCHAR(50) DEFAULT 'Cash',  -- Cash, M-PESA, Bank Transfer
    reference_number VARCHAR(100),  -- Transaction reference if applicable
    receipt_url TEXT,  -- URL to uploaded receipt image
    
    -- Timing
    expense_date DATE NOT NULL DEFAULT CURRENT_DATE,
    recorded_by UUID REFERENCES auth.users(id),
    
    -- Metadata
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX IF NOT EXISTS idx_expense_categories_business ON expense_categories(business_id);
CREATE INDEX IF NOT EXISTS idx_expenses_business ON expenses(business_id);
CREATE INDEX IF NOT EXISTS idx_expenses_category ON expenses(category_id);
CREATE INDEX IF NOT EXISTS idx_expenses_date ON expenses(expense_date);
CREATE INDEX IF NOT EXISTS idx_expenses_business_date ON expenses(business_id, expense_date DESC);

-- ============================================
-- ROW LEVEL SECURITY
-- ============================================

ALTER TABLE expense_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE expenses ENABLE ROW LEVEL SECURITY;

-- Drop existing policies if they exist
DROP POLICY IF EXISTS "Users can view their business expense categories" ON expense_categories;
DROP POLICY IF EXISTS "Users can insert expense categories for their business" ON expense_categories;
DROP POLICY IF EXISTS "Users can update their business expense categories" ON expense_categories;
DROP POLICY IF EXISTS "Users can delete their business expense categories" ON expense_categories;

DROP POLICY IF EXISTS "Users can view their business expenses" ON expenses;
DROP POLICY IF EXISTS "Users can insert expenses for their business" ON expenses;
DROP POLICY IF EXISTS "Users can update their business expenses" ON expenses;
DROP POLICY IF EXISTS "Users can delete their business expenses" ON expenses;

-- Expense Categories Policies
CREATE POLICY "Users can view their business expense categories"
    ON expense_categories FOR SELECT
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can insert expense categories for their business"
    ON expense_categories FOR INSERT
    WITH CHECK (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can update their business expense categories"
    ON expense_categories FOR UPDATE
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can delete their business expense categories"
    ON expense_categories FOR DELETE
    USING (
        business_id IN (
            SELECT id FROM businesses WHERE owner_user_id = auth.uid()
        )
        AND is_system = false  -- Can't delete system categories
    );

-- Expenses Policies
CREATE POLICY "Users can view their business expenses"
    ON expenses FOR SELECT
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can insert expenses for their business"
    ON expenses FOR INSERT
    WITH CHECK (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can update their business expenses"
    ON expenses FOR UPDATE
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can delete their business expenses"
    ON expenses FOR DELETE
    USING (business_id IN (
        SELECT id FROM businesses WHERE owner_user_id = auth.uid()
    ));

-- ============================================
-- TRIGGERS
-- ============================================

-- Update timestamp trigger
DROP TRIGGER IF EXISTS update_expense_categories_updated_at ON expense_categories;
CREATE TRIGGER update_expense_categories_updated_at
    BEFORE UPDATE ON expense_categories
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_expenses_updated_at ON expenses;
CREATE TRIGGER update_expenses_updated_at
    BEFORE UPDATE ON expenses
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- SEED DEFAULT CATEGORIES
-- ============================================
-- These will be created per business when they sign up
-- For now, we'll create a function to initialize categories for a business

CREATE OR REPLACE FUNCTION initialize_expense_categories(p_business_id UUID)
RETURNS void AS $$
BEGIN
    INSERT INTO expense_categories (business_id, name, icon, color, is_system)
    VALUES 
        (p_business_id, 'Utilities', 'Bolt', '#F59E0B', true),
        (p_business_id, 'Rent', 'Home', '#6366F1', true),
        (p_business_id, 'Supplies', 'ShoppingCart', '#10B981', true),
        (p_business_id, 'Transport', 'LocalShipping', '#3B82F6', true),
        (p_business_id, 'Salary', 'People', '#EC4899', true),
        (p_business_id, 'Maintenance', 'Build', '#8B5CF6', true),
        (p_business_id, 'Other', 'MoreHoriz', '#6B7280', true)
    ON CONFLICT (business_id, name) DO NOTHING;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
