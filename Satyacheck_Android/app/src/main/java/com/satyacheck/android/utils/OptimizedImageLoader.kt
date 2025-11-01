package com.satyacheck.android.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.compose.rememberAsyncImagePainter
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Size
import timber.log.Timber

/**
 * Optimized image loading utility that leverages memory management
 * to load and cache images efficiently
 */
object OptimizedImageLoader {

    /**
     * Load image with optimized memory management
     */
    @Composable
    fun OptimizedImage(
        url: String,
        modifier: Modifier = Modifier,
        contentDescription: String? = null,
        alignment: Alignment = Alignment.Center,
        contentScale: ContentScale = ContentScale.Fit,
        alpha: Float = DefaultAlpha,
        colorFilter: ColorFilter? = null,
        placeholderDrawable: Drawable? = null,
        errorDrawable: Drawable? = null,
        loadingContent: @Composable (() -> Unit)? = null,
        errorContent: @Composable (() -> Unit)? = null,
        memoryCachePolicy: CachePolicy = CachePolicy.ENABLED,
        diskCachePolicy: CachePolicy = CachePolicy.ENABLED
    ) {
        val context = LocalContext.current
        
        // Track image loading performance
        val performanceMonitor = PerformanceMonitor.getInstance(context.applicationContext as android.app.Application)
        val operationId = "image_load_${url.hashCode()}"
        
        SubcomposeAsyncImage(
            model = createOptimizedImageRequest(
                context, 
                url, 
                memoryCachePolicy, 
                diskCachePolicy
            ),
            contentDescription = contentDescription,
            modifier = modifier,
            alignment = alignment,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
            loading = {
                loadingContent?.invoke() 
                performanceMonitor.trackOperationStart(operationId)
            },
            success = {
                Image(
                    painter = it.painter,
                    contentDescription = contentDescription,
                    modifier = modifier,
                    alignment = alignment,
                    contentScale = contentScale,
                    alpha = alpha,
                    colorFilter = colorFilter
                )
                performanceMonitor.trackOperationEnd(operationId)
            },
            error = {
                errorContent?.invoke() ?: errorDrawable?.let { drawable ->
                    androidx.compose.foundation.Image(
                        painter = rememberAsyncImagePainter(model = drawable),
                        contentDescription = contentDescription,
                        modifier = modifier,
                        alignment = alignment,
                        contentScale = contentScale,
                        alpha = alpha,
                        colorFilter = colorFilter
                    )
                }
                performanceMonitor.trackOperationEnd(operationId)
                // Log error without creating an exception
                Timber.e("Failed to load image")
            }
        )
    }
    
    /**
     * Creates an optimized image request that leverages memory and disk caching
     */
    private fun createOptimizedImageRequest(
        context: Context,
        url: String,
        memoryCachePolicy: CachePolicy = CachePolicy.ENABLED,
        diskCachePolicy: CachePolicy = CachePolicy.ENABLED
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .data(url)
            .memoryCachePolicy(memoryCachePolicy)
            .diskCachePolicy(diskCachePolicy)
            // Use the memory manager's image loader
            .listener(
                onStart = { 
                    // Start tracking image loading
                },
                onSuccess = { _, _ ->
                    // Image loaded successfully
                },
                onError = { _, _ ->
                    // Log error without using the throwable parameter directly
                    Timber.e("Error loading image from " + url)
                },
                onCancel = {
                    // Image loading was canceled
                }
            )
            .crossfade(true)
            .size(Size.ORIGINAL) // Load original size for high quality
            .build()
    }
    
    /**
     * Preload images in advance for smoother scrolling
     */
    fun preloadImages(context: Context, urls: List<String>) {
        urls.forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build()
            
            MemoryManager.getImageLoader(context).enqueue(request)
        }
    }
    
    /**
     * Clear all image caches
     */
    fun clearImageCaches(context: Context) {
        MemoryManager.getImageLoader(context).memoryCache?.clear()
        MemoryManager.getImageLoader(context).diskCache?.clear()
    }
}
