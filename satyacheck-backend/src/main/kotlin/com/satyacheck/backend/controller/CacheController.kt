package com.satyacheck.backend.controller

import com.satyacheck.backend.model.dto.ApiResponse
import com.satyacheck.backend.service.impl.ArticleServiceImpl
import org.springframework.cache.CacheManager
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.logging.Logger

@RestController
@RequestMapping("/api/admin/cache")
@PreAuthorize("hasRole('ADMIN')")
class CacheController(
    private val cacheManager: CacheManager,
    private val analysisResultsCacheManager: CacheManager,
    private val articleService: ArticleServiceImpl
) {
    private val logger = Logger.getLogger(CacheController::class.java.name)

    @GetMapping
    fun getAllCaches(): ResponseEntity<ApiResponse<List<String>>> {
        val cacheNames = cacheManager.cacheNames.toList()
        return ResponseEntity.ok(ApiResponse.success(cacheNames, "Available caches"))
    }

    @GetMapping("/stats")
    fun getCacheStats(): ResponseEntity<ApiResponse<Map<String, Any>>> {
        val stats = mutableMapOf<String, Any>()
        
        cacheManager.cacheNames.forEach { cacheName ->
            val cache = cacheManager.getCache(cacheName)
            stats[cacheName] = mapOf(
                "name" to cacheName,
                "active" to (cache != null)
            )
        }
        
        return ResponseEntity.ok(ApiResponse.success(stats, "Cache statistics"))
    }

    @DeleteMapping("/{cacheName}")
    fun clearCache(@PathVariable cacheName: String): ResponseEntity<ApiResponse<Void>> {
        val cache = cacheManager.getCache(cacheName)
        if (cache != null) {
            cache.clear()
            logger.info("Cleared cache: $cacheName")
            return ResponseEntity.ok(ApiResponse.success(null, "Cache '$cacheName' cleared successfully"))
        }
        return ResponseEntity.notFound().build()
    }

    @DeleteMapping
    fun clearAllCaches(): ResponseEntity<ApiResponse<Void>> {
        cacheManager.cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }
        
        analysisResultsCacheManager.cacheNames.forEach { cacheName ->
            analysisResultsCacheManager.getCache(cacheName)?.clear()
        }
        
        articleService.clearArticleCaches()
        
        logger.info("Cleared all caches")
        return ResponseEntity.ok(ApiResponse.success(null, "All caches cleared successfully"))
    }
}