package com.satyacheck.android.utils

import android.content.Context
import com.satyacheck.android.domain.model.AnalysisResult
import com.satyacheck.android.domain.model.Verdict
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalysisCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cache = mutableMapOf<String, AnalysisResult>()
    private val cacheMutex = Mutex()
    private val prefs by lazy { 
        context.getSharedPreferences("analysis_cache", Context.MODE_PRIVATE) 
    }
    
    companion object {
        private const val MAX_CACHE_SIZE = 100
        private const val CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000L // 24 hours
    }
    
    suspend fun get(text: String): AnalysisResult? = cacheMutex.withLock {
        val key = generateKey(text)
        
        // Check memory cache first
        cache[key]?.let { return@withLock it }
        
        // Check persistent cache
        val cachedVerdict = prefs.getString("${key}_verdict", null)
        val cachedExplanation = prefs.getString("${key}_explanation", null)
        val cachedTime = prefs.getLong("${key}_time", 0L)
        
        if (cachedVerdict != null && cachedExplanation != null) {
            // Check if cache is still valid
            if (System.currentTimeMillis() - cachedTime < CACHE_EXPIRY_MS) {
                val result = AnalysisResult(
                    verdict = Verdict.fromString(cachedVerdict),
                    explanation = cachedExplanation
                )
                cache[key] = result
                return@withLock result
            }
        }
        
        return@withLock null
    }
    
    suspend fun put(text: String, result: AnalysisResult) = cacheMutex.withLock {
        val key = generateKey(text)
        
        // Add to memory cache
        cache[key] = result
        
        // Ensure cache size limit
        if (cache.size > MAX_CACHE_SIZE) {
            val oldestKey = cache.keys.first()
            cache.remove(oldestKey)
            clearPersistentCache(oldestKey)
        }
        
        // Add to persistent cache
        prefs.edit()
            .putString("${key}_verdict", result.verdict.name)
            .putString("${key}_explanation", result.explanation)
            .putLong("${key}_time", System.currentTimeMillis())
            .apply()
    }
    
    private fun generateKey(text: String): String {
        return MessageDigest.getInstance("MD5")
            .digest(text.toLowerCase().trim().toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    
    private fun clearPersistentCache(key: String) {
        prefs.edit()
            .remove("${key}_verdict")
            .remove("${key}_explanation")
            .remove("${key}_time")
            .apply()
    }
}