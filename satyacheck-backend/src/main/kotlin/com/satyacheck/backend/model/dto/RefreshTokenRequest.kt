package com.satyacheck.backend.model.dto

import jakarta.validation.constraints.NotBlank

/**
 * DTO for refreshing an access token using a refresh token
 */
data class RefreshTokenRequest(
    @field:NotBlank
    val refreshToken: String
)