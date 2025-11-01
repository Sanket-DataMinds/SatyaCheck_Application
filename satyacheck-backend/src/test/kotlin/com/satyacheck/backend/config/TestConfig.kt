package com.satyacheck.backend.config

import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.reactive.function.client.WebClient

/**
 * Test configuration for unit tests
 */
@TestConfiguration
class TestConfig {
    
    @Bean
    @Primary
    fun mockWebClient(): WebClient = mockk(relaxed = true)
    
    @Bean
    @Primary
    fun mockMongoTemplate(): MongoTemplate = mockk(relaxed = true)
    
    @Bean
    @Primary
    fun testCacheManager(): CacheManager {
        return ConcurrentMapCacheManager().apply {
            setCacheNames(listOf(
                "enhancedAnalysis",
                "misinformationAnalysis",
                "contentCategories",
                "extractedTopics",
                "webContent",
                "urlAnalysis",
                "translations",
                "languageDetection"
            ))
        }
    }
}