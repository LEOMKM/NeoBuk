# 🔧 Product Image Upload - Quick Fix Guide

## Error: "Failed to upload image"

### Most Common Cause: ⚠️ **Supabase Storage Bucket Not Created**

**Quick Fix:**
1. Go to **Supabase Dashboard** → **Storage**
2. Click **"New bucket"**
3. Name: `product-images`
4. Check ✅ **"Public bucket"**
5. Click **"Create bucket"**

---

## Step-by-Step Debugging

### 1️⃣ Check Android Logs

Run this command in terminal:
```bash
adb logcat | grep ProductsViewModel
```

Look for:
- ✅ `Starting image compression for URI:` - Compression started
- ✅ `Image compressed successfully. Size: XXkb` - Compression worked
- ✅ `Starting upload to Supabase...` - Upload attempt
- ❌ `Image compression failed` - Problem with image file
- ❌ `Upload failed:` - Problem with Supabase

### 2️⃣ Common Error Messages & Solutions

| Error Message | Cause | Solution |
|--------------|-------|----------|
| "Storage bucket 'product-images' not found" | Bucket doesn't exist | Create bucket in Supabase Dashboard |
| "Permission denied" | Bucket not public or wrong policies | Make bucket public or add policies |
| "Failed to compress image" | Invalid image file | Try different image |
| "Network error" | No internet | Check WiFi/data connection |

### 3️⃣ Verify Supabase Setup

**Check 1: Bucket Exists**
```sql
SELECT * FROM storage.buckets WHERE name = 'product-images';
```
Should return 1 row.

**Check 2: Bucket is Public**
```sql
SELECT name, public FROM storage.buckets WHERE name = 'product-images';
```
`public` column should be `true`.

**Check 3: Database Column Exists**
```sql
SELECT column_name 
FROM information_schema.columns 
WHERE table_name = 'products' 
AND column_name = 'image_url';
```
Should return `image_url`.

### 4️⃣ Test Without Image First

Try adding a product **without** selecting an image:
- If this works → Issue is with image upload
- If this fails → Issue is with product creation

### 5️⃣ Check Permissions

**Android Permissions:**
```xml
<!-- In AndroidManifest.xml -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

**Device Settings:**
1. Open Android Settings
2. Apps → NeoBuk
3. Permissions
4. Enable ✅ Camera and ✅ Storage

---

## Quick Tests

### Test 1: Can you take/select photo?
- ✅ Camera opens → Permission OK
- ❌ Crashes → Check camera permission

### Test 2: Does compression work?
Look for log: `Image compressed successfully. Size: XXkb`
- ✅ See this → Compression OK
- ❌ Don't see this → Image file issue

### Test 3: Does upload start?
Look for log: `Starting upload to Supabase...`
- ✅ See this → Network OK
- ❌ Don't see this → Compression failed

### Test 4: What's the actual error?
Look for log: `Upload failed: [ERROR MESSAGE]`
This tells you exactly what went wrong.

---

## Manual Bucket Creation (Visual Guide)

1. **Login to Supabase:**
   - Go to https://supabase.com/dashboard
   - Select your project

2. **Navigate to Storage:**
   - Click "Storage" in left sidebar
   - You should see list of buckets (might be empty)

3. **Create Bucket:**
   - Click "New bucket" button
   - Enter "product-images" as name
   - Toggle ON "Public bucket"
   - Click "Create bucket"

4. **Verify:**
   - You should now see "product-images" in bucket list
   - Public badge should show ✅

5. **Test Upload:**
   - Try adding product with image again
   - Should work now!

---

## Still Not Working?

### Enable Detailed Logging

The app now logs everything. Run:
```bash
adb logcat -s ProductsViewModel:D ProductsRepository:D ImageCompressor:D
```

This shows:
- When compression starts
- Compressed file size
- Upload attempts
- Exact error messages

### Check Full Error in Logcat

```bash
adb logcat | grep -A 10 "Upload failed"
```

This shows 10 lines after the error for full stack trace.

---

## Success Indicators

You'll know it's working when you see:

✅ **In Logs:**
```
D/ProductsViewModel: Starting image compression for URI: content://...
D/ProductsViewModel: Image compressed successfully. Size: 245KB
D/ProductsViewModel: Starting upload to Supabase...
```

✅ **In App:**
- Toast: "Product added successfully!"
- Product appears in list with image

✅ **In Supabase:**
- Storage → product-images → {businessId} → image file appears

---

## Need More Help?

1. **Check the full setup guide:** `SUPABASE_STORAGE_SETUP.md`
2. **Share the logs:** Run `adb logcat` and share the output
3. **Check Supabase logs:** Dashboard → Project → Logs

---

**Most issues are solved by creating the bucket!** ✨
