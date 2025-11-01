# SatyaCheck Backend Integration Guide

This guide provides information about how the SatyaCheck Android app integrates with the backend services.

## Overview

The SatyaCheck Android app uses a hybrid approach for analysis:

1. **Online mode**: Utilizes the SatyaCheck backend API for content analysis and user data management
2. **Offline mode**: Falls back to on-device analysis using Gemini API when network connectivity is unavailable

## Architecture

The backend integration follows the Repository pattern:

```
UI (Compose) <-> ViewModel <-> Repository <-> [Remote DataSource | Local DataSource]
```

- **Repository**: Abstracts data sources and provides business logic
- **Remote DataSource**: Handles API calls via Retrofit
- **Local DataSource**: Manages local storage via DataStore and Room

## Key Components

### API Services

Located in `data/remote/api/ApiServices.kt`, these interfaces define the endpoints for each service:

- `AuthService`: Authentication endpoints
- `AnalysisService`: Content analysis endpoints
- `UserService`: User profile management
- `CommunityService`: Community features
- `EducationalService`: Educational content

### Data Transfer Objects (DTOs)

Located in `data/remote/dto/ApiDtos.kt`, these classes model the JSON responses from the API:

- `ApiResponse<T>`: Wrapper for all API responses
- `AuthDto`: Authentication data
- `AnalysisResultDto`: Analysis result from the server
- `UserProfileDto`: User profile data

### Repositories

Located in `data/repository/`, these classes implement the business logic:

- `AuthRepository`: Handles authentication
- `AnalysisRepository`: Manages content analysis
- `UserRepository`: Manages user profiles
- `CommunityRepository`: Handles community features
- `EducationalRepository`: Provides educational content

### Network Module

Located in `di/NetworkModule.kt`, this class provides the Retrofit dependencies for Hilt:

- `OkHttpClient` with logging and auth interceptors
- Retrofit service instances for each API service

## Authentication Flow

1. User signs in with email/password
2. API returns JWT token and refresh token
3. App stores tokens securely in UserPreferencesRepository
4. AuthInterceptor adds token to subsequent API requests
5. When token expires, app automatically refreshes it

## Fallback Mechanism

When the network is unavailable or the API request fails, the app falls back to on-device processing:

```kotlin
// Example from AnalyzeViewModel.kt
if (state.textInput.isNotBlank()) {
    viewModelScope.launch {
        try {
            val localResult = textAnalyzer.analyzeText(state.textInput)
            // Use local result
        } catch (e: Exception) {
            // Handle error
        }
    }
}
```

## Adding New API Endpoints

1. Add the endpoint constant in `ApiConstants.kt`
2. Add the corresponding method in the appropriate service interface
3. Create or update DTOs as needed
4. Implement the repository method to handle the API call
5. Ensure proper error handling and fallback mechanisms

## Testing Backend Integration

For testing, you can:

1. Use the `staging-api.satyacheck.org` server for development
2. Check API responses with the NetworkUtils class
3. Test both online and offline scenarios

## Security Considerations

- Auth tokens are stored in DataStore with encryption
- All communication uses HTTPS
- Sensitive user data is never stored locally unencrypted
- API requests include the minimum necessary information
