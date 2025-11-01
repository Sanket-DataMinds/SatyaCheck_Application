package com.satyacheck.android.data.remote.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token manager interface for handling JWT tokens
 */
interface TokenManager {
    fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun clearTokens()
}

/**
 * Retrofit client for SatyaCheck API
 */
@Singleton
class SatyaCheckApiClient @Inject constructor(
    private val tokenManager: TokenManager
) {

    companion object {
        // Base URL for the API - using localhost for the embedded server
        const val BASE_URL = "http://127.0.0.1:8080/"
        // Using localhost (127.0.0.1) ensures the app connects to the embedded server running on the device
        
        // Public test API for connectivity checking
        const val TEST_API_URL = "https://jsonplaceholder.typicode.com/"
        
        // Connection timeout values - reduced for embedded server
        private const val CONNECTION_TIMEOUT = 5L
        private const val READ_TIMEOUT = 5L
        private const val WRITE_TIMEOUT = 5L
    }
    
    // Create Gson instance for JSON parsing - NO REFLECTION REQUIRED
    private val gson = GsonBuilder()
        .setLenient()
        .create()
    
    // Create the API service
    val apiService: SatyaCheckApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(SatyaCheckApiService::class.java)
    }
    
    // Create OkHttpClient with interceptors
    private fun createOkHttpClient(): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(tokenManager::getAccessToken))
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }
    
    /**
     * Interceptor to add authorization header to requests
     */
    private class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            
            val token = tokenProvider()
            return if (token != null) {
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(newRequest)
            } else {
                chain.proceed(originalRequest)
            }
        }
    }
}