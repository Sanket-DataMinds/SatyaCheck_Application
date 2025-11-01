package com.satyacheck.android.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * Utility class to optimize ViewModel memory usage and coroutine management
 * This helps prevent memory leaks and resource wastage
 */
object ViewModelOptimizer {

    private val jobRegistry = ConcurrentHashMap<String, Job>()
    private val viewModelRegistry = ConcurrentHashMap<String, WeakReference<ViewModel>>()
    
    /**
     * Register a ViewModel for optimization
     */
    fun registerViewModel(viewModel: ViewModel, identifier: String) {
        viewModelRegistry[identifier] = WeakReference(viewModel)
    }
    
    /**
     * Unregister a ViewModel when it's no longer needed
     */
    fun unregisterViewModel(identifier: String) {
        viewModelRegistry.remove(identifier)
        cancelAllJobs(identifier)
    }
    
    /**
     * Launch a cancellable coroutine job from a ViewModel
     * This ensures proper cleanup when the job is no longer needed
     */
    fun launchJob(
        viewModel: ViewModel,
        jobId: String,
        block: suspend () -> Unit
    ): Job {
        // Cancel any existing job with the same ID
        cancelJob(jobId)
        
        // Create a new job
        val job = viewModel.viewModelScope.launch {
            block()
        }
        
        // Register the job
        jobRegistry[jobId] = job
        
        return job
    }
    
    /**
     * Cancel a specific job
     */
    fun cancelJob(jobId: String) {
        jobRegistry[jobId]?.let { job ->
            if (job.isActive) {
                job.cancel()
            }
            jobRegistry.remove(jobId)
        }
    }
    
    /**
     * Cancel all jobs for a specific ViewModel
     */
    fun cancelAllJobs(viewModelIdentifier: String) {
        val prefix = "$viewModelIdentifier:"
        
        jobRegistry.keys
            .filter { it.startsWith(prefix) }
            .forEach { jobId ->
                cancelJob(jobId)
            }
    }
    
    /**
     * Debounce a coroutine operation
     * Useful for search queries or other user input that shouldn't trigger immediate operations
     */
    fun <T> debounce(
        viewModel: ViewModel,
        operationId: String,
        delayMillis: Long = 300,
        action: suspend (T) -> Unit
    ): (T) -> Unit {
        return { param: T ->
            cancelJob(operationId)
            
            val job = viewModel.viewModelScope.launch {
                delay(delayMillis)
                action(param)
            }
            
            jobRegistry[operationId] = job
        }
    }
    
    /**
     * Helper method to create a unique job ID for a ViewModel
     */
    fun createJobId(viewModel: ViewModel, operationName: String): String {
        val viewModelClass = viewModel.javaClass.simpleName
        return "$viewModelClass:$operationName"
    }
    
    /**
     * Helper extension function for ViewModels to launch optimized coroutines
     */
    fun ViewModel.launchOptimized(operationName: String, block: suspend () -> Unit): Job {
        val jobId = createJobId(this, operationName)
        return launchJob(this, jobId, block)
    }
    
    /**
     * Helper extension function for ViewModels to create debounced operations
     */
    fun <T> ViewModel.debounced(
        operationName: String,
        delayMillis: Long = 300,
        action: suspend (T) -> Unit
    ): (T) -> Unit {
        val jobId = createJobId(this, operationName)
        return debounce(this, jobId, delayMillis, action)
    }
}
