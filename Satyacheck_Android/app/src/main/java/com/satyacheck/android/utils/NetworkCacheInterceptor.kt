package com.satyacheck.android.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * Network cache interceptor for handling offline caching
 * Determines when to use cached responses based on network connectivity
 */
class NetworkCacheInterceptor(private val context: Context) : Interceptor {

    private val CACHE_CONTROL_HEADER = "Cache-Control"
    private val PRAGMA_HEADER = "Pragma"
    
    // Cache durations
    private val CACHE_MAX_STALE = 7 * 24 * 60 * 60 // 1 week in seconds
    private val CACHE_MAX_AGE = 5 * 60 // 5 minutes in seconds

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        
        // Check if we should use the cache based on network availability
        request = if (!isNetworkAvailable()) {
            // No network, get from cache if available
            request.newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build()
        } else {
            // Network is available, proceed normally
            request
        }
        
        val response = chain.proceed(request)
        
        return if (isNetworkAvailable()) {
            // Network available - read from cache for CACHE_MAX_AGE time
            response.newBuilder()
                .header(CACHE_CONTROL_HEADER, "public, max-age=$CACHE_MAX_AGE")
                .removeHeader(PRAGMA_HEADER)
                .build()
        } else {
            // No network - use cache for a longer time
            response.newBuilder()
                .header(CACHE_CONTROL_HEADER, "public, only-if-cached, max-stale=$CACHE_MAX_STALE")
                .removeHeader(PRAGMA_HEADER)
                .build()
        }
    }
    
    /**
     * Check if network is available
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // For API level 29 and above
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
