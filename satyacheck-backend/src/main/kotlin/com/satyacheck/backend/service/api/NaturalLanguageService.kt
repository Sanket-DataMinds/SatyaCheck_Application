package com.satyacheck.backend.service.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.language.v1.AnalyzeEntitiesRequest
import com.google.cloud.language.v1.AnalyzeSentimentRequest
import com.google.cloud.language.v1.Document
import com.google.cloud.language.v1.EncodingType
import com.google.cloud.language.v1.LanguageServiceClient
import org.springframework.stereotype.Service
import java.util.logging.Logger

/**
 * Service for natural language processing using Google's Natural Language API
 */
@Service
class NaturalLanguageService {
    private val logger = Logger.getLogger(NaturalLanguageService::class.java.name)

    /**
     * Analyzes the sentiment of the provided text
     * @return A map with sentiment score and magnitude
     */
    fun analyzeSentiment(text: String, language: String = "en"): Map<String, Any> {
        LanguageServiceClient.create().use { languageService ->
            // Set up the request
            val doc = Document.newBuilder()
                .setContent(text)
                .setType(Document.Type.PLAIN_TEXT)
                .setLanguage(language)
                .build()
                
            val request = AnalyzeSentimentRequest.newBuilder()
                .setDocument(doc)
                .setEncodingType(EncodingType.UTF8)
                .build()

            // Analyze the sentiment
            val response = languageService.analyzeSentiment(request)
            val sentiment = response.documentSentiment

            logger.info("Sentiment analysis completed with score: ${sentiment.score}")
            
            return mapOf(
                "score" to sentiment.score,
                "magnitude" to sentiment.magnitude,
                "overallSentiment" to when {
                    sentiment.score >= 0.25 -> "POSITIVE"
                    sentiment.score <= -0.25 -> "NEGATIVE"
                    else -> "NEUTRAL"
                },
                "intensity" to when {
                    sentiment.magnitude < 0.5 -> "LOW"
                    sentiment.magnitude < 1.5 -> "MEDIUM"
                    else -> "HIGH"
                }
            )
        }
    }

    /**
     * Extracts entities from the provided text
     * @return A list of entity information
     */
    fun extractEntities(text: String, language: String = "en"): List<Map<String, Any>> {
        LanguageServiceClient.create().use { languageService ->
            // Set up the request
            val doc = Document.newBuilder()
                .setContent(text)
                .setType(Document.Type.PLAIN_TEXT)
                .setLanguage(language)
                .build()
                
            val request = AnalyzeEntitiesRequest.newBuilder()
                .setDocument(doc)
                .setEncodingType(EncodingType.UTF8)
                .build()

            // Extract entities
            val response = languageService.analyzeEntities(request)
            val entities = response.entitiesList
            
            logger.info("Entity extraction completed, found ${entities.size} entities")
            
            return entities.map { entity ->
                mapOf(
                    "name" to entity.name,
                    "type" to entity.type.name,
                    "salience" to entity.salience,
                    "mentionCount" to entity.mentionsCount,
                    "metadata" to entity.metadataMap
                )
            }
        }
    }
}