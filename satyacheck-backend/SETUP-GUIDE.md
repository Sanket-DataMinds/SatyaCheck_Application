# Satyacheck Backend Setup Guide

This guide provides comprehensive steps for setting up and running the Satyacheck backend application.

## Step 1: MongoDB Setup

### Option A: Set Up MongoDB Atlas (Recommended)

1. Follow the detailed instructions in [MongoDB Atlas Setup Guide](mongodb-atlas-setup.md)
2. After setup, update your connection string in `application.properties`

### Option B: Set Up Local MongoDB

1. Download and install MongoDB Community Server from the [official website](https://www.mongodb.com/try/download/community)
2. Create a data directory: `mkdir -p C:\data\db`
3. Start MongoDB server: `mongod --dbpath C:\data\db`
4. Keep the default connection string in `application.properties`: `mongodb://localhost:27017/satyacheck`

## Step 2: Google Cloud API Setup

1. Ensure your Google Cloud service account key file is placed at the correct location
   - Current path in properties: `D:/z+/SatyaApp/satyacheck-backend/satyacheck-backend-281deb66d0d0.json`
   - Update the path in `application.properties` if needed

2. Verify your API keys in `application.properties`
   ```properties
   google.cloud.natural-language.api-key=AIzaSyAg0vwaVbvvLC33nVmHDQDRxAkrKcgjLTs
   google.cloud.gemini.api-key=AIzaSyBgEvPNNOMwNA_M93tsTho2Y0aDChMwuDQ
   ```

## Step 3: Authentication Setup

The application now uses JWT authentication for API security.

1. JWT configuration is already set up in `application.properties`:
   ```properties
   app.jwt.secret=satyacheckSecretKey123!@#$%^&*()_+ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789
   app.jwt.expiration-ms=3600000
   app.jwt.refresh-expiration-ms=86400000
   ```

2. For production, update the JWT secret to a secure random string.

## Step 4: Build and Run

1. Build the application:
   ```bash
   ./gradlew build
   ```

2. Run the application:
   ```bash
   ./gradlew bootRun
   ```

3. To test MongoDB connectivity specifically, run:
   ```bash
   ./test-mongodb.bat
   ```

## Step 5: Verify API Endpoints

1. Test the health endpoint:
   ```bash
   curl http://localhost:8080/health
   ```
   Should return: `{"status":"UP"}`

2. Test the articles endpoint (public):
   ```bash
   curl http://localhost:8080/api/public/articles
   ```
   Should return a list of articles

3. Register a new user:
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
        -H "Content-Type: application/json" \
        -d '{"username":"testuser","password":"testpassword","email":"test@example.com","name":"Test User"}'
   ```
   This will return access and refresh tokens

4. Test the analysis endpoint (protected - requires authentication):
   ```bash
   curl -X POST http://localhost:8080/api/analyze \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
        -d '{"content":"This is sample text to analyze", "contentType":"TEXT", "language":"en"}'
   ```
   Replace `YOUR_ACCESS_TOKEN` with the token received from registration or login

## Troubleshooting

### MongoDB Connection Issues

- Check if MongoDB is running (locally or if Atlas is accessible)
- Verify connection string format in `application.properties`
- Ensure network connectivity to MongoDB Atlas
- Check if IP address is whitelisted in MongoDB Atlas

### Google Cloud API Issues

- Verify API keys are correct
- Ensure service account has necessary permissions
- Check if APIs are enabled in Google Cloud Console

### Application Startup Issues

- Check logs for specific error messages
- Verify JDK version (should be 17+)
- Ensure all dependencies are resolved properly

## Next Steps

After successful setup:

1. Create more educational articles in the database
2. Test the analysis API with various types of content
3. Monitor API performance and database operations
4. Integrate with the Android application