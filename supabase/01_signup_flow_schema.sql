-- ============================================
-- NEOBUK - COMPLETE SIGNUP FLOW SCHEMA
-- Run this in Supabase SQL Editor
-- ============================================
-- This creates all tables needed for the signup wizard:
-- Step 1: users (Account Basics)
-- Step 2: businesses (Business Setup)
-- Step 3 & 4: subscriptions + payments (Subscription & Payment)
-- ============================================

-- ============================================
-- 1. USERS TABLE (Account Basics - Step 1)
-- Links to Supabase Auth's built-in auth.users
-- ============================================
CREATE TABLE IF NOT EXISTS public.users (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    email TEXT,
    phone TEXT NOT NULL,
    accepted_terms_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.users IS 'Extended user profile information for NeoBuk users';

-- ============================================
-- 2. BUSINESSES TABLE (Business Setup - Step 2)
-- ============================================
CREATE TABLE IF NOT EXISTS public.businesses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_user_id UUID NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    business_name TEXT NOT NULL,
    category TEXT NOT NULL CHECK (category IN ('SERVICES', 'PRODUCTS')),
    subtype TEXT, -- e.g., "Salon", "Kinyozi", "Retail Shop"
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.businesses IS 'Business entities owned by users';

-- Index for quick lookup by owner
CREATE INDEX IF NOT EXISTS idx_businesses_owner ON public.businesses(owner_user_id);

-- ============================================
-- 3. SUBSCRIPTIONS TABLE (Subscription Plan - Step 3)
-- ============================================
CREATE TABLE IF NOT EXISTS public.subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    plan_type TEXT NOT NULL CHECK (plan_type IN ('FREE_TRIAL', 'MONTHLY', 'YEARLY')),
    price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    currency TEXT NOT NULL DEFAULT 'KES',
    status TEXT NOT NULL DEFAULT 'TRIALING' CHECK (status IN ('TRIALING', 'ACTIVE', 'PAST_DUE', 'GRACE_PERIOD', 'LOCKED', 'CANCELED')),
    trial_start TIMESTAMPTZ,
    trial_end TIMESTAMPTZ,
    current_period_start TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    current_period_end TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.subscriptions IS 'Subscription status and billing periods for businesses';

-- Index for quick lookup by business
CREATE INDEX IF NOT EXISTS idx_subscriptions_business ON public.subscriptions(business_id);

-- ============================================
-- 4. PAYMENTS TABLE (Payment - Step 4)
-- ============================================
CREATE TABLE IF NOT EXISTS public.payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID NOT NULL REFERENCES public.subscriptions(id) ON DELETE CASCADE,
    provider TEXT NOT NULL CHECK (provider IN ('MPESA', 'PAYSTACK', 'CHAPA')),
    amount DECIMAL(10, 2) NOT NULL,
    currency TEXT NOT NULL DEFAULT 'KES',
    reference TEXT, -- Transaction reference from provider
    phone_number TEXT, -- Phone used for M-PESA
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    paid_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.payments IS 'Payment transactions for subscriptions';

-- Index for quick lookup by subscription
CREATE INDEX IF NOT EXISTS idx_payments_subscription ON public.payments(subscription_id);

-- ============================================
-- ROW LEVEL SECURITY (RLS)
-- ============================================

-- Enable RLS on all tables
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.businesses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subscriptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.payments ENABLE ROW LEVEL SECURITY;

-- USERS POLICIES
CREATE POLICY "Users can view own profile" 
    ON public.users FOR SELECT 
    USING (auth.uid() = id);

CREATE POLICY "Users can update own profile" 
    ON public.users FOR UPDATE 
    USING (auth.uid() = id);

CREATE POLICY "Users can insert own profile" 
    ON public.users FOR INSERT 
    WITH CHECK (auth.uid() = id);

-- BUSINESSES POLICIES
CREATE POLICY "Users can view own businesses" 
    ON public.businesses FOR SELECT 
    USING (owner_user_id = auth.uid());

CREATE POLICY "Users can create own businesses" 
    ON public.businesses FOR INSERT 
    WITH CHECK (owner_user_id = auth.uid());

CREATE POLICY "Users can update own businesses" 
    ON public.businesses FOR UPDATE 
    USING (owner_user_id = auth.uid());

-- SUBSCRIPTIONS POLICIES
CREATE POLICY "Users can view own subscriptions" 
    ON public.subscriptions FOR SELECT 
    USING (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can create subscriptions for own businesses" 
    ON public.subscriptions FOR INSERT 
    WITH CHECK (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can update own subscriptions" 
    ON public.subscriptions FOR UPDATE 
    USING (business_id IN (
        SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
    ));

-- PAYMENTS POLICIES
CREATE POLICY "Users can view own payments" 
    ON public.payments FOR SELECT 
    USING (subscription_id IN (
        SELECT s.id FROM public.subscriptions s
        JOIN public.businesses b ON s.business_id = b.id
        WHERE b.owner_user_id = auth.uid()
    ));

CREATE POLICY "Users can create payments for own subscriptions" 
    ON public.payments FOR INSERT 
    WITH CHECK (subscription_id IN (
        SELECT s.id FROM public.subscriptions s
        JOIN public.businesses b ON s.business_id = b.id
        WHERE b.owner_user_id = auth.uid()
    ));

-- ============================================
-- TRIGGER: Auto-create user profile on signup
-- ============================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.users (id, full_name, email, phone)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'full_name', 'User'),
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'phone', NEW.phone, '')
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Drop existing trigger if exists, then create
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();

-- ============================================
-- HELPER FUNCTION: Calculate trial end date
-- ============================================
CREATE OR REPLACE FUNCTION public.calculate_trial_end(start_date TIMESTAMPTZ DEFAULT NOW())
RETURNS TIMESTAMPTZ AS $$
BEGIN
    RETURN start_date + INTERVAL '30 days';
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- HELPER FUNCTION: Calculate subscription end date
-- ============================================
CREATE OR REPLACE FUNCTION public.calculate_subscription_end(
    start_date TIMESTAMPTZ,
    plan TEXT
)
RETURNS TIMESTAMPTZ AS $$
BEGIN
    IF plan = 'MONTHLY' THEN
        RETURN start_date + INTERVAL '1 month';
    ELSIF plan = 'YEARLY' THEN
        RETURN start_date + INTERVAL '1 year';
    ELSE
        -- Free trial
        RETURN start_date + INTERVAL '30 days';
    END IF;
END;
$$ LANGUAGE plpgsql;

-- ============================================
-- VERIFICATION QUERIES
-- Run these to verify tables were created:
-- ============================================
-- SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';
-- SELECT * FROM public.users;
-- SELECT * FROM public.businesses;
-- SELECT * FROM public.subscriptions;
-- SELECT * FROM public.payments;
