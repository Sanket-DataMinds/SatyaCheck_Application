package com.satyacheck.backend.config.health

import com.satyacheck.backend.service.api.GeminiService
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component

/**
 * Custom health indicator for Gemini API
 */
@Component
class GeminiHealthIndicator(private val geminiService: GeminiService) : HealthIndicator {
    
    override fun health(): Health {
        return try {
            val isAvailable = checkGeminiAvailability()
            
            if (isAvailable) {
                Health.up()
                    .withDetail("service", "Gemini API")
                    .withDetail("status", "Available")
                    .build()
            } else {
                Health.down()
                    .withDetail("service", "Gemini API")
                    .withDetail("status", "Unavailable")
                    .build()
            }
        } catch (e: Exception) {
            Health.down()
                .withDetail("service", "Gemini API")
                .withDetail("status", "Error")
                .withDetail("error", e.message)
                .build()
        }
    }
    
    /**
     * Check if Gemini API is available
     * This is a simplified check for demonstration purposes
     */
    private fun checkGeminiAvailability(): Boolean {
        // In a real implementation, you might want to make a lightweight API call
        // For now, we'll just return true if the service is initialized properly
        return geminiService.isApiKeyConfigured()
    }
}