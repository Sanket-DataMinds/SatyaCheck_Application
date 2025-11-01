package com.satyacheck.backend.model.dto

/**
 * DTO for authentication response containing JWT tokens
 */
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val username: String,
    val roles: Set<String>
)