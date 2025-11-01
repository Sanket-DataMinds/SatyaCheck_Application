package com.satyacheck.android.data.repository

import com.satyacheck.android.data.remote.api.AuthService
import com.satyacheck.android.data.remote.dto.ApiResponse
import com.satyacheck.android.data.remote.dto.AuthDto
import com.satyacheck.android.domain.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authService: AuthService,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend fun login(email: String, password: String): Flow<Result<Boolean>> = flow {
        emit(Result.Loading())
        try {
            val request = mapOf(
                "email" to email,
                "password" to password
            )
            val response = authService.login(request)
            
            if (response.status == "success" && response.data != null) {
                // Save auth info to preferences
                saveAuthInfo(response.data)
                emit(Result.Success(true))
            } else {
                emit(Result.Error(response.message ?: "Login failed"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun register(name: String, email: String, password: String, language: String): Flow<Result<Boolean>> = flow {
        emit(Result.Loading())
        try {
            val request = mapOf(
                "name" to name,
                "email" to email,
                "password" to password,
                "language" to language
            )
            val response = authService.register(request)
            
            if (response.status == "success" && response.data != null) {
                // Save auth info to preferences
                saveAuthInfo(response.data)
                // Also save name and email
                userPreferencesRepository.saveUserName(name)
                userPreferencesRepository.saveUserEmail(email)
                userPreferencesRepository.saveUserLanguage(language)
                emit(Result.Success(true))
            } else {
                emit(Result.Error(response.message ?: "Registration failed"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun refreshToken(): Flow<Result<Boolean>> = flow {
        emit(Result.Loading())
        try {
            userPreferencesRepository.getRefreshToken().collect { token ->
                if (token.isEmpty()) {
                    emit(Result.Error("No refresh token available"))
                    return@collect
                }
                
                val request = mapOf(
                    "refreshToken" to token
                )
                
                val response = authService.refreshToken(request)
                
                if (response.status == "success" && response.data != null) {
                    // Save auth info to preferences
                    saveAuthInfo(response.data)
                    emit(Result.Success(true))
                } else {
                    emit(Result.Error(response.message ?: "Token refresh failed"))
                }
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun logout() {
        userPreferencesRepository.clearAuthData()
    }
    
    private suspend fun saveAuthInfo(authDto: AuthDto) {
        userPreferencesRepository.saveAuthInfo(
            token = authDto.token,
            refreshToken = authDto.refreshToken,
            userId = authDto.userId
        )
    }
}
