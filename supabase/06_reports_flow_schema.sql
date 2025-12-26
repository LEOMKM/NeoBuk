-- ==========================================
-- FLOW 8: REPORTS SCHEMA
-- ==========================================

-- Function to get a comprehensive report summary for a given period
DROP FUNCTION IF EXISTS get_report_summary(UUID, TIMESTAMPTZ, TIMESTAMPTZ);
CREATE OR REPLACE FUNCTION get_report_summary(
    p_business_id UUID,
    p_start_date TIMESTAMPTZ,
    p_end_date TIMESTAMPTZ
)
RETURNS TABLE (
    total_sales DECIMAL(12, 2),
    sales_count BIGINT,
    avg_sale_value DECIMAL(12, 2),
    total_expenses DECIMAL(12, 2),
    gross_profit DECIMAL(12, 2),
    net_profit DECIMAL(12, 2),
    net_profit_margin DECIMAL(5, 2)
) AS $$
DECLARE
    v_sales_revenue DECIMAL(12, 2) := 0;
    v_sales_count BIGINT := 0;
    v_service_revenue DECIMAL(12, 2) := 0;
    v_service_count BIGINT := 0;
    
    v_total_revenue DECIMAL(12, 2) := 0;
    v_total_count BIGINT := 0;
    
    v_total_expenses DECIMAL(12, 2) := 0;
    v_total_cost DECIMAL(12, 2) := 0;
BEGIN
    -- 1. Revenue from Sales (POS) - Calculated strictly from sales table
    SELECT 
        COALESCE(SUM(total_amount), 0),
        COUNT(*)
    INTO 
        v_sales_revenue,
        v_sales_count
    FROM sales
    WHERE business_id = p_business_id 
    AND COALESCE(sale_date, created_at) BETWEEN p_start_date AND p_end_date
    AND payment_status IN ('PAID', 'PARTIAL');

    -- 2. COGS from Sale Items - Calculated from sale_items joined with sales
    SELECT 
        COALESCE(SUM(si.quantity * si.unit_cost), 0) + COALESCE(SUM(si.commission_amount), 0)
    INTO 
        v_total_cost
    FROM sale_items si
    JOIN sales s ON si.sale_id = s.id
    WHERE s.business_id = p_business_id 
    AND COALESCE(s.sale_date, s.created_at) BETWEEN p_start_date AND p_end_date
    AND s.payment_status IN ('PAID', 'PARTIAL');

    -- 3. Revenue from Service Records (Work Logs)
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'service_records') THEN
        SELECT 
            COALESCE(SUM(service_price), 0),
            COUNT(*)
        INTO 
            v_service_revenue,
            v_service_count
        FROM service_records
        WHERE business_id = p_business_id
        AND date_offered BETWEEN p_start_date AND p_end_date;
    END IF;

    v_total_revenue := v_sales_revenue + v_service_revenue;
    v_total_count := v_sales_count + v_service_count;

    -- 4. Expenses
    SELECT 
        COALESCE(SUM(amount), 0)
    INTO 
        v_total_expenses
    FROM expenses
    WHERE business_id = p_business_id
    AND expense_date >= (p_start_date AT TIME ZONE 'Africa/Nairobi')::DATE
    AND expense_date <= (p_end_date AT TIME ZONE 'Africa/Nairobi')::DATE;

    -- 5. Calculate Margins and Profits
    -- Gross Profit = Revenue - COGS
    -- Net Profit = Gross Profit - OpEx
    
    -- Return result
    RETURN QUERY SELECT 
        v_total_revenue,
        v_total_count,
        CASE WHEN v_total_count > 0 THEN v_total_revenue / v_total_count ELSE 0 END,
        v_total_expenses,
        (v_total_revenue - v_total_cost),
        (v_total_revenue - v_total_cost - v_total_expenses),
        CASE WHEN v_total_revenue > 0 THEN ((v_total_revenue - v_total_cost - v_total_expenses) / v_total_revenue * 100) ELSE 0 END;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- Function to get top selling products
CREATE OR REPLACE FUNCTION get_top_selling_products(
    p_business_id UUID,
    p_start_date TIMESTAMPTZ,
    p_end_date TIMESTAMPTZ,
    p_limit INTEGER DEFAULT 5
)
RETURNS TABLE (
    product_name VARCHAR,
    units_sold DECIMAL,
    revenue DECIMAL(12, 2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        si.product_name,
        SUM(si.quantity) as units_sold,
        SUM(si.total_price) as revenue
    FROM sale_items si
    JOIN sales s ON si.sale_id = s.id
    WHERE s.business_id = p_business_id
    AND COALESCE(s.sale_date, s.created_at) BETWEEN p_start_date AND p_end_date
    AND si.item_type = 'PRODUCT'
    GROUP BY si.product_id, si.product_name
    ORDER BY revenue DESC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- Function to get sales breakdown by payment method
CREATE OR REPLACE FUNCTION get_sales_by_payment_method_stats(
    p_business_id UUID,
    p_start_date TIMESTAMPTZ,
    p_end_date TIMESTAMPTZ
)
RETURNS TABLE (
    payment_method VARCHAR,
    total_amount DECIMAL(12, 2),
    transaction_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        s.payment_method,
        COALESCE(SUM(s.total_amount), 0) as total_amount,
        COUNT(*) as transaction_count
    FROM sales s
    WHERE s.business_id = p_business_id
    AND COALESCE(s.sale_date, s.created_at) BETWEEN p_start_date AND p_end_date
    GROUP BY s.payment_method
    ORDER BY total_amount DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;


-- Function to get daily sales trend
CREATE OR REPLACE FUNCTION get_daily_sales_trend(
    p_business_id UUID,
    p_start_date TIMESTAMPTZ,
    p_end_date TIMESTAMPTZ
)
RETURNS TABLE (
    period_date DATE,
    daily_total DECIMAL(12, 2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        d_date as period_date,
        SUM(amount) as daily_total
    FROM (
        -- POS Sales
        SELECT 
            (COALESCE(sale_date, created_at) AT TIME ZONE 'UTC' AT TIME ZONE 'Africa/Nairobi')::DATE as d_date, 
            total_amount as amount
        FROM sales
        WHERE business_id = p_business_id
        AND COALESCE(sale_date, created_at) BETWEEN p_start_date AND p_end_date
        AND payment_status IN ('PAID', 'PARTIAL')
        
        UNION ALL
        
        -- Service Revenue
        SELECT 
            (date_offered AT TIME ZONE 'UTC' AT TIME ZONE 'Africa/Nairobi')::DATE as d_date, 
            service_price as amount
        FROM service_records
        WHERE business_id = p_business_id
        AND date_offered BETWEEN p_start_date AND p_end_date
    ) combined_sales
    GROUP BY d_date
    ORDER BY d_date;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
