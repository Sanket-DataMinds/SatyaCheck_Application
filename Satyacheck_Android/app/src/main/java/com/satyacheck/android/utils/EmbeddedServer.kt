package com.satyacheck.android.utils

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Embedded HTTP server that runs on the device itself to simulate the backend
 * This allows the app to function without an external server connection
 */
@Singleton
class EmbeddedServer @Inject constructor(
    private val context: Context
) : NanoHTTPD(8080) {

    private val TAG = "EmbeddedServer"
    private var isServerRunning = false
    
    companion object {
        // Server status
        const val STATUS_STOPPED = "STOPPED"
        const val STATUS_RUNNING = "RUNNING"
        const val STATUS_ERROR = "ERROR"
    }
    
    /**
     * Start the embedded server if it's not already running
     */
    fun startServer(): Boolean {
        if (isServerRunning) {
            Log.d(TAG, "Server is already running")
            return true
        }
        
        return try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
            isServerRunning = true
            Log.d(TAG, "Embedded server started on port 8080")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to start embedded server", e)
            isServerRunning = false
            false
        }
    }
    
    /**
     * Stop the embedded server if it's running
     */
    fun stopServer() {
        if (isServerRunning) {
            stop()
            isServerRunning = false
            Log.d(TAG, "Embedded server stopped")
        }
    }
    
    /**
     * Get the current status of the server
     */
    fun getStatus(): String {
        return if (isServerRunning) STATUS_RUNNING else STATUS_STOPPED
    }
    
    /**
     * Handle incoming HTTP requests
     */
    override fun serve(session: IHTTPSession): Response {
        val method = session.method
        val uri = session.uri
        
        Log.d(TAG, "Received $method request to $uri")
        
        // Parse query parameters
        val queryParams = session.parameters ?: mapOf()
        
        // Log request details for debugging
        logRequestDetails(session)
        
        return when {
            uri == "/" -> handleHealthCheck()
            uri.contains("/api/analyze/text") -> handleTextAnalysis(queryParams)
            uri.contains("/api/v1/enhanced-analysis/comprehensive") -> handleEnhancedAnalysis(queryParams)
            else -> createResponse(
                Response.Status.NOT_FOUND, 
                JSONObject().put("status", "error").put("message", "Endpoint not found").toString()
            )
        }
    }
    
    /**
     * Handle health check request
     */
    private fun handleHealthCheck(): Response {
        val responseJson = JSONObject()
            .put("status", "success")
            .put("message", "SatyaCheck Embedded API Server is running!")
        
        return createResponse(Response.Status.OK, responseJson.toString())
    }
    
    /**
     * Handle text analysis request
     */
    private fun handleTextAnalysis(params: Map<String, List<String>>): Response {
        // Extract query parameters
        val content = params["content"]?.firstOrNull() ?: ""
        val language = params["language"]?.firstOrNull() ?: "en"
        
        // Log the content being analyzed
        Log.d(TAG, "Analyzing text: $content in language: $language")
        
        // Generate a simple response based on content keywords
        val (verdict, explanation) = when {
            content.contains("fake", ignoreCase = true) || content.contains("false", ignoreCase = true) -> 
                Pair("HIGH_MISINFORMATION_RISK", "This content contains claims that are known to be false or highly suspicious.")
                
            content.contains("misleading", ignoreCase = true) || content.contains("exaggerated", ignoreCase = true) -> 
                Pair("POTENTIALLY_MISLEADING", "This content appears to contain exaggerated or potentially misleading information.")
                
            content.contains("scam", ignoreCase = true) || content.contains("fraud", ignoreCase = true) -> 
                Pair("SCAM_ALERT", "Warning: This content matches patterns commonly seen in fraudulent schemes.")
                
            else -> 
                Pair("CREDIBLE", "This content appears to be credible based on our analysis.")
        }
        
        val responseJson = JSONObject()
            .put("status", "success")
            .put("analysis", JSONObject()
                .put("verdict", verdict)
                .put("explanation", explanation)
                .put("confidence", 0.85)
                .put("source", "embedded-server")
                .put("processingTime", 500)
            )
        
        return createResponse(Response.Status.OK, responseJson.toString())
    }
    
    /**
     * Handle enhanced analysis request
     */
    private fun handleEnhancedAnalysis(params: Map<String, List<String>>): Response {
        return handleTextAnalysis(params) // Use same logic for now
    }
    
    /**
     * Create a standardized HTTP response
     */
    private fun createResponse(status: Response.Status, jsonContent: String): Response {
        return newFixedLengthResponse(status, "application/json", jsonContent)
    }
    
    /**
     * Log detailed information about the incoming request
     */
    private fun logRequestDetails(session: IHTTPSession) {
        val method = session.method
        val uri = session.uri
        val queryParams = session.parameters
        
        Log.d(TAG, "==== REQUEST DETAILS ====")
        Log.d(TAG, "Method: $method")
        Log.d(TAG, "URI: $uri")
        Log.d(TAG, "Query Parameters:")
        
        queryParams?.forEach { (param, values) ->
            Log.d(TAG, "  $param: ${values.joinToString(", ")}")
        }
        
        Log.d(TAG, "=======================")
    }
}