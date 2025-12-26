package com.neobuk.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Utility object for compressing images before uploading to Supabase
 * 
 * Features:
 * - Compresses images to reduce file size
 * - Maintains aspect ratio
 * - Handles EXIF orientation
 * - Targets max file size of 500KB
 */
object ImageCompressor {
    
    private const val MAX_WIDTH = 1024f
    private const val MAX_HEIGHT = 1024f
    private const val TARGET_FILE_SIZE_KB = 500
    private const val INITIAL_QUALITY = 90
    
    /**
     * Compresses an image from URI and saves to cache directory
     * 
     * @param context Android context
     * @param imageUri URI of the image to compress
     * @return File object of the compressed image, or null if compression fails
     */
    fun compressImage(context: Context, imageUri: Uri): File? {
        return try {
            // Read the original image
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) {
                return null
            }
            
            // Get EXIF orientation to rotate image if needed
            val rotatedBitmap = handleImageOrientation(context, imageUri, originalBitmap)
            
            // Calculate new dimensions
            val scaleFactor = calculateScaleFactor(
                rotatedBitmap.width.toFloat(),
                rotatedBitmap.height.toFloat()
            )
            
            // Resize image
            val scaledBitmap = if (scaleFactor < 1f) {
                Bitmap.createScaledBitmap(
                    rotatedBitmap,
                    (rotatedBitmap.width * scaleFactor).toInt(),
                    (rotatedBitmap.height * scaleFactor).toInt(),
                    true
                )
            } else {
                rotatedBitmap
            }
            
            // Compress to target file size
            val compressedFile = compressToTargetSize(context, scaledBitmap)
            
            // Clean up bitmaps
            if (scaledBitmap != rotatedBitmap) {
                scaledBitmap.recycle()
            }
            if (rotatedBitmap != originalBitmap) {
                rotatedBitmap.recycle()
            }
            originalBitmap.recycle()
            
            compressedFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Handles image rotation based on EXIF orientation
     */
    private fun handleImageOrientation(
        context: Context,
        imageUri: Uri,
        bitmap: Bitmap
    ): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val exif = ExifInterface(inputStream!!)
            inputStream.close()
            
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }
    
    /**
     * Rotates a bitmap by the specified degrees
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    /**
     * Calculates the scale factor to fit image within max dimensions
     */
    private fun calculateScaleFactor(width: Float, height: Float): Float {
        val widthScale = MAX_WIDTH / width
        val heightScale = MAX_HEIGHT / height
        return min(min(widthScale, heightScale), 1f)
    }
    
    /**
     * Compresses bitmap to target file size by adjusting quality
     */
    private fun compressToTargetSize(context: Context, bitmap: Bitmap): File {
        val cacheDir = context.cacheDir
        val compressedFile = File(cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        
        var quality = INITIAL_QUALITY
        var fileSize: Long
        
        do {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()
            fileSize = (byteArray.size / 1024).toLong() // Convert to KB
            
            if (fileSize <= TARGET_FILE_SIZE_KB || quality <= 10) {
                // Save the file
                val fileOutputStream = FileOutputStream(compressedFile)
                fileOutputStream.write(byteArray)
                fileOutputStream.close()
                break
            }
            
            // Reduce quality for next iteration
            quality -= 10
        } while (fileSize > TARGET_FILE_SIZE_KB && quality > 0)
        
        return compressedFile
    }
}
