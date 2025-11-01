package com.satyacheck.backend.controller

import com.satyacheck.backend.service.batch.*
import org.springframework.web.bind.annotation.*
import java.util.logging.Logger

/**
 * Controller for bulk analysis operations
 */
@RestController
@RequestMapping("/api/v1/bulk-analysis")
class BulkAnalysisController(
    private val bulkAnalysisService: BulkAnalysisService
) {
    private val logger = Logger.getLogger(BulkAnalysisController::class.java.name)

    /**
     * Bulk analyze multiple content items
     */
    @PostMapping("/content")
    suspend fun bulkAnalyzeContent(
        @RequestBody request: BulkContentRequest
    ): BulkAnalysisResult {
        logger.info("Received bulk content analysis request for ${request.items.size} items")
        return bulkAnalysisService.bulkAnalyzeContent(request.items)
    }

    /**
     * Bulk analyze multiple URLs
     */
    @PostMapping("/urls")
    suspend fun bulkAnalyzeUrls(
        @RequestBody request: BulkUrlRequest
    ): BulkUrlAnalysisResult {
        logger.info("Received bulk URL analysis request for ${request.items.size} items")
        return bulkAnalysisService.bulkAnalyzeUrls(request.items)
    }
}

/**
 * Request data class for bulk content analysis
 */
data class BulkContentRequest(
    val items: List<BulkContentItem>,
    val batchMetadata: Map<String, Any> = emptyMap()
)

/**
 * Request data class for bulk URL analysis
 */
data class BulkUrlRequest(
    val items: List<BulkUrlItem>,
    val batchMetadata: Map<String, Any> = emptyMap()
)