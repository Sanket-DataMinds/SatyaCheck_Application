package com.satyacheck.backend.model.dto

import jakarta.validation.constraints.NotBlank

/**
 * DTO for login request
 */
data class LoginRequest(
    @field:NotBlank(message = "Username is required")
    val username: String,
    
    @field:NotBlank(message = "Password is required")
    val password: String
)