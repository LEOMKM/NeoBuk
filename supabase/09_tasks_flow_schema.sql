-- 09_tasks_flow_schema.sql

-- 1. Create tasks table
CREATE TABLE IF NOT EXISTS public.tasks (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    business_id UUID NOT NULL REFERENCES public.businesses(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT, -- Used for 'relatedLink' or details
    due_date TIMESTAMPTZ,
    status TEXT NOT NULL DEFAULT 'TODO', -- TODO, IN_PROGRESS, DONE
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Indexes
CREATE INDEX IF NOT EXISTS idx_tasks_business_id ON public.tasks(business_id);

-- 3. RLS
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;

-- Drop existing policies if any (for idempotent runs)
DROP POLICY IF EXISTS "Users can view tasks for their business" ON public.tasks;
DROP POLICY IF EXISTS "Users can insert tasks for their business" ON public.tasks;
DROP POLICY IF EXISTS "Users can update tasks for their business" ON public.tasks;
DROP POLICY IF EXISTS "Users can delete tasks for their business" ON public.tasks;

CREATE POLICY "Users can view tasks for their business"
    ON public.tasks FOR SELECT
    USING (
        business_id IN (
            SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
        )
    );

CREATE POLICY "Users can insert tasks for their business"
    ON public.tasks FOR INSERT
    WITH CHECK (
        business_id IN (
            SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
        )
    );

CREATE POLICY "Users can update tasks for their business"
    ON public.tasks FOR UPDATE
    USING (
        business_id IN (
            SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
        )
    );

CREATE POLICY "Users can delete tasks for their business"
    ON public.tasks FOR DELETE
    USING (
        business_id IN (
            SELECT id FROM public.businesses WHERE owner_user_id = auth.uid()
        )
    );

-- 4. Triggers
-- Define the function if it doesn't exist (it should from flow 02, but safe to repeat)
CREATE OR REPLACE FUNCTION public.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_tasks_updated_at ON public.tasks;
CREATE TRIGGER update_tasks_updated_at
    BEFORE UPDATE ON public.tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
