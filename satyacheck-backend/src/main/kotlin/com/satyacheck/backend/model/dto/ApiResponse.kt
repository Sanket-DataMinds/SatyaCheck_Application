package com.satyacheck.backend.model.dto

import java.time.LocalDateTime

/**
 * Generic API response wrapper for consistent API responses
 */
data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun <T> success(data: T, message: String = "Operation successful"): ApiResponse<T> {
            return ApiResponse(
                status = "success",
                message = message,
                data = data
            )
        }

        fun <T> error(message: String, data: T? = null): ApiResponse<T> {
            return ApiResponse(
                status = "error",
                message = message,
                data = data
            )
        }
    }
}