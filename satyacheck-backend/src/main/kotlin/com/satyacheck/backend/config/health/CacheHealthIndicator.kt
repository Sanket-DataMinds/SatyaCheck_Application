package com.satyacheck.backend.config.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component

/**
 * Custom health indicator for cache systems
 */
@Component
class CacheHealthIndicator(private val cacheManager: CacheManager) : HealthIndicator {
    
    override fun health(): Health {
        return try {
            // Get all cache names
            val cacheNames = cacheManager.cacheNames
            
            // Check if all caches are working
            val cacheStatuses = cacheNames.associateWith { cacheName ->
                val cache = cacheManager.getCache(cacheName)
                if (cache != null) {
                    // Try to perform cache operations to check if it's working
                    val testKey = "health-check-${System.currentTimeMillis()}"
                    cache.put(testKey, "test-value")
                    val retrieved = cache.get(testKey)?.get() as String?
                    cache.evict(testKey)
                    
                    retrieved == "test-value"
                } else {
                    false
                }
            }
            
            // Check if all caches are working
            val allCachesWorking = cacheStatuses.values.all { it }
            
            if (allCachesWorking) {
                Health.up()
                    .withDetail("cache", "Caffeine")
                    .withDetail("cacheNames", cacheNames)
                    .withDetail("status", "Available")
                    .build()
            } else {
                // Find which caches are not working
                val failedCaches = cacheStatuses.filterValues { !it }.keys
                
                Health.down()
                    .withDetail("cache", "Caffeine")
                    .withDetail("status", "Partially Available")
                    .withDetail("failedCaches", failedCaches)
                    .build()
            }
        } catch (e: Exception) {
            Health.down()
                .withDetail("cache", "Caffeine")
                .withDetail("status", "Error")
                .withDetail("error", e.message)
                .build()
        }
    }
}