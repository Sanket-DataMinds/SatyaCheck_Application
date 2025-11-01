package com.satyacheck.android.utils

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Utility to monitor app performance metrics and detect performance issues
 */
class PerformanceMonitor private constructor(private val application: Application) : LifecycleObserver {
    
    private val TAG = "PerformanceMonitor"
    private var isMonitoring = false
    private val handler = Handler(Looper.getMainLooper())
    private var startTime: Long = 0
    private val metrics = mutableMapOf<String, Long>()
    private val lagThreshold = 16 // 16ms frame threshold (60 FPS)
    
    companion object {
        private var instance: PerformanceMonitor? = null
        
        fun getInstance(application: Application): PerformanceMonitor {
            return instance ?: synchronized(this) {
                instance ?: PerformanceMonitor(application).also { instance = it }
            }
        }
    }
    
    /**
     * Initialize performance monitoring
     */
    fun initialize() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Log.d(TAG, "Performance monitor initialized")
    }
    
    /**
     * Start performance monitoring
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        startTime = System.currentTimeMillis()
        schedulePerformanceCheck()
        
        Log.d(TAG, "Performance monitoring started")
    }
    
    /**
     * Stop performance monitoring
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stopMonitoring() {
        isMonitoring = false
        handler.removeCallbacksAndMessages(null)
        
        Log.d(TAG, "Performance monitoring stopped")
    }
    
    /**
     * Schedule periodic performance checks
     */
    private fun schedulePerformanceCheck() {
        val checkRunnable = object : Runnable {
            override fun run() {
                checkPerformance()
                if (isMonitoring) {
                    handler.postDelayed(this, TimeUnit.SECONDS.toMillis(5)) // Check every 5 seconds
                }
            }
        }
        
        handler.post(checkRunnable)
    }
    
    /**
     * Check performance metrics
     */
    private fun checkPerformance() {
        val memoryInfo = MemoryManager.getMemoryInfo(application)
        Log.d(TAG, memoryInfo)
        
        // Check for potential memory leaks (consistent increase in used memory)
        checkForMemoryIssues(memoryInfo)
    }
    
    /**
     * Check for memory issues by analyzing memory patterns
     */
    private fun checkForMemoryIssues(memoryInfo: String) {
        // Basic memory leak detection
        if (memoryInfo.contains("Used:")) {
            val usedMemoryStr = memoryInfo.substringAfter("Used: ").substringBefore("MB")
            try {
                val usedMemory = usedMemoryStr.toInt()
                val lastUsedMemory = metrics["lastUsedMemory"] ?: 0
                
                // If memory consistently increases over time, might indicate a leak
                if (usedMemory > lastUsedMemory && (usedMemory - lastUsedMemory) > 10) {
                    metrics["consecutiveIncreases"] = (metrics["consecutiveIncreases"] ?: 0) + 1
                    
                    if ((metrics["consecutiveIncreases"] ?: 0) > 5) {
                        Log.w(TAG, "Potential memory leak detected: Memory consistently increasing")
                    }
                } else {
                    metrics["consecutiveIncreases"] = 0
                }
                
                metrics["lastUsedMemory"] = usedMemory.toLong()
            } catch (e: NumberFormatException) {
                Log.e(TAG, "Error parsing memory info: $e")
            }
        }
    }
    
    /**
     * Track the start of a performance-critical operation
     */
    fun trackOperationStart(operationName: String) {
        metrics["${operationName}_start"] = System.currentTimeMillis()
    }
    
    /**
     * Track the end of a performance-critical operation
     */
    fun trackOperationEnd(operationName: String) {
        val startTime = metrics["${operationName}_start"] ?: return
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        metrics["${operationName}_duration"] = duration
        
        Log.d(TAG, "Operation '$operationName' took ${duration}ms")
        
        // Log warnings for slow operations
        when {
            duration > 5000 -> Log.w(TAG, "Very slow operation detected: '$operationName' took ${duration}ms")
            duration > 1000 -> Log.w(TAG, "Slow operation detected: '$operationName' took ${duration}ms")
            duration > 100 -> Log.d(TAG, "Operation '$operationName' took ${duration}ms (moderate)")
        }
    }
    
    /**
     * Log current app performance state to a file
     */
    suspend fun logPerformanceSnapshot(context: Context) = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date())
            val logFile = File(context.filesDir, "performance_log_$timestamp.txt")
            
            FileOutputStream(logFile).use { output ->
                // Log basic device info
                val deviceInfo = "Device: ${Build.MANUFACTURER} ${Build.MODEL}, " +
                                 "Android ${Build.VERSION.RELEASE}, " +
                                 "API Level ${Build.VERSION.SDK_INT}\n"
                output.write(deviceInfo.toByteArray())
                
                // Log memory info
                val memoryInfo = MemoryManager.getMemoryInfo(context)
                output.write("Memory Info: $memoryInfo\n".toByteArray())
                
                // Log tracked metrics
                output.write("Performance Metrics:\n".toByteArray())
                metrics.forEach { (key, value) ->
                    output.write("$key: $value\n".toByteArray())
                }
            }
            
            Log.d(TAG, "Performance snapshot logged to ${logFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error logging performance snapshot", e)
        }
    }
}
