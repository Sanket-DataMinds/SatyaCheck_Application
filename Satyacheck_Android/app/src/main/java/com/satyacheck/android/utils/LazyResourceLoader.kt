package com.satyacheck.android.utils

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility for lazy loading resources in Compose
 * Helps reduce initial rendering time and memory usage
 */
object LazyResourceLoader {

    /**
     * Composable that lazily loads a heavy resource and shows it only when needed
     * This helps reduce the initial memory footprint of screens with heavy resources
     */
    @Composable
    fun <T> LazyLoadResource(
        key: Any,
        loadResource: suspend () -> T,
        content: @Composable (resource: T) -> Unit,
        loadingContent: @Composable () -> Unit = { DefaultLoadingIndicator() }
    ) {
        var resource by remember(key) { mutableStateOf<T?>(null) }
        var isLoading by remember(key) { mutableStateOf(false) }
        
        LaunchedEffect(key) {
            isLoading = true
            resource = withContext(Dispatchers.IO) {
                loadResource()
            }
            isLoading = false
        }
        
        if (isLoading || resource == null) {
            loadingContent()
        } else {
            content(resource!!)
        }
    }
    
    /**
     * Composable that efficiently loads resources on demand
     * It prioritizes UI responsiveness by deferring heavy loading operations
     */
    @Composable
    fun <T> LazyResourceRenderer(
        isVisible: Boolean,
        key: Any,
        loadResource: suspend (Context) -> T,
        content: @Composable (resource: T) -> Unit,
        loadingContent: @Composable () -> Unit = { DefaultLoadingIndicator() }
    ) {
        var resource by remember(key) { mutableStateOf<T?>(null) }
        var isLoading by remember(key) { mutableStateOf(false) }
        val context = LocalContext.current
        
        LaunchedEffect(key, isVisible) {
            if (isVisible && resource == null && !isLoading) {
                isLoading = true
                resource = withContext(Dispatchers.IO) {
                    loadResource(context)
                }
                isLoading = false
            }
        }
        
        if (!isVisible) {
            // Don't render anything when not visible
            return
        }
        
        if (isLoading || resource == null) {
            loadingContent()
        } else {
            content(resource!!)
        }
    }
    
    /**
     * Default loading indicator for lazy loaded resources
     */
    @Composable
    private fun DefaultLoadingIndicator() {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
