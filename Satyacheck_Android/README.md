# SatyaCheck Android

SatyaCheck is a native Android application converted from the original web application. It helps users verify the credibility of content they encounter online by analyzing text, images, and audio for potential misinformation and provides categorized verdicts.

## Project Status

This project represents a complete conversion from the original Next.js web application to a native Android app. The conversion includes:

- ✅ All core functionality from the web app 
- ✅ Enhanced with Android-specific features like Accessibility Service
- ✅ Same multilingual support (English, Hindi, Marathi)
- ✅ Same analysis capabilities and verdict classifications
- ✅ Optimized UI with Jetpack Compose
- ✅ Full backend integration with RESTful API

## Features

- **Multi-modal Analysis**: Analyze text, images, and audio for misinformation
- **Verdicts**: Categorized as Credible, Potentially Misleading, High Misinformation Risk, or Scam Alert
- **Multilingual Support**: Interface and analysis in English, Hindi, and Marathi
- **Photo/Video Analysis**: Core functionality for detecting manipulated media
- **Android 14+ Support**: Uses the new Selected Photos Access API
- **Accessibility Service**: Background scanning of content on device
- **Educational Content**: Resources to help users learn about misinformation
- **Community Features**: Engage with community alerts and reports
- **User Dashboard**: Track user activity and analysis history
- **Backend Integration**: User authentication, analysis history, community features

## Architecture

This application follows the MVVM (Model-View-ViewModel) architecture pattern and uses Jetpack Compose for the UI.

## Project Structure

- `app/` - Main application module
- `app/src/main/java/com/satyacheck/android/` - Kotlin source files
  - `data/` - Data layer (repositories, data sources, models)
    - `remote/` - API services and data transfer objects
    - `repository/` - Repository implementations
  - `domain/` - Domain layer (use cases, domain models, analyzers)
  - `presentation/` - UI layer (screens, viewmodels, composables)
  - `di/` - Dependency injection
  - `utils/` - Utility classes and services
- `app/src/main/res/` - Resources
  - `values/` - String resources, themes, etc.
  - `values-hi/` - Hindi string resources
  - `values-mr/` - Marathi string resources
  - `drawable/` - Images and icons

## Key Components

1. **Onboarding Experience**: Complete user onboarding with language selection
2. **Analysis Screen**: Main screen for submitting content for analysis
3. **Analysis Result**: Detailed view of analysis verdict with explanation
4. **Accessibility Service**: Background scanning of screen content
5. **Gemini Integration**: AI analysis capabilities using Google's Gemini API
6. **Multilingual Support**: Full language support as in the original app
7. **API Integration**: Full integration with SatyaCheck backend services

## Tech Stack

- **Kotlin** - Programming language
- **Jetpack Compose** - Modern UI toolkit
- **Coroutines & Flow** - Asynchronous programming
- **Hilt** - Dependency injection
- **Retrofit** - API communication
- **OkHttp** - HTTP client
- **Gemini API** - Advanced AI capabilities
- **CameraX** - Camera functionality
- **Media3** - Audio/video playback and processing
- **Accessibility Service** - Background content scanning

## Backend Communication

The app communicates with the SatyaCheck backend service for:
- User authentication and profile management
- Remote analysis of content (as a fallback to on-device)
- Storing analysis history
- Community features (alerts, posts, comments)
- Educational content delivery

The API is RESTful and uses JWT authentication.

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34
- JDK 17
- Gemini API Key (add to your gradle.properties)
- SatyaCheck API access (for backend features)

### Building the Project

1. Clone the repository
2. Open the project in Android Studio
3. Add your Gemini API key to gradle.properties: `GEMINI_API_KEY="your_api_key_here"`
4. Sync Gradle files
5. Run the application on an emulator or physical device

## Permissions Used

This app requires the following permissions:

### Media Access Permissions
- **READ_MEDIA_IMAGES** and **READ_MEDIA_AUDIO**: Used to analyze images and audio for potential misinformation. This is a core functionality of the app.
- **READ_MEDIA_VISUAL_USER_SELECTED**: For Android 14+, allows access to only the specific photos you select.
- **READ_EXTERNAL_STORAGE**: For Android versions below 13, used to access media for analysis.

### Accessibility Service
- The accessibility service in SatyaCheck is ONLY used when you explicitly request to analyze content on your screen.
- It does NOT monitor or collect any content without your direct action.
- We never store or transmit the content of your screen.

### Other Permissions
- **CAMERA**: For scanning physical media and documents (optional)
- **RECORD_AUDIO**: For analyzing audio content for misinformation (optional)
- **ACCESS_COARSE_LOCATION**: Used only for showing community alerts in your area (optional)
- **INTERNET** and **ACCESS_NETWORK_STATE**: Required for analyzing content using our AI models
- **POST_NOTIFICATIONS**: To send alerts about analyzed content
- **VIBRATE**: For haptic feedback

## Privacy Commitments

1. We NEVER collect or store content you analyze without your consent
2. We do NOT use the accessibility service to monitor your activity
3. Media is only accessed when you explicitly select it
4. All analysis is performed using secure, privacy-preserving techniques
