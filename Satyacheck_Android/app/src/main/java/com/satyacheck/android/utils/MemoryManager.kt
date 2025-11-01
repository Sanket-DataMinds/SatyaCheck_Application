package com.satyacheck.android.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.LruCache
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.min

/**
 * Comprehensive memory and cache management utility for the app
 * Handles memory optimization, image caching, and resource cleanup
 */
object MemoryManager {
    private const val TAG = "MemoryManager"
    private const val DISK_CACHE_SIZE = 50L * 1024 * 1024  // 50MB
    private const val MEMORY_CACHE_PERCENTAGE = 0.2 // 20% of available memory
    private const val ANALYSIS_RESULT_CACHE_SIZE = 30 // Number of analysis results to cache
    
    // LRU cache for analysis results to avoid repeated API calls
    private var analysisResultCache: LruCache<String, Any>? = null
    
    // Custom image loader with optimized cache settings
    private var imageLoader: ImageLoader? = null
    
    /**
     * Initialize memory management components
     */
    fun initialize(context: Context) {
        initializeAnalysisCache()
        initializeImageLoader(context)
        scheduleMemoryCleanup(context)
        
        Log.d(TAG, "Memory manager initialized with ${getAvailableMemory(context)}MB available")
    }
    
    /**
     * Initialize the analysis result cache
     */
    private fun initializeAnalysisCache() {
        analysisResultCache = LruCache(ANALYSIS_RESULT_CACHE_SIZE)
    }
    
    /**
     * Initialize optimized image loader with proper cache configuration
     */
    private fun initializeImageLoader(context: Context) {
        // Calculate memory cache size based on available memory
        val availableMemoryMb = getAvailableMemory(context)
        val memoryCacheSize = (availableMemoryMb * 1024 * 1024 * MEMORY_CACHE_PERCENTAGE).toLong()
        
        // Create disk cache directory if it doesn't exist
        val cacheDir = File(context.cacheDir, "image_cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        
        imageLoader = ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(MEMORY_CACHE_PERCENTAGE)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir)
                    .maxSizeBytes(DISK_CACHE_SIZE)
                    .build()
            }
            .respectCacheHeaders(false) // We'll manage our own cache expiration
            .crossfade(true)
            .build()
    }
    
    /**
     * Schedule periodic memory cleanup
     */
    private fun scheduleMemoryCleanup(context: Context) {
        val handler = Handler(Looper.getMainLooper())
        val cleanupRunnable = object : Runnable {
            override fun run() {
                trimMemory(context)
                handler.postDelayed(this, TimeUnit.MINUTES.toMillis(15)) // Run every 15 minutes
            }
        }
        
        // Start the periodic cleanup
        handler.post(cleanupRunnable)
    }
    
    /**
     * Get the image loader with optimized cache settings
     */
    fun getImageLoader(context: Context): ImageLoader {
        if (imageLoader == null) {
            initializeImageLoader(context)
        }
        return imageLoader!!
    }
    
    /**
     * Create an optimized image request
     */
    fun createImageRequest(
        context: Context, 
        url: String,
        memoryCachePolicy: CachePolicy = CachePolicy.ENABLED,
        diskCachePolicy: CachePolicy = CachePolicy.ENABLED
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .data(url)
            .memoryCachePolicy(memoryCachePolicy)
            .diskCachePolicy(diskCachePolicy)
            .crossfade(true)
            .build()
    }
    
    /**
     * Add an item to the analysis result cache
     */
    fun <T> cacheAnalysisResult(key: String, result: T) {
        analysisResultCache?.put(key, result as Any)
    }
    
    /**
     * Get an item from the analysis result cache
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getCachedAnalysisResult(key: String): T? {
        return analysisResultCache?.get(key) as? T
    }
    
    /**
     * Clear all caches
     */
    fun clearAllCaches(context: Context) {
        // Clear analysis cache
        analysisResultCache?.evictAll()
        
        // Clear image caches
        imageLoader?.memoryCache?.clear()
        imageLoader?.diskCache?.clear()
        
        // Clear app cache directories
        clearCacheDir(context.cacheDir)
        context.externalCacheDir?.let { clearCacheDir(it) }
        
        Log.d(TAG, "All caches cleared")
    }
    
    /**
     * Clear a specific cache directory
     */
    private fun clearCacheDir(dir: File) {
        if (dir.isDirectory) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    clearCacheDir(file)
                } else {
                    file.delete()
                }
            }
        }
    }
    
    /**
     * Trim memory when the app is under memory pressure
     */
    fun trimMemory(context: Context) {
        // Clear image caches
        imageLoader?.memoryCache?.clear()
        
        // Clear weak references and force a GC
        System.gc()
        
        Log.d(TAG, "Memory trimmed, now available: ${getAvailableMemory(context)}MB")
    }
    
    /**
     * Get available memory in MB
     */
    private fun getAvailableMemory(context: Context): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        return (memoryInfo.availMem / (1024 * 1024)).toInt()
    }
    
    /**
     * Get memory information for logging and debugging
     */
    fun getMemoryInfo(context: Context): String {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val freeMemory = runtime.freeMemory() / (1024 * 1024)
        val totalMemory = runtime.totalMemory() / (1024 * 1024)
        val maxMemory = runtime.maxMemory() / (1024 * 1024)
        
        return "Memory - Used: ${usedMemory}MB, Free: ${freeMemory}MB, " +
               "Total: ${totalMemory}MB, Max: ${maxMemory}MB"
    }
    
    /**
     * Generate a cache key for analysis requests
     */
    fun generateAnalysisCacheKey(content: String, language: String): String {
        // Create a compact but unique hash for the content
        val contentHash = content.hashCode().toString()
        return "analysis_${language}_$contentHash"
    }
}
