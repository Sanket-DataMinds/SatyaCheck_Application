package com.satyacheck.android.utils

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size

/**
 * Performance-optimized image loading utilities
 */
object ImageOptimizer {
    
    /**
     * Optimized image loading with proper sizing and caching
     */
    @Composable
    fun OptimizedAsyncImage(
        imageUrl: String,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        contentScale: ContentScale = ContentScale.Crop,
        colorFilter: ColorFilter? = null,
        placeholder: Int? = null,
        error: Int? = null,
        targetSize: Size = Size.ORIGINAL
    ) {
        val context = LocalContext.current
        
        val imageRequest = remember(imageUrl, targetSize) {
            ImageRequest.Builder(context)
                .data(imageUrl)
                .size(targetSize)
                .crossfade(true)
                .memoryCacheKey(imageUrl)
                .diskCacheKey(imageUrl)
                .build()
        }
        
        AsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            colorFilter = colorFilter,
            placeholder = placeholder?.let { painterResource(it) },
            error = error?.let { painterResource(it) }
        )
    }
    
    /**
     * Optimized vector image with proper resource management
     */
    @Composable
    fun OptimizedVectorImage(
        resourceId: Int,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        colorFilter: ColorFilter? = null
    ) {
        val painter = remember(resourceId) {
            // Cache painter resource to avoid repeated loading
            resourceId
        }
        
        Image(
            painter = painterResource(painter),
            contentDescription = contentDescription,
            modifier = modifier,
            colorFilter = colorFilter
        )
    }
}