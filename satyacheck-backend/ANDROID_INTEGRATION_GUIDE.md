# SatyaCheck Integration Guide for Android App

This guide provides information about connecting the SatyaCheck Android app with the SatyaCheck backend.

## API Endpoints Overview

The SatyaCheck backend provides the following API endpoints:

### Authentication

- **POST /api/auth/register**: Register a new user
- **POST /api/auth/login**: Login with username and password
- **POST /api/auth/refresh**: Refresh authentication token

### Analysis

- **POST /api/v1/enhanced-analysis/url**: Analyze a URL for credibility
- **POST /api/v1/enhanced-analysis/comprehensive**: Analyze text content
- **POST /api/v1/bulk-analysis/urls**: Submit multiple URLs for batch analysis
- **GET /api/v1/bulk-analysis/{id}**: Get results of a specific bulk analysis
- **GET /api/v1/bulk-analysis/user/{userId}**: Get all analyses for a user

### Feedback

- **POST /api/v1/feedback**: Submit user feedback for an analysis
- **GET /api/v1/feedback/url**: Get all feedback for a specific URL

## Setup Instructions

1. **Add Dependencies**

Add the following dependencies to your app's `build.gradle`:

```gradle
// Network
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-moshi:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
implementation 'com.squareup.okhttp3:okhttp:4.11.0'

// JSON
implementation 'com.squareup.moshi:moshi:1.15.0'
implementation 'com.squareup.moshi:moshi-kotlin:1.15.0'
kapt 'com.squareup.moshi:moshi-kotlin-codegen:1.15.0'

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
```

2. **API Client Setup**

Copy the sample API client files provided in this repository:

- `SampleAndroidApiClient.kt` → API interface with all endpoint definitions
- `SampleAndroidRetrofitClient.kt` → Retrofit client implementation
- `SampleAndroidModels.kt` → Data models for requests and responses
- `SampleAndroidRepository.kt` → Repository pattern implementation

3. **Token Management**

Implement the `TokenManager` interface to handle JWT token storage and retrieval. Example:

```kotlin
class SharedPreferencesTokenManager(
    private val context: Context
) : TokenManager {
    private val prefs = context.getSharedPreferences("satyacheck_prefs", Context.MODE_PRIVATE)
    
    override fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Long) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .putLong("expires_at", System.currentTimeMillis() + expiresIn)
            .apply()
    }
    
    override fun getAccessToken(): String? {
        val token = prefs.getString("access_token", null)
        val expiresAt = prefs.getLong("expires_at", 0)
        
        // Check if token is expired
        return if (token != null && expiresAt > System.currentTimeMillis()) {
            token
        } else {
            null
        }
    }
    
    override fun getRefreshToken(): String? {
        return prefs.getString("refresh_token", null)
    }
    
    override fun clearTokens() {
        prefs.edit()
            .remove("access_token")
            .remove("refresh_token")
            .remove("expires_at")
            .apply()
    }
}
```

4. **Update Base URL**

Update the base URL in the `SatyaCheckApiClient.kt` file to point to the production backend:

```kotlin
private const val BASE_URL = "https://api.satyacheck.com/"
```

5. **Error Handling**

Implement proper error handling for API responses. The backend returns standardized error formats:

```json
{
  "status": "error",
  "message": "Error message describing what went wrong",
  "data": null
}
```

## Usage Examples

### Analyzing a URL

```kotlin
// In your ViewModel
private val repository = SatyaCheckRepository(tokenManager)

fun analyzeUrl(url: String) {
    viewModelScope.launch {
        _state.value = State.Loading
        
        val result = repository.analyzeUrl(url)
        result.fold(
            onSuccess = { response ->
                _state.value = State.Success(response)
            },
            onFailure = { error ->
                _state.value = State.Error("Analysis failed: ${error.message}")
            }
        )
    }
}
```

### Authentication

```kotlin
fun login(username: String, password: String) {
    viewModelScope.launch {
        _loginState.value = LoginState.Loading
        
        val result = repository.login(username, password)
        result.fold(
            onSuccess = { response ->
                _loginState.value = LoginState.Success(response)
            },
            onFailure = { error ->
                _loginState.value = LoginState.Error("Login failed: ${error.message}")
            }
        )
    }
}
```

## Testing the Connection

Before integrating into your app, you can test the API connection using a tool like Postman:

1. Register a new user:
   ```http
   POST https://api.satyacheck.com/api/auth/register
   Content-Type: application/json
   
   {
     "username": "testuser",
     "password": "testpassword",
     "email": "test@example.com",
     "name": "Test User"
   }
   ```

2. Use the returned token to make an authenticated request:
   ```http
   POST https://api.satyacheck.com/api/v1/enhanced-analysis/url?url=https://example.com
   Authorization: Bearer YOUR_ACCESS_TOKEN
   ```

## Security Considerations

1. Store tokens securely using encrypted SharedPreferences or Android Keystore
2. Implement token refresh logic to handle expired tokens
3. Use HTTPS for all communications
4. Don't hardcode sensitive information in your codebase

## Error Codes and Troubleshooting

| HTTP Status | Meaning | Troubleshooting |
|-------------|---------|----------------|
| 400 | Bad Request | Check your request parameters |
| 401 | Unauthorized | Token is invalid or expired |
| 403 | Forbidden | User doesn't have permission |
| 404 | Not Found | Resource doesn't exist |
| 500 | Server Error | Contact backend team |

## Support

For any integration issues, please contact the backend development team.