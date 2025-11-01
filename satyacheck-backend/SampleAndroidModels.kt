package com.satyacheck.android.model

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
    
    @Json(name = "topics")
    val topics: List<String>?,
    
    @Json(name = "mainTopic")
    val mainTopic: String?,
    
    @Json(name = "languageTone")
    val languageTone: String?,
    
    @Json(name = "emotionalAppeal")
    val emotionalAppeal: String?,
    
    @Json(name = "clickbaitScore")
    val clickbaitScore: Double?,
    
    @Json(name = "biasScore")
    val biasScore: Double?,
    
    @Json(name = "factualityScore")
    val factualityScore: Double?
)

@JsonClass(generateAdapter = true)
data class SourceCredibility(
    @Json(name = "credibilityLevel")
    val credibilityLevel: String,
    
    @Json(name = "knownTrustedSource")
    val knownTrustedSource: Boolean,
    
    @Json(name = "knownFakeSource")
    val knownFakeSource: Boolean,
    
    @Json(name = "domainAge")
    val domainAge: Int?,
    
    @Json(name = "transparencyScore")
    val transparencyScore: Double?
)

/**
 * Enhanced Analysis response
 */
@JsonClass(generateAdapter = true)
data class EnhancedAnalysisResponse(
    @Json(name = "verdict")
    val verdict: String,
    
    @Json(name = "confidence")
    val confidence: Double,
    
    @Json(name = "explanation")
    val explanation: String,
    
    @Json(name = "topics")
    val topics: List<String>?,
    
    @Json(name = "mainTopic")
    val mainTopic: String?,
    
    @Json(name = "languageTone")
    val languageTone: String?,
    
    @Json(name = "emotionalAppeal")
    val emotionalAppeal: String?,
    
    @Json(name = "clickbaitScore")
    val clickbaitScore: Double?,
    
    @Json(name = "biasScore")
    val biasScore: Double?,
    
    @Json(name = "sourceCitation")
    val sourceCitation: String?,
    
    @Json(name = "factualityScore")
    val factualityScore: Double?
)

/**
 * Bulk Analysis request and response
 */
@JsonClass(generateAdapter = true)
data class BulkAnalysisRequest(
    @Json(name = "items")
    val items: List<BulkUrlItem>,
    
    @Json(name = "batchMetadata")
    val batchMetadata: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class BulkUrlItem(
    @Json(name = "url")
    val url: String,
    
    @Json(name = "metadata")
    val metadata: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class BulkAnalysisResponse(
    @Json(name = "id")
    val id: String,
    
    @Json(name = "userId")
    val userId: String,
    
    @Json(name = "urls")
    val urls: List<String>,
    
    @Json(name = "status")
    val status: String,
    
    @Json(name = "results")
    val results: Map<String, String>?,
    
    @Json(name = "submittedAt")
    val submittedAt: Long,
    
    @Json(name = "completedAt")
    val completedAt: Long?
)

/**
 * Feedback request
 */
@JsonClass(generateAdapter = true)
data class FeedbackRequest(
    @Json(name = "userId")
    val userId: String,
    
    @Json(name = "url")
    val url: String,
    
    @Json(name = "type")
    val type: String,
    
    @Json(name = "comment")
    val comment: String?,
    
    @Json(name = "originalVerdict")
    val originalVerdict: String?
)