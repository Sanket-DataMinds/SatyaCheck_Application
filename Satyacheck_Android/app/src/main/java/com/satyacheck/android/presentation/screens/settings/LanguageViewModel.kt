package com.satyacheck.android.presentation.screens.settings

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyacheck.android.data.repository.UserPreferencesRepository
import com.satyacheck.android.utils.LanguageManager
import com.satyacheck.android.utils.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LanguageState(
    val currentLanguage: String = LanguageManager.DEFAULT_LANGUAGE,
    val isLoading: Boolean = false,
    val languageChanged: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    var state by mutableStateOf(LanguageState())
        private set
    
    init {
        viewModelScope.launch {
            userPreferencesRepository.getUserLanguage().collectLatest { language ->
                state = state.copy(currentLanguage = language)
            }
        }
    }
    
    /**
     * Save the selected language to preferences
     * 
     * @param languageCode ISO language code (e.g., "en", "hi", "mr")
     */
    fun saveLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                
                // Save in preferences repository
                userPreferencesRepository.saveUserLanguage(languageCode)
                
                // Also save in SharedPreferences for immediate application
                LocaleHelper.saveLanguagePreference(context, languageCode)
                
                // Apply language change
                LanguageManager.applyLanguage(context, languageCode)
                
                state = state.copy(
                    currentLanguage = languageCode,
                    isLoading = false,
                    languageChanged = true
                )
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * Reset the language changed flag
     */
    fun resetLanguageChangedFlag() {
        state = state.copy(languageChanged = false)
    }
}
