package com.satyacheck.android.domain.analyzer

import com.satyacheck.android.domain.cache.AnalysisCache
import com.satyacheck.android.domain.cache.CachedAnalysisResult
import com.satyacheck.android.domain.model.AnalysisResult
import com.satyacheck.android.domain.network.NetworkManager
import com.satyacheck.android.domain.network.NetworkStatus
import com.satyacheck.android.utils.TextAnalyzer
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartAnalysisCoordinator @Inject constructor(
    private val textAnalyzer: TextAnalyzer,
    private val imageAnalyzer: ImageAnalyzer,
    private val speechToTextService: SpeechToTextService,
    private val analysisCache: AnalysisCache,
    private val networkManager: NetworkManager
) {

    /**
     * Smart text analysis with caching and offline handling
     */
    suspend fun analyzeText(
        text: String,
        forceRefresh: Boolean = false
    ): SmartAnalysisResult {
        try {
            val analysisType = "text"
            
            // Check cache first (unless force refresh)
            if (!forceRefresh) {
                val cached = analysisCache.getCachedResult(text, analysisType)
                if (cached != null) {
                    return SmartAnalysisResult(
                        result = cached.result,
                        fromCache = true,
                        networkRequired = false,
                        cacheTimestamp = cached.timestamp
                    )
                }
            }

            // Check network availability
            val networkStatus = networkManager.observeNetworkStatus().first()
            if (networkStatus != NetworkStatus.AVAILABLE) {
                return SmartAnalysisResult(
                    result = null,
                    fromCache = false,
                    networkRequired = true,
                    error = "Internet connection required for new analysis"
                )
            }

            // Perform fresh analysis
            val result = textAnalyzer.analyzeText(text)
            
            // Cache the result for future offline access
            analysisCache.cacheResult(text, result, analysisType)
            
            return SmartAnalysisResult(
                result = result,
                fromCache = false,
                networkRequired = false,
                cacheTimestamp = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            // Try to return cached result as fallback
            val analysisType = "text"
            val cached = analysisCache.getCachedResult(text, analysisType)
            if (cached != null) {
                return SmartAnalysisResult(
                    result = cached.result,
                    fromCache = true,
                    networkRequired = false,
                    cacheTimestamp = cached.timestamp,
                    error = "Using cached result due to network error"
                )
            }
            
            return SmartAnalysisResult(
                result = null,
                fromCache = false,
                networkRequired = true,
                error = "Analysis failed: ${e.message}"
            )
        }
    }

    /**
     * Smart image analysis with caching
     */
    suspend fun analyzeImage(
        bitmap: android.graphics.Bitmap,
        imageKey: String = "image_${bitmap.hashCode()}",
        forceRefresh: Boolean = false
    ): SmartAnalysisResult {
        try {
            val analysisType = "image"
            
            // Check cache first (unless force refresh)
            if (!forceRefresh) {
                val cached = analysisCache.getCachedResult(imageKey, analysisType)
                if (cached != null) {
                    return SmartAnalysisResult(
                        result = cached.result,
                        fromCache = true,
                        networkRequired = false,
                        cacheTimestamp = cached.timestamp
                    )
                }
            }

            // Check network availability for Gemini analysis part
            val networkStatus = networkManager.observeNetworkStatus().first()
            if (networkStatus != NetworkStatus.AVAILABLE) {
                return SmartAnalysisResult(
                    result = null,
                    fromCache = false,
                    networkRequired = true,
                    error = "Internet connection required for image analysis"
                )
            }

            // Perform fresh image analysis
            val result = imageAnalyzer.analyzeImage(bitmap)
            
            // Cache the result
            analysisCache.cacheResult(imageKey, result, analysisType)
            
            return SmartAnalysisResult(
                result = result,
                fromCache = false,
                networkRequired = false,
                cacheTimestamp = System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            val analysisType = "image"
            val cached = analysisCache.getCachedResult(imageKey, analysisType)
            if (cached != null) {
                return SmartAnalysisResult(
                    result = cached.result,
                    fromCache = true,
                    networkRequired = false,
                    cacheTimestamp = cached.timestamp,
                    error = "Using cached result due to error"
                )
            }
            
            return SmartAnalysisResult(
                result = null,
                fromCache = false,
                networkRequired = true,
                error = "Image analysis failed: ${e.message}"
            )
        }
    }

    /**
     * Check if content can be analyzed offline (from cache)
     */
    suspend fun canAnalyzeOffline(content: String, analysisType: String = "text"): Boolean {
        return analysisCache.isCached(content, analysisType)
    }

    /**
     * Get network status information
     */
    fun getNetworkInfo(): NetworkInfo {
        return NetworkInfo(
            isAvailable = networkManager.isNetworkAvailable(),
            networkType = networkManager.getNetworkType(),
            isMetered = networkManager.isNetworkMetered(),
            quality = networkManager.getNetworkQuality()
        )
    }

    /**
     * Get cache statistics
     */
    suspend fun getCacheInfo() = analysisCache.getCacheStats()

    /**
     * Clear analysis cache
     */
    suspend fun clearCache() = analysisCache.clearCache()
}

/**
 * Smart analysis result with metadata
 */
data class SmartAnalysisResult(
    val result: AnalysisResult?,
    val fromCache: Boolean,
    val networkRequired: Boolean,
    val cacheTimestamp: Long? = null,
    val error: String? = null
)

/**
 * Network information
 */
data class NetworkInfo(
    val isAvailable: Boolean,
    val networkType: com.satyacheck.android.domain.network.NetworkType,
    val isMetered: Boolean,
    val quality: com.satyacheck.android.domain.network.NetworkQuality
)