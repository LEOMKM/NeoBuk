-- ==========================================
-- FLOW 10: DASHBOARD SCHEMA
-- ==========================================

-- Optimized function to get dashboard metrics in a single call
CREATE OR REPLACE FUNCTION get_dashboard_metrics(
    p_business_id UUID,
    p_start_date TIMESTAMPTZ,
    p_end_date TIMESTAMPTZ
)
RETURNS TABLE (
    today_sales DECIMAL(12, 2),
    today_expenses DECIMAL(12, 2),
    today_profit DECIMAL(12, 2),
    sales_growth VARCHAR, 
    net_profit_margin DECIMAL(5, 2)
) AS $$
DECLARE
    v_sales DECIMAL(12, 2) := 0;
    v_expenses DECIMAL(12, 2) := 0;
    v_services DECIMAL(12, 2) := 0;
    v_total_revenue DECIMAL(12, 2) := 0;
    v_profit DECIMAL(12, 2) := 0;
    
    -- Variables for previous period (yesterday)
    v_yesterday_start TIMESTAMPTZ;
    v_yesterday_end TIMESTAMPTZ;
    v_yesterday_sales DECIMAL(12, 2) := 0;
    v_yesterday_services DECIMAL(12, 2) := 0;
    v_yesterday_revenue DECIMAL(12, 2) := 0;
    v_growth_decimal DECIMAL(10, 2) := 0;
    v_growth_sign TEXT := '';
BEGIN
    -- 1. Today's Sales Revenue
    SELECT COALESCE(SUM(total_amount), 0) INTO v_sales
    FROM sales 
    WHERE business_id = p_business_id 
    AND sale_date BETWEEN p_start_date AND p_end_date
    AND payment_status = 'PAID';

    -- 2. Today's Service Revenue (if table exists)
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'service_records') THEN
        SELECT COALESCE(SUM(service_price), 0) INTO v_services
        FROM service_records
        WHERE business_id = p_business_id
        AND date_offered BETWEEN p_start_date AND p_end_date;
    END IF;

    v_total_revenue := v_sales + v_services;

    -- 3. Today's Expenses
    SELECT COALESCE(SUM(amount), 0) INTO v_expenses
    FROM expenses
    WHERE business_id = p_business_id
    AND expense_date BETWEEN p_start_date AND p_end_date;

    v_profit := v_total_revenue - v_expenses;
    
    -- 4. Calculate Growth (Compare with Yesterday)
    -- define yesterday's range (assuming p_start_date is start of today)
    v_yesterday_start := p_start_date - INTERVAL '1 day';
    v_yesterday_end := p_end_date - INTERVAL '1 day';
    
    SELECT COALESCE(SUM(total_amount), 0) INTO v_yesterday_sales
    FROM sales 
    WHERE business_id = p_business_id 
    AND sale_date BETWEEN v_yesterday_start AND v_yesterday_end
    AND payment_status = 'PAID';
    
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'service_records') THEN
        SELECT COALESCE(SUM(service_price), 0) INTO v_yesterday_services
        FROM service_records
        WHERE business_id = p_business_id
        AND date_offered BETWEEN v_yesterday_start AND v_yesterday_end;
    END IF;
    
    v_yesterday_revenue := v_yesterday_sales + v_yesterday_services;
    
    IF v_yesterday_revenue > 0 THEN
        v_growth_decimal := ((v_total_revenue - v_yesterday_revenue) / v_yesterday_revenue) * 100;
        IF v_growth_decimal > 0 THEN v_growth_sign := '+'; END IF;
        -- Format: "+12%" or "-5%"
        sales_growth := v_growth_sign || ROUND(v_growth_decimal, 0) || '%';
    ELSIF v_total_revenue > 0 THEN
        sales_growth := '+100%'; -- Growth from 0 to something
    ELSE
        sales_growth := '0%'; -- No change (0 to 0)
    END IF;

    -- Return
    RETURN QUERY SELECT 
        v_total_revenue,
        v_expenses,
        v_profit,
        sales_growth,
        CASE WHEN v_total_revenue > 0 THEN (v_profit / v_total_revenue * 100) ELSE 0 END;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
