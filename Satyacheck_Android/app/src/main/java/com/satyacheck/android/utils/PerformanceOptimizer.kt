package com.satyacheck.android.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

/**
 * Performance optimization utilities for monitoring and improving app performance
 */
object PerformanceOptimizer {
    
    private const val TAG = "PerformanceOptimizer"
    
    /**
     * Monitor composition performance - useful for debugging
     */
    @Composable
    fun MonitorComposition(
        name: String,
        content: @Composable () -> Unit
    ) {
        val startTime = remember { System.nanoTime() }
        
        LaunchedEffect(Unit) {
            withContext(Dispatchers.Default) {
                val compositionTime = (System.nanoTime() - startTime) / 1_000_000
                if (compositionTime > 16) { // More than one frame at 60fps
                    Log.w(TAG, "Slow composition detected in $name: ${compositionTime}ms")
                }
            }
        }
        
        content()
    }
    
    /**
     * Optimize heavy computations by moving them to background thread
     */
    suspend fun <T> optimizedComputation(
        computation: suspend () -> T
    ): T = withContext(Dispatchers.Default) {
        computation()
    }
    
    /**
     * Check if we should enable performance optimizations based on device capabilities
     */
    fun shouldUsePerformanceMode(): Boolean {
        // Simple heuristic - can be enhanced based on device specs
        return Runtime.getRuntime().availableProcessors() < 4
    }
}

/**
 * Extension functions for performance-conscious UI updates
 */
fun <T> T.performanceOptimized(
    condition: Boolean = PerformanceOptimizer.shouldUsePerformanceMode(),
    optimized: () -> T,
    default: () -> T = { this }
): T {
    return if (condition) optimized() else default()
}