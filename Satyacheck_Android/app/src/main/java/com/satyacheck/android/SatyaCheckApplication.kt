package com.satyacheck.android

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.satyacheck.android.utils.AssetManager
import com.satyacheck.android.utils.LanguageManager
import com.satyacheck.android.utils.LocaleHelper
import com.satyacheck.android.utils.MemoryManager
import com.satyacheck.android.utils.NotificationHelper
import com.satyacheck.android.utils.OptimizationCoordinator
import com.satyacheck.android.utils.PerformanceMonitor
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class SatyaCheckApplication : Application() {
    
    // Application scope that survives configuration changes
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Lazy initialization of components for better performance
    private val performanceMonitor by lazy {
        PerformanceMonitor.getInstance(this)
    }

    override fun onCreate() {
        // Start tracking application startup
        performanceMonitor.trackOperationStart("app_startup")
        
        super.onCreate()
        
        // Enable vector drawables compatibility
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        
        // Initialize memory management and optimization components
        OptimizationCoordinator.initialize(this)

        // Initialize notification channels for the app
        NotificationHelper.createNotificationChannel(this)

        // Initialize AssetManager directly - use the application scope
        val assetManager = AssetManager(this)

        // Copy assets to storage in a background thread
        applicationScope.launch {
            assetManager.copyAssetsToStorage()
        }
        
        // End tracking of application startup
        performanceMonitor.trackOperationEnd("app_startup")
    }
    
    /**
     * Override attachBaseContext to apply saved language preference
     * This ensures the app starts with the correct language
     */
    override fun attachBaseContext(base: Context) {
        // Apply the saved language preference
        val context = LocaleHelper.onAttach(base)
        super.attachBaseContext(context)
    }
    
    /**
     * Handle configuration changes, including language changes
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-apply the saved language to maintain consistency
        LocaleHelper.onAttach(this)
    }
    
    /**
     * Handle low memory conditions
     */
    override fun onLowMemory() {
        super.onLowMemory()
        OptimizationCoordinator.handleLowMemory(this)
    }
    
    /**
     * Handle trim memory requests
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        // Handle different trim levels
        when (level) {
            TRIM_MEMORY_RUNNING_MODERATE,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                // App is in foreground but system is under memory pressure
                MemoryManager.trimMemory(this)
            }
            
            TRIM_MEMORY_UI_HIDDEN -> {
                // App's UI is now hidden from user
                // Good time to release UI resources
                performanceMonitor.trackOperationStart("trim_ui_resources")
                // Clear UI-related caches
                performanceMonitor.trackOperationEnd("trim_ui_resources")
            }
            
            TRIM_MEMORY_BACKGROUND,
            TRIM_MEMORY_MODERATE,
            TRIM_MEMORY_COMPLETE -> {
                // App is in background and system needs memory
                // Aggressively release resources
                OptimizationCoordinator.handleLowMemory(this)
            }
        }
    }
}