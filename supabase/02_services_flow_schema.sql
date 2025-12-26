-- ============================================
-- NEOBUK - FLOW 4: SERVICES SCHEMA
-- Run this in Supabase SQL Editor
-- ============================================
-- Tables:
-- 1. service_providers (Staff who render services)
-- 2. service_definitions (Services offered)  
-- 3. service_records (Service transactions)
-- ============================================

-- ============================================
-- 1. SERVICE PROVIDERS (Staff)
-- ============================================
CREATE TABLE IF NOT EXISTS public.service_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'Service Provider',
    commission_type TEXT NOT NULL DEFAULT 'PERCENTAGE' CHECK (commission_type IN ('PERCENTAGE', 'FLAT_FEE')),
    commission_rate DECIMAL(5, 2) NOT NULL DEFAULT 0, -- e.g., 30.00 for 30%
    flat_fee DECIMAL(10, 2) NOT NULL DEFAULT 0, -- For flat fee type
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.service_providers IS 'Staff members who render services';

CREATE INDEX IF NOT EXISTS idx_service_providers_business ON public.service_providers(business_id);
CREATE INDEX IF NOT EXISTS idx_service_providers_active ON public.service_providers(business_id, is_active);

-- ============================================
-- 2. SERVICE DEFINITIONS
-- ============================================
CREATE TABLE IF NOT EXISTS public.service_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    commission_override DECIMAL(5, 2), -- Optional override for this specific service
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.service_definitions IS 'Services offered by the business';

CREATE INDEX IF NOT EXISTS idx_service_definitions_business ON public.service_definitions(business_id);
CREATE INDEX IF NOT EXISTS idx_service_definitions_active ON public.service_definitions(business_id, is_active);

-- ============================================
-- 3. SERVICE RECORDS (Transactions)
-- ============================================
CREATE TABLE IF NOT EXISTS public.service_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    
    -- Snapshot values (copied at time of recording for audit safety)
    service_name TEXT NOT NULL,
    service_provider_name TEXT NOT NULL,
    service_price DECIMAL(10, 2) NOT NULL,
    
    -- Commission details (locked at time of recording)
    commission_rate_used DECIMAL(5, 2) NOT NULL,
    commission_amount DECIMAL(10, 2) NOT NULL,
    business_amount DECIMAL(10, 2) NOT NULL,
    
    -- Metadata
    date_offered TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    recorded_by TEXT,
    
    -- References (for linking, values are snapshotted above)
    service_id UUID REFERENCES public.service_definitions(id) ON DELETE SET NULL,
    provider_id UUID REFERENCES public.service_providers(id) ON DELETE SET NULL,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.service_records IS 'Service transactions with snapshot data for audit';

CREATE INDEX IF NOT EXISTS idx_service_records_business ON public.service_records(business_id);
CREATE INDEX IF NOT EXISTS idx_service_records_date ON public.service_records(business_id, date_offered DESC);
CREATE INDEX IF NOT EXISTS idx_service_records_provider ON public.service_records(provider_id);

-- ============================================
-- ENABLE RLS
-- ============================================
ALTER TABLE public.service_providers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.service_definitions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.service_records ENABLE ROW LEVEL SECURITY;

-- ============================================
-- DROP EXISTING POLICIES (Safe re-run)
-- ============================================
DROP POLICY IF EXISTS "Users can view own service providers" ON public.service_providers;
DROP POLICY IF EXISTS "Users can create service providers" ON public.service_providers;
DROP POLICY IF EXISTS "Users can update own service providers" ON public.service_providers;
DROP POLICY IF EXISTS "Users can delete own service providers" ON public.service_providers;

DROP POLICY IF EXISTS "Users can view own service definitions" ON public.service_definitions;
DROP POLICY IF EXISTS "Users can create service definitions" ON public.service_definitions;
DROP POLICY IF EXISTS "Users can update own service definitions" ON public.service_definitions;
DROP POLICY IF EXISTS "Users can delete own service definitions" ON public.service_definitions;

DROP POLICY IF EXISTS "Users can view own service records" ON public.service_records;
DROP POLICY IF EXISTS "Users can create service records" ON public.service_records;

-- ============================================
-- SERVICE PROVIDERS POLICIES
-- ============================================
CREATE POLICY "Users can view own service providers" 
    ON public.service_providers FOR SELECT 
    USING (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can create service providers" 
    ON public.service_providers FOR INSERT 
    WITH CHECK (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can update own service providers" 
    ON public.service_providers FOR UPDATE 
    USING (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can delete own service providers" 
    ON public.service_providers FOR DELETE 
    USING (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

-- ============================================
-- SERVICE DEFINITIONS POLICIES
-- ============================================
CREATE POLICY "Users can view own service definitions" 
    ON public.service_definitions FOR SELECT 
    USING (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can create service definitions" 
    ON public.service_definitions FOR INSERT 
    WITH CHECK (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can update own service definitions" 
    ON public.service_definitions FOR UPDATE 
    USING (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can delete own service definitions" 
    ON public.service_definitions FOR DELETE 
    USING (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

-- ============================================
-- SERVICE RECORDS POLICIES
-- ============================================
CREATE POLICY "Users can view own service records" 
    ON public.service_records FOR SELECT 
    USING (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can create service records" 
    ON public.service_records FOR INSERT 
    WITH CHECK (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

-- ============================================
-- HELPER: Update timestamp trigger
-- ============================================
CREATE OR REPLACE FUNCTION public.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply to service tables
DROP TRIGGER IF EXISTS update_service_providers_updated_at ON public.service_providers;
CREATE TRIGGER update_service_providers_updated_at
    BEFORE UPDATE ON public.service_providers
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at_column();

DROP TRIGGER IF EXISTS update_service_definitions_updated_at ON public.service_definitions;
CREATE TRIGGER update_service_definitions_updated_at
    BEFORE UPDATE ON public.service_definitions
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at_column();

-- ============================================
-- VERIFICATION QUERIES
-- ============================================
-- SELECT * FROM public.service_providers;
-- SELECT * FROM public.service_definitions;
-- SELECT * FROM public.service_records;
