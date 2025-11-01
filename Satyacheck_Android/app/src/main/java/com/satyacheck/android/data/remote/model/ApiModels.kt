package com.satyacheck.android.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Authentication request classes
 */
sealed class AuthRequest {
    @JsonClass(generateAdapter = true)
    data class Register(
        val username: String,
        val password: String,
        val email: String,
        val name: String
    ) : AuthRequest()

    @JsonClass(generateAdapter = true)
    data class Login(
        val username: String,
        val password: String
    ) : AuthRequest()

    @JsonClass(generateAdapter = true)
    data class RefreshToken(
        @Json(name = "refreshToken")
        val refreshToken: String
    ) : AuthRequest()
}

/**
 * Authentication response
 */
@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "status")
    val status: String,
    
    @Json(name = "message")
    val message: String,
    
    @Json(name = "data")
    val data: AuthData?
)

@JsonClass(generateAdapter = true)
data class AuthData(
    @Json(name = "accessToken")
    val accessToken: String,
    
    @Json(name = "refreshToken")
    val refreshToken: String,
    
    @Json(name = "expiresIn")
    val expiresIn: Long,
    
    @Json(name = "username")
    val username: String,
    
    @Json(name = "roles")
    val roles: List<String>
)

/**
 * URL Analysis response
 */
@JsonClass(generateAdapter = true)
data class UrlAnalysisResponse(
    @Json(name = "url")
    val url: String,
    
    @Json(name = "title")
    val title: String?,
    
    @Json(name = "description")
    val description: String?,
    
    @Json(name = "domain")
    val domain: String,
    
    @Json(name = "language")
    val language: String,
    
    @Json(name = "wasTranslated")
    val wasTranslated: Boolean,
    
    @Json(name = "contentAnalysis")
    val contentAnalysis: ContentAnalysis?,
    
    @Json(name = "sourceCredibility")
    val sourceCredibility: SourceCredibility?
)

@JsonClass(generateAdapter = true)
data class ContentAnalysis(
    @Json(name = "verdict")
    val verdict: String,
    
    @Json(name = "confidence")
    val confidence: Double,
    
    @Json(name = "explanation")
    val explanation: String,
    
    @Json(name = "flags")
    val flags: List<String>?,
    
    @Json(name = "categories")
    val categories: List<String>?
)

@JsonClass(generateAdapter = true)
data class SourceCredibility(
    @Json(name = "domain")
    val domain: String,
    
    @Json(name = "rating")
    val rating: String?,
    
    @Json(name = "verifiedSource")
    val verifiedSource: Boolean,
    
    @Json(name = "factCheckCount")
    val factCheckCount: Int?,
    
    @Json(name = "domainAge")
    val domainAge: String?
)

/**
 * Enhanced Analysis response for comprehensive content analysis
 */
@JsonClass(generateAdapter = true)
data class EnhancedAnalysisResponse(
    @Json(name = "content")
    val content: String,
    
    @Json(name = "language")
    val language: String,
    
    @Json(name = "analysis")
    val analysis: ContentAnalysis,
    
    @Json(name = "source")
    val source: String?,
    
    @Json(name = "metadata")
    val metadata: Map<String, Any>?
)

/**
 * Bulk analysis request and response
 */
@JsonClass(generateAdapter = true)
data class BulkAnalysisRequest(
    @Json(name = "urls")
    val urls: List<String>,
    
    @Json(name = "language")
    val language: String = "en",
    
    @Json(name = "callback")
    val callback: String? = null
)

@JsonClass(generateAdapter = true)
data class BulkAnalysisResponse(
    @Json(name = "id")
    val id: String,
    
    @Json(name = "userId")
    val userId: String,
    
    @Json(name = "status")
    val status: String,
    
    @Json(name = "results")
    val results: List<UrlAnalysisResponse>?,
    
    @Json(name = "createdAt")
    val createdAt: String,
    
    @Json(name = "completedAt")
    val completedAt: String?
)

/**
 * Feedback request
 */
@JsonClass(generateAdapter = true)
data class FeedbackRequest(
    @Json(name = "analysisId")
    val analysisId: String?,
    
    @Json(name = "url")
    val url: String?,
    
    @Json(name = "content")
    val content: String?,
    
    @Json(name = "rating")
    val rating: Int,
    
    @Json(name = "comment")
    val comment: String?
)

/**
 * API error response
 */
@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    @Json(name = "status")
    val status: String,
    
    @Json(name = "message")
    val message: String,
    
    @Json(name = "data")
    val data: Any?
)