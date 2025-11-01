package com.satyacheck.android.data.remote

/**
 * Constants related to API endpoints and configuration
 */
object ApiConstants {
    // Base URL for the SatyaCheck backend service
    // For Android Emulator (special address that points to host machine's localhost)
    const val BASE_URL = "http://10.0.2.2:8080/"
    
    // For physical device testing (uncomment and replace with your computer's actual IP)
    // const val BASE_URL = "http://192.168.1.xxx:8080/"
    
    // Authentication endpoints
    const val LOGIN = "auth/login"
    const val REGISTER = "auth/register"
    const val REFRESH_TOKEN = "auth/refresh"
    
    // Analysis endpoints
    const val ANALYZE_TEXT = "analyze/text"
    const val ANALYZE_IMAGE = "analyze/image"
    const val ANALYZE_AUDIO = "analyze/audio"
    const val ANALYSIS_HISTORY = "analyze/history"
    
    // Community endpoints
    const val COMMUNITY_ALERTS = "community/alerts"
    const val COMMUNITY_POSTS = "community/posts"
    const val COMMUNITY_REPORT = "community/report"
    
    // User endpoints
    const val USER_PROFILE = "user/profile"
    const val USER_PROFILE_BY_ID = "user/profile/{userId}"
    const val USER_SETTINGS = "user/settings"
    
    // Educational content endpoints
    const val EDUCATIONAL_CONTENT = "education/content"
    const val EDUCATIONAL_CATEGORIES = "education/categories"
    
    // API keys - should be moved to secure storage in production
    const val API_KEY_HEADER = "X-API-Key"
    
    // Auth token header
    const val AUTH_HEADER = "Authorization"
    
    // Timeout values (in seconds)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}
