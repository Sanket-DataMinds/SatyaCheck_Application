# Satyacheck Backend

Backend service for Satyacheck Android app, supporting fact-checking analysis and educational articles.

## Features

- **Text Analysis API**: Analyze text for credibility and misinformation using Google Cloud APIs
- **Educational Articles API**: Retrieve educational content about digital literacy and misinformation

## Tech Stack

- **Framework**: Spring Boot 3.2 with Kotlin
- **Database**: MongoDB for storing articles and analysis results
- **Cloud Services**: Google Cloud APIs (Gemini API, Natural Language API)
- **Authentication**: Basic API security (can be extended)

## API Endpoints

### Authentication API

See [Authentication API Documentation](docs/auth-api.md) for details on the authentication endpoints.


### Analysis API

```
POST /api/analyze
Request:
{
  "content": "Text to analyze",
  "contentType": "TEXT",
  "language": "en" // Optional, defaults to English
}

Response:
{
  "status": "success",
  "data": {
    "verdict": "POTENTIALLY_MISLEADING",
    "explanation": "Detailed explanation of the analysis..."
  }
}
```

### Articles API

```
GET /api/articles?language=en
Response: List of all articles

GET /api/articles/{slug}?language=en
Response: Single article by slug

GET /api/articles/category/{category}?language=en
Response: Articles filtered by category
```

## Setup Instructions

1. **Prerequisites**
   - JDK 17+
   - MongoDB
   - Google Cloud account with enabled APIs

2. **Configuration**
   - Set environment variables:
     - `GOOGLE_APPLICATION_CREDENTIALS`: Path to your service account key file
     - `NATURAL_LANGUAGE_API_KEY`: Google Cloud Natural Language API key
     - `GEMINI_API_KEY`: Google Cloud Gemini API key

3. **Running the Application**
   ```
   ./gradlew bootRun
   ```

4. **Testing**
   - Access the health endpoint: http://localhost:8080/health

## Integration with Android App

This backend is designed to integrate with the Satyacheck Android app. The API endpoints match the expected format of the Android app's service calls.
