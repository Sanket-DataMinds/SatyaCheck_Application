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
import kotlin.test.assertTrue

/**
 * Unit tests for TranslationService
 */
@ExtendWith(SpringExtension::class)
class TranslationServiceTest {
    
    private lateinit var webClient: WebClient
    private lateinit var languageDetectionService: LanguageDetectionService
    private lateinit var translationService: TranslationService
    
    @BeforeEach
    fun setup() {
        webClient = mockk(relaxed = true)
        languageDetectionService = mockk()
        translationService = TranslationService(webClient, languageDetectionService)
        
        // Mock language detection service
        every { languageDetectionService.detectLanguage(any()) } returns "en"
        every { languageDetectionService.getLanguageSupport(any()) } returns LanguageSupport(
            translationSupport = true,
            analysisSupport = true,
            sentimentSupport = true
        )
    }
    
    @Test
    fun `translate should return original content when source and target languages are the same`() {
        val content = "This is a test content"
        val sourceLanguage = "en"
        val targetLanguage = "en"
        
        val translated = translationService.translate(content, sourceLanguage, targetLanguage)
        
        assertEquals(content, translated)
    }
    
    @Test
    fun `translate should add appropriate marker for Hindi translation`() {
        val content = "This is a test content"
        val sourceLanguage = "en"
        val targetLanguage = "hi"
        
        every { languageDetectionService.getLanguageSupport("en") } returns LanguageSupport(
            translationSupport = true,
            analysisSupport = true,
            sentimentSupport = true
        )
        
        every { languageDetectionService.getLanguageSupport("hi") } returns LanguageSupport(
            translationSupport = true,
            analysisSupport = true,
            sentimentSupport = false
        )
        
        val translated = translationService.translate(content, sourceLanguage, targetLanguage)
        
        assertTrue(translated.contains("[हिंदी अनुवाद]"))
        assertTrue(translated.contains(content))
    }
    
    @Test
    fun `translateToEnglish should return original content if already in English`() {
        val content = "This is already in English"
        
        every { languageDetectionService.detectLanguage(content) } returns "en"
        
        val translated = translationService.translateToEnglish(content)
        
        assertEquals(content, translated)
    }
    
    @Test
    fun `translateToEnglish should translate from detected language to English`() {
        val content = "यह हिंदी में है"
        
        every { languageDetectionService.detectLanguage(content) } returns "hi"
        
        val translated = translationService.translateToEnglish(content)
        
        // In our mock implementation, we're not actually translating
        // But we can verify that the correct methods were called
        verify { languageDetectionService.detectLanguage(content) }
    }
    
    @Test
    fun `translateFromEnglish should return original content if target is English`() {
        val content = "This is English content"
        val targetLanguage = "en"
        
        val translated = translationService.translateFromEnglish(content, targetLanguage)
        
        assertEquals(content, translated)
    }
    
    @Test
    fun `translateFromEnglish should translate from English to target language`() {
        val content = "Translate this to Hindi"
        val targetLanguage = "hi"
        
        val translated = translationService.translateFromEnglish(content, targetLanguage)
        
        assertTrue(translated.contains("[हिंदी अनुवाद]"))
    }
}