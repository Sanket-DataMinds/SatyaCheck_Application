package com.satyacheck.android.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyacheck.android.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for handling the onboarding process
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    /**
     * Mark onboarding as complete and save user preferences
     */
    fun completeOnboarding(
        notificationPermissionGranted: Boolean,
        cameraPermissionGranted: Boolean,
        microphonePermissionGranted: Boolean,
        privacyPolicyAccepted: Boolean,
        termsAccepted: Boolean
    ) {
        viewModelScope.launch {
            // Save onboarding status
            userPreferencesRepository.setOnboardingCompleted(true)
            
            // Save permission states
            userPreferencesRepository.setNotificationPermissionGranted(notificationPermissionGranted)
            userPreferencesRepository.setCameraPermissionGranted(cameraPermissionGranted)
            userPreferencesRepository.setMicrophonePermissionGranted(microphonePermissionGranted)
            
            // Save legal agreement states
            userPreferencesRepository.setPrivacyPolicyAccepted(privacyPolicyAccepted)
            userPreferencesRepository.setTermsAccepted(termsAccepted)
        }
    }
    
    /**
     * Check if onboarding has been completed
     */
    suspend fun isOnboardingCompleted(): Boolean {
        return userPreferencesRepository.isOnboardingCompleted()
    }
}
