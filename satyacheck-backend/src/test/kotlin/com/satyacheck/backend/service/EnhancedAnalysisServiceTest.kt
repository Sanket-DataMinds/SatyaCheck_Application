package com.satyacheck.backend.service

import com.satyacheck.backend.model.dto.AnalysisResult
import com.satyacheck.backend.model.dto.ContentCategory
import com.satyacheck.backend.model.dto.MisinformationAnalysis
import com.satyacheck.backend.service.api.ContentCategorizationService
import com.satyacheck.backend.service.api.GeminiService
import com.satyacheck.backend.service.api.NaturalLanguageService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class EnhancedAnalysisServiceTest {
    
    private lateinit var enhancedAnalysisService: EnhancedAnalysisServiceImpl
    private lateinit var analysisService: AnalysisService
    private lateinit var geminiService: GeminiService
    private lateinit var contentCategorizationService: ContentCategorizationService
    private lateinit var naturalLanguageService: NaturalLanguageService
    
    @BeforeEach
    fun setUp() {
        analysisService = mock()
        geminiService = mock()
        contentCategorizationService = mock()
        naturalLanguageService = mock()
        
        enhancedAnalysisService = EnhancedAnalysisServiceImpl(
            analysisService,
            geminiService,
            contentCategorizationService,
            naturalLanguageService
        )
        
        // Set up mock responses
        runBlocking {
            `when`(analysisService.analyzeContent(any(), any())).thenReturn(
                AnalysisResult(
                    verified = true,
                    confidenceScore = 0.85,
                    issues = emptyList(),
                    sourcesReferences = listOf("Source 1", "Source 2"),
                    explanation = "Test explanation",
                    recommendedActions = listOf("Action 1")
                )
            )
            
            `when`(contentCategorizationService.categorizeContent(any(), any())).thenReturn(
                ContentCategory(
                    primaryCategory = "NEWS",
                    subCategories = listOf("Politics", "International"),
                    confidence = 0.9,
                    tags = listOf("election", "democracy")
                )
            )
            
            `when`(contentCategorizationService.extractTopics(any(), any())).thenReturn(
                listOf(
                    com.satyacheck.backend.model.dto.ExtractedTopic(
                        topic = "Politics",
                        relevance = 0.95,
                        subtopics = listOf("Election", "Democracy"),
                        keywords = listOf("vote", "election", "democracy")
                    )
                )
            )
            
            `when`(naturalLanguageService.extractEntities(any())).thenReturn(
                listOf(
                    mapOf(
                        "name" to "John Doe",
                        "type" to "PERSON",
                        "salience" to 0.8
                    )
                )
            )
            
            `when`(naturalLanguageService.analyzeSentiment(any())).thenReturn(
                mapOf(
                    "score" to 0.2,
                    "magnitude" to 0.6,
                    "language" to "en"
                )
            )
            
            `when`(geminiService.analyzeContentForMisinformation(any(), any())).thenReturn(
                MisinformationAnalysis(
                    isLikelyMisinformation = false,
                    confidenceScore = 0.9,
                    riskLevel = "LOW",
                    patterns = listOf("No concerning patterns detected"),
                    techniques = emptyList(),
                    explanation = "Content appears to be factual"
                )
            )
        }
    }
    
    @Test
    fun `comprehensive analysis returns complete results`() = runBlocking {
        // Given
        val testContent = "This is a test article about politics and elections."
        
        // When
        val result = enhancedAnalysisService.analyzeComprehensively(testContent, "en")
        
        // Then
        assertNotNull(result)
        assertEquals("NEWS", result.contentCategory?.primaryCategory)
        assertTrue(result.extractedTopics.isNotEmpty())
        assertEquals("Politics", result.extractedTopics.first().topic)
        assertTrue(result.namedEntities.isNotEmpty())
        assertEquals("John Doe", result.namedEntities.first()["name"])
        assertEquals(0.2, result.sentimentScore)
        assertTrue(result.factCheckResult.verified)
        assertEquals(0.85, result.factCheckResult.confidenceScore)
    }
    
    @Test
    fun `misinformation analysis returns specific patterns`() = runBlocking {
        // Given
        val testContent = "This is a test article about politics and elections."
        
        // When
        val result = enhancedAnalysisService.analyzeMisinformationPatterns(testContent, "en")
        
        // Then
        assertNotNull(result)
        assertTrue(result.factCheckResult.verified)
        assertEquals("No concerning patterns detected", result.misinformationPatterns?.firstOrNull())
        assertEquals("LOW", result.additionalContext["misinformationRisk"])
        assertEquals("NEWS", result.contentCategory?.primaryCategory)
    }
}