package com.satyacheck.backend.service.web

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.util.logging.Logger

/**
 * Service for fetching and extracting content from web URLs
 */
@Service
class WebContentFetcherService(private val webClient: WebClient) {
    private val logger = Logger.getLogger(WebContentFetcherService::class.java.name)
    
    /**
     * Fetch web content from a URL and extract the main text
     * Cached to improve performance for repeated requests
     */
    @Cacheable(value = ["webContent"], key = "#url")
    suspend fun fetchAndExtractContent(url: String): WebContentResult {
        try {
            logger.info("Fetching content from URL: $url")
            
            // Fetch raw HTML
            val html = fetchHtmlContent(url)
            
            // Parse with JSoup
            val document = Jsoup.parse(html)
            
            // Extract content
            val title = extractTitle(document)
            val mainContent = extractMainContent(document)
            val metadata = extractMetadata(document)
            val language = detectLanguage(document)
            
            logger.info("Successfully extracted content from URL: $url")
            
            return WebContentResult(
                url = url,
                title = title,
                content = mainContent,
                metadata = metadata,
                language = language,
                statusCode = 200,
                error = null
            )
        } catch (e: Exception) {
            logger.severe("Error fetching content from URL: $url - ${e.message}")
            
            return WebContentResult(
                url = url,
                title = null,
                content = null,
                metadata = emptyMap(),
                language = "unknown",
                statusCode = 500,
                error = "Error fetching content: ${e.message}"
            )
        }
    }
    
    /**
     * Fetch HTML content from a URL using WebClient
     */
    private suspend fun fetchHtmlContent(url: String): String {
        return try {
            webClient.get()
                .uri(url)
                .header("User-Agent", "SatyaCheck/1.0 Content Analyzer")
                .retrieve()
                .awaitBody<String>()
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch HTML from $url: ${e.message}")
        }
    }
    
    /**
     * Extract title from document
     */
    private fun extractTitle(document: Document): String {
        return document.title() ?: ""
    }
    
    /**
     * Extract main content from document
     * Uses heuristics to find the main content area
     */
    private fun extractMainContent(document: Document): String {
        // Remove script, style, nav, and other non-content elements
        document.select("script, style, nav, footer, header, aside, .ads, .comments").remove()
        
        // Try to find main content container using common selectors
        val contentSelectors = listOf(
            "article", ".article", ".post", ".content", "main", 
            "#content", "#main", ".main-content", "[role=main]"
        )
        
        // Try each selector
        for (selector in contentSelectors) {
            val element = document.select(selector).first()
            if (element != null && element.text().length > 200) {
                return element.text()
            }
        }
        
        // If no matching container found, use body text
        val bodyText = document.body().text()
        
        // If body text is too long, try a more focused approach
        if (bodyText.length > 10000) {
            // Extract paragraphs and headers
            val paragraphs = document.select("p, h1, h2, h3, h4, h5, h6")
            if (paragraphs.isNotEmpty()) {
                return paragraphs.joinToString("\n\n") { it.text() }
            }
        }
        
        return bodyText
    }
    
    /**
     * Extract metadata from the document
     */
    private fun extractMetadata(document: Document): Map<String, String> {
        val metadata = mutableMapOf<String, String>()
        
        // Extract Open Graph metadata
        document.select("meta[property^=og:]").forEach { element ->
            val property = element.attr("property").removePrefix("og:")
            val content = element.attr("content")
            if (property.isNotEmpty() && content.isNotEmpty()) {
                metadata["og:$property"] = content
            }
        }
        
        // Extract Twitter card metadata
        document.select("meta[name^=twitter:]").forEach { element ->
            val name = element.attr("name").removePrefix("twitter:")
            val content = element.attr("content")
            if (name.isNotEmpty() && content.isNotEmpty()) {
                metadata["twitter:$name"] = content
            }
        }
        
        // Extract description and keywords
        document.select("meta[name=description]").firstOrNull()?.let {
            metadata["description"] = it.attr("content")
        }
        
        document.select("meta[name=keywords]").firstOrNull()?.let {
            metadata["keywords"] = it.attr("content")
        }
        
        return metadata
    }
    
    /**
     * Detect language from document
     */
    private fun detectLanguage(document: Document): String {
        // Try to get language from html tag
        val htmlLang = document.select("html").attr("lang")
        if (htmlLang.isNotEmpty()) {
            return htmlLang.split("-")[0] // Extract primary language code
        }
        
        // Try to get language from meta tags
        val metaLang = document.select("meta[http-equiv=content-language]").attr("content")
        if (metaLang.isNotEmpty()) {
            return metaLang.split(",")[0].trim() // Take first language if multiple
        }
        
        // Default to English if no language found
        return "en"
    }
}

/**
 * Data class to hold the result of a web content fetch operation
 */
data class WebContentResult(
    val url: String,
    val title: String?,
    val content: String?,
    val metadata: Map<String, String>,
    val language: String,
    val statusCode: Int,
    val error: String?
)