package com.satyacheck.backend.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.WebClient
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for LanguageDetectionService
 */
@ExtendWith(SpringExtension::class)
class LanguageDetectionServiceTest {
    
    private lateinit var webClient: WebClient
    private lateinit var languageDetectionService: LanguageDetectionService
    
    @BeforeEach
    fun setup() {
        webClient = mockk(relaxed = true)
        languageDetectionService = LanguageDetectionService(webClient)
    }
    
    @Test
    fun `detectLanguage should detect English correctly`() {
        val content = "This is a sample text in English language."
        
        val detectedLanguage = languageDetectionService.detectLanguage(content)
        
        assertEquals("en", detectedLanguage)
    }
    
    @Test
    fun `detectLanguage should detect Hindi correctly`() {
        val content = "यह हिंदी भाषा में एक नमूना पाठ है।"
        
        val detectedLanguage = languageDetectionService.detectLanguage(content)
        
        assertEquals("hi", detectedLanguage)
    }
    
    @Test
    fun `isLanguageSupported should return true for supported languages`() {
        assertTrue(languageDetectionService.isLanguageSupported("en"))
        assertTrue(languageDetectionService.isLanguageSupported("hi"))
    }
    
    @Test
    fun `isLanguageSupported should return false for unsupported languages`() {
        assertFalse(languageDetectionService.isLanguageSupported("xx"))
        assertFalse(languageDetectionService.isLanguageSupported(""))
    }
    
    @Test
    fun `getLanguageSupport should return correct support levels for English`() {
        val support = languageDetectionService.getLanguageSupport("en")
        
        assertTrue(support.translationSupport)
        assertTrue(support.analysisSupport)
        assertTrue(support.sentimentSupport)
    }
    
    @Test
    fun `getLanguageSupport should return correct support levels for Hindi`() {
        val support = languageDetectionService.getLanguageSupport("hi")
        
        assertTrue(support.translationSupport)
        assertTrue(support.analysisSupport)
        assertFalse(support.sentimentSupport)
    }
    
    @Test
    fun `getSupportedLanguages should return all supported languages`() {
        val supportedLanguages = languageDetectionService.getSupportedLanguages()
        
        assertTrue(supportedLanguages.containsKey("en"))
        assertTrue(supportedLanguages.containsKey("hi"))
        assertTrue(supportedLanguages.containsKey("mr"))
        assertEquals(10, supportedLanguages.size)
    }
}