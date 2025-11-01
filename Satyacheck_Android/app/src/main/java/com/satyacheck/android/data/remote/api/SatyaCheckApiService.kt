package com.satyacheck.android.data.remote.api

import com.satyacheck.android.data.remote.model.AuthRequest
import com.satyacheck.android.data.remote.model.AuthResponse
import com.satyacheck.android.data.remote.model.BulkAnalysisRequest
import com.satyacheck.android.data.remote.model.BulkAnalysisResponse
import com.satyacheck.android.data.remote.model.EnhancedAnalysisResponse
import com.satyacheck.android.data.remote.model.FeedbackRequest
import com.satyacheck.android.data.remote.model.UrlAnalysisResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * SatyaCheck API service interface for the Android app.
 * This interface defines all the API endpoints that the app needs to communicate with the backend.
 */
interface SatyaCheckApiService {

    /**
     * Authentication endpoints
     */
    @POST("api/auth/register")
    suspend fun register(@Body request: AuthRequest.Register): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthRequest.Login): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: AuthRequest.RefreshToken): Response<AuthResponse>

    /**
     * URL Analysis endpoints
     */
    @POST("api/v1/enhanced-analysis/url")
    suspend fun analyzeUrl(@Query("url") url: String): Response<UrlAnalysisResponse>

    /**
     * Content Analysis endpoints
     */
    @POST("api/v1/enhanced-analysis/comprehensive")
    suspend fun analyzeContent(
        @Query("content") content: String,
        @Query("language") language: String = "en",
        @Query("source") source: String? = null,
        @Query("contentType") contentType: String? = null,
        @Query("userRegion") userRegion: String? = null,
        @Query("deviceType") deviceType: String? = null
    ): Response<EnhancedAnalysisResponse>

    /**
     * Bulk Analysis endpoints
     */
    @POST("api/v1/bulk-analysis/urls")
    suspend fun bulkAnalyzeUrls(@Body request: BulkAnalysisRequest): Response<BulkAnalysisResponse>

    @GET("api/v1/bulk-analysis/{id}")
    suspend fun getBulkAnalysis(@Path("id") id: String): Response<BulkAnalysisResponse>

    @GET("api/v1/bulk-analysis/user/{userId}")
    suspend fun getUserBulkAnalyses(@Path("userId") userId: String): Response<List<BulkAnalysisResponse>>

    /**
     * Feedback endpoints
     */
    @POST("api/v1/feedback")
    suspend fun submitFeedback(@Body request: FeedbackRequest): Response<Any>

    @GET("api/v1/feedback/url")
    suspend fun getUrlFeedback(@Query("url") url: String): Response<List<Any>>
    
    /**
     * Educational Content endpoints
     */
    @GET("api/v1/articles")
    suspend fun getArticles(
        @Query("language") language: String = "en",
        @Query("category") category: String? = null
    ): Response<List<com.satyacheck.android.domain.model.Article>>
    
    @GET("api/v1/articles/{slug}")
    suspend fun getArticleBySlug(
        @Path("slug") slug: String,
        @Query("language") language: String = "en"
    ): Response<com.satyacheck.android.domain.model.Article>
}