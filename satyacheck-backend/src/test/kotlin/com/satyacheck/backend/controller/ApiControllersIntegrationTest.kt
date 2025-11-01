package com.satyacheck.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.satyacheck.backend.model.AnalysisRequest
import com.satyacheck.backend.model.BulkAnalysisRequest
import com.satyacheck.backend.model.FeedbackRequest
import com.satyacheck.backend.service.analysis.BulkAnalysisService
import com.satyacheck.backend.service.feedback.FeedbackService
import com.satyacheck.backend.service.web.UrlAnalysisService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*

/**
 * Integration tests for API Controllers
 */
class ApiControllersIntegrationTest {
    
    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper
    private lateinit var urlAnalysisService: UrlAnalysisService
    private lateinit var bulkAnalysisService: BulkAnalysisService
    private lateinit var feedbackService: FeedbackService
    
    @BeforeEach
    fun setup() {
        urlAnalysisService = mockk()
        bulkAnalysisService = mockk()
        feedbackService = mockk()
        objectMapper = ObjectMapper()
        
        val urlAnalysisController = UrlAnalysisController(urlAnalysisService)
        val bulkAnalysisController = BulkAnalysisController(bulkAnalysisService)
        val feedbackController = FeedbackController(feedbackService)
        
        mockMvc = MockMvcBuilders.standaloneSetup(
            urlAnalysisController,
            bulkAnalysisController,
            feedbackController
        ).build()
    }
    
    @Test
    fun `analyze URL endpoint should return analysis result`() {
        val analysisRequest = AnalysisRequest(url = "https://example.com/article")
        val analysisResult = mockk<UrlAnalysisResult>(relaxed = true) {
            every { url } returns "https://example.com/article"
            every { title } returns "Example Article"
            every { domain } returns "example.com"
            every { contentAnalysis?.verdict } returns "CREDIBLE"
            every { contentAnalysis?.confidence } returns 0.85
        }
        
        every { urlAnalysisService.analyzeUrl(any()) } returns analysisResult
        
        mockMvc.perform(
            post("/api/v1/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(analysisRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.url").value("https://example.com/article"))
            .andExpect(jsonPath("$.title").value("Example Article"))
            .andExpect(jsonPath("$.domain").value("example.com"))
            .andExpect(jsonPath("$.contentAnalysis.verdict").value("CREDIBLE"))
            .andExpect(jsonPath("$.contentAnalysis.confidence").value(0.85))
        
        verify { urlAnalysisService.analyzeUrl("https://example.com/article") }
    }
    
    @Test
    fun `submit bulk analysis endpoint should return bulk request details`() {
        val bulkRequest = BulkAnalysisRequest(
            userId = "user123",
            urls = listOf("https://example.com/1", "https://example.com/2"),
            notificationToken = "token123"
        )
        
        val bulkAnalysis = mockk<BulkAnalysis>(relaxed = true) {
            every { id } returns "bulk123"
            every { userId } returns "user123"
            every { urls } returns listOf("https://example.com/1", "https://example.com/2")
            every { status } returns BulkAnalysisStatus.PENDING
        }
        
        every { 
            bulkAnalysisService.submitBulkAnalysis(any(), any(), any()) 
        } returns bulkAnalysis
        
        mockMvc.perform(
            post("/api/v1/analyze/bulk")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest))
        )
            .andExpect(status().isAccepted)
            .andExpect(jsonPath("$.id").value("bulk123"))
            .andExpect(jsonPath("$.userId").value("user123"))
            .andExpect(jsonPath("$.urls").isArray)
            .andExpect(jsonPath("$.urls.length()").value(2))
            .andExpect(jsonPath("$.status").value("PENDING"))
        
        verify { 
            bulkAnalysisService.submitBulkAnalysis(
                userId = "user123", 
                urls = listOf("https://example.com/1", "https://example.com/2"),
                notificationToken = "token123"
            ) 
        }
    }
    
    @Test
    fun `get bulk analysis endpoint should return analysis details`() {
        val bulkId = "bulk123"
        
        val bulkAnalysis = mockk<BulkAnalysis>(relaxed = true) {
            every { id } returns bulkId
            every { userId } returns "user123"
            every { urls } returns listOf("https://example.com/1", "https://example.com/2")
            every { status } returns BulkAnalysisStatus.COMPLETED
            every { results } returns mapOf(
                "https://example.com/1" to "CREDIBLE",
                "https://example.com/2" to "SUSPICIOUS"
            )
        }
        
        every { bulkAnalysisService.getBulkAnalysis(bulkId) } returns bulkAnalysis
        
        mockMvc.perform(get("/api/v1/analyze/bulk/{bulkId}", bulkId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(bulkId))
            .andExpect(jsonPath("$.userId").value("user123"))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.results").isMap)
            .andExpect(jsonPath("$.results.['https://example.com/1']").value("CREDIBLE"))
            .andExpect(jsonPath("$.results.['https://example.com/2']").value("SUSPICIOUS"))
        
        verify { bulkAnalysisService.getBulkAnalysis(bulkId) }
    }
    
    @Test
    fun `get user bulk analyses endpoint should return user analyses`() {
        val userId = "user123"
        
        val bulkAnalyses = listOf(
            mockk<BulkAnalysis>(relaxed = true) {
                every { id } returns "bulk1"
                every { userId } returns userId
                every { status } returns BulkAnalysisStatus.COMPLETED
            },
            mockk<BulkAnalysis>(relaxed = true) {
                every { id } returns "bulk2"
                every { userId } returns userId
                every { status } returns BulkAnalysisStatus.PENDING
            }
        )
        
        every { bulkAnalysisService.getUserBulkAnalyses(userId) } returns bulkAnalyses
        
        mockMvc.perform(get("/api/v1/analyze/bulk/user/{userId}", userId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("bulk1"))
            .andExpect(jsonPath("$[0].userId").value(userId))
            .andExpect(jsonPath("$[0].status").value("COMPLETED"))
            .andExpect(jsonPath("$[1].id").value("bulk2"))
            .andExpect(jsonPath("$[1].status").value("PENDING"))
        
        verify { bulkAnalysisService.getUserBulkAnalyses(userId) }
    }
    
    @Test
    fun `submit feedback endpoint should return feedback details`() {
        val feedbackRequest = FeedbackRequest(
            userId = "user123",
            url = "https://example.com/article",
            type = FeedbackType.DISAGREE_WITH_VERDICT,
            comment = "I disagree with this verdict",
            originalVerdict = "CREDIBLE"
        )
        
        val feedback = mockk<Feedback>(relaxed = true) {
            every { id } returns "feedback123"
            every { userId } returns "user123"
            every { url } returns "https://example.com/article"
            every { type } returns FeedbackType.DISAGREE_WITH_VERDICT
            every { comment } returns "I disagree with this verdict"
            every { originalVerdict } returns "CREDIBLE"
        }
        
        every { 
            feedbackService.submitFeedback(any(), any(), any(), any(), any()) 
        } returns feedback
        
        mockMvc.perform(
            post("/api/v1/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feedbackRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value("feedback123"))
            .andExpect(jsonPath("$.userId").value("user123"))
            .andExpect(jsonPath("$.url").value("https://example.com/article"))
            .andExpect(jsonPath("$.type").value("DISAGREE_WITH_VERDICT"))
            .andExpect(jsonPath("$.comment").value("I disagree with this verdict"))
            .andExpect(jsonPath("$.originalVerdict").value("CREDIBLE"))
        
        verify { 
            feedbackService.submitFeedback(
                userId = "user123",
                url = "https://example.com/article",
                type = FeedbackType.DISAGREE_WITH_VERDICT,
                comment = "I disagree with this verdict",
                originalVerdict = "CREDIBLE"
            ) 
        }
    }
    
    @Test
    fun `get feedback for URL endpoint should return feedback list`() {
        val url = "https://example.com/article"
        
        val feedbackList = listOf(
            mockk<Feedback>(relaxed = true) {
                every { id } returns "feedback1"
                every { userId } returns "user1"
                every { url } returns url
                every { type } returns FeedbackType.DISAGREE_WITH_VERDICT
            },
            mockk<Feedback>(relaxed = true) {
                every { id } returns "feedback2"
                every { userId } returns "user2"
                every { url } returns url
                every { type } returns FeedbackType.MISLEADING_CONTENT
            }
        )
        
        every { feedbackService.getUrlFeedback(url) } returns feedbackList
        
        mockMvc.perform(get("/api/v1/feedback/url")
            .param("url", url))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("feedback1"))
            .andExpect(jsonPath("$[0].userId").value("user1"))
            .andExpect(jsonPath("$[0].type").value("DISAGREE_WITH_VERDICT"))
            .andExpect(jsonPath("$[1].id").value("feedback2"))
            .andExpect(jsonPath("$[1].type").value("MISLEADING_CONTENT"))
        
        verify { feedbackService.getUrlFeedback(url) }
    }
    
    @Test
    fun `get user feedback endpoint should return feedback list`() {
        val userId = "user123"
        
        val feedbackList = listOf(
            mockk<Feedback>(relaxed = true) {
                every { id } returns "feedback1"
                every { userId } returns userId
                every { url } returns "https://example.com/article1"
                every { type } returns FeedbackType.DISAGREE_WITH_VERDICT
            },
            mockk<Feedback>(relaxed = true) {
                every { id } returns "feedback2"
                every { userId } returns userId
                every { url } returns "https://example.com/article2"
                every { type } returns FeedbackType.MISLEADING_CONTENT
            }
        )
        
        every { feedbackService.getUserFeedback(userId) } returns feedbackList
        
        mockMvc.perform(get("/api/v1/feedback/user/{userId}", userId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("feedback1"))
            .andExpect(jsonPath("$[0].url").value("https://example.com/article1"))
            .andExpect(jsonPath("$[0].type").value("DISAGREE_WITH_VERDICT"))
            .andExpect(jsonPath("$[1].id").value("feedback2"))
            .andExpect(jsonPath("$[1].url").value("https://example.com/article2"))
        
        verify { feedbackService.getUserFeedback(userId) }
    }
    
    @Test
    fun `get feedback statistics endpoint should return statistics`() {
        val limit = 5
        
        val feedbackStats = mockk<FeedbackStatistics>(relaxed = true) {
            every { byType } returns listOf(
                FeedbackTypeCount(FeedbackType.DISAGREE_WITH_VERDICT.name, 10),
                FeedbackTypeCount(FeedbackType.MISLEADING_CONTENT.name, 5)
            )
            every { topUrls } returns listOf(
                FeedbackUrlCount("https://example.com/article1", 8),
                FeedbackUrlCount("https://example.com/article2", 5)
            )
        }
        
        every { feedbackService.getFeedbackStatistics(limit) } returns feedbackStats
        
        mockMvc.perform(get("/api/v1/feedback/statistics")
            .param("limit", limit.toString()))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.byType").isArray)
            .andExpect(jsonPath("$.byType.length()").value(2))
            .andExpect(jsonPath("$.byType[0].type").value("DISAGREE_WITH_VERDICT"))
            .andExpect(jsonPath("$.byType[0].count").value(10))
            .andExpect(jsonPath("$.topUrls").isArray)
            .andExpect(jsonPath("$.topUrls.length()").value(2))
            .andExpect(jsonPath("$.topUrls[0].url").value("https://example.com/article1"))
            .andExpect(jsonPath("$.topUrls[0].count").value(8))
        
        verify { feedbackService.getFeedbackStatistics(limit) }
    }
}