package com.satyacheck.android.utils

import android.content.Context
import android.widget.Toast
import com.satyacheck.android.data.remote.api.SatyaCheckApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class to test backend connectivity
 */
@Singleton
class BackendConnectionTester @Inject constructor(
    private val apiClient: SatyaCheckApiClient
) {
    
    /**
     * Tests the connection to the backend by making a simple request
     * @param context Context to show toast messages
     * @return True if connection is successful, false otherwise
     */
    suspend fun testConnection(context: Context): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val response = apiClient.apiService.getArticles()
                
                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Successfully connected to backend!", Toast.LENGTH_LONG).show()
                    }
                    true
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context, 
                            "Failed to connect to backend: ${response.code()} ${response.message()}", 
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    false
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context, 
                    "Error connecting to backend: ${e.message}", 
                    Toast.LENGTH_LONG
                ).show()
            }
            false
        }
    }
}