package com.satyacheck.android.domain.cache

import android.content.Context
import com.satyacheck.android.domain.model.AnalysisResult
import com.satyacheck.android.domain.model.Verdict
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalysisCache @Inject constructor(
    private val context: Context
) {
    private val cacheDir = File(context.cacheDir, "analysis_cache")
    private val maxCacheSize = 50 * 1024 * 1024 // 50MB max cache size
    
    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    /**
     * Generate hash key for content
     */
    private fun generateCacheKey(content: String, analysisType: String): String {
        val input = "$analysisType:$content"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(32)
    }

    /**
     * Cache analysis result
     */
    suspend fun cacheResult(
        content: String, 
        result: AnalysisResult, 
        analysisType: String = "text"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val cacheKey = generateCacheKey(content, analysisType)
            val cacheFile = File(cacheDir, "$cacheKey.json")
            
            val cacheData = JSONObject().apply {
                put("content", content)
                put("analysisType", analysisType)
                put("timestamp", System.currentTimeMillis())
                put("explanation", result.explanation)
                put("verdict", result.verdict.name)
            }
            
            cacheFile.writeText(cacheData.toString())
            cleanupCache()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Retrieve cached analysis result
     */
    suspend fun getCachedResult(
        content: String, 
        analysisType: String = "text"
    ): CachedAnalysisResult? = withContext(Dispatchers.IO) {
        try {
            val cacheKey = generateCacheKey(content, analysisType)
            val cacheFile = File(cacheDir, "$cacheKey.json")
            
            if (!cacheFile.exists()) return@withContext null
            
            val cacheData = JSONObject(cacheFile.readText())
            val timestamp = cacheData.getLong("timestamp")
            val age = System.currentTimeMillis() - timestamp
            
            // Cache expires after 24 hours
            if (age > 24 * 60 * 60 * 1000) {
                cacheFile.delete()
                return@withContext null
            }
            
            val result = AnalysisResult(
                explanation = cacheData.getString("explanation"),
                verdict = Verdict.valueOf(cacheData.getString("verdict"))
            )
            
            CachedAnalysisResult(
                result = result,
                timestamp = timestamp,
                fromCache = true
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if content is cached
     */
    suspend fun isCached(content: String, analysisType: String = "text"): Boolean = 
        withContext(Dispatchers.IO) {
            val cacheKey = generateCacheKey(content, analysisType)
            val cacheFile = File(cacheDir, "$cacheKey.json")
            cacheFile.exists()
        }

    /**
     * Get cache statistics
     */
    suspend fun getCacheStats(): CacheStats = withContext(Dispatchers.IO) {
        val files = cacheDir.listFiles() ?: emptyArray()
        val totalSize = files.sumOf { it.length() }
        val totalFiles = files.size
        
        CacheStats(
            totalFiles = totalFiles,
            totalSize = totalSize,
            maxSize = maxCacheSize.toLong()
        )
    }

    /**
     * Clean up cache to maintain size limits
     */
    private suspend fun cleanupCache() = withContext(Dispatchers.IO) {
        try {
            val files = cacheDir.listFiles() ?: return@withContext
            val totalSize = files.sumOf { it.length() }
            
            if (totalSize > maxCacheSize) {
                // Sort by last modified (oldest first)
                val sortedFiles = files.sortedBy { it.lastModified() }
                var currentSize = totalSize
                
                for (file in sortedFiles) {
                    if (currentSize <= maxCacheSize * 0.8) break // Keep 80% of max size
                    currentSize -= file.length()
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    /**
     * Clear all cache
     */
    suspend fun clearCache() = withContext(Dispatchers.IO) {
        try {
            cacheDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            // Ignore errors
        }
    }
}

/**
 * Cached analysis result with metadata
 */
data class CachedAnalysisResult(
    val result: AnalysisResult,
    val timestamp: Long,
    val fromCache: Boolean
)

/**
 * Cache statistics
 */
data class CacheStats(
    val totalFiles: Int,
    val totalSize: Long,
    val maxSize: Long
)