package com.satyacheck.android.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Base response wrapper for all API responses
 */
data class ApiResponse<T>(
    @SerializedName("status") val status: String,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?
)

/**
 * Authentication response DTO
 */
data class AuthDto(
    @SerializedName("token") val token: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Int,
    @SerializedName("userId") val userId: String
)

/**
 * User profile DTO
 */
data class UserProfileDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("language") val language: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("totalAnalyses") val totalAnalyses: Int,
    @SerializedName("misinformationDetected") val misinformationDetected: Int
)

/**
 * Analysis result DTO from the server
 */
data class AnalysisResultDto(
    @SerializedName("id") val id: String,
    @SerializedName("content") val content: String,
    @SerializedName("contentType") val contentType: String, // TEXT, IMAGE, AUDIO
    @SerializedName("verdict") val verdict: String,
    @SerializedName("explanation") val explanation: String,
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("language") val language: String
)

/**
 * Analysis request DTO
 */
data class AnalysisRequestDto(
    @SerializedName("content") val content: String,
    @SerializedName("contentType") val contentType: String, // TEXT, IMAGE, AUDIO
    @SerializedName("language") val language: String
)

/**
 * Community alert DTO
 */
data class CommunityAlertDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("severity") val severity: String, // LOW, MEDIUM, HIGH
    @SerializedName("location") val location: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("reportCount") val reportCount: Int
)

/**
 * Community post DTO
 */
data class CommunityPostDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("username") val username: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("analysisId") val analysisId: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("comments") val comments: List<CommentDto>
)

/**
 * Comment DTO for community posts
 */
data class CommentDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("username") val username: String,
    @SerializedName("content") val content: String,
    @SerializedName("createdAt") val createdAt: String
)

/**
 * Educational content DTO
 */
data class EducationalContentDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("summary") val summary: String,
    @SerializedName("content") val content: String,
    @SerializedName("category") val category: String,
    @SerializedName("imageUrl") val imageUrl: String?,
    @SerializedName("createdAt") val createdAt: String
)
