package com.satyacheck.android.data.remote.api

import com.satyacheck.android.data.remote.ApiConstants
import com.satyacheck.android.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.http.Path
import retrofit2.http.*

interface AuthService {
    @POST(ApiConstants.LOGIN)
    suspend fun login(
        @Body loginRequest: Map<String, String>
    ): ApiResponse<AuthDto>
    
    @POST(ApiConstants.REGISTER)
    suspend fun register(
        @Body registerRequest: Map<String, String>
    ): ApiResponse<AuthDto>
    
    @POST(ApiConstants.REFRESH_TOKEN)
    suspend fun refreshToken(
        @Body refreshRequest: Map<String, String>
    ): ApiResponse<AuthDto>
}

interface AnalysisService {
    @POST(ApiConstants.ANALYZE_TEXT)
    suspend fun analyzeText(
        @Body request: AnalysisRequestDto
    ): ApiResponse<AnalysisResultDto>
    
    @Multipart
    @POST(ApiConstants.ANALYZE_IMAGE)
    suspend fun analyzeImage(
        @Part image: MultipartBody.Part,
        @Part("language") language: String
    ): ApiResponse<AnalysisResultDto>
    
    @Multipart
    @POST(ApiConstants.ANALYZE_AUDIO)
    suspend fun analyzeAudio(
        @Part audio: MultipartBody.Part,
        @Part("language") language: String
    ): ApiResponse<AnalysisResultDto>
    
    @GET(ApiConstants.ANALYSIS_HISTORY)
    suspend fun getAnalysisHistory(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<List<AnalysisResultDto>>
}

interface UserService {
    @GET(ApiConstants.USER_PROFILE)
    suspend fun getUserProfile(): ApiResponse<UserProfileDto>
    
    @GET(ApiConstants.USER_PROFILE_BY_ID)
    suspend fun getUserProfileById(
        @Path("userId") userId: String
    ): ApiResponse<UserProfileDto>
    
    @PATCH(ApiConstants.USER_PROFILE)
    suspend fun updateUserProfile(
        @Body profileData: Map<String, String>
    ): ApiResponse<UserProfileDto>
    
    @PUT(ApiConstants.USER_SETTINGS)
    suspend fun updateUserSettings(
        @Body settings: Map<String, Any>
    ): ApiResponse<Map<String, Any>>
}

interface CommunityService {
    @GET(ApiConstants.COMMUNITY_ALERTS)
    suspend fun getCommunityAlerts(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("location") location: String? = null
    ): ApiResponse<List<CommunityAlertDto>>
    
    @GET(ApiConstants.COMMUNITY_POSTS)
    suspend fun getCommunityPosts(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): ApiResponse<List<CommunityPostDto>>
    
    @GET("${ApiConstants.COMMUNITY_POSTS}/{id}")
    suspend fun getCommunityPost(
        @Path("id") postId: String
    ): ApiResponse<CommunityPostDto>
    
    @POST(ApiConstants.COMMUNITY_POSTS)
    suspend fun createCommunityPost(
        @Body post: Map<String, String>
    ): ApiResponse<CommunityPostDto>
    
    @POST("${ApiConstants.COMMUNITY_POSTS}/{id}/comments")
    suspend fun addComment(
        @Path("id") postId: String,
        @Body comment: Map<String, String>
    ): ApiResponse<CommentDto>
    
    @POST(ApiConstants.COMMUNITY_REPORT)
    suspend fun reportMisinformation(
        @Body report: Map<String, String>
    ): ApiResponse<Map<String, String>>
}

interface EducationalService {
    @GET(ApiConstants.EDUCATIONAL_CONTENT)
    suspend fun getEducationalContent(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("category") category: String? = null
    ): ApiResponse<List<EducationalContentDto>>
    
    @GET("${ApiConstants.EDUCATIONAL_CONTENT}/{id}")
    suspend fun getEducationalContentById(
        @Path("id") contentId: String
    ): ApiResponse<EducationalContentDto>
    
    @GET(ApiConstants.EDUCATIONAL_CATEGORIES)
    suspend fun getEducationalCategories(): ApiResponse<List<String>>
}
