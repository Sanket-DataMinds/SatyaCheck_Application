package com.satyacheck.android.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

/**
 * Utility class for optimizing resources like images and drawables
 * Implements caching and compression to improve performance and reduce memory usage
 */
object ResourceOptimizer {

    private const val BITMAP_CACHE_SIZE = 20 // Number of bitmaps to cache
    private const val DRAWABLE_CACHE_SIZE = 30 // Number of drawables to cache
    private const val COMPRESSION_QUALITY = 85 // JPEG compression quality (0-100)
    
    // Caches for resources
    private val bitmapCache = LruCache<Int, Bitmap>(BITMAP_CACHE_SIZE)
    private val drawableCache = LruCache<Int, Drawable>(DRAWABLE_CACHE_SIZE)
    
    /**
     * Get an optimized bitmap from a resource ID
     */
    fun getOptimizedBitmap(context: Context, @DrawableRes resId: Int, width: Int? = null, height: Int? = null): Bitmap {
        // Check cache first
        bitmapCache.get(resId)?.let { cachedBitmap ->
            // If dimensions are not specified or match the cached bitmap, return it
            if (width == null || height == null || (cachedBitmap.width == width && cachedBitmap.height == height)) {
                return cachedBitmap
            }
        }
        
        // Load and optimize the bitmap
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(context.resources, resId, options)
        
        // Calculate sample size if dimensions are provided
        if (width != null && height != null) {
            options.inSampleSize = calculateInSampleSize(options, width, height)
        }
        
        // Decode with the calculated sample size
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeResource(context.resources, resId, options) ?: 
                     throw IllegalArgumentException("Failed to decode resource with ID: $resId")
        
        // Scale the bitmap if needed
        val scaledBitmap = if (width != null && height != null && 
                              (bitmap.width != width || bitmap.height != height)) {
            Bitmap.createScaledBitmap(bitmap, width, height, true)
        } else {
            bitmap
        }
        
        // Cache the bitmap
        bitmapCache.put(resId, scaledBitmap)
        
        return scaledBitmap
    }
    
    /**
     * Get a drawable from a resource ID with caching
     */
    fun getDrawable(context: Context, @DrawableRes resId: Int): Drawable {
        // Check cache first
        drawableCache.get(resId)?.let { return it }
        
        // Load the drawable
        val drawable = ContextCompat.getDrawable(context, resId)
            ?: throw IllegalArgumentException("Failed to load drawable with ID: $resId")
        
        // Cache the drawable
        drawableCache.put(resId, drawable)
        
        return drawable
    }
    
    /**
     * Calculate the optimal sample size for loading a bitmap
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * Compress and cache a bitmap to disk for faster loading
     */
    fun cacheBitmapToDisk(context: Context, bitmap: Bitmap, cacheKey: String): String {
        val cacheDir = File(context.cacheDir, "compressed_images")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        
        val cacheFile = File(cacheDir, "$cacheKey.jpg")
        
        try {
            FileOutputStream(cacheFile).use { fos ->
                // Compress the bitmap to JPEG format
                bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, fos)
                fos.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return cacheFile.absolutePath
    }
    
    /**
     * Clear all resource caches
     */
    fun clearResourceCaches() {
        bitmapCache.evictAll()
        drawableCache.evictAll()
    }
    
    /**
     * Adjust bitmap size based on the device's current configuration
     * to optimize for night mode or different screen densities
     */
    fun getConfigurationAwareBitmap(
        context: Context, 
        @DrawableRes resId: Int, 
        width: Int? = null, 
        height: Int? = null
    ): Bitmap {
        val nightModeActive = isNightModeActive(context)
        val density = context.resources.displayMetrics.density
        
        // Calculate adjusted dimensions based on screen density if needed
        val adjustedWidth = width?.let { (it * density).roundToInt() }
        val adjustedHeight = height?.let { (it * density).roundToInt() }
        
        // Get the optimized bitmap
        val bitmap = getOptimizedBitmap(context, resId, adjustedWidth, adjustedHeight)
        
        // Apply night mode adjustments if needed
        return if (nightModeActive) {
            applyNightModeAdjustments(bitmap)
        } else {
            bitmap
        }
    }
    
    /**
     * Check if night mode is active
     */
    private fun isNightModeActive(context: Context): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }
    
    /**
     * Apply night mode adjustments to a bitmap
     * This could be brightness/contrast adjustments or other transformations
     */
    private fun applyNightModeAdjustments(bitmap: Bitmap): Bitmap {
        // This is a simple implementation; you could implement more complex
        // transformations for night mode if needed
        return bitmap
    }
    
    /**
     * Compress a bitmap with optimal settings for sharing or saving
     */
    fun compressBitmapForSharing(bitmap: Bitmap, quality: Int = COMPRESSION_QUALITY): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }
}
