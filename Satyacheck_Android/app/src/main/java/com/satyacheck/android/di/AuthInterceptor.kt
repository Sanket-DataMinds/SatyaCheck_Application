package com.satyacheck.android.di

import android.content.Context
import com.satyacheck.android.data.remote.ApiConstants
import com.satyacheck.android.data.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor that adds authentication tokens to requests
 */
@Singleton
class AuthInterceptor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip authentication for login and register endpoints
        val path = originalRequest.url.encodedPath
        if (path.contains("auth/login") || path.contains("auth/register")) {
            return chain.proceed(originalRequest)
        }
        
        // Get token - must use runBlocking here since Interceptor interface is not suspending
        val token = runBlocking {
            userPreferencesRepository.getAuthToken().first()
        }
        
        // Add auth header if token exists
        val request = if (token.isNotEmpty()) {
            originalRequest.newBuilder()
                .header(ApiConstants.AUTH_HEADER, "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(request)
    }
}
