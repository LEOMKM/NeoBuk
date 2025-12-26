-- ==========================================
-- FLOW 10: DASHBOARD SCHEMA
-- ==========================================

-- Optimized function to get dashboard metrics in a single call
DROP FUNCTION IF EXISTS get_dashboard_metrics(UUID, TIMESTAMPTZ, TIMESTAMPTZ);
CREATE OR REPLACE FUNCTION get_dashboard_metrics(
    p_business_id UUID,
    p_start_date TIMESTAMPTZ,
    p_end_date TIMESTAMPTZ
)
RETURNS TABLE (
    today_sales DECIMAL(12, 2),
    today_expenses DECIMAL(12, 2),
    today_profit DECIMAL(12, 2), -- This is Net Profit
    sales_growth VARCHAR, 
    net_profit_margin DECIMAL(5, 2),
    gross_profit DECIMAL(12, 2)
) AS $$
DECLARE
    v_sales DECIMAL(12, 2) := 0;
    v_expenses DECIMAL(12, 2) := 0;
    v_services DECIMAL(12, 2) := 0;
    v_total_revenue DECIMAL(12, 2) := 0;
    v_total_cost DECIMAL(12, 2) := 0;
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
    -- 1. Today's Sales Revenue (Strictly from sales table)
    SELECT COALESCE(SUM(total_amount), 0) INTO v_sales
    FROM sales
    WHERE business_id = p_business_id 
    AND COALESCE(sale_date, created_at) BETWEEN p_start_date AND p_end_date
    AND payment_status IN ('PAID', 'PARTIAL');

    -- 2. Today's Cost of Goods Sold (From sale_items)
    SELECT COALESCE(SUM(si.quantity * si.unit_cost), 0) + COALESCE(SUM(si.commission_amount), 0)
    INTO v_total_cost
    FROM sale_items si
    JOIN sales s ON si.sale_id = s.id
    WHERE s.business_id = p_business_id 
    AND COALESCE(s.sale_date, s.created_at) BETWEEN p_start_date AND p_end_date
    AND s.payment_status IN ('PAID', 'PARTIAL');

    -- 3. Today's Service Revenue (if table exists)
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'service_records') THEN
        SELECT COALESCE(SUM(service_price), 0) INTO v_services
        FROM service_records
        WHERE business_id = p_business_id
        AND date_offered BETWEEN p_start_date AND p_end_date;
    END IF;

    v_total_revenue := v_sales + v_services;

    -- 4. Today's Expenses
    SELECT COALESCE(SUM(amount), 0) INTO v_expenses
    FROM expenses
    WHERE business_id = p_business_id
    AND expense_date = (p_start_date AT TIME ZONE 'Africa/Nairobi')::DATE;

    v_profit := v_total_revenue - v_total_cost - v_expenses;
    
    -- 5. Calculate Growth (Compare with Yesterday)
    -- define yesterday's range (assuming p_start_date is start of today)
    v_yesterday_start := p_start_date - INTERVAL '1 day';
    v_yesterday_end := p_end_date - INTERVAL '1 day';
    
    SELECT COALESCE(SUM(total_amount), 0) INTO v_yesterday_sales
    FROM sales 
    WHERE business_id = p_business_id 
    AND COALESCE(sale_date, created_at) BETWEEN v_yesterday_start AND v_yesterday_end
    AND payment_status IN ('PAID', 'PARTIAL');
    
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
        CASE WHEN v_total_revenue > 0 THEN (v_profit / v_total_revenue * 100) ELSE 0 END,
        (v_total_revenue - v_total_cost);
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
    v_today_nairobi DATE;
    v_start_of_week DATE;
BEGIN
    -- Get Today and Start of Week (Monday) in Nairobi
    v_today_nairobi := (CURRENT_TIMESTAMP AT TIME ZONE 'Africa/Nairobi')::DATE;
    v_start_of_week := date_trunc('week', CURRENT_TIMESTAMP AT TIME ZONE 'Africa/Nairobi')::DATE;
    
    RETURN QUERY
    WITH date_series AS (
        SELECT generate_series(v_start_of_week, v_start_of_week + 6, '1 day'::interval)::DATE as date_val
    ),
    daily_revenue AS (
        SELECT 
            (COALESCE(sale_date, created_at) AT TIME ZONE 'Africa/Nairobi')::DATE as d_date, 
            SUM(total_amount) as amount
        FROM sales
        WHERE business_id = p_business_id 
          AND payment_status IN ('PAID', 'PARTIAL')
          AND (COALESCE(sale_date, created_at) AT TIME ZONE 'Africa/Nairobi')::DATE >= v_start_of_week
        GROUP BY 1
        UNION ALL
        SELECT 
            (date_offered AT TIME ZONE 'Africa/Nairobi')::DATE as d_date, 
            SUM(service_price) as amount
        FROM service_records
        WHERE business_id = p_business_id 
          AND (date_offered AT TIME ZONE 'Africa/Nairobi')::DATE >= v_start_of_week
        GROUP BY 1
    ),
    daily_cogs AS (
        SELECT 
            d_date, 
            SUM(amount) as amount 
        FROM (
            -- Product COGS
            SELECT (COALESCE(s.sale_date, s.created_at) AT TIME ZONE 'Africa/Nairobi')::DATE as d_date, 
                   (si.quantity * si.unit_cost + si.commission_amount) as amount
            FROM sale_items si
            JOIN sales s ON si.sale_id = s.id
            WHERE s.business_id = p_business_id 
              AND s.payment_status IN ('PAID', 'PARTIAL')
              AND (COALESCE(s.sale_date, s.created_at) AT TIME ZONE 'Africa/Nairobi')::DATE >= v_start_of_week
            UNION ALL
            -- Service Commissions
            SELECT (date_offered AT TIME ZONE 'Africa/Nairobi')::DATE as d_date, 
                   commission_amount as amount
            FROM service_records
            WHERE business_id = p_business_id 
              AND (date_offered AT TIME ZONE 'Africa/Nairobi')::DATE >= v_start_of_week
        ) c GROUP BY 1
    ),
    daily_expenses AS (
        SELECT expense_date::DATE as d_date, SUM(amount) as amount
        FROM expenses
        WHERE business_id = p_business_id 
          AND expense_date >= v_start_of_week
        GROUP BY 1
    ),
    all_stats AS (
        SELECT d_date, amount as rev, 0 as cogs, 0 as expe FROM daily_revenue
        UNION ALL
        SELECT d_date, 0 as rev, amount as cogs, 0 as expe FROM daily_cogs
        UNION ALL
        SELECT d_date, 0 as rev, 0 as cogs, amount as expe FROM daily_expenses
    )
    SELECT 
        TO_CHAR(ds.date_val, 'Dy') as day_name,
        COALESCE(SUM(s.rev), 0)::DECIMAL(12, 2) as total_sales,
        (COALESCE(SUM(s.rev), 0) - COALESCE(SUM(s.cogs), 0) - COALESCE(SUM(s.expe), 0))::DECIMAL(12, 2) as total_profit,
        ds.date_val
    FROM date_series ds
    LEFT JOIN all_stats s ON ds.date_val = s.d_date
    GROUP BY ds.date_val
    ORDER BY ds.date_val;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
