package com.satyacheck.backend.service.batch

import com.satyacheck.backend.model.dto.EnhancedAnalysisResult
import com.satyacheck.backend.service.EnhancedAnalysisService
import com.satyacheck.backend.service.web.UrlAnalysisResult
import com.satyacheck.backend.service.web.UrlAnalysisService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import java.util.logging.Logger
import java.util.UUID

/**
 * Service for batch processing multiple analysis requests
 */
@Service
class BulkAnalysisService(
    private val enhancedAnalysisService: EnhancedAnalysisService,
    private val urlAnalysisService: UrlAnalysisService
) {
    private val logger = Logger.getLogger(BulkAnalysisService::class.java.name)
    
    /**
     * Process multiple text content items in parallel
     */
    suspend fun bulkAnalyzeContent(
        contents: List<BulkContentItem>
    ): BulkAnalysisResult = coroutineScope {
        logger.info("Starting bulk analysis for ${contents.size} content items")
        
        val startTime = System.currentTimeMillis()
        
        // Process each item in parallel
        val results = contents.map { contentItem ->
            async {
                try {
                    val result = enhancedAnalysisService.analyzeComprehensively(
                        contentItem.content,
                        contentItem.language ?: "en"
                    )
                    
                    BulkItemResult(
                        id = contentItem.id,
                        metadata = contentItem.metadata,
                        analysis = result,
                        error = null
                    )
                } catch (e: Exception) {
                    logger.warning("Error analyzing content item ${contentItem.id}: ${e.message}")
                    
                    BulkItemResult(
                        id = contentItem.id,
                        metadata = contentItem.metadata,
                        analysis = null,
                        error = "Analysis failed: ${e.message}"
                    )
                }
            }
        }.awaitAll()
        
        val processingTime = System.currentTimeMillis() - startTime
        
        logger.info("Completed bulk analysis of ${contents.size} items in $processingTime ms")
        
        BulkAnalysisResult(
            batchId = UUID.randomUUID().toString(),
            itemsProcessed = results.size,
            itemsSucceeded = results.count { it.error == null },
            itemsFailed = results.count { it.error != null },
            processingTimeMs = processingTime,
            results = results
        )
    }
    
    /**
     * Process multiple URLs in parallel
     */
    suspend fun bulkAnalyzeUrls(urls: List<BulkUrlItem>): BulkUrlAnalysisResult = coroutineScope {
        logger.info("Starting bulk URL analysis for ${urls.size} URLs")
        
        val startTime = System.currentTimeMillis()
        
        // Process each URL in parallel
        val results = urls.map { urlItem ->
            async {
                try {
                    val result = urlAnalysisService.analyzeUrl(urlItem.url)
                    
                    BulkUrlItemResult(
                        id = urlItem.id,
                        metadata = urlItem.metadata,
                        analysis = result,
                        error = null
                    )
                } catch (e: Exception) {
                    logger.warning("Error analyzing URL ${urlItem.url}: ${e.message}")
                    
                    BulkUrlItemResult(
                        id = urlItem.id,
                        metadata = urlItem.metadata,
                        analysis = null,
                        error = "Analysis failed: ${e.message}"
                    )
                }
            }
        }.awaitAll()
        
        val processingTime = System.currentTimeMillis() - startTime
        
        logger.info("Completed bulk URL analysis of ${urls.size} items in $processingTime ms")
        
        BulkUrlAnalysisResult(
            batchId = UUID.randomUUID().toString(),
            itemsProcessed = results.size,
            itemsSucceeded = results.count { it.error == null },
            itemsFailed = results.count { it.error != null },
            processingTimeMs = processingTime,
            results = results
        )
    }
}

/**
 * Data class for a bulk content analysis item
 */
data class BulkContentItem(
    val id: String,
    val content: String,
    val language: String? = "en",
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Data class for a bulk URL analysis item
 */
data class BulkUrlItem(
    val id: String,
    val url: String,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Data class for bulk analysis result
 */
data class BulkAnalysisResult(
    val batchId: String,
    val itemsProcessed: Int,
    val itemsSucceeded: Int,
    val itemsFailed: Int,
    val processingTimeMs: Long,
    val results: List<BulkItemResult>
)

/**
 * Data class for individual item result within a bulk operation
 */
data class BulkItemResult(
    val id: String,
    val metadata: Map<String, Any>,
    val analysis: EnhancedAnalysisResult?,
    val error: String?
)

/**
 * Data class for bulk URL analysis result
 */
data class BulkUrlAnalysisResult(
    val batchId: String,
    val itemsProcessed: Int,
    val itemsSucceeded: Int,
    val itemsFailed: Int,
    val processingTimeMs: Long,
    val results: List<BulkUrlItemResult>
)

/**
 * Data class for individual URL result within a bulk operation
 */
data class BulkUrlItemResult(
    val id: String,
    val metadata: Map<String, Any>,
    val analysis: UrlAnalysisResult?,
    val error: String?
)