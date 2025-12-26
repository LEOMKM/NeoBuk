-- ============================================
-- NeoBuk Product Images - Complete Setup SQL
-- ============================================
-- Run this entire script in Supabase SQL Editor
-- It will create the bucket, set policies, and add the database column

-- Step 1: Create the storage bucket
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'product-images',
    'product-images', 
    true,  -- Public bucket
    5242880,  -- 5MB limit
    ARRAY['image/jpeg', 'image/jpg', 'image/png', 'image/webp']
)
ON CONFLICT (id) DO NOTHING;

-- Step 2: Drop existing policies if they exist (to avoid duplicates)
DROP POLICY IF EXISTS "Allow authenticated uploads" ON storage.objects;
DROP POLICY IF EXISTS "Allow public read" ON storage.objects;
DROP POLICY IF EXISTS "Allow authenticated delete" ON storage.objects;

-- Step 3: Create upload policy (authenticated users can upload)
CREATE POLICY "Allow authenticated uploads"
ON storage.objects
FOR INSERT
TO authenticated
WITH CHECK (bucket_id = 'product-images');

-- Step 4: Create read policy (anyone can view - needed for public images)
CREATE POLICY "Allow public read"
ON storage.objects
FOR SELECT
TO public
USING (bucket_id = 'product-images');

-- Step 5: Create delete policy (authenticated users can delete their uploads)
CREATE POLICY "Allow authenticated delete"
ON storage.objects
FOR DELETE
TO authenticated
USING (bucket_id = 'product-images');

-- Step 6: Add image_url column to products table
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS image_url TEXT;

-- Step 7: Add index for better performance
CREATE INDEX IF NOT EXISTS idx_products_image_url 
ON products(image_url) 
WHERE image_url IS NOT NULL;

-- Step 8: Verify setup
SELECT 
    'Bucket created: ' || name || ' (public: ' || public || ')' as status
FROM storage.buckets 
WHERE id = 'product-images'
UNION ALL
SELECT 
    'Policies created: ' || COUNT(*)::text as status
FROM pg_policies 
WHERE tablename = 'objects' 
AND schemaname = 'storage'
AND (policyname LIKE '%authenticated uploads%' 
     OR policyname LIKE '%public read%' 
     OR policyname LIKE '%authenticated delete%')
UNION ALL
SELECT 
    'Column added: image_url exists in products table' as status
FROM information_schema.columns 
WHERE table_name = 'products' 
AND column_name = 'image_url';

-- ============================================
-- Setup Complete! ✅
-- ============================================
-- You should see 3 status messages confirming:
-- 1. Bucket created: product-images (public: true)
-- 2. Policies created: 3
-- 3. Column added: image_url exists in products table
-- 
-- Now you can upload product images in the app!
-- ============================================
