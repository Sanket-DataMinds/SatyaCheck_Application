package com.satyacheck.android.repository

import com.satyacheck.android.model.AuthRequest
import com.satyacheck.android.model.AuthResponse
import com.satyacheck.android.model.BulkAnalysisRequest
import com.satyacheck.android.model.BulkAnalysisResponse
import com.satyacheck.android.model.EnhancedAnalysisResponse
import com.satyacheck.android.model.FeedbackRequest
import com.satyacheck.android.model.UrlAnalysisResponse
import com.satyacheck.android.network.SatyaCheckApiClient
import com.satyacheck.android.network.SatyaCheckApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for interacting with the SatyaCheck API
 */
class SatyaCheckRepository(
    private val tokenManager: TokenManager
) {
    private val apiService: SatyaCheckApiService = SatyaCheckApiClient.create {
        tokenManager.getAccessToken()
    }

    /**
     * Authentication functions
     */
    suspend fun register(username: String, password: String, email: String, name: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = AuthRequest.Register(
                    username = username,
                    password = password,
                    email = email,
                    name = name
                )
                
                val response = apiService.register(request)
                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        authResponse.data?.let { authData ->
                            tokenManager.saveTokens(
                                accessToken = authData.accessToken,
                                refreshToken = authData.refreshToken,
                                expiresIn = authData.expiresIn
                            )
                        }
                        Result.success(authResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Registration failed: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = AuthRequest.Login(
                    username = username,
                    password = password
                )
                
                val response = apiService.login(request)
                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        authResponse.data?.let { authData ->
                            tokenManager.saveTokens(
                                accessToken = authData.accessToken,
                                refreshToken = authData.refreshToken,
                                expiresIn = authData.expiresIn
                            )
                        }
                        Result.success(authResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Login failed: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun refreshToken(): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = tokenManager.getRefreshToken()
                    ?: return@withContext Result.failure(Exception("No refresh token available"))
                
                val request = AuthRequest.RefreshToken(refreshToken)
                val response = apiService.refreshToken(request)
                
                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        authResponse.data?.let { authData ->
                            tokenManager.saveTokens(
                                accessToken = authData.accessToken,
                                refreshToken = authData.refreshToken,
                                expiresIn = authData.expiresIn
                            )
                        }
                        Result.success(authResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Token refresh failed: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * URL Analysis functions
     */
    suspend fun analyzeUrl(url: String): Result<UrlAnalysisResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.analyzeUrl(url)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("URL analysis failed: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Content Analysis functions
     */
    suspend fun analyzeContent(
        content: String,
        language: String = "en",
        source: String? = null,
        contentType: String? = null
    ): Result<EnhancedAnalysisResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.analyzeContent(
                    content = content,
                    language = language,
                    source = source,
                    contentType = contentType
                )
                
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Content analysis failed: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Bulk Analysis functions
     */
    suspend fun bulkAnalyzeUrls(urlList: List<String>): Result<BulkAnalysisResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = BulkAnalysisRequest(
                    items = urlList.map { url -> 
                        com.satyacheck.android.model.BulkUrlItem(url = url) 
                    }
                )
                
                val response = apiService.bulkAnalyzeUrls(request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Bulk analysis failed: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getBulkAnalysis(id: String): Result<BulkAnalysisResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBulkAnalysis(id)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Get bulk analysis failed: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Feedback functions
     */
    suspend fun submitFeedback(
        userId: String,
        url: String,
        type: String,
        comment: String? = null,
        originalVerdict: String? = null
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val request = FeedbackRequest(
                    userId = userId,
                    url = url,
                    type = type,
                    comment = comment,
                    originalVerdict = originalVerdict
                )
                
                val response = apiService.submitFeedback(request)
                if (response.isSuccessful) {
                    Result.success(true)
                } else {
                    Result.failure(Exception("Submit feedback failed: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

/**
 * Interface for token management
 */
interface TokenManager {
    fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun clearTokens()
}