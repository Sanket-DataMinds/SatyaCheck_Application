package com.satyacheck.backend

import com.satyacheck.backend.service.api.GeminiService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient

/**
 * A simplified application for testing Gemini API integration
 */
@SpringBootApplication
class GeminiTestApplication {
    
    @Autowired
    lateinit var webClientBuilder: WebClient.Builder
    
    @Bean
    fun webClient(): WebClient {
        return webClientBuilder.build()
    }
    
    @Bean
    @Profile("test-gemini")
    fun testGeminiRunner(geminiService: GeminiService): CommandLineRunner {
        return CommandLineRunner { args ->
            println("Testing Gemini API integration...")
            
            val testContent = "This is a test content to check if the Gemini API integration works."
            
            try {
                val (verdict, explanation) = geminiService.analyzeContent(testContent)
                println("Verdict: $verdict")
                println("Explanation: $explanation")
                println("Gemini API test successful!")
            } catch (e: Exception) {
                println("Failed to test Gemini API: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}

fun main(args: Array<String>) {
    System.setProperty("spring.profiles.active", "test-gemini")
    SpringApplication.run(GeminiTestApplication::class.java, *args)
}