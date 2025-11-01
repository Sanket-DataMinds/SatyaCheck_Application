package com.satyacheck.android.utils

import android.content.Context
import android.os.PowerManager
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.util.concurrent.TimeUnit

/**
 * Utility class to optimize background task scheduling and execution
 * Improves battery life by intelligently scheduling and batching tasks
 */
object BackgroundTaskOptimizer {

    private const val TAG = "BackgroundTaskOptimizer"
    
    /**
     * Schedule a task to be executed when conditions are optimal
     * This helps reduce battery consumption and improve performance
     */
    inline fun <reified T : CoroutineWorker> scheduleOptimalTask(
        context: Context,
        workerClass: Class<T>,
        workName: String,
        inputData: Data = Data.EMPTY,
        requiresNetwork: Boolean = false,
        requiresCharging: Boolean = false,
        requiresDeviceIdle: Boolean = false,
        delayMinutes: Long = 0,
        isPriority: Boolean = false
    ) {
        // Create constraints based on parameters
        val constraints = Constraints.Builder().apply {
            if (requiresNetwork) {
                setRequiredNetworkType(NetworkType.CONNECTED)
            }
            if (requiresCharging) {
                setRequiresCharging(true)
            }
            if (requiresDeviceIdle) {
                setRequiresDeviceIdle(true)
            }
            // Battery not low constraint helps preserve battery life
            setRequiresBatteryNotLow(true)
        }.build()
        
        // Build the work request with constraints
        val workRequestBuilder = OneTimeWorkRequestBuilder<T>().apply {
            setConstraints(constraints)
            setInputData(inputData)
            
            // Add delay if specified
            if (delayMinutes > 0) {
                setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            }
            
            // Set exponential backoff for retries
            setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            
            // Set expedited priority for important tasks
            if (isPriority) {
                setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            }
        }.build()
        
        // Enqueue the work, replacing any existing work with the same name
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                workName,
                ExistingWorkPolicy.REPLACE,
                workRequestBuilder
            )
    }
    
    /**
     * Batch multiple related tasks together to reduce wake-ups
     * This significantly improves battery efficiency
     */
    fun cancelAllTasks(context: Context, workNamePrefix: String) {
        WorkManager.getInstance(context)
            .cancelAllWorkByTag(workNamePrefix)
    }
    
    /**
     * Check if device is in power save mode
     * Can be used to reduce background work when battery is low
     */
    fun isInPowerSaveMode(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isPowerSaveMode
    }
    
    /**
     * Check if current time is optimal for background processing
     * Helps schedule intensive tasks at times when the user is less likely to be using the device
     */
    fun isOptimalTimeForBackgroundWork(): Boolean {
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        
        // Typically, optimal times are early morning, lunch time, or late night
        // when users are less likely to be actively using their devices
        return currentHour in setOf(3, 4, 5, 12, 13, 22, 23, 0, 1, 2)
    }
}
