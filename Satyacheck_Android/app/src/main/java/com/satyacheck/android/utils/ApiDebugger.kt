package com.satyacheck.android.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import com.satyacheck.android.data.remote.api.SatyaCheckApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for debugging API connectivity issues
 */
@Singleton
class ApiDebugger @Inject constructor(
    private val apiClient: SatyaCheckApiClient
) {
    private val TAG = "ApiDebugger"
    
    data class TestResult(
        val isSuccess: Boolean,
        val message: String
    )
    
    /**
     * Tests the connection to the backend API with comprehensive diagnostics
     * @return TestResult containing success flag and message
     */
    suspend fun testBackendConnection(): TestResult = withContext(Dispatchers.IO) {
        try {
            // First, try to connect to a public API to verify internet connectivity
            val testApiUrl = "https://jsonplaceholder.typicode.com/posts/1"
            Log.d(TAG, "Testing connection to public test API: $testApiUrl")
            
            // Test basic HTTP connectivity to public API
            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()
                
            val publicTestRequest = Request.Builder()
                .url(testApiUrl)
                .build()
                
            try {
                client.newCall(publicTestRequest).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext TestResult(
                            isSuccess = false,
                            message = "Internet connection check failed. Cannot connect to public API test endpoint."
                        )
                    }
                    Log.d(TAG, "Public API connection successful - internet is working")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Public API connection failed", e)
                return@withContext TestResult(
                    isSuccess = false,
                    message = "Internet connection issue: ${e.message}. Check if you're connected to the internet."
                )
            }
            
            // Now try to connect to the actual backend URL
            val baseUrl = SatyaCheckApiClient.BASE_URL
            Log.d(TAG, "Testing connection to backend: $baseUrl")
            
            // Extract the hostname from the URL
            val url = URL(baseUrl)
            val hostname = url.host
            val port = if (url.port == -1) url.defaultPort else url.port
            
            // Test DNS resolution
            try {
                val ipAddress = InetAddress.getByName(hostname)
                Log.d(TAG, "DNS lookup successful: ${ipAddress.hostAddress}")
            } catch (e: Exception) {
                Log.e(TAG, "DNS resolution failed", e)
                return@withContext TestResult(
                    isSuccess = false,
                    message = "Internet connection is working, but DNS lookup for backend failed: ${e.message}. Check backend hostname."
                )
            }
            
            // Test basic HTTP connectivity to backend
            val backendRequest = Request.Builder()
                .url(baseUrl)
                .build()
                
            try {
                client.newCall(backendRequest).execute().use { response ->
                    // For testing purposes, even if we get an error response (like 404), we'll consider it a successful connection
                    // as long as we get any response from the server
                    Log.d(TAG, "Backend server responded with code: ${response.code}")
                    return@withContext TestResult(
                        isSuccess = true,
                        message = "Backend connection successful! Server responded with status code: ${response.code}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Backend connection failed", e)
                return@withContext TestResult(
                    isSuccess = false,
                    message = "Backend connection error: ${e.message}. Server may be down or not accessible at $baseUrl"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "General error testing connection", e)
            return@withContext TestResult(
                isSuccess = false,
                message = "Connection test failed: ${e.message ?: "Unknown error"}"
            )
        }
    }
    
    /**
     * Test the analysis API connection with detailed error reporting
     */
    suspend fun testAnalysisApiConnection(context: Context): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                Log.d(TAG, "Testing connection to the analysis API...")
                
                try {
                    val testContent = "This is a test message to check API connectivity."
                    val response = apiClient.apiService.analyzeContent(
                        content = testContent,
                        language = "en",
                        source = "debug-test",
                        contentType = "text"
                    )
                    
                    Log.d(TAG, "Response code: ${response.code()}")
                    Log.d(TAG, "Response message: ${response.message()}")
                    
                    if (response.isSuccessful) {
                        Log.d(TAG, "Response successful. Body: ${response.body()}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context, 
                                "API connection successful: ${response.code()}", 
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        true
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "No error body"
                        Log.e(TAG, "API error. Code: ${response.code()}, Error: $errorBody")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context, 
                                "API error: ${response.code()} - $errorBody", 
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during API call", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context, 
                            "API connection failed: ${e.message}", 
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Outer exception", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context, 
                    "API connection test failed: ${e.message}", 
                    Toast.LENGTH_LONG
                ).show()
            }
            false
        }
    }
    
    /**
     * Check if the backend URL is correct and accessible
     */
    suspend fun debugApiUrl(context: Context) {
        withContext(Dispatchers.Main) {
            try {
                val message = "API URL: ${SatyaCheckApiClient.BASE_URL}"
                Log.d(TAG, message)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error getting API URL: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}