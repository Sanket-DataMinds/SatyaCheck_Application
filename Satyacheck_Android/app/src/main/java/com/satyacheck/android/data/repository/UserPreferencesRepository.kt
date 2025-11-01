package com.satyacheck.android.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Keys for preferences
    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_LANGUAGE = stringPreferencesKey("user_language")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val NOTIFICATION_ENABLED = stringPreferencesKey("notification_enabled")
        
        // Onboarding and permissions keys
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val NOTIFICATION_PERMISSION_GRANTED = booleanPreferencesKey("notification_permission_granted")
        private val CAMERA_PERMISSION_GRANTED = booleanPreferencesKey("camera_permission_granted")
        private val MICROPHONE_PERMISSION_GRANTED = booleanPreferencesKey("microphone_permission_granted")
        private val PRIVACY_POLICY_ACCEPTED = booleanPreferencesKey("privacy_policy_accepted")
        private val TERMS_ACCEPTED = booleanPreferencesKey("terms_accepted")
        
        // Theme preference key
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        
        // Auth mode
        private val GUEST_MODE = booleanPreferencesKey("guest_mode")
        
        // Learning progress keys
        private val READ_ARTICLES = stringPreferencesKey("read_articles")
        private val COMPLETED_QUIZZES = stringPreferencesKey("completed_quizzes")
        
        // Deprecated, kept for backward compatibility
        private val STORAGE_PERMISSION_GRANTED = booleanPreferencesKey("storage_permission_granted")
    }
    
    // Get the auth token flow
    fun getAuthToken(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN] ?: ""
    }
    
    // Save auth token
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
        }
    }
    
    // Get the refresh token flow
    fun getRefreshToken(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN] ?: ""
    }
    
    // Save refresh token
    suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN] = token
        }
    }
    
    // Get the user ID flow
    fun getUserId(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_ID] ?: ""
    }
    
    // Save user ID
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
        }
    }
    
    // Get the user language flow
    fun getUserLanguage(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_LANGUAGE] ?: "en" // Default to English language code
    }
    
    // Save user language
    suspend fun saveUserLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_LANGUAGE] = language
        }
    }
    
    // Get the user language synchronously
    suspend fun getUserLanguageSync(): String {
        return getUserLanguage().first()
    }
    
    // Get the user name flow
    fun getUserName(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME] ?: ""
    }
    
    // Save user name
    suspend fun saveUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME] = name
        }
    }
    
    // Get the user email flow
    fun getUserEmail(): Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL] ?: ""
    }
    
    // Save user email
    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
        }
    }
    
    // Get notification enabled flow
    fun getNotificationEnabled(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_ENABLED]?.toBoolean() ?: true
    }
    
    // Save notification enabled
    suspend fun saveNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED] = enabled.toString()
        }
    }
    
    // Save auth info at once
    suspend fun saveAuthInfo(token: String, refreshToken: String, userId: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
            preferences[REFRESH_TOKEN] = refreshToken
            preferences[USER_ID] = userId
        }
    }
    
    // Clear all auth data
    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(USER_ID)
        }
    }
    
    /**
     * Onboarding status
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }
    
    suspend fun isOnboardingCompleted(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }.first()
    }
    
    /**
     * Permission preferences
     */
    suspend fun setNotificationPermissionGranted(granted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATION_PERMISSION_GRANTED] = granted
        }
    }
    
    suspend fun isNotificationPermissionGranted(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[NOTIFICATION_PERMISSION_GRANTED] ?: false
        }.first()
    }
    
    suspend fun setCameraPermissionGranted(granted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[CAMERA_PERMISSION_GRANTED] = granted
        }
    }
    
    suspend fun isCameraPermissionGranted(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[CAMERA_PERMISSION_GRANTED] ?: false
        }.first()
    }
    
    suspend fun setStoragePermissionGranted(granted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[STORAGE_PERMISSION_GRANTED] = granted
        }
    }
    
    suspend fun isStoragePermissionGranted(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[STORAGE_PERMISSION_GRANTED] ?: false
        }.first()
    }
    
    suspend fun setMicrophonePermissionGranted(granted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MICROPHONE_PERMISSION_GRANTED] = granted
        }
    }
    
    suspend fun isMicrophonePermissionGranted(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[MICROPHONE_PERMISSION_GRANTED] ?: false
        }.first()
    }
    
    /**
     * Legal agreement preferences
     */
    suspend fun setPrivacyPolicyAccepted(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PRIVACY_POLICY_ACCEPTED] = accepted
        }
    }
    
    suspend fun isPrivacyPolicyAccepted(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[PRIVACY_POLICY_ACCEPTED] ?: false
        }.first()
    }
    
    suspend fun setTermsAccepted(accepted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TERMS_ACCEPTED] = accepted
        }
    }
    
    suspend fun isTermsAccepted(): Boolean {
        return context.dataStore.data.map { preferences ->
            preferences[TERMS_ACCEPTED] ?: false
        }.first()
    }
    
    /**
     * Theme mode preferences
     */
    fun getThemeMode(): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[THEME_MODE] ?: "SYSTEM" // Default to system theme
        }
    }
    
    suspend fun setThemeMode(themeMode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = themeMode
        }
    }
    
    /**
     * Guest mode preferences
     */
    fun isGuestMode(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[GUEST_MODE] ?: false
        }
    }
    
    suspend fun saveGuestMode(isGuest: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[GUEST_MODE] = isGuest
        }
    }
    
    /**
     * Learning progress preferences for article reading
     */
    fun getReadArticles(): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            val articlesString = preferences[READ_ARTICLES] ?: ""
            if (articlesString.isEmpty()) emptySet() else articlesString.split(",").toSet()
        }
    }
    
    suspend fun saveReadArticles(readArticles: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[READ_ARTICLES] = readArticles.joinToString(",")
        }
    }
    
    suspend fun addReadArticle(articleSlug: String) {
        context.dataStore.edit { preferences ->
            val currentArticles = preferences[READ_ARTICLES] ?: ""
            val articlesSet = if (currentArticles.isEmpty()) {
                emptySet()
            } else {
                currentArticles.split(",").toSet()
            }
            val updatedArticles = articlesSet + articleSlug
            preferences[READ_ARTICLES] = updatedArticles.joinToString(",")
        }
    }
    
    suspend fun getReadArticlesCount(): Int {
        return getReadArticles().first().size
    }
    
    /**
     * Learning progress preferences for quiz completion
     */
    fun getCompletedQuizzes(): Flow<Set<String>> {
        return context.dataStore.data.map { preferences ->
            val quizzesString = preferences[COMPLETED_QUIZZES] ?: ""
            if (quizzesString.isEmpty()) emptySet() else quizzesString.split(",").toSet()
        }
    }
    
    suspend fun saveCompletedQuizzes(completedQuizzes: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[COMPLETED_QUIZZES] = completedQuizzes.joinToString(",")
        }
    }
    
    suspend fun addCompletedQuiz(quizId: String) {
        context.dataStore.edit { preferences ->
            val currentQuizzes = preferences[COMPLETED_QUIZZES] ?: ""
            val quizzesSet = if (currentQuizzes.isEmpty()) {
                emptySet()
            } else {
                currentQuizzes.split(",").toSet()
            }
            val updatedQuizzes = quizzesSet + quizId
            preferences[COMPLETED_QUIZZES] = updatedQuizzes.joinToString(",")
        }
    }
    
    suspend fun getCompletedQuizzesCount(): Int {
        return getCompletedQuizzes().first().size
    }
}
