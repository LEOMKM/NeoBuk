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


-- Function to get Weekly Performance (Sales & Profit for current week Mon-Sun)
CREATE OR REPLACE FUNCTION get_weekly_performance(
    p_business_id UUID
)
RETURNS TABLE (
    day_name TEXT,
    total_sales DECIMAL(12, 2),
    total_profit DECIMAL(12, 2),
    day_date DATE
) AS $$
DECLARE
    v_start_of_week DATE;
BEGIN
    -- Get start of current week (Monday)
    v_start_of_week := date_trunc('week', CURRENT_DATE)::DATE;
    
    RETURN QUERY
    WITH date_series AS (
        SELECT generate_series(v_start_of_week, v_start_of_week + 6, '1 day'::interval)::DATE as date_val
    ),
    daily_revenue AS (
        SELECT DATE(sale_date) as d_date, SUM(total_amount) as amount
        FROM sales
        WHERE business_id = p_business_id AND payment_status = 'PAID' AND sale_date >= v_start_of_week
        GROUP BY 1
        UNION ALL
        SELECT DATE(date_offered) as d_date, SUM(service_price) as amount
        FROM service_records
        WHERE business_id = p_business_id AND date_offered >= v_start_of_week
        GROUP BY 1
    ),
    daily_expenses AS (
        SELECT DATE(expense_date) as d_date, SUM(amount) as amount
        FROM expenses
        WHERE business_id = p_business_id AND expense_date >= v_start_of_week
        GROUP BY 1
    ),
    total_daily_revenue AS (
        SELECT d_date, SUM(amount) as amount FROM daily_revenue GROUP BY 1
    )
    SELECT 
        TO_CHAR(ds.date_val, 'Dy') as day_name,
        COALESCE(tdr.amount, 0) as total_sales,
        COALESCE(tdr.amount, 0) - COALESCE(de.amount, 0) as total_profit,
        ds.date_val
    FROM date_series ds
    LEFT JOIN total_daily_revenue tdr ON ds.date_val = tdr.d_date
    LEFT JOIN daily_expenses de ON ds.date_val = de.d_date
    ORDER BY ds.date_val;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
