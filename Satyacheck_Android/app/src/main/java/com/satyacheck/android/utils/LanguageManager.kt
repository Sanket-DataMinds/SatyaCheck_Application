package com.satyacheck.android.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.satyacheck.android.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Utility class to manage language settings across the app
 */
object LanguageManager {
    private const val TAG = "LanguageManager"
    
    // Supported languages
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_HINDI = "hi"
    const val LANGUAGE_MARATHI = "mr"
    
    // Default language (fallback)
    const val DEFAULT_LANGUAGE = LANGUAGE_ENGLISH
    
    /**
     * Get the list of supported languages with their display names
     * @return Map of language codes to their display names
     */
    fun getSupportedLanguages(): Map<String, String> {
        return mapOf(
            LANGUAGE_ENGLISH to "English",
            LANGUAGE_HINDI to "हिंदी",
            LANGUAGE_MARATHI to "मराठी"
        )
    }
    
    /**
     * Apply the selected language to the app
     * 
     * @param context The application context
     * @param languageCode The ISO language code (e.g., "en", "hi", "mr")
     * @return Configuration with the updated locale
     */
    fun applyLanguage(context: Context, languageCode: String): Configuration {
        val locale = when (languageCode) {
            LANGUAGE_HINDI -> Locale(LANGUAGE_HINDI)
            LANGUAGE_MARATHI -> Locale(LANGUAGE_MARATHI)
            else -> Locale(LANGUAGE_ENGLISH)
        }
        
        Locale.setDefault(locale)
        
        val config = context.resources.configuration
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            config.setLocales(localeList)
        } else {
            config.locale = locale
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context.createConfigurationContext(config)
        } else {
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
        
        Log.d(TAG, "Applied language: $languageCode")
        return config
    }
    
    /**
     * Save the selected language to user preferences
     * 
     * @param userPreferencesRepository Repository to save preferences
     * @param languageCode The language code to save
     */
    fun saveLanguagePreference(userPreferencesRepository: UserPreferencesRepository, languageCode: String) {
        CoroutineScope(Dispatchers.IO).launch {
            userPreferencesRepository.saveUserLanguage(languageCode)
            Log.d(TAG, "Saved language preference: $languageCode")
        }
    }
    
    /**
     * Update API and model language based on user's preference
     * This method should be called when making API requests to ensure
     * the response is in the user's preferred language
     * 
     * @param context Application context
     * @return ISO language code to use for API requests
     */
    fun getApiLanguageCode(context: Context): String {
        val currentLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            context.resources.configuration.locale
        }
        
        return currentLocale.language
    }
    
    /**
     * Get the current active locale of the app
     * 
     * @param context Application context
     * @return The current Locale
     */
    fun getCurrentLocale(context: Context): Locale {
        val configuration = context.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales.get(0)
        } else {
            configuration.locale
        }
    }
    
    /**
     * Get the current language code (e.g., "en", "hi", "mr")
     * 
     * @param context Application context
     * @return The current language code
     */
    fun getCurrentLanguageCode(context: Context): String {
        return getCurrentLocale(context).language
    }
    
    /**
     * Apply the system's night mode setting
     * 
     * @param nightMode The night mode to apply
     */
    fun applyNightMode(nightMode: Int) {
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
