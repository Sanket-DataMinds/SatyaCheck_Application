package com.satyacheck.backend.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig {

    /**
     * Configure the cache manager with Caffeine as the cache provider
     */
    @Bean
    fun cacheManager(): CacheManager {
        val caffeineCacheManager = CaffeineCacheManager()
        caffeineCacheManager.setCacheNames(listOf(
            "articles", 
            "articleBySlug", 
            "articlesByCategory", 
            "analysisResults",
            "enhancedAnalysis",
            "contentCategories",
            "extractedTopics",
            "misinformationAnalysis",
            "geminiAnalysis",
            "webContent",
            "urlAnalysis",
            "translations",
            "languageDetection"
        ))
        caffeineCacheManager.setCaffeine(caffeineCacheBuilder())
        return caffeineCacheManager
    }

    /**
     * Configure Caffeine cache properties
     */
    fun caffeineCacheBuilder(): Caffeine<Any, Any> {
        return Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(500)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()
    }
    
    /**
     * Special cache configuration for analysis results
     */
    @Bean
    fun analysisResultsCacheManager(): CacheManager {
        val caffeineCacheManager = CaffeineCacheManager("analysisResults")
        caffeineCacheManager.setCaffeine(
            Caffeine.newBuilder()
                .initialCapacity(200)
                .maximumSize(1000)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .recordStats()
        )
        return caffeineCacheManager
    }
    
    /**
     * Special cache configuration for language-related caches with longer expiration
     */
    @Bean
    fun languageCacheManager(): CacheManager {
        val caffeineCacheManager = CaffeineCacheManager("translations", "languageDetection")
        caffeineCacheManager.setCaffeine(
            Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(7, TimeUnit.DAYS)  // 7 days expiration
                .recordStats()
        )
        return caffeineCacheManager
    }
}