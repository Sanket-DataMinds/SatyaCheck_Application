package com.satyacheck.android.data.remote.auth

import android.content.Context
import com.satyacheck.android.data.remote.api.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TokenManager that uses SharedPreferences to store and retrieve JWT tokens
 */
@Singleton
class SharedPreferencesTokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenManager {
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    override fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + (expiresIn * 1000))
            .apply()
    }
    
    override fun getAccessToken(): String? {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0)
        
        // Check if token is expired
        return if (token != null && expiresAt > System.currentTimeMillis()) {
            token
        } else {
            null
        }
    }
    
    override fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }
    
    override fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_EXPIRES_AT)
            .apply()
    }
    
    companion object {
        private const val PREFS_NAME = "satyacheck_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
    }
}