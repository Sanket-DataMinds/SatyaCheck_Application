package com.satyacheck.backend.service.web

import com.satyacheck.backend.service.LanguageDetectionService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for WebContentFetcherService
 */
@ExtendWith(SpringExtension::class)
class WebContentFetcherServiceTest {
    
    private lateinit var webClient: WebClient
    private lateinit var languageDetectionService: LanguageDetectionService
    private lateinit var webContentFetcherService: WebContentFetcherService
    
    @BeforeEach
    fun setup() {
        webClient = mockk()
        languageDetectionService = mockk()
        webContentFetcherService = WebContentFetcherService(webClient, languageDetectionService)
        
        // Mock language detection
        every { languageDetectionService.detectLanguage(any()) } returns "en"
        
        // Mock WebClient
        val requestHeadersUriSpec = mockk<WebClient.RequestHeadersUriSpec<*>>()
        val requestHeadersSpec = mockk<WebClient.RequestHeadersSpec<*>>()
        val responseSpec = mockk<WebClient.ResponseSpec>()
        
        every { webClient.get() } returns requestHeadersUriSpec
        every { requestHeadersUriSpec.uri(any<String>()) } returns requestHeadersSpec
        every { requestHeadersSpec.retrieve() } returns responseSpec
        every { responseSpec.bodyToMono(String::class.java) } returns Mono.just(
            """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="description" content="Test description">
                <title>Test Title</title>
            </head>
            <body>
                <article>
                    <h1>Test Article</h1>
                    <p>This is a test article content. It contains enough text to be considered the main content.</p>
                    <p>More paragraphs with additional content to make it substantial.</p>
                </article>
            </body>
            </html>
            """.trimIndent()
        )
    }
    
    @Test
    fun `fetchAndExtractContent should extract content correctly`() {
        val url = "https://example.com/article"
        
        val result = webContentFetcherService.fetchAndExtractContent(url)
        
        assertNotNull(result)
        assertEquals("Test Title", result.title)
        assertEquals("Test description", result.description)
        assertEquals("en", result.language)
        assertEquals(url, result.url)
        assertEquals("Test Article This is a test article content. It contains enough text to be considered the main content. More paragraphs with additional content to make it substantial.", result.mainContent)
    }
    
    @Test
    fun `fetchAndExtractContent should normalize URL`() {
        val url = "example.com/article"
        
        val result = webContentFetcherService.fetchAndExtractContent(url)
        
        assertEquals("https://example.com/article", result.url)
    }
    
    @Test
    fun `fetchAndExtractContent should use language detection`() {
        val url = "https://example.com/article"
        
        webContentFetcherService.fetchAndExtractContent(url)
        
        verify { languageDetectionService.detectLanguage(any()) }
    }
}