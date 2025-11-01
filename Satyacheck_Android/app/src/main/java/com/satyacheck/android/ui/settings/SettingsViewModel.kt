package com.satyacheck.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyacheck.android.data.repository.UserPreferencesRepository
import com.satyacheck.android.presentation.theme.ThemeMode
import com.satyacheck.android.utils.PermissionHelpers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class representing the UI state of the Settings screen
 */
data class SettingsUiState(
    val notificationPermissionGranted: Boolean = false,
    val cameraPermissionGranted: Boolean = false,
    val microphonePermissionGranted: Boolean = false,
    val privacyPolicyAccepted: Boolean = false,
    val termsAccepted: Boolean = false,
    val currentLanguage: String = "English",
    val notificationsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

/**
 * ViewModel for the Settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val permissionHelpers: PermissionHelpers
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    /**
     * Load all settings from the repository
     */
    private fun loadSettings() {
        viewModelScope.launch {
            // Check permission states from both DataStore and actual runtime permissions
            val notificationPermissionState = userPreferencesRepository.isNotificationPermissionGranted() &&
                    permissionHelpers.isPermissionGranted(android.Manifest.permission.POST_NOTIFICATIONS)
            
            val cameraPermissionState = userPreferencesRepository.isCameraPermissionGranted() &&
                    permissionHelpers.isPermissionGranted(android.Manifest.permission.CAMERA)
            
            val microphonePermissionState = userPreferencesRepository.isMicrophonePermissionGranted() &&
                    permissionHelpers.isPermissionGranted(android.Manifest.permission.RECORD_AUDIO)
            
            // Load theme mode
            val themeModeFlow = userPreferencesRepository.getThemeMode().map { themeModeString ->
                try {
                    ThemeMode.valueOf(themeModeString)
                } catch (e: IllegalArgumentException) {
                    ThemeMode.SYSTEM
                }
            }
            
            // Update local state
            _uiState.update { currentState ->
                currentState.copy(
                    notificationPermissionGranted = notificationPermissionState,
                    cameraPermissionGranted = cameraPermissionState,
                    microphonePermissionGranted = microphonePermissionState,
                    privacyPolicyAccepted = userPreferencesRepository.isPrivacyPolicyAccepted(),
                    termsAccepted = userPreferencesRepository.isTermsAccepted()
                )
            }
            
            // Update UI state with theme mode
            themeModeFlow.collectLatest { themeMode ->
                _uiState.update { it.copy(themeMode = themeMode) }
            }
            
            // Update preferences to match actual permission states
            userPreferencesRepository.setNotificationPermissionGranted(notificationPermissionState)
            userPreferencesRepository.setCameraPermissionGranted(cameraPermissionState)
            userPreferencesRepository.setMicrophonePermissionGranted(microphonePermissionState)
        }
    }
    
    /**
     * Request a specific permission
     */
    fun requestPermission(permission: String) {
        viewModelScope.launch {
            val granted = permissionHelpers.requestPermission(permission)
            
            // Update the UI state based on the permission type
            when (permission) {
                android.Manifest.permission.CAMERA -> {
                    _uiState.update { it.copy(cameraPermissionGranted = granted) }
                    userPreferencesRepository.setCameraPermissionGranted(granted)
                }
                android.Manifest.permission.RECORD_AUDIO -> {
                    _uiState.update { it.copy(microphonePermissionGranted = granted) }
                    userPreferencesRepository.setMicrophonePermissionGranted(granted)
                }
                android.Manifest.permission.POST_NOTIFICATIONS -> {
                    _uiState.update { it.copy(notificationPermissionGranted = granted) }
                    userPreferencesRepository.setNotificationPermissionGranted(granted)
                }
            }
        }
    }
    
    /**
     * Set language preference
     */
    fun setLanguage(language: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveUserLanguage(language)
            _uiState.update { it.copy(currentLanguage = language) }
        }
    }
    
    /**
     * Set the theme mode and save it to preferences
     */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode.name)
            _uiState.update { it.copy(themeMode = mode) }
        }
    }
    
    /**
     * Toggle notification state
     */
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveNotificationEnabled(enabled)
            _uiState.update { it.copy(notificationsEnabled = enabled) }
        }
    }
}
