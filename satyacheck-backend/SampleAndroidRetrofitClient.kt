package com.satyacheck.android.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client for SatyaCheck API
 */
class SatyaCheckApiClient {

    companion object {
        // Base URL for the API - change to your production endpoint
        private const val BASE_URL = "https://api.satyacheck.com/"
        
        // Connection timeout values
        private const val CONNECTION_TIMEOUT = 30L
        private const val READ_TIMEOUT = 30L
        private const val WRITE_TIMEOUT = 30L
        
        // Create Moshi instance for JSON parsing
        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        
        // Create Retrofit instance
        fun create(authTokenProvider: () -> String?): SatyaCheckApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(createOkHttpClient(authTokenProvider))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SatyaCheckApiService::class.java)
        }
        
        // Create OkHttpClient with interceptors
        private fun createOkHttpClient(authTokenProvider: () -> String?): OkHttpClient {
            val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            return OkHttpClient.Builder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(authTokenProvider))
                .addInterceptor(httpLoggingInterceptor)
                .build()
        }
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