package com.satyacheck.android.config

/**
 * Performance configuration constants for consistent app-wide optimizations
 */
object PerformanceConfig {
    
    // Animation durations (in milliseconds)
    const val ANIMATION_FAST = 150
    const val ANIMATION_MEDIUM = 300
    const val ANIMATION_SLOW = 500
    
    // Frame rate targets
    const val TARGET_FPS = 60
    const val FRAME_TIME_MS = 1000 / TARGET_FPS // 16.67ms per frame
    
    // Memory management
    const val IMAGE_CACHE_SIZE_MB = 50
    const val MAX_BITMAP_SIZE = 2048
    
    // UI performance thresholds
    const val COMPOSITION_WARNING_THRESHOLD_MS = 16
    const val LAYOUT_WARNING_THRESHOLD_MS = 10
    
    // Navigation performance
    const val PRELOAD_DISTANCE = 1 // How many screens to preload
    const val NAVIGATION_DEBOUNCE_MS = 500L // Prevent rapid navigation
    
    // List performance
    const val LAZY_LIST_BUFFER_SIZE = 5
    const val STAGGERED_ANIMATION_DELAY = 50L
    
    // Hardware layer usage
    const val USE_HARDWARE_LAYERS = true
    const val LAYER_CLEANUP_DELAY_MS = 100L
}