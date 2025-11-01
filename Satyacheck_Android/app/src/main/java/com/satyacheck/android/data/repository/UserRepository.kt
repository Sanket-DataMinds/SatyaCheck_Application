package com.satyacheck.android.data.repository

import com.satyacheck.android.data.remote.api.UserService
import com.satyacheck.android.domain.model.Result
import com.satyacheck.android.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userService: UserService,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    
    suspend fun getUserProfile(): Flow<Result<UserProfile>> = flow {
        emit(Result.Loading())
        try {
            val response = userService.getUserProfile()
            
            if (response.status == "success" && response.data != null) {
                val data = response.data
                
                // Update preferences with latest user data
                userPreferencesRepository.saveUserName(data.name)
                userPreferencesRepository.saveUserEmail(data.email)
                userPreferencesRepository.saveUserLanguage(data.language)
                
                val userProfile = UserProfile(
                    id = data.id,
                    name = data.name,
                    email = data.email,
                    language = data.language,
                    totalAnalyses = data.totalAnalyses,
                    misinformationDetected = data.misinformationDetected
                )
                
                emit(Result.Success(userProfile))
            } else {
                emit(Result.Error(response.message ?: "Failed to get user profile"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun updateUserProfile(name: String? = null, language: String? = null): Flow<Result<UserProfile>> = flow {
        emit(Result.Loading())
        try {
            val updates = mutableMapOf<String, String>()
            
            name?.let { updates["name"] = it }
            language?.let { updates["language"] = it }
            
            if (updates.isEmpty()) {
                emit(Result.Error("No updates provided"))
                return@flow
            }
            
            val response = userService.updateUserProfile(updates)
            
            if (response.status == "success" && response.data != null) {
                val data = response.data
                
                // Update preferences with latest user data
                name?.let { userPreferencesRepository.saveUserName(it) }
                language?.let { userPreferencesRepository.saveUserLanguage(it) }
                
                val userProfile = UserProfile(
                    id = data.id,
                    name = data.name,
                    email = data.email,
                    language = data.language,
                    totalAnalyses = data.totalAnalyses,
                    misinformationDetected = data.misinformationDetected
                )
                
                emit(Result.Success(userProfile))
            } else {
                emit(Result.Error(response.message ?: "Failed to update user profile"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    suspend fun updateNotificationSettings(enabled: Boolean): Flow<Result<Boolean>> = flow {
        emit(Result.Loading())
        try {
            val settings = mapOf(
                "notificationsEnabled" to enabled
            )
            
            val response = userService.updateUserSettings(settings)
            
            if (response.status == "success") {
                // Update local preference
                userPreferencesRepository.saveNotificationEnabled(enabled)
                emit(Result.Success(true))
            } else {
                emit(Result.Error(response.message ?: "Failed to update notification settings"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
    
    // Function to get another user's profile by ID
    suspend fun getUserProfileById(userId: String): Flow<Result<UserProfile>> = flow {
        emit(Result.Loading())
        try {
            val response = userService.getUserProfileById(userId)
            
            if (response.status == "success" && response.data != null) {
                val data = response.data
                
                val userProfile = UserProfile(
                    id = data.id,
                    name = data.name,
                    email = data.email,
                    language = data.language,
                    totalAnalyses = data.totalAnalyses,
                    misinformationDetected = data.misinformationDetected
                )
                
                emit(Result.Success(userProfile))
            } else {
                emit(Result.Error(response.message ?: "Failed to get user profile"))
            }
        } catch (e: IOException) {
            emit(Result.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Result.Error("An error occurred: ${e.message}"))
        }
    }
}
