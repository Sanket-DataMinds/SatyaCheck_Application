package com.satyacheck.backend.config.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * Custom health indicator for external APIs
 */
@Component
class ExternalApisHealthIndicator(private val webClient: WebClient) : HealthIndicator {
    
    override fun health(): Health {
        val builder = Health.Builder()
        
        // Check Google APIs
        val googleApiStatus = checkGoogleApiAvailability()
        if (googleApiStatus) {
            builder.withDetail("googleApis", "Available")
        } else {
            builder.down().withDetail("googleApis", "Unavailable")
        }
        
        // Return overall health
        return builder.build()
    }
    
    /**
     * Check if Google APIs are available
     * This is a simplified check for demonstration purposes
     */
    private fun checkGoogleApiAvailability(): Boolean {
        return try {
            // Try a simple request to check Google APIs availability
            webClient.get()
                .uri("https://www.googleapis.com/discovery/v1/apis")
                .retrieve()
                .bodyToMono(String::class.java)
                .timeout(Duration.ofSeconds(3))
                .onErrorResume { Mono.just("error") }
                .block()
                ?.let { !it.equals("error") } ?: false
        } catch (e: Exception) {
            false
        }
    }
}