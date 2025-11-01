package com.satyacheck.android.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyacheck.android.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    // Current theme state
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    init {
        // Load the saved theme preference when the ViewModel is created
        viewModelScope.launch {
            userPreferencesRepository.getThemeMode().collectLatest { themeModeString ->
                _themeMode.value = try {
                    ThemeMode.valueOf(themeModeString)
                } catch (e: IllegalArgumentException) {
                    ThemeMode.SYSTEM // Default if the saved value isn't valid
                }
            }
        }
    }
    
    /**
     * Set the theme mode and save it to preferences
     */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode.name)
            _themeMode.value = mode
        }
    }
}
