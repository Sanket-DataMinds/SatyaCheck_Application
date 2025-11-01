package com.satyacheck.backend.controller

import org.springframework.cache.CacheManager
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.logging.Logger

/**
 * Admin controller for cache management
 * In production, this should be secured with proper authorization
 */
@RestController
@RequestMapping("/api/admin/cache")
class CacheAdminController(private val cacheManager: CacheManager) {
    
    private val logger = Logger.getLogger(CacheAdminController::class.java.name)
    
    /**
     * Clear a specific cache by name
     */
    @PostMapping("/clear/{cacheName}")
    fun clearCache(@PathVariable cacheName: String): ResponseEntity<Map<String, String>> {
        logger.info("Request to clear cache: $cacheName")
        
        val cache = cacheManager.getCache(cacheName)
        
        return if (cache != null) {
            cache.clear()
            logger.info("Cache '$cacheName' cleared successfully")
            ResponseEntity.ok(mapOf(
                "status" to "success",
                "message" to "Cache '$cacheName' cleared successfully"
            ))
        } else {
            logger.warning("Cache '$cacheName' not found")
            ResponseEntity.badRequest().body(mapOf(
                "status" to "error",
                "message" to "Cache '$cacheName' not found"
            ))
        }
    }
    
    /**
     * Clear all caches
     */
    @PostMapping("/clear-all")
    fun clearAllCaches(): ResponseEntity<Map<String, Any>> {
        logger.info("Request to clear all caches")
        
        val cacheNames = cacheManager.cacheNames.toList()
        val clearedCaches = mutableListOf<String>()
        
        cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
            clearedCaches.add(cacheName)
            logger.info("Cache '$cacheName' cleared")
        }
        
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "message" to "${clearedCaches.size} caches cleared successfully",
            "clearedCaches" to clearedCaches
        ))
    }
    
    /**
     * Get cache statistics
     */
    @GetMapping("/stats")
    fun getCacheStats(): ResponseEntity<Map<String, Any>> {
        logger.info("Request for cache statistics")
        
        val cacheNames = cacheManager.cacheNames.toList()
        val stats = mutableMapOf<String, Any>()
        
        cacheNames.forEach { cacheName ->
            // We can't get exact statistics without direct access to Caffeine's native cache
            // This is just a placeholder - in a real implementation you'd use:
            // val nativeCache = (cacheManager.getCache(cacheName)?.nativeCache as? com.github.benmanes.caffeine.cache.Cache<*, *>)
            // val stats = nativeCache?.stats()
            
            stats[cacheName] = mapOf(
                "available" to true,
                "name" to cacheName
                // In real implementation, add: "hitRate", "missRate", "size", etc.
            )
        }
        
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "caches" to stats
        ))
    }
}