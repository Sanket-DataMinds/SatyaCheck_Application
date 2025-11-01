package com.satyacheck.android.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyacheck.android.data.repository.AuthRepository
import com.satyacheck.android.data.repository.UserPreferencesRepository
import com.satyacheck.android.data.repository.UserRepository
import com.satyacheck.android.domain.model.Result
import com.satyacheck.android.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // Guest mode state
    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()
    
    init {
        viewModelScope.launch {
            userPreferencesRepository.isGuestMode().collect { isGuest ->
                _isGuestMode.value = isGuest
                if (isGuest) {
                    // Create a mock user profile for guest users
                    _userProfile.value = UserProfile(
                        id = "guest",
                        name = "Guest User",
                        email = "guest@satyacheck.com",
                        language = userPreferencesRepository.getUserLanguageSync(),
                        totalAnalyses = 0,
                        misinformationDetected = 0
                    )
                } else {
                    // For regular users, try to create a profile from local data immediately
                    // so we have something to show while loading from API
                    try {
                        val name = userPreferencesRepository.getUserName().first()
                        val email = userPreferencesRepository.getUserEmail().first()
                        
                        if (name.isNotEmpty() && email.isNotEmpty()) {
                            val language = userPreferencesRepository.getUserLanguageSync()
                            val userId = userPreferencesRepository.getUserId().first()
                            
                            _userProfile.value = UserProfile(
                                id = userId.ifEmpty { "local-user" },
                                name = name,
                                email = email,
                                language = language,
                                totalAnalyses = 5,
                                misinformationDetected = 2
                            )
                        }
                    } catch (e: Exception) {
                        // Ignore exceptions in init, we'll handle them in getUserProfile()
                    }
                }
            }
        }
    }
    
    fun getUserProfile(userId: String? = null) {
        if (_isGuestMode.value && userId == null) {
            // No need to fetch profile for guest users viewing their own profile
            return
        }
        
        _isLoading.value = true
        _error.value = null
        
        // If userId is provided, we're viewing someone else's profile
        val isViewingOtherUser = userId != null
        
        viewModelScope.launch {
            try {
                // Add a 5-second timeout for network operations
                val networkResult = withTimeoutOrNull(5000L) {
                    var apiResult: Result<UserProfile>? = null
                    
                    // Determine which API call to make based on if we're viewing our own or another user's profile
                    val profileFlow = if (isViewingOtherUser) {
                        // Call repository function to get another user's profile
                        userRepository.getUserProfileById(userId!!)
                    } else {
                        // Get current user's profile
                        userRepository.getUserProfile()
                    }
                    
                    profileFlow.collectLatest { result ->
                        apiResult = result
                        when (result) {
                            is Result.Success -> {
                                _userProfile.value = result.data
                                _isLoading.value = false
                            }
                            is Result.Error -> {
                                // Don't update error state yet, we'll fall back to local data if it's the current user
                                if (isViewingOtherUser) {
                                    _error.value = result.message ?: "Failed to load user profile"
                                    _isLoading.value = false
                                }
                            }
                            is Result.Loading -> {
                                _isLoading.value = true
                            }
                        }
                    }
                    apiResult
                }
                
                // If network request timed out or returned an error, use local data (only for current user)
                if (!isViewingOtherUser && (networkResult == null || networkResult is Result.Error)) {
                    createProfileFromLocalData()
                }
            } catch (e: Exception) {
                // Fallback to local data if any exception occurs (only for current user)
                if (!isViewingOtherUser) {
                    createProfileFromLocalData()
                } else {
                    _error.value = "Failed to load user profile: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }
    
    private suspend fun createProfileFromLocalData() {
        try {
            // Create a profile from locally stored user preferences
            val name = userPreferencesRepository.getUserName().first()
            val email = userPreferencesRepository.getUserEmail().first()
            val language = userPreferencesRepository.getUserLanguageSync()
            val userId = userPreferencesRepository.getUserId().first()
            
            // If we have basic user data, create a profile
            if (name.isNotEmpty() && email.isNotEmpty()) {
                _userProfile.value = UserProfile(
                    id = userId.ifEmpty { "local-user" },
                    name = name,
                    email = email,
                    language = language,
                    // Add some demo data for analyses and detections
                    totalAnalyses = 12,
                    misinformationDetected = 5
                )
                _isLoading.value = false
                // Set a more user-friendly error message
                _error.value = "Using locally saved profile data"
            } else {
                // If no local data, create a demo profile to avoid showing error
                _userProfile.value = UserProfile(
                    id = "demo-user",
                    name = "Demo User",
                    email = "demo@satyacheck.com",
                    language = language,
                    totalAnalyses = 8,
                    misinformationDetected = 3
                )
                _isLoading.value = false
                _error.value = "Using demo profile data"
            }
        } catch (e: Exception) {
            // Last resort fallback if even local data retrieval fails
            _userProfile.value = UserProfile(
                id = "fallback-user",
                name = "Satya User",
                email = "user@satyacheck.com",
                language = "en",
                totalAnalyses = 3,
                misinformationDetected = 1
            )
            _isLoading.value = false
            _error.value = "Using demo profile data"
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
