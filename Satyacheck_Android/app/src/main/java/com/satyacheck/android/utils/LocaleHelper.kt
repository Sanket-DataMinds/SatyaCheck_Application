package com.satyacheck.android.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Helper class to handle locale changes at the Context level
 * This ensures that all Activities and UI components use the selected language
 */
class LocaleHelper {
    companion object {
        private const val LANGUAGE_PREFERENCE = "language_preference"
        
        /**
         * Attach the selected language to a context
         * 
         * @param context Base context
         * @param languageCode ISO language code (e.g., "en", "hi", "mr")
         * @return Context with updated locale
         */
        fun onAttach(context: Context, languageCode: String): Context {
            val locale = getLocaleFromLanguage(languageCode)
            val config = setLocale(context, locale)
            return updateResources(context, config)
        }
        
        /**
         * Attach using the saved language preference
         * 
         * @param context Base context
         * @return Context with updated locale
         */
        fun onAttach(context: Context): Context {
            val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val language = sharedPreferences.getString(LANGUAGE_PREFERENCE, LanguageManager.DEFAULT_LANGUAGE)
                ?: LanguageManager.DEFAULT_LANGUAGE
            return onAttach(context, language)
        }
        
        /**
         * Set the locale configuration
         * 
         * @param context Application context
         * @param locale The locale to apply
         * @return Updated configuration
         */
        private fun setLocale(context: Context, locale: Locale): Configuration {
            Locale.setDefault(locale)
            
            val config = context.resources.configuration
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val localeList = LocaleList(locale)
                LocaleList.setDefault(localeList)
                config.setLocales(localeList)
            } else {
                config.locale = locale
            }
            
            return config
        }
        
        /**
         * Update resources with new configuration
         * 
         * @param context Application context
         * @param config Updated configuration
         * @return Context with updated resources
         */
        @SuppressLint("NewApi")
        private fun updateResources(context: Context, config: Configuration): Context {
            var updatedContext = context
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                updatedContext = context.createConfigurationContext(config)
            } else {
                val resources = context.resources
                resources.updateConfiguration(config, resources.displayMetrics)
            }
            
            return updatedContext
        }
        
        /**
         * Get Locale from language code
         * 
         * @param languageCode ISO language code
         * @return Locale object
         */
        private fun getLocaleFromLanguage(languageCode: String): Locale {
            return when (languageCode) {
                LanguageManager.LANGUAGE_HINDI -> Locale(LanguageManager.LANGUAGE_HINDI)
                LanguageManager.LANGUAGE_MARATHI -> Locale(LanguageManager.LANGUAGE_MARATHI)
                else -> Locale(LanguageManager.LANGUAGE_ENGLISH)
            }
        }
        
        /**
         * Save the selected language preference
         * 
         * @param context Application context
         * @param languageCode ISO language code
         */
        fun saveLanguagePreference(context: Context, languageCode: String) {
            val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString(LANGUAGE_PREFERENCE, languageCode).apply()
        }
        
        /**
         * Get the current saved language code
         * 
         * @param context Application context
         * @return ISO language code
         */
        fun getLanguagePreference(context: Context): String {
            val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            return sharedPreferences.getString(LANGUAGE_PREFERENCE, LanguageManager.DEFAULT_LANGUAGE)
                ?: LanguageManager.DEFAULT_LANGUAGE
        }
    }
}
