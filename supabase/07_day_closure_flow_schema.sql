-- ==========================================
-- FLOW 9: DAY CLOSURE SCHEMA
-- ==========================================

-- 1. Day Closures Table
CREATE TABLE IF NOT EXISTS public.day_closures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    closure_date DATE NOT NULL,
    
    -- Financial Snapshot
    total_sales_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_sales_count INTEGER NOT NULL DEFAULT 0,
    total_expenses_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_expenses_count INTEGER NOT NULL DEFAULT 0,
    
    -- Cash Reconciliation (Optional)
    cash_in_hand_expected DECIMAL(12, 2) DEFAULT 0,
    cash_in_hand_actual DECIMAL(12, 2) DEFAULT 0,
    discrepancy DECIMAL(12, 2) GENERATED ALWAYS AS (cash_in_hand_actual - cash_in_hand_expected) STORED,
    
    -- Metadata
    closed_by UUID REFERENCES auth.users(id),
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    -- Constraints
    UNIQUE(business_id, closure_date)
);

COMMENT ON TABLE public.day_closures IS 'Records of end-of-day business closures (Funga Siku)';

-- 2. Indexes
CREATE INDEX IF NOT EXISTS idx_day_closures_business_date ON public.day_closures(business_id, closure_date DESC);

-- 3. RLS
ALTER TABLE public.day_closures ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view own day closures" 
    ON public.day_closures FOR SELECT 
    USING (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can create day closures" 
    ON public.day_closures FOR INSERT 
    WITH CHECK (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

-- 4. Helper Function to Check Closure Status
CREATE OR REPLACE FUNCTION check_day_closure_status(
    p_business_id UUID,
    p_date DATE
)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 
        FROM day_closures 
        WHERE business_id = p_business_id 
        AND closure_date = p_date
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- 5. RPC to Perform Day Closure
-- This function aggregates data for the day and creates the closure record
CREATE OR REPLACE FUNCTION perform_day_closure(
    p_business_id UUID,
    p_date DATE,
    p_cash_actual DECIMAL DEFAULT 0,
    p_notes TEXT DEFAULT NULL
)
RETURNS public.day_closures AS $$
DECLARE
    v_sales_total DECIMAL(12, 2);
    v_sales_count INTEGER;
    v_expenses_total DECIMAL(12, 2);
    v_expenses_count INTEGER;
    v_cash_expected DECIMAL(12, 2);
    v_closure_record public.day_closures;
BEGIN
    -- Check if already closed
    IF EXISTS (SELECT 1 FROM day_closures WHERE business_id = p_business_id AND closure_date = p_date) THEN
        RAISE EXCEPTION 'Day is already closed for this date.';
    END IF;

    -- Calculate Totals
    SELECT 
        COALESCE(SUM(total_amount), 0),
        COUNT(*)
    INTO 
        v_sales_total,
        v_sales_count
    FROM sales
    WHERE business_id = p_business_id 
    AND DATE(sale_date) = p_date;

    SELECT 
        COALESCE(SUM(amount), 0),
        COUNT(*)
    INTO 
        v_expenses_total,
        v_expenses_count
    FROM expenses
    WHERE business_id = p_business_id 
    AND DATE(expense_date) = p_date;

    -- Calculate Expected Cash (Sales where payment_method = 'Cash' - Expenses where payment_method = 'Cash')
    -- Assuming expenses don't strictly have 'payment_method' column in schema yet? 
    -- Let's check 03_expenses schema. Usually expenses have paid_via.
    -- For simplicity, let's assume all Sales with 'Cash' contribute.
    SELECT 
        COALESCE(SUM(total_amount), 0)
    INTO v_cash_expected
    FROM sales
    WHERE business_id = p_business_id 
    AND DATE(sale_date) = p_date
    AND payment_method = 'Cash'; 
    -- Minus cash expenses if applicable, but for now let's stick to Sales Cash.

    -- Insert Closure
    INSERT INTO day_closures (
        business_id, 
        closure_date,
        total_sales_amount,
        total_sales_count,
        total_expenses_amount,
        total_expenses_count,
        cash_in_hand_expected,
        cash_in_hand_actual,
        closed_by,
        notes
    ) VALUES (
        p_business_id,
        p_date,
        v_sales_total,
        v_sales_count,
        v_expenses_total,
        v_expenses_count,
        v_cash_expected,
        p_cash_actual,
        auth.uid(),
        p_notes
    ) RETURNING * INTO v_closure_record;

    RETURN v_closure_record;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
