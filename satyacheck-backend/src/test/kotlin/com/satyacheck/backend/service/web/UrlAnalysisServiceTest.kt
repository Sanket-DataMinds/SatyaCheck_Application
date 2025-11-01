package com.satyacheck.backend.service.web

import com.satyacheck.backend.service.EnhancedAnalysisService
import com.satyacheck.backend.service.TranslationService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for UrlAnalysisService
 */
@ExtendWith(SpringExtension::class)
class UrlAnalysisServiceTest {
    
    private lateinit var webContentFetcherService: WebContentFetcherService
    private lateinit var enhancedAnalysisService: EnhancedAnalysisService
    private lateinit var translationService: TranslationService
    private lateinit var urlAnalysisService: UrlAnalysisService
    
    @BeforeEach
    fun setup() {
        webContentFetcherService = mockk()
        enhancedAnalysisService = mockk()
        translationService = mockk()
        urlAnalysisService = UrlAnalysisService(webContentFetcherService, enhancedAnalysisService, translationService)
        
        // Mock web content fetcher
        every { 
            webContentFetcherService.fetchAndExtractContent(any()) 
        } returns WebContent(
            url = "https://example.com/article",
            title = "Example Article",
            description = "An example article for testing",
            mainContent = "This is the main content of the article.",
            language = "en",
            timestamp = System.currentTimeMillis()
        )
        
        // Mock enhanced analysis service
        every { 
            enhancedAnalysisService.analyzeComprehensively(any()) 
        } returns mapOf(
            "verdict" to "CREDIBLE",
            "confidence" to 0.85,
            "explanation" to "The content appears to be credible."
        )
        
        // Mock translation service
        every { 
            translationService.translateToEnglish(any()) 
        } answers { firstArg() }
    }
    
    @Test
    fun `analyzeUrl should extract and analyze content correctly`() {
        val url = "https://example.com/article"
        
        val result = urlAnalysisService.analyzeUrl(url)
        
        assertNotNull(result)
        assertEquals("https://example.com/article", result.url)
        assertEquals("Example Article", result.title)
        assertEquals("An example article for testing", result.description)
        assertEquals("example.com", result.domain)
        assertEquals("en", result.language)
        assertFalse(result.wasTranslated)
        assertNotNull(result.contentAnalysis)
        
        // Verify method calls
        verify { webContentFetcherService.fetchAndExtractContent(url) }
        verify { enhancedAnalysisService.analyzeComprehensively(any()) }
    }
    
    @Test
    fun `analyzeUrl should translate non-English content`() {
        val url = "https://example.com/hindi-article"
        
        // Mock response for Hindi content
        every { 
            webContentFetcherService.fetchAndExtractContent(url) 
        } returns WebContent(
            url = "https://example.com/hindi-article",
            title = "हिंदी लेख",
            description = "परीक्षण के लिए एक हिंदी लेख",
            mainContent = "यह लेख की मुख्य सामग्री है।",
            language = "hi",
            timestamp = System.currentTimeMillis()
        )
        
        val result = urlAnalysisService.analyzeUrl(url)
        
        assertNotNull(result)
        assertEquals("hi", result.language)
        assertTrue(result.wasTranslated)
        
        // Verify translation was called
        verify { translationService.translateToEnglish(any()) }
    }
    
    @Test
    fun `analyzeUrl should evaluate source credibility`() {
        val url = "https://bbc.com/news/article"
        
        every { 
            webContentFetcherService.fetchAndExtractContent(url) 
        } returns WebContent(
            url = "https://bbc.com/news/article",
            title = "BBC News Article",
            description = "A news article from BBC",
            mainContent = "This is the main content of the BBC article.",
            language = "en",
            timestamp = System.currentTimeMillis()
        )
        
        val result = urlAnalysisService.analyzeUrl(url)
        
        assertEquals("bbc.com", result.domain)
        assertEquals(CredibilityLevel.HIGH, result.sourceCredibility.credibilityLevel)
        assertTrue(result.sourceCredibility.knownTrustedSource)
        assertFalse(result.sourceCredibility.knownFakeSource)
    }
    
    @Test
    fun `analyzeUrl should identify known fake sources`() {
        val url = "https://fakeindianews.com/article"
        
        every { 
            webContentFetcherService.fetchAndExtractContent(url) 
        } returns WebContent(
            url = "https://fakeindianews.com/article",
            title = "Fake News Article",
            description = "A fake news article",
            mainContent = "This is the main content of the fake article.",
            language = "en",
            timestamp = System.currentTimeMillis()
        )
        
        val result = urlAnalysisService.analyzeUrl(url)
        
        assertEquals("fakeindianews.com", result.domain)
        assertEquals(CredibilityLevel.LOW, result.sourceCredibility.credibilityLevel)
        assertFalse(result.sourceCredibility.knownTrustedSource)
        assertTrue(result.sourceCredibility.knownFakeSource)
    }
}