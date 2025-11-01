package com.satyacheck.backend.service.language

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.util.logging.Logger

/**
 * Service for language detection and support
 */
@Service
class LanguageDetectionService(private val webClient: WebClient) {
    private val logger = Logger.getLogger(LanguageDetectionService::class.java.name)
    
    // Common languages with their codes
    private val supportedLanguages = mapOf(
        "en" to LanguageSupport("English", true, true, true),
        "hi" to LanguageSupport("Hindi", true, true, false),
        "mr" to LanguageSupport("Marathi", true, false, false),
        "es" to LanguageSupport("Spanish", true, false, false),
        "fr" to LanguageSupport("French", true, false, false),
        "de" to LanguageSupport("German", true, false, false),
        "zh" to LanguageSupport("Chinese", true, false, false),
        "ja" to LanguageSupport("Japanese", false, false, false),
        "ar" to LanguageSupport("Arabic", false, false, false),
        "ru" to LanguageSupport("Russian", false, false, false),
        "pt" to LanguageSupport("Portuguese", true, false, false),
        "bn" to LanguageSupport("Bengali", false, false, false),
        "ur" to LanguageSupport("Urdu", false, false, false),
        "te" to LanguageSupport("Telugu", false, false, false),
        "ta" to LanguageSupport("Tamil", false, false, false)
    )
    
    /**
     * Detect the language of a text
     * Cached to improve performance for repeated requests
     */
    @Cacheable(value = ["languageDetection"], key = "#text.hashCode()")
    suspend fun detectLanguage(text: String): DetectedLanguage {
        // For very short text, detection can be unreliable
        if (text.length < 10) {
            return DetectedLanguage("en", "English", 1.0)
        }
        
        try {
            logger.info("Detecting language for text: ${text.take(50)}...")
            
            // In a real implementation, we'd use a language detection service like Google's
            // For now, let's use a simplified approach with common language patterns
            
            // This is just a placeholder - in a real implementation, you would use:
            // 1. Google Cloud Translation API
            // 2. Azure Cognitive Services
            // 3. A specialized library like language-detector
            
            val detectedCode = detectLanguageSimple(text)
            val languageName = supportedLanguages[detectedCode]?.name ?: "Unknown"
            
            return DetectedLanguage(
                code = detectedCode,
                name = languageName,
                confidence = 0.8
            )
        } catch (e: Exception) {
            logger.severe("Error detecting language: ${e.message}")
            return DetectedLanguage("en", "English", 0.5)
        }
    }
    
    /**
     * Simple language detection based on character sets and patterns
     * This is a basic implementation and would be replaced with a proper service
     */
    private fun detectLanguageSimple(text: String): String {
        val sample = text.take(100).lowercase()
        
        // Check for Devanagari script (Hindi)
        if (sample.any { it in '\u0900'..'\u097F' }) {
            return "hi"
        }
        
        // Check for Marathi (similar to Hindi but with some specific patterns)
        if (sample.contains("आहे") || sample.contains("मराठी")) {
            return "mr"
        }
        
        // Check for Spanish
        if (sample.contains("ñ") || (sample.contains(" el ") && sample.contains(" la ") && sample.contains(" que "))) {
            return "es"
        }
        
        // Check for French
        if (sample.contains("ç") || sample.contains(" est ") && sample.contains(" le ") && sample.contains(" la ")) {
            return "fr"
        }
        
        // Check for German
        if (sample.contains("ß") || (sample.contains(" und ") && sample.contains(" der ") && sample.contains(" die "))) {
            return "de"
        }
        
        // Check for Chinese
        if (sample.any { it.code in 0x4E00..0x9FFF }) {
            return "zh"
        }
        
        // Check for Japanese
        if (sample.any { it.code in 0x3040..0x309F || it.code in 0x30A0..0x30FF }) {
            return "ja"
        }
        
        // Check for Arabic
        if (sample.any { it.code in 0x0600..0x06FF }) {
            return "ar"
        }
        
        // Check for Russian
        if (sample.any { it.code in 0x0400..0x04FF }) {
            return "ru"
        }
        
        // Default to English
        return "en"
    }
    
    /**
     * Get support information for a specific language
     */
    fun getLanguageSupport(languageCode: String): LanguageSupport {
        return supportedLanguages[languageCode] ?: LanguageSupport(
            name = "Unknown",
            isSupported = false,
            hasTranslation = false,
            hasFullAnalysis = false
        )
    }
    
    /**
     * Get all supported languages
     */
    fun getAllSupportedLanguages(): Map<String, LanguageSupport> {
        return supportedLanguages
    }
    
    /**
     * Get languages that support full analysis
     */
    fun getFullAnalysisLanguages(): Map<String, LanguageSupport> {
        return supportedLanguages.filter { it.value.hasFullAnalysis }
    }
    
    /**
     * Normalize language code
     */
    fun normalizeLanguageCode(code: String): String {
        // Handle common variations
        return when (code.lowercase()) {
            "en", "en-us", "en-gb", "eng", "english" -> "en"
            "hi", "hin", "hindi" -> "hi"
            "mr", "mar", "marathi" -> "mr"
            "es", "spa", "spanish", "español" -> "es"
            "fr", "fra", "fre", "french", "français" -> "fr"
            "de", "ger", "german", "deutsch" -> "de"
            "zh", "zh-cn", "zh-tw", "chinese", "中文" -> "zh"
            "ja", "jpn", "japanese", "日本語" -> "ja"
            "ar", "ara", "arabic", "العربية" -> "ar"
            "ru", "rus", "russian", "русский" -> "ru"
            else -> code.take(2).lowercase() // Default to first two chars
        }
    }
}

/**
 * Data class for detected language information
 */
data class DetectedLanguage(
    val code: String,
    val name: String,
    val confidence: Double
)

/**
 * Data class for language support information
 */
data class LanguageSupport(
    val name: String,
    val isSupported: Boolean,
    val hasTranslation: Boolean,
    val hasFullAnalysis: Boolean
)