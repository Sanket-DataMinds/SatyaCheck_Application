package com.satyacheck.backend.controller

import com.satyacheck.backend.service.language.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.logging.Logger

/**
 * Controller for language-related operations
 */
@RestController
@RequestMapping("/api/v1/language")
class LanguageController(
    private val languageDetectionService: LanguageDetectionService,
    private val translationService: TranslationService
) {
    private val logger = Logger.getLogger(LanguageController::class.java.name)

    /**
     * Detect the language of a text
     */
    @PostMapping("/detect")
    suspend fun detectLanguage(@RequestParam text: String): ResponseEntity<DetectedLanguage> {
        logger.info("Detecting language for text: ${text.take(50)}...")
        val detected = languageDetectionService.detectLanguage(text)
        return ResponseEntity.ok(detected)
    }

    /**
     * Get support information for a language
     */
    @GetMapping("/support/{languageCode}")
    fun getLanguageSupport(@PathVariable languageCode: String): ResponseEntity<LanguageSupport> {
        val normalizedCode = languageDetectionService.normalizeLanguageCode(languageCode)
        val support = languageDetectionService.getLanguageSupport(normalizedCode)
        return ResponseEntity.ok(support)
    }

    /**
     * List all supported languages
     */
    @GetMapping("/supported")
    fun getAllSupportedLanguages(): ResponseEntity<Map<String, LanguageSupport>> {
        val languages = languageDetectionService.getAllSupportedLanguages()
        return ResponseEntity.ok(languages)
    }

    /**
     * List languages that support full analysis
     */
    @GetMapping("/full-analysis-languages")
    fun getFullAnalysisLanguages(): ResponseEntity<Map<String, LanguageSupport>> {
        val languages = languageDetectionService.getFullAnalysisLanguages()
        return ResponseEntity.ok(languages)
    }

    /**
     * Translate text to English
     */
    @PostMapping("/translate-to-english")
    suspend fun translateToEnglish(
        @RequestParam text: String,
        @RequestParam sourceLanguage: String
    ): ResponseEntity<TranslationResult> {
        val normalizedSource = languageDetectionService.normalizeLanguageCode(sourceLanguage)
        val translation = translationService.translateToEnglish(text, normalizedSource)
        return ResponseEntity.ok(translation)
    }

    /**
     * Translate text from English to another language
     */
    @PostMapping("/translate-from-english")
    suspend fun translateFromEnglish(
        @RequestParam text: String,
        @RequestParam targetLanguage: String
    ): ResponseEntity<TranslationResult> {
        val normalizedTarget = languageDetectionService.normalizeLanguageCode(targetLanguage)
        val translation = translationService.translateFromEnglish(text, normalizedTarget)
        return ResponseEntity.ok(translation)
    }

    /**
     * Translate text between any two languages
     */
    @PostMapping("/translate")
    suspend fun translate(
        @RequestParam text: String,
        @RequestParam sourceLanguage: String,
        @RequestParam targetLanguage: String
    ): ResponseEntity<TranslationResult> {
        val normalizedSource = languageDetectionService.normalizeLanguageCode(sourceLanguage)
        val normalizedTarget = languageDetectionService.normalizeLanguageCode(targetLanguage)
        
        if (!translationService.isTranslationSupported(normalizedSource, normalizedTarget)) {
            return ResponseEntity.badRequest().body(
                TranslationResult(
                    originalText = text,
                    translatedText = text,
                    sourceLanguage = normalizedSource,
                    targetLanguage = normalizedTarget,
                    characterCount = text.length,
                    error = "Translation not supported between these languages"
                )
            )
        }
        
        val translation = translationService.translate(text, normalizedSource, normalizedTarget)
        return ResponseEntity.ok(translation)
    }
}