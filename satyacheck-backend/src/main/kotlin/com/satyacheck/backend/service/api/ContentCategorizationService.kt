package com.satyacheck.backend.service.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.satyacheck.backend.model.dto.ContentCategory
import com.satyacheck.backend.model.dto.ExtractedTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.util.logging.Logger

/**
 * Service for content categorization and topic extraction using Gemini API
 */
@Service
class ContentCategorizationService(
    private val webClient: WebClient,
    private val objectMapper: ObjectMapper
) {
    private val logger = Logger.getLogger(ContentCategorizationService::class.java.name)

    @Value("\${google.cloud.gemini.api-key}")
    private lateinit var geminiApiKey: String

    /**
     * Categorize content into relevant categories
     */
    @Cacheable(value = ["contentCategories"], key = "#content.hashCode() + '_' + #language")
    suspend fun categorizeContent(content: String, language: String = "en"): ContentCategory {
        try {
            logger.info("Categorizing content with Gemini API: ${content.take(50)}...")
            
            val prompt = buildCategorizationPrompt(content, language)
            
            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to prompt)
                        )
                    )
                ),
                "generationConfig" to mapOf(
                    "temperature" to 0.1,
                    "topK" to 40,
                    "topP" to 0.95,
                    "maxOutputTokens" to 1024
                )
            )
            
            val response = webClient.post()
                .uri("https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=$geminiApiKey")
                .bodyValue(requestBody)
                .retrieve()
                .awaitBody<Map<String, Any>>()
            
            return processCategorizationResponse(response)
        } catch (e: Exception) {
            logger.severe("Error categorizing content with Gemini API: ${e.message}")
            return ContentCategory(
                primaryCategory = "UNCATEGORIZED",
                subCategories = emptyList(),
                confidence = 0.0,
                tags = emptyList()
            )
        }
    }

    /**
     * Extract main topics from content
     */
    @Cacheable(value = ["extractedTopics"], key = "#content.hashCode() + '_' + #language")
    suspend fun extractTopics(content: String, language: String = "en"): List<ExtractedTopic> {
        try {
            logger.info("Extracting topics from content with Gemini API: ${content.take(50)}...")
            
            val prompt = buildTopicExtractionPrompt(content, language)
            
            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to prompt)
                        )
                    )
                ),
                "generationConfig" to mapOf(
                    "temperature" to 0.1,
                    "topK" to 40,
                    "topP" to 0.95,
                    "maxOutputTokens" to 1024
                )
            )
            
            val response = webClient.post()
                .uri("https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=$geminiApiKey")
                .bodyValue(requestBody)
                .retrieve()
                .awaitBody<Map<String, Any>>()
            
            return processTopicsResponse(response)
        } catch (e: Exception) {
            logger.severe("Error extracting topics with Gemini API: ${e.message}")
            return listOf(
                ExtractedTopic(
                    topic = "ERROR",
                    relevance = 0.0,
                    keywords = listOf("error", "processing", "failed")
                )
            )
        }
    }

    /**
     * Build prompt for content categorization
     */
    private fun buildCategorizationPrompt(content: String, language: String): String {
        val languageSpecificInstructions = if (language == "en") {
            "Categorize the following text content."
        } else {
            "Categorize the following text content. The text is in $language."
        }
        
        return """
            $languageSpecificInstructions
            
            You are a content categorization system for the SatyaCheck app. Analyze the provided content and categorize it.
            
            Content to categorize:
            "${content}"
            
            Categorize this content into a primary category and subcategories. Choose the primary category from these options:
            - NEWS
            - POLITICS
            - HEALTH
            - SCIENCE
            - TECHNOLOGY
            - ENTERTAINMENT
            - SPORTS
            - BUSINESS
            - EDUCATION
            - OPINION
            - SOCIAL_MEDIA
            - OTHER
            
            Format your response as a JSON object with these keys:
            - "primaryCategory": The main category (use one from the list above)
            - "subCategories": An array of more specific subcategories (up to 3)
            - "confidence": A number between 0.0 and 1.0 indicating confidence in this categorization
            - "tags": An array of relevant tags or keywords (up to 5)
            
            Response format:
            {"primaryCategory": "CATEGORY", "subCategories": ["sub1", "sub2"], "confidence": 0.95, "tags": ["tag1", "tag2", "tag3"]}
        """.trimIndent()
    }
    
    /**
     * Build prompt for topic extraction
     */
    private fun buildTopicExtractionPrompt(content: String, language: String): String {
        val languageSpecificInstructions = if (language == "en") {
            "Extract main topics from the following text content."
        } else {
            "Extract main topics from the following text content. The text is in $language."
        }
        
        return """
            $languageSpecificInstructions
            
            You are a topic extraction system for the SatyaCheck app. Analyze the provided content and extract the main topics.
            
            Content to analyze:
            "${content}"
            
            Extract up to 3 main topics from this content. For each topic, provide:
            - The main topic name
            - Relevance score (between 0.0 and 1.0)
            - Related subtopics (up to 3)
            - Associated keywords (up to 5)
            
            Format your response as a JSON array with objects having these keys:
            - "topic": The name of the main topic
            - "relevance": A number between 0.0 and 1.0 indicating relevance to the content
            - "subtopics": An array of related subtopics
            - "keywords": An array of relevant keywords
            
            Response format:
            [{"topic": "Climate Change", "relevance": 0.95, "subtopics": ["Global Warming", "Carbon Emissions"], "keywords": ["climate", "warming", "emissions", "environment", "global"]}]
        """.trimIndent()
    }
    
    /**
     * Process the response from Gemini API for content categorization
     */
    @Suppress("UNCHECKED_CAST")
    private fun processCategorizationResponse(response: Map<String, Any>): ContentCategory {
        try {
            // Extract the generated content from the Gemini API response
            val candidates = response["candidates"] as? List<Map<String, Any>>
            val content = candidates?.firstOrNull()?.get("content") as? Map<String, Any>
            val parts = content?.get("parts") as? List<Map<String, Any>>
            val text = parts?.firstOrNull()?.get("text") as? String
                ?: return ContentCategory("ERROR", emptyList(), 0.0, emptyList())
            
            // Find JSON content within the text
            val jsonPattern = "\\{.*\"primaryCategory\".*\\}".toRegex(RegexOption.DOT_MATCHES_ALL)
            val jsonMatch = jsonPattern.find(text)
            val jsonContent = jsonMatch?.value ?: text
            
            // Parse the JSON
            val resultMap = objectMapper.readValue(jsonContent, object : TypeReference<Map<String, Any>>() {})
            
            val primaryCategory = resultMap["primaryCategory"] as? String ?: "UNCATEGORIZED"
            val subCategories = resultMap["subCategories"] as? List<String> ?: emptyList()
            val confidence = (resultMap["confidence"] as? Number)?.toDouble() ?: 0.0
            val tags = resultMap["tags"] as? List<String> ?: emptyList()
            
            return ContentCategory(
                primaryCategory = primaryCategory,
                subCategories = subCategories,
                confidence = confidence,
                tags = tags
            )
        } catch (e: Exception) {
            logger.severe("Error processing categorization response: ${e.message}")
            return ContentCategory(
                primaryCategory = "UNCATEGORIZED",
                subCategories = emptyList(),
                confidence = 0.0,
                tags = emptyList()
            )
        }
    }
    
    /**
     * Process the response from Gemini API for topic extraction
     */
    @Suppress("UNCHECKED_CAST")
    private fun processTopicsResponse(response: Map<String, Any>): List<ExtractedTopic> {
        try {
            // Extract the generated content from the Gemini API response
            val candidates = response["candidates"] as? List<Map<String, Any>>
            val content = candidates?.firstOrNull()?.get("content") as? Map<String, Any>
            val parts = content?.get("parts") as? List<Map<String, Any>>
            val text = parts?.firstOrNull()?.get("text") as? String
                ?: return listOf(ExtractedTopic("ERROR", 0.0))
            
            // Find JSON content within the text
            val jsonPattern = "\\[.*\\]".toRegex(RegexOption.DOT_MATCHES_ALL)
            val jsonMatch = jsonPattern.find(text)
            val jsonContent = jsonMatch?.value ?: text
            
            // Parse the JSON
            val topicsArray = objectMapper.readValue(jsonContent, object : TypeReference<List<Map<String, Any>>>() {})
            
            return topicsArray.map { topic ->
                ExtractedTopic(
                    topic = topic["topic"] as? String ?: "UNKNOWN",
                    relevance = (topic["relevance"] as? Number)?.toDouble() ?: 0.0,
                    subtopics = topic["subtopics"] as? List<String> ?: emptyList(),
                    keywords = topic["keywords"] as? List<String> ?: emptyList()
                )
            }
        } catch (e: Exception) {
            logger.severe("Error processing topics response: ${e.message}")
            return listOf(ExtractedTopic("ERROR", 0.0))
        }
    }
}