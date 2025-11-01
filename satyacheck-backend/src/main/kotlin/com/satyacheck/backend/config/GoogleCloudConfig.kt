package com.satyacheck.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class GoogleCloudConfig {

    @Value("\${google.cloud.credentials.path}")
    private lateinit var credentialsPath: String
    
    @Value("\${google.cloud.project-id}")
    private lateinit var projectId: String
    
    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .build()
    }
}