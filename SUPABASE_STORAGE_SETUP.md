# 📸 NeoBuk Product Images - Supabase Storage Setup Guide

## Prerequisites
You need to set up Supabase Storage before product images will work.

---

## Step 1: Create Storage Bucket

1. Go to your **Supabase Dashboard**: https://supabase.com/dashboard
2. Select your project
3. Navigate to **Storage** in the left sidebar
4. Click **"New bucket"**
5. Enter the following details:
   - **Name:** `product-images`
   - **Public bucket:** ✅ **Enable** (images need to be publicly accessible)
   - Click **"Create bucket"**

---

## Step 2: Set Bucket Policies

The bucket needs to allow:
- ✅ **Authenticated users** can upload (INSERT)
- ✅ **Anyone** can view (SELECT) - for displaying images in the app

### Option A: Using the Dashboard (Recommended)

1. Click on the `product-images` bucket
2. Go to **"Policies"** tab
3. Click **"New Policy"**
4. Create **Upload Policy**:
   - **Policy name:** `Allow authenticated uploads`
   - **Allowed operation:** INSERT
   - **Target roles:** authenticated
   - **Policy definition:**
     ```sql
     authenticated
     ```
   - Click **"Review"** then **"Save policy"**

5. Create **View Policy**:
   - **Policy name:** `Allow public read`
   - **Allowed operation:** SELECT
   - **Target roles:** public, authenticated
   - **Policy definition:**
     ```sql
     true
     ```
   - Click **"Review"** then **"Save policy"**

### Option B: Using SQL (Advanced)

Go to **SQL Editor** and run:

```sql
-- Policy for authenticated users to upload images
CREATE POLICY "Allow authenticated uploads"
ON storage.objects
FOR INSERT
TO authenticated
WITH CHECK (bucket_id = 'product-images');

-- Policy for public read access
CREATE POLICY "Allow public read"
ON storage.objects
FOR SELECT
TO public, authenticated
USING (bucket_id = 'product-images');
```

---

## Step 3: Add image_url Column to Database

Run this SQL in the **SQL Editor**:

```sql
-- Add image_url column to products table
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS image_url TEXT;

-- Add index for faster queries
CREATE INDEX IF NOT EXISTS idx_products_image_url 
ON products(image_url) 
WHERE image_url IS NOT NULL;
```

---

## Step 4: Test the Setup

1. Open NeoBuk app
2. Go to **Products** → **Add Product**
3. Click **Manual Entry**
4. Click **Camera** or **Gallery** to add an image
5. Fill in product details
6. Click **"Add Product"**

### ✅ Success Indicators:
- Toast message: "Product added successfully!"
- Product appears in list with image
- Image loads when viewing product

### ❌ Common Errors:

| Error Message | Solution |
|--------------|----------|
| "Bucket not found" | Create `product-images` bucket in Supabase Storage |
| "Permission denied" | Set bucket to Public or add policies |
| "Failed to compress image" | Check camera/gallery permissions in Android settings |
| "Failed to upload image: null" | Check internet connection |

---

## Step 5: Verify Bucket Structure

Your bucket should organize images like this:

```
product-images/
└── {businessId}/
    ├── 1735251234567_temp_photo_123.jpg
    ├── 1735251245678_compressed_456.jpg
    └── ...
```

Each business has its own folder for organization.

---

## Troubleshooting

### Check Bucket Exists
```sql
SELECT * FROM storage.buckets WHERE name = 'product-images';
```

Should return:
```
id | name            | public | created_at
1  | product-images  | true   | 2025-12-27 ...
```

### Check Policies
```sql
SELECT * FROM storage.policies WHERE bucket_id = 'product-images';
```

Should show at least 2 policies (upload + read).

### View Uploaded Images
```sql
SELECT name, bucket_id, created_at 
FROM storage.objects 
WHERE bucket_id = 'product-images' 
ORDER BY created_at DESC 
LIMIT 10;
```

---

## Image Specifications

- **Format:** JPEG (automatically converted)
- **Max size:** 500KB (compressed)
- **Max dimensions:** 1024x1024px
- **Compression quality:** 90% initially, reduced if needed
- **EXIF orientation:** Automatically handled

---

## Security Notes

⚠️ **Important:**
- Images are **public** - anyone with the URL can view them
- Don't upload sensitive product information
- For V2: Consider private buckets with signed URLs for sensitive products

---

## Next Steps

Once images are working:
- [ ] Implement image deletion when product is deleted
- [ ] Add image editing/cropping
- [ ] Support multiple images per product
- [ ] Implement offline-first with local image caching

---

**Setup Complete!** 🎉

Your NeoBuk app can now handle product images with automatic compression and cloud storage.
