package com.satyacheck.backend.controller

import com.satyacheck.backend.model.dto.EnhancedAnalysisResult
import com.satyacheck.backend.service.EnhancedAnalysisService
import com.satyacheck.backend.service.trends.AnalysisRecordService
import com.satyacheck.backend.service.web.UrlAnalysisResult
import com.satyacheck.backend.service.web.UrlAnalysisService
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.logging.Logger

/**
 * Controller for enhanced content analysis endpoints
 */
@RestController
@RequestMapping("/api/v1/enhanced-analysis")
class EnhancedAnalysisController(
    private val enhancedAnalysisService: EnhancedAnalysisService,
    private val urlAnalysisService: UrlAnalysisService,
    private val analysisRecordService: AnalysisRecordService
) {
    private val logger = Logger.getLogger(EnhancedAnalysisController::class.java.name)

    /**
     * Analyze content comprehensively using multiple analysis services
     */
    @PostMapping("/comprehensive")
    suspend fun analyzeComprehensively(
        @RequestParam content: String,
        @RequestParam(defaultValue = "en") language: String,
        @RequestParam(required = false) source: String?,
        @RequestParam(required = false) contentType: String?,
        @RequestParam(required = false) userRegion: String?,
        @RequestParam(required = false) deviceType: String?
    ): EnhancedAnalysisResult {
        logger.info("Received comprehensive analysis request for content: ${content.take(50)}...")
        
        val result = enhancedAnalysisService.analyzeComprehensively(content, language)
        
        // Record analysis for trend tracking
        analysisRecordService.recordAnalysis(
            result = result,
            content = content,
            source = source,
            contentType = contentType,
            language = language,
            userRegion = userRegion,
            deviceType = deviceType
        )
        
        return result
    }

    /**
     * Analyze content specifically for misinformation patterns
     */
    @PostMapping("/misinformation")
    suspend fun analyzeMisinformation(
        @RequestParam content: String,
        @RequestParam(defaultValue = "en") language: String
    ): EnhancedAnalysisResult {
        logger.info("Received misinformation analysis request for content: ${content.take(50)}...")
        return enhancedAnalysisService.analyzeMisinformationPatterns(content, language)
    }

    /**
     * Process content from URL for comprehensive analysis
     */
    @PostMapping("/url")
    suspend fun analyzeUrl(
        @RequestParam url: String
    ): UrlAnalysisResult {
        logger.info("Received analysis request for URL: $url")
        return urlAnalysisService.analyzeUrl(url)
    }

    /**
     * Admin endpoint to clear enhanced analysis caches
     * Should be secured in production
     */
    @PostMapping("/admin/clear-cache")
    suspend fun clearCache(): Mono<ServerResponse> {
        logger.info("Clearing enhanced analysis caches")
        
        // In a real implementation, you would inject CacheManager and clear specific caches
        // cacheManager.getCache("enhancedAnalysis")?.clear()
        // cacheManager.getCache("misinformationAnalysis")?.clear()
        // cacheManager.getCache("contentCategories")?.clear()
        
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf(
                "status" to "success",
                "message" to "Enhanced analysis caches cleared"
            )).awaitSingle()
    }
}