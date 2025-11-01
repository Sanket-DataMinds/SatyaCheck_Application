package com.satyacheck.backend.service

import com.satyacheck.backend.model.dto.AnalysisResult
import com.satyacheck.backend.model.dto.ContentCategory
import com.satyacheck.backend.model.dto.EnhancedAnalysisResult
import com.satyacheck.backend.model.dto.ExtractedTopic
import com.satyacheck.backend.service.api.ContentCategorizationService
import com.satyacheck.backend.service.api.GeminiService
import com.satyacheck.backend.service.api.NaturalLanguageService
import com.satyacheck.backend.service.api.TranslationService
import com.satyacheck.backend.service.api.VisionService
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.logging.Logger

/**
 * Implementation of EnhancedAnalysisService that combines multiple analysis services
 * for comprehensive content evaluation
 */
@Service
@CacheConfig(cacheNames = ["enhancedAnalysis"])
class EnhancedAnalysisServiceImpl(
    private val analysisService: AnalysisService,
    private val geminiService: GeminiService,
    private val contentCategorizationService: ContentCategorizationService,
    private val naturalLanguageService: NaturalLanguageService,
    private val translationService: TranslationService,
    private val visionService: VisionService
) : EnhancedAnalysisService {

    private val logger = Logger.getLogger(EnhancedAnalysisServiceImpl::class.java.name)

    /**
     * Perform comprehensive analysis on the provided content using multiple services
     * Cached to improve performance for repeated requests
     */
    @Cacheable(key = "#content.hashCode() + '_' + #language")
    override suspend fun analyzeComprehensively(content: String, language: String): EnhancedAnalysisResult {
        logger.info("Starting comprehensive analysis for content: ${content.take(50)}...")
        
        // Run all analysis operations in parallel where possible
        
        // Get base fact-checking analysis
        val baseAnalysis = analysisService.analyzeContent(content, language)
        
        // Get content categorization
        val contentCategory = contentCategorizationService.categorizeContent(content, language)
        
        // Extract topics
        val extractedTopics = contentCategorizationService.extractTopics(content, language)
        
        // Get named entities if language is English (NLP API supports limited languages)
        val entities = if (language == "en") {
            naturalLanguageService.extractEntities(content)
        } else {
            emptyList()
        }
        
        // Get sentiment analysis if language is English
        val sentiment = if (language == "en") {
            naturalLanguageService.analyzeSentiment(content)
        } else {
            null
        }
        
        return combineResults(baseAnalysis, contentCategory, extractedTopics, entities, sentiment)
    }
    
    /**
     * Perform enhanced analysis focused on detecting misinformation patterns
     */
    @Cacheable(key = "'misinformation_' + #content.hashCode() + '_' + #language")
    override suspend fun analyzeMisinformationPatterns(content: String, language: String): EnhancedAnalysisResult {
        logger.info("Analyzing misinformation patterns for content: ${content.take(50)}...")
        
        // Get base fact-checking analysis
        val baseAnalysis = analysisService.analyzeContent(content, language)
        
        // Get deeper misinformation analysis from Gemini
        val misinformationAnalysis = geminiService.analyzeContentForMisinformation(content, language)
        
        // Extract topics and get content categorization
        val contentCategory = contentCategorizationService.categorizeContent(content, language)
        val extractedTopics = contentCategorizationService.extractTopics(content, language)
        
        return EnhancedAnalysisResult(
            factCheckResult = baseAnalysis,
            misinformationPatterns = misinformationAnalysis.patterns ?: emptyList(),
            misinformationTechniques = misinformationAnalysis.techniques ?: emptyList(),
            contentCategory = contentCategory,
            extractedTopics = extractedTopics,
            namedEntities = emptyList(),
            sentimentScore = null,
            additionalContext = mapOf(
                "misinformationRisk" to (misinformationAnalysis.riskLevel ?: "UNKNOWN"),
                "techniquesIdentified" to (misinformationAnalysis.techniques?.size ?: 0),
                "analysisType" to "MISINFORMATION_FOCUSED"
            )
        )
    }
    
    /**
     * Cross-language analysis with translation support
     */
    override suspend fun analyzeCrossLanguage(content: String, targetLanguages: List<String>): Map<String, Any> {
        logger.info("Starting cross-language analysis for ${targetLanguages.size} languages")
        
        // Detect original language
        val languageDetection = translationService.detectLanguage(content)
        val originalLanguage = languageDetection["detectedLanguage"] as? String ?: "unknown"
        
        // Translate to target languages and analyze each
        val analyses = mutableMapOf<String, Map<String, Any>>()
        
        targetLanguages.forEach { targetLang ->
            val translatedContent = if (targetLang == originalLanguage) {
                content
            } else {
                val translation = translationService.translateText(content, targetLang, originalLanguage)
                translation["translatedText"] as? String ?: content
            }
            
            // Analyze in target language
            val analysis = naturalLanguageService.analyzeComprehensively(translatedContent, targetLang)
            analyses[targetLang] = analysis
        }
        
        return mapOf(
            "originalContent" to content,
            "detectedLanguage" to originalLanguage,
            "targetLanguages" to targetLanguages,
            "analyses" to analyses,
            "crossLanguageConsistency" to calculateCrossLanguageConsistency(analyses),
            "recommendations" to generateCrossLanguageRecommendations(analyses, originalLanguage)
        )
    }
    
    /**
     * Analyze image content for misinformation
     */
    override suspend fun analyzeImageContent(imageData: ByteArray, language: String): Map<String, Any> {
        logger.info("Starting image content analysis for misinformation detection")
        
        // Comprehensive image analysis
        val imageAnalysis = visionService.analyzeImageComprehensively(imageData)
        
        // Extract text and analyze if present
        val textAnalysis = imageAnalysis["textAnalysis"] as? Map<String, Any> ?: emptyMap()
        val extractedText = textAnalysis["extractedText"] as? String ?: ""
        
        val textMisinformationAnalysis = if (extractedText.isNotEmpty()) {
            // Analyze extracted text for misinformation
            val baseAnalysis = analysisService.analyzeContent(extractedText, language)
            val nlpAnalysis = naturalLanguageService.analyzeComprehensively(extractedText, language)
            
            mapOf(
                "factCheckResult" to baseAnalysis,
                "nlpAnalysis" to nlpAnalysis,
                "hasTextContent" to true
            )
        } else {
            mapOf(
                "hasTextContent" to false,
                "message" to "No text content found in image"
            )
        }
        
        // Check for image manipulation
        val manipulationAnalysis = visionService.detectImageManipulation(imageData)
        
        return mapOf(
            "imageAnalysis" to imageAnalysis,
            "textMisinformationAnalysis" to textMisinformationAnalysis,
            "manipulationAnalysis" to manipulationAnalysis,
            "combinedRisk" to calculateImageMisinformationRisk(imageAnalysis, textMisinformationAnalysis, manipulationAnalysis),
            "recommendations" to generateImageAnalysisRecommendations(extractedText, imageAnalysis, manipulationAnalysis)
        )
    }
    
    // Helper methods for new functionality
    private fun calculateCrossLanguageConsistency(analyses: Map<String, Map<String, Any>>): Double {
        // Calculate consistency based on sentiment scores across languages
        val sentimentScores = analyses.values.mapNotNull { analysis ->
            val sentiment = analysis["sentiment"] as? Map<String, Any>
            sentiment?.get("score") as? Double
        }
        
        if (sentimentScores.size < 2) return 1.0
        
        val avg = sentimentScores.average()
        val variance = sentimentScores.map { (it - avg) * (it - avg) }.average()
        
        // Convert variance to consistency score (lower variance = higher consistency)
        return maxOf(0.0, 1.0 - variance)
    }
    
    private fun generateCrossLanguageRecommendations(
        analyses: Map<String, Map<String, Any>>,
        originalLanguage: String
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        val consistency = calculateCrossLanguageConsistency(analyses)
        
        when {
            consistency > 0.8 -> {
                recommendations.add("High cross-language consistency - content meaning preserved")
                recommendations.add("Analysis results are reliable across languages")
            }
            consistency > 0.6 -> {
                recommendations.add("Moderate cross-language consistency - some variation in analysis")
                recommendations.add("Recommend verification in original language ($originalLanguage)")
            }
            else -> {
                recommendations.add("Low cross-language consistency - significant translation impact")
                recommendations.add("Strongly recommend analysis in original language ($originalLanguage)")
                recommendations.add("Consider human translation for critical analysis")
            }
        }
        
        return recommendations
    }
    
    private fun calculateImageMisinformationRisk(
        imageAnalysis: Map<String, Any>,
        textAnalysis: Map<String, Any>,
        manipulationAnalysis: Map<String, Any>
    ): String {
        var riskScore = 0
        
        // Check image misinformation indicators
        val imageRisk = ((imageAnalysis["misinformationIndicators"] as? Map<String, Any>)?.get("riskLevel") as? String) ?: "LOW"
        riskScore += when (imageRisk) {
            "HIGH", "VERY_HIGH" -> 3
            "MEDIUM" -> 2
            else -> 1
        }
        
        // Check text analysis if available
        if (textAnalysis["hasTextContent"] as? Boolean == true) {
            val factCheckResult = textAnalysis["factCheckResult"] as? AnalysisResult
            riskScore += when (factCheckResult?.verdict?.name) {
                "HIGH_MISINFORMATION_RISK", "SCAM_ALERT" -> 3
                "POTENTIALLY_MISLEADING" -> 2
                else -> 1
            }
        }
        
        // Check manipulation likelihood
        val manipulationRisk = manipulationAnalysis["manipulationLikelihood"] as? String ?: "LOW"
        riskScore += when (manipulationRisk) {
            "HIGH" -> 3
            "MEDIUM" -> 2
            else -> 1
        }
        
        return when {
            riskScore >= 7 -> "VERY_HIGH"
            riskScore >= 5 -> "HIGH"
            riskScore >= 3 -> "MEDIUM"
            else -> "LOW"
        }
    }
    
    private fun generateImageAnalysisRecommendations(
        extractedText: String,
        imageAnalysis: Map<String, Any>,
        manipulationAnalysis: Map<String, Any>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (extractedText.isNotEmpty()) {
            recommendations.add("Verify text claims found in image through official sources")
            recommendations.add("Cross-check extracted text with fact-checking databases")
        }
        
        val manipulationRisk = manipulationAnalysis["manipulationLikelihood"] as? String ?: "LOW"
        if (manipulationRisk in listOf("HIGH", "MEDIUM")) {
            recommendations.add("Image shows signs of potential manipulation - verify authenticity")
            recommendations.add("Use reverse image search to find original source")
        }
        
        recommendations.add("Check image metadata for creation date and camera information")
        recommendations.add("Verify image source and distribution chain")
        
        return recommendations
    }

    /**
     * Combines results from various analysis services into a single comprehensive result
     */
    private fun combineResults(
        baseAnalysis: AnalysisResult,
        contentCategory: ContentCategory,
        extractedTopics: List<ExtractedTopic>,
        entities: List<Map<String, Any>>,
        sentiment: Map<String, Any>?
    ): EnhancedAnalysisResult {
        return EnhancedAnalysisResult(
            factCheckResult = baseAnalysis,
            misinformationPatterns = emptyList(), // Will be populated by specific analysis
            misinformationTechniques = emptyList(),
            contentCategory = contentCategory,
            extractedTopics = extractedTopics,
            namedEntities = entities,
            sentimentScore = sentiment?.get("score") as? Double,
            additionalContext = mapOf(
                "sentimentAnalysis" to (sentiment ?: emptyMap<String, Any>()),
                "entityCount" to entities.size,
                "analysisType" to "COMPREHENSIVE"
            )
        )
    }
}
     */
    private fun combineResults(
        baseAnalysis: AnalysisResult,
        contentCategory: ContentCategory,
        extractedTopics: List<ExtractedTopic>,
        entities: List<Map<String, Any>>,
        sentiment: Map<String, Any>?
    ): EnhancedAnalysisResult {
        // Extract sentiment score if available
        val sentimentScore = sentiment?.get("score") as? Double
        
        // Build additional context with relevant information
        val additionalContext = mutableMapOf<String, Any>(
            "categoryConfidence" to contentCategory.confidence,
            "topicCount" to extractedTopics.size,
            "entityCount" to entities.size,
            "analysisType" to "COMPREHENSIVE"
        )
        
        // Add sentiment information if available
        sentiment?.let {
            additionalContext["sentimentMagnitude"] = it["magnitude"] as? Double ?: 0.0
            additionalContext["sentimentLanguage"] = it["language"] as? String ?: "unknown"
        }
        
        return EnhancedAnalysisResult(
            factCheckResult = baseAnalysis,
            contentCategory = contentCategory,
            extractedTopics = extractedTopics,
            namedEntities = entities,
            sentimentScore = sentimentScore,
            additionalContext = additionalContext
        )
    }
}