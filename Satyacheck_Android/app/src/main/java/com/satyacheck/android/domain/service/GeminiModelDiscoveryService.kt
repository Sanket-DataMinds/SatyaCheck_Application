package com.satyacheck.android.domain.service

import android.util.Log
import com.satyacheck.android.config.GeminiModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiModelDiscoveryService @Inject constructor() {
    
    companion object {
        private const val TAG = "GeminiModelDiscovery"
        private const val MODELS_API_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        
        // Cache the discovered model
        private var cachedModel: String? = null
        private var cacheTimestamp: Long = 0
    }
    
    /**
     * Discovers the best available model from Google's API
     * Uses caching to avoid frequent API calls
     */
    suspend fun discoverBestModel(apiKey: String): String = withContext(Dispatchers.IO) {
        try {
            // Check if auto discovery is enabled
            if (!GeminiModelConfig.ENABLE_AUTO_DISCOVERY) {
                Log.d(TAG, "Auto discovery disabled, using fallback model")
                return@withContext GeminiModelConfig.FALLBACK_MODEL
            }
            
            // Check cache first
            if (isCacheValid()) {
                Log.d(TAG, "Using cached model: $cachedModel")
                return@withContext cachedModel!!
            }
            
            Log.d(TAG, "Discovering available models from Google API...")
            val availableModels = fetchAvailableModels(apiKey)
            
            if (availableModels.isEmpty()) {
                Log.w(TAG, "No models discovered, using fallback")
                return@withContext GeminiModelConfig.FALLBACK_MODEL
            }
            
            // Find the best model based on preferences
            val bestModel = findBestModel(availableModels)
            
            // Cache the result
            cachedModel = bestModel
            cacheTimestamp = System.currentTimeMillis()
            
            Log.i(TAG, "Discovered best model: $bestModel")
            return@withContext bestModel
            
        } catch (e: Exception) {
            Log.e(TAG, "Error discovering models: ${e.message}", e)
            // Return cached model if available, otherwise fallback
            return@withContext cachedModel ?: GeminiModelConfig.FALLBACK_MODEL
        }
    }
    
    /**
     * Fetches available models from Google's REST API
     */
    private suspend fun fetchAvailableModels(apiKey: String): List<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$MODELS_API_URL?key=$apiKey")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = GeminiModelConfig.API_TIMEOUT_MS.toInt()
                readTimeout = GeminiModelConfig.API_TIMEOUT_MS.toInt()
            }
            
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "API request failed with code: $responseCode")
                return@withContext emptyList()
            }
            
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()
            
            return@withContext parseModels(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching models: ${e.message}", e)
            return@withContext emptyList()
        }
    }
    
    /**
     * Parses the JSON response to extract model names
     */
    private fun parseModels(jsonResponse: String): List<String> {
        try {
            val jsonObject = JSONObject(jsonResponse)
            val modelsArray = jsonObject.getJSONArray("models")
            val modelNames = mutableListOf<String>()
            
            for (i in 0 until modelsArray.length()) {
                val model = modelsArray.getJSONObject(i)
                val modelName = model.getString("name")
                
                // Remove "models/" prefix if present
                val cleanName = modelName.removePrefix("models/")
                
                // Skip excluded models
                if (GeminiModelConfig.EXCLUDED_MODELS.contains(cleanName)) {
                    Log.d(TAG, "Skipping excluded model: $cleanName")
                    continue
                }
                
                // Only include models that support generateContent
                val supportedMethods = model.optJSONArray("supportedGenerationMethods")
                if (supportedMethods != null) {
                    for (j in 0 until supportedMethods.length()) {
                        if (supportedMethods.getString(j) == "generateContent") {
                            modelNames.add(cleanName)
                            break
                        }
                    }
                }
            }
            
            Log.d(TAG, "Found ${modelNames.size} compatible models: $modelNames")
            return modelNames
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing models JSON: ${e.message}", e)
            return emptyList()
        }
    }
    
    /**
     * Finds the best model based on preference patterns
     */
    private fun findBestModel(availableModels: List<String>): String {
        // Try exact matches from preferred models first
        for (preferredModel in GeminiModelConfig.PREFERRED_MODELS) {
            if (availableModels.contains(preferredModel)) {
                Log.d(TAG, "Found exact match for preferred model: $preferredModel")
                return preferredModel
            }
        }
        
        // Try regex pattern matches
        for (pattern in GeminiModelConfig.MODEL_PATTERNS) {
            val regex = pattern.toRegex()
            val match = availableModels.find { it.matches(regex) }
            if (match != null) {
                Log.d(TAG, "Found regex match for pattern: $pattern -> $match")
                return match
            }
        }
        
        // If no preferred model found, return the first available
        val fallback = availableModels.first()
        Log.w(TAG, "No preferred model found, using first available: $fallback")
        return fallback
    }
    
    /**
     * Checks if cached model is still valid
     */
    private fun isCacheValid(): Boolean {
        return cachedModel != null && 
               (System.currentTimeMillis() - cacheTimestamp) < GeminiModelConfig.MODEL_CACHE_DURATION_MS
    }
    
    /**
     * Forces cache refresh on next discovery
     */
    fun invalidateCache() {
        cachedModel = null
        cacheTimestamp = 0
        Log.d(TAG, "Cache invalidated")
    }
    
    /**
     * Gets the currently cached model without making API calls
     */
    fun getCachedModel(): String? = cachedModel
}