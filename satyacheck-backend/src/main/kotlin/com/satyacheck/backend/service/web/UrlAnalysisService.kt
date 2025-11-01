package com.satyacheck.backend.service.web

import com.satyacheck.backend.model.dto.EnhancedAnalysisResult
import com.satyacheck.backend.service.EnhancedAnalysisService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.net.URL
import java.util.logging.Logger

/**
 * Service for analyzing web content from URLs
 */
@Service
class UrlAnalysisService(
    private val webContentFetcherService: WebContentFetcherService,
    private val enhancedAnalysisService: EnhancedAnalysisService
) {
    private val logger = Logger.getLogger(UrlAnalysisService::class.java.name)

    /**
     * Analyze content from a URL
     * Cached to improve performance for repeated requests
     */
    @Cacheable(value = ["urlAnalysis"], key = "#url")
    suspend fun analyzeUrl(url: String): UrlAnalysisResult {
        try {
            logger.info("Starting URL analysis for: $url")
            
            // Validate URL
            val validatedUrl = validateAndNormalizeUrl(url)
            
            // Fetch and extract content
            val webContentResult = webContentFetcherService.fetchAndExtractContent(validatedUrl)
            
            // Check if content was successfully fetched
            if (webContentResult.content.isNullOrBlank()) {
                return UrlAnalysisResult(
                    url = validatedUrl,
                    title = webContentResult.title,
                    language = webContentResult.language,
                    metadata = webContentResult.metadata,
                    analysis = null,
                    error = webContentResult.error ?: "No content could be extracted from the URL"
                )
            }
            
            // Analyze the extracted content
            val enhancedAnalysis = enhancedAnalysisService.analyzeComprehensively(
                webContentResult.content, 
                webContentResult.language
            )
            
            logger.info("Successfully completed URL analysis for: $validatedUrl")
            
            return UrlAnalysisResult(
                url = validatedUrl,
                title = webContentResult.title,
                language = webContentResult.language,
                metadata = webContentResult.metadata,
                analysis = enhancedAnalysis,
                error = null
            )
        } catch (e: Exception) {
            logger.severe("Error analyzing URL: $url - ${e.message}")
            
            return UrlAnalysisResult(
                url = url,
                title = null,
                language = "unknown",
                metadata = emptyMap(),
                analysis = null,
                error = "Error analyzing URL: ${e.message}"
            )
        }
    }
    
    /**
     * Validate and normalize a URL
     */
    private fun validateAndNormalizeUrl(url: String): String {
        try {
            val normalizedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }
            
            // Validate URL by parsing it
            URL(normalizedUrl)
            
            return normalizedUrl
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid URL format: $url")
        }
    }
}

/**
 * Data class to hold the result of a URL analysis operation
 */
data class UrlAnalysisResult(
    val url: String,
    val title: String?,
    val language: String,
    val metadata: Map<String, String>,
    val analysis: EnhancedAnalysisResult?,
    val error: String?
)