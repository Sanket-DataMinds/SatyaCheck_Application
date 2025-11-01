package com.satyacheck.android.presentation.screens.onboarding

import android.content.Context
import androidx.compose.foundation.pager.PagerState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyacheck.android.data.repository.AuthRepository
import com.satyacheck.android.data.repository.UserPreferencesRepository
import com.satyacheck.android.domain.model.Result
import com.satyacheck.android.utils.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _selectedLanguage = MutableStateFlow(LanguageManager.DEFAULT_LANGUAGE)
    val selectedLanguage = _selectedLanguage.asStateFlow()
    
    private val _locationPermissionGranted = MutableStateFlow(false)
    val locationPermissionGranted = _locationPermissionGranted.asStateFlow()
    
    // Auth state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()
    
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()
    
    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode = _isGuestMode.asStateFlow()
    
    fun setLanguage(languageCode: String) {
        _selectedLanguage.value = languageCode
        // Save language preference to DataStore
        viewModelScope.launch {
            userPreferencesRepository.saveUserLanguage(languageCode)
        }
    }
    
    fun setLocationPermission(granted: Boolean) {
        _locationPermissionGranted.value = granted
    }
    
    @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
    fun nextPage(pagerState: PagerState) {
        viewModelScope.launch {
            if (pagerState.currentPage < pagerState.pageCount - 1) {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    }
    
    fun signIn(email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            authRepository.login(email, password).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _isAuthenticated.value = true
                        _isLoading.value = false
                    }
                    is Result.Error -> {
                        _errorMessage.value = result.message
                        _isLoading.value = false
                    }
                    is Result.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }
    
    fun signUp(name: String, email: String, password: String) {
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            authRepository.register(name, email, password, _selectedLanguage.value).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _isAuthenticated.value = true
                        _isLoading.value = false
                    }
                    is Result.Error -> {
                        _errorMessage.value = result.message
                        _isLoading.value = false
                    }
                    is Result.Loading -> {
                        _isLoading.value = true
                    }
                }
            }
        }
    }
    
    fun googleSignIn() {
        // This will be handled in the activity/fragment with the Google Sign-In SDK
        // The result will be passed back to the ViewModel
        _isLoading.value = true
    }
    
    fun handleGoogleSignInResult(success: Boolean, errorMsg: String? = null) {
        _isLoading.value = false
        if (success) {
            _isAuthenticated.value = true
        } else {
            _errorMessage.value = errorMsg ?: "Google sign-in failed"
        }
    }
    
    fun continueAsGuest() {
        _isGuestMode.value = true
        _isAuthenticated.value = true
    }
    
    fun completeOnboarding(context: Context) {
        val prefs = context.getSharedPreferences("satya_check_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("onboarding_completed", true)
            .putString("language", _selectedLanguage.value)
            .putBoolean("guest_mode", _isGuestMode.value)
            .apply()
    }
}
