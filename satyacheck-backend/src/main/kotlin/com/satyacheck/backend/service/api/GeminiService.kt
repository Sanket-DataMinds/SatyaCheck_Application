package com.satyacheck.backend.service.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.satyacheck.backend.model.dto.AnalysisResult
import com.satyacheck.backend.model.dto.MisinformationAnalysis
import com.satyacheck.backend.model.enum.Verdict
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.util.logging.Logger

@Service
class GeminiService(
    private val webClient: WebClient,
    private val objectMapper: ObjectMapper
) {
    private val logger = Logger.getLogger(GeminiService::class.java.name)

    @Value("\${google.cloud.gemini.api-key}")
    private lateinit var geminiApiKey: String
    
    /**
     * Check if the Gemini API key is configured
     * 
     * @return true if API key is configured, false otherwise
     */
    fun isApiKeyConfigured(): Boolean {
        return ::geminiApiKey.isInitialized && geminiApiKey.isNotBlank() && geminiApiKey != "your-gemini-api-key"
    }
    
    /**
     * Analyzes text content using Google's Gemini API for fact-checking
     * 
     * @param content The text content to analyze
     * @param language The language of the content (default: "en")
     * @return A Pair containing the verdict and explanation
     */
    suspend fun analyzeContent(content: String, language: String = "en"): Pair<Verdict, String> {
        try {
            logger.info("Analyzing content with Gemini API: ${content.take(100)}...")
            
            val prompt = buildPrompt(content, language)
            
            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to prompt)
                        )
                    )
                ),
                "generationConfig" to mapOf(
                    "temperature" to 0.2,
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
            
            return processGeminiResponse(response)
        } catch (e: Exception) {
            logger.severe("Error analyzing content with Gemini API: ${e.message}")
            return Pair(Verdict.UNKNOWN, "An error occurred during analysis: ${e.message}")
        }
    }
    
    /**
     * Builds a prompt for the Gemini API to analyze content
     */
    private fun buildPrompt(content: String, language: String): String {
        val languageSpecificInstructions = if (language == "en") {
            "Analyze the following text for factual accuracy and potential misinformation."
        } else {
            "Analyze the following text for factual accuracy and potential misinformation. The text is in $language."
        }
        
        return """
            $languageSpecificInstructions
            
            You are a fact-checking assistant for the SatyaCheck app. Carefully analyze the provided content for:
            1. Factual accuracy
            2. Misleading claims
            3. False information
            4. Missing context
            5. Logical inconsistencies
            
            Content to analyze:
            "${content}"
            
            First, determine if the content is:
            - ACCURATE (contains only factually correct information)
            - POTENTIALLY_MISLEADING (contains some misleading elements or needs context)
            - FALSE (contains demonstrably false information)
            - SATIRE (intended as humor or parody, not factual)
            - OPINION (primarily an opinion rather than factual claim)
            - INSUFFICIENT_INFO (not enough information to determine accuracy)
            
            Then, provide a detailed explanation of your verdict, citing specific parts of the content and why they are accurate or problematic.
            
            Format your response as a JSON object with two keys:
            - "verdict": One of the verdicts listed above (exact string match)
            - "explanation": A detailed explanation of your analysis, with evidence
            
            Response format:
            {"verdict": "VERDICT_HERE", "explanation": "Your detailed explanation here"}
        """.trimIndent()
    }
    
    /**
     * Processes the response from the Gemini API
     */
    @Suppress("UNCHECKED_CAST")
    private fun processGeminiResponse(response: Map<String, Any>): Pair<Verdict, String> {
        try {
            // Extract the generated content from the Gemini API response
            val candidates = response["candidates"] as? List<Map<String, Any>>
            val content = candidates?.firstOrNull()?.get("content") as? Map<String, Any>
            val parts = content?.get("parts") as? List<Map<String, Any>>
            val text = parts?.firstOrNull()?.get("text") as? String
                ?: return Pair(Verdict.ERROR, "Failed to extract text from Gemini API response")
            
            // Try to parse the JSON response
            try {
                // Find JSON content within the text (sometimes Gemini wraps the JSON in markdown code blocks)
                val jsonPattern = "\\{\"verdict\":.+\"explanation\":.+\\}".toRegex()
                val jsonMatch = jsonPattern.find(text)
                val jsonContent = jsonMatch?.value ?: text
                
                val resultMap = objectMapper.readValue(jsonContent, Map::class.java) as Map<String, String>
                
                val verdictStr = resultMap["verdict"] ?: return Pair(Verdict.ERROR, "Missing verdict in response")
                val explanation = resultMap["explanation"] ?: return Pair(Verdict.ERROR, "Missing explanation in response")
                
                // Parse the verdict
                val verdict = try {
                    Verdict.valueOf(verdictStr)
                } catch (e: IllegalArgumentException) {
                    logger.warning("Invalid verdict: $verdictStr, falling back to INSUFFICIENT_INFO")
                    Verdict.INSUFFICIENT_INFO
                }
                
                return Pair(verdict, explanation)
            } catch (e: Exception) {
                // If JSON parsing fails, extract information manually using regex
                logger.warning("Failed to parse JSON from Gemini response, attempting regex extraction: ${e.message}")
                
                val verdictRegex = "verdict\"?:\\s*\"?(ACCURATE|POTENTIALLY_MISLEADING|FALSE|SATIRE|OPINION|INSUFFICIENT_INFO|ERROR)\"?".toRegex()
                val verdictMatch = verdictRegex.find(text)
                
                val verdict = if (verdictMatch != null) {
                    val verdictStr = verdictMatch.groupValues[1]
                    try {
                        Verdict.valueOf(verdictStr)
                    } catch (e: IllegalArgumentException) {
                        Verdict.INSUFFICIENT_INFO
                    }
                } else {
                    Verdict.INSUFFICIENT_INFO
                }
                
                // Extract the explanation - look for text after "explanation":
                val explanationRegex = "explanation\"?:\\s*\"?([^\"]+)\"?".toRegex()
                val explanationMatch = explanationRegex.find(text)
                val explanation = explanationMatch?.groupValues?.get(1)
                    ?: "Unable to extract explanation from the analysis."
                
                return Pair(verdict, explanation)
            }
        } catch (e: Exception) {
            logger.severe("Error processing Gemini API response: ${e.message}")
            return Pair(Verdict.ERROR, "Error processing analysis results: ${e.message}")
        }
    }
    
    /**
     * Analyze content specifically for misinformation patterns and techniques
     * Cached to improve performance for repeated requests
     */
    @Cacheable(value = ["misinformationAnalysis"], key = "#content.hashCode() + '_' + #language")
    suspend fun analyzeContentForMisinformation(content: String, language: String = "en"): MisinformationAnalysis {
        try {
            logger.info("Analyzing content for misinformation with Gemini API: ${content.take(50)}...")
            
            val prompt = buildMisinformationPrompt(content, language)
            
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
            
            return processMisinformationResponse(response)
        } catch (e: Exception) {
            logger.severe("Error analyzing misinformation with Gemini API: ${e.message}")
            return MisinformationAnalysis(
                isLikelyMisinformation = false,
                confidenceScore = 0.0,
                explanation = "Failed to analyze content due to an error: ${e.message}"
            )
        }
    }
    
    /**
     * Build prompt for misinformation analysis
     */
    private fun buildMisinformationPrompt(content: String, language: String): String {
        val languageSpecificInstructions = if (language == "en") {
            "Analyze the following content for misinformation patterns and techniques."
        } else {
            "Analyze the following content for misinformation patterns and techniques. The content is in $language."
        }
        
        return """
            $languageSpecificInstructions
            
            Content to analyze:
            "${content}"
            
            You are an expert misinformation detection system for the SatyaCheck fact-checking app. 
            Deeply analyze this content for misinformation patterns, propaganda techniques, and manipulation tactics.
            
            Provide a detailed response in JSON format with these fields:
            - "isLikelyMisinformation": boolean (true if the content likely contains misinformation)
            - "confidenceScore": number between 0.0 and 1.0
            - "riskLevel": string, one of "LOW", "MEDIUM", "HIGH", or "CRITICAL"
            - "patterns": array of strings describing specific misinformation patterns detected
            - "techniques": array of strings naming specific propaganda or manipulation techniques used
            - "explanation": detailed explanation of the analysis
            
            Focus on detecting these misinformation techniques:
            - Appeal to emotion
            - False attribution
            - Cherry-picking data
            - False equivalence
            - Strawman arguments
            - Misleading headlines
            - Out-of-context quotes or images
            - Conspiracy theory patterns
            - False dichotomies
            - Fake experts or credentials
            - Loaded language
            
            JSON format:
            {
                "isLikelyMisinformation": true/false,
                "confidenceScore": 0.85,
                "riskLevel": "MEDIUM",
                "patterns": ["pattern 1", "pattern 2"],
                "techniques": ["technique 1", "technique 2"],
                "explanation": "detailed explanation"
            }
        """.trimIndent()
    }
    
    /**
     * Process the response from Gemini API for misinformation analysis
     */
    @Suppress("UNCHECKED_CAST")
    private fun processMisinformationResponse(response: Map<String, Any>): MisinformationAnalysis {
        try {
            // Extract the generated content from the Gemini API response
            val candidates = response["candidates"] as? List<Map<String, Any>>
            val content = candidates?.firstOrNull()?.get("content") as? Map<String, Any>
            val parts = content?.get("parts") as? List<Map<String, Any>>
            val text = parts?.firstOrNull()?.get("text") as? String
                ?: return MisinformationAnalysis(
                    isLikelyMisinformation = false,
                    confidenceScore = 0.0,
                    explanation = "Failed to extract response from API"
                )
            
            // Find JSON content within the text
            val jsonPattern = "\\{.*\"isLikelyMisinformation\".*\\}".toRegex(RegexOption.DOT_MATCHES_ALL)
            val jsonMatch = jsonPattern.find(text)
            val jsonContent = jsonMatch?.value ?: text
            
            // Parse the JSON
            val resultMap = objectMapper.readValue(jsonContent, object : TypeReference<Map<String, Any>>() {})
            
            val isLikelyMisinformation = resultMap["isLikelyMisinformation"] as? Boolean ?: false
            val confidenceScore = (resultMap["confidenceScore"] as? Number)?.toDouble() ?: 0.0
            val riskLevel = resultMap["riskLevel"] as? String
            val patterns = resultMap["patterns"] as? List<String>
            val techniques = resultMap["techniques"] as? List<String>
            val explanation = resultMap["explanation"] as? String
            
            return MisinformationAnalysis(
                isLikelyMisinformation = isLikelyMisinformation,
                confidenceScore = confidenceScore,
                riskLevel = riskLevel,
                patterns = patterns,
                techniques = techniques,
                explanation = explanation
            )
        } catch (e: Exception) {
            logger.severe("Error processing misinformation response: ${e.message}")
            return MisinformationAnalysis(
                isLikelyMisinformation = false,
                confidenceScore = 0.0,
                explanation = "Failed to process API response due to an error: ${e.message}"
            )
        }
    }
}