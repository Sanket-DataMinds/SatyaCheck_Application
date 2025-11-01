package com.satyacheck.android.utils

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Central optimization coordinator that manages all optimization
 * utilities in one place for easier integration
 */
object OptimizationCoordinator {

    private const val TAG = "OptimizationCoordinator"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    /**
     * Initialize all optimization components
     */
    fun initialize(context: Context) {
        Log.d(TAG, "Initializing optimization components")
        
        // Initialize memory management
        MemoryManager.initialize(context)
        
        // Track startup performance
        val performanceMonitor = PerformanceMonitor.getInstance(context.applicationContext as android.app.Application)
        performanceMonitor.initialize()
        
        // Perform initial cache cleanup
        scope.launch {
            cleanupStaleCache(context)
        }
    }
    
    /**
     * Clean up stale cache files
     */
    private fun cleanupStaleCache(context: Context) {
        // Clear caches older than 7 days
        val cacheDir = context.cacheDir
        val currentTime = System.currentTimeMillis()
        val maxAge = 7 * 24 * 60 * 60 * 1000 // 7 days in milliseconds
        
        var bytesFreed = 0L
        
        cacheDir.listFiles()?.forEach { file ->
            if (file.isFile) {
                val fileAge = currentTime - file.lastModified()
                if (fileAge > maxAge) {
                    bytesFreed += file.length()
                    file.delete()
                }
            }
        }
        
        Log.d(TAG, "Cleaned up ${bytesFreed / 1024} KB of stale cache")
    }
    
    /**
     * Perform low memory optimization when the app
     * receives a low memory warning
     */
    fun handleLowMemory(context: Context) {
        Log.w(TAG, "Low memory condition detected")
        
        // Trim memory aggressively
        MemoryManager.trimMemory(context)
        
        // Clear non-essential caches
        ResourceOptimizer.clearResourceCaches()
        
        // Clear image caches
        OptimizedImageLoader.clearImageCaches(context)
        
        // Force garbage collection
        System.gc()
    }
    
    /**
     * Composable helper that sets up performance monitoring
     */
    @Composable
    fun MonitorPerformance(screenName: String, content: @Composable () -> Unit) {
        val context = LocalContext.current
        val performanceMonitor = PerformanceMonitor.getInstance(context.applicationContext as android.app.Application)
        
        // Track screen rendering performance
        performanceMonitor.trackOperationStart("render_$screenName")
        
        // Render the content
        content()
        
        // End performance tracking (note: this isn't perfect in Compose, 
        // but gives a rough idea of initial composition time)
        performanceMonitor.trackOperationEnd("render_$screenName")
    }
    
    /**
     * Get optimization status summary
     */
    fun getOptimizationStatus(context: Context): String {
        val memoryInfo = MemoryManager.getMemoryInfo(context)
        val isInPowerSaveMode = BackgroundTaskOptimizer.isInPowerSaveMode(context)
        val isOptimalTime = BackgroundTaskOptimizer.isOptimalTimeForBackgroundWork()
        
        return "Memory: $memoryInfo\n" +
               "Power Save Mode: $isInPowerSaveMode\n" +
               "Optimal Background Time: $isOptimalTime"
    }
    
    /**
     * Preload essential app resources
     */
    fun preloadEssentialResources(context: Context) {
        // Preload common images
        val commonImageUrls = listOf(
            "default_profile.png",
            "default_header.png"
        )
        OptimizedImageLoader.preloadImages(context, commonImageUrls)
    }
}
