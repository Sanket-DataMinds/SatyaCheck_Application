package com.satyacheck.android.utils

import android.content.Context
import androidx.startup.Initializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * App initialization optimizer using the App Startup library
 * This handles initialization of non-critical components 
 * in a lazy, on-demand fashion for faster app startup
 */
class AppStartupInitializer : Initializer<Unit> {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun create(context: Context) {
        // Perform initializations that don't need to happen immediately
        // but should happen soon after app start
        applicationScope.launch {
            // Initialize components in background thread
            initializeNonCriticalComponents(context)
        }
    }

    private suspend fun initializeNonCriticalComponents(context: Context) {
        // Initialize any components that aren't needed immediately
        // These will be initialized shortly after app startup
        
        // Pre-warm the image cache
        val defaultImages = listOf(
            "default_profile.png",
            "default_header.png"
        )
        OptimizedImageLoader.preloadImages(context, defaultImages)
        
        // Clean up old cache files
        cleanupOldCacheFiles(context)
    }
    
    private fun cleanupOldCacheFiles(context: Context) {
        // Clean up files older than 7 days
        val cacheDir = context.cacheDir
        val currentTime = System.currentTimeMillis()
        val maxAge = 7 * 24 * 60 * 60 * 1000 // 7 days in milliseconds
        
        cacheDir.listFiles()?.forEach { file ->
            val fileAge = currentTime - file.lastModified()
            if (fileAge > maxAge) {
                file.delete()
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // This initializer has no dependencies
        return emptyList()
    }
}
