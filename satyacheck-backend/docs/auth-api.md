# Authentication API Documentation

This document provides details about the authentication API endpoints for the Satyacheck backend.

## Overview

The authentication system uses JWT (JSON Web Token) for secure API access. There are three main endpoints:

1. **Register** - Create a new user account
2. **Login** - Authenticate and receive access tokens
3. **Refresh Token** - Get a new access token using a refresh token

## Authentication Flow

1. User registers or logs in
2. Server returns an access token and a refresh token
3. Client includes the access token in the Authorization header for protected API requests
4. When the access token expires, client uses the refresh token to get a new access token

## API Endpoints

### Register

Creates a new user account and returns authentication tokens.

- **URL**: `/api/auth/register`
- **Method**: `POST`
- **Authentication**: None (public endpoint)

**Request Body**:
```json
{
  "username": "user123",
  "password": "securepassword",
  "email": "user@example.com",
  "name": "John Doe"
}
```

**Response**:
```json
{
  "status": "success",
  "message": "User registered successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "username": "user123",
    "roles": ["ROLE_USER"]
  },
  "timestamp": "2025-09-17T12:34:56.789"
}
```

### Login

Authenticates a user and returns authentication tokens.

- **URL**: `/api/auth/login`
- **Method**: `POST`
- **Authentication**: None (public endpoint)

**Request Body**:
```json
{
  "username": "user123",
  "password": "securepassword"
}
```

**Response**:
```json
{
  "status": "success",
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "username": "user123",
    "roles": ["ROLE_USER"]
  },
  "timestamp": "2025-09-17T12:34:56.789"
}
```

### Refresh Token

Get a new access token using a refresh token.

- **URL**: `/api/auth/refresh`
- **Method**: `POST`
- **Authentication**: None (public endpoint, but requires valid refresh token)

**Request Body**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response**:
```json
{
  "status": "success",
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "username": "user123",
    "roles": ["ROLE_USER"]
  },
  "timestamp": "2025-09-17T12:34:56.789"
}
```

## Using the Access Token

For protected API endpoints, include the access token in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Error Responses

Authentication errors will return a response with status code 400 or 401:

```json
{
  "status": "error",
  "message": "Error message here",
  "data": null,
  "timestamp": "2025-09-17T12:34:56.789"
}
```

## Protected API Endpoints

The following endpoints require authentication:

- `/api/analyze` - For analyzing text content
- Any other endpoint that is not explicitly marked as public

## Public API Endpoints

The following endpoints are publicly accessible without authentication:

- `/api/auth/*` - All authentication endpoints
- `/api/public/*` - All public endpoints (like articles)
- `/health` - Health check endpoint