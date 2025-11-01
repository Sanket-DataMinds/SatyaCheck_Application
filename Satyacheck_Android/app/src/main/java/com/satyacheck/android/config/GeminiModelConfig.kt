package com.satyacheck.android.config

/**
 * Configuration for Gemini Model Management
 * Update these values to adapt to Google API changes
 */
object GeminiModelConfig {
    
    /**
     * How often to check for new models (in milliseconds)
     * Default: 24 hours
     */
    const val MODEL_CACHE_DURATION_MS = 24 * 60 * 60 * 1000L
    
    /**
     * Timeout for model discovery API calls (in milliseconds)
     */
    const val API_TIMEOUT_MS = 10000L
    
    /**
     * Preferred models in order of preference
     * The system will try these models in order when multiple are available
     */
    val PREFERRED_MODELS = listOf(
        "gemini-2.5-flash",           // Current stable fast model
        "gemini-flash-latest",        // Auto-updating latest flash
        "gemini-2.5-pro",            // Current stable pro model  
        "gemini-pro-latest",         // Auto-updating latest pro
        "gemini-2.0-flash",          // Previous generation
        "gemini-1.5-flash",          // Older generation (if restored)
        "gemini-1.5-pro",           // Older pro model
    )
    
    /**
     * Regex patterns for model discovery
     * Used when exact matches aren't found
     */
    val MODEL_PATTERNS = listOf(
        "gemini-.*-flash",           // Any flash model
        "gemini-.*-pro",             // Any pro model  
        "gemini-2\\.[0-9]+-.*",      // Any Gemini 2.x model
        "gemini-.*"                  // Any Gemini model as last resort
    )
    
    /**
     * Fallback model if all discovery fails
     * This should be updated manually when known working models change
     */
    const val FALLBACK_MODEL = "gemini-2.5-flash"
    
    /**
     * Enable/disable automatic model discovery
     * Set to false to use only FALLBACK_MODEL
     */
    const val ENABLE_AUTO_DISCOVERY = true
    
    /**
     * Models to exclude (known problematic ones)
     */
    val EXCLUDED_MODELS = setOf(
        "gemini-pro",               // Deprecated
        "gemini-1.5-flash",        // Deprecated  
        "text-bison-001",          // Deprecated
        "embedding-gecko-001",     // Not for text generation
        "embedding-001",           // Not for text generation
        "text-embedding-004"       // Not for text generation
    )
}