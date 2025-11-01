package com.satyacheck.backend.service.feedback

import com.satyacheck.backend.repository.FeedbackRepository
import com.satyacheck.backend.service.NotificationService
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for FeedbackService
 */
@ExtendWith(SpringExtension::class)
class FeedbackServiceTest {

    private lateinit var feedbackRepository: FeedbackRepository
    private lateinit var notificationService: NotificationService
    private lateinit var feedbackService: FeedbackService

    @BeforeEach
    fun setup() {
        feedbackRepository = mockk()
        notificationService = mockk(relaxUnitFun = true)
        feedbackService = FeedbackService(feedbackRepository, notificationService)
        
        // Mock repository save
        every { feedbackRepository.save(any()) } answers { firstArg() }
        
        // Mock finding by URL
        every { feedbackRepository.findByUrl(any()) } returns emptyList()
    }

    @Test
    fun `submitFeedback should save and return feedback`() {
        val userId = "user123"
        val url = "https://example.com/article"
        val type = FeedbackType.DISAGREE_WITH_VERDICT
        val comment = "I think this content is actually misleading"
        val originalVerdict = "CREDIBLE"
        
        val result = feedbackService.submitFeedback(
            userId = userId,
            url = url,
            type = type,
            comment = comment,
            originalVerdict = originalVerdict
        )
        
        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(url, result.url)
        assertEquals(type, result.type)
        assertEquals(comment, result.comment)
        assertEquals(originalVerdict, result.originalVerdict)
        assertNotNull(result.submittedAt)
        
        // Verify repository was called
        verify { feedbackRepository.save(any()) }
        
        // Verify notification service was called
        verify { notificationService.notifyNewFeedback(result) }
    }
    
    @Test
    fun `getUrlFeedback should return feedback for a URL`() {
        val url = "https://example.com/article"
        val feedbackList = listOf(
            Feedback(
                id = "id1",
                userId = "user1",
                url = url,
                type = FeedbackType.DISAGREE_WITH_VERDICT,
                comment = "I disagree",
                originalVerdict = "CREDIBLE",
                submittedAt = Instant.now().toEpochMilli()
            ),
            Feedback(
                id = "id2",
                userId = "user2",
                url = url,
                type = FeedbackType.MISLEADING_CONTENT,
                comment = "This is misleading",
                originalVerdict = "CREDIBLE",
                submittedAt = Instant.now().toEpochMilli() - 1000
            )
        )
        
        every { feedbackRepository.findByUrl(url) } returns feedbackList
        
        val results = feedbackService.getUrlFeedback(url)
        
        assertEquals(2, results.size)
        assertEquals(feedbackList[0].id, results[0].id)
        assertEquals(feedbackList[1].id, results[1].id)
        
        verify { feedbackRepository.findByUrl(url) }
    }
    
    @Test
    fun `getUserFeedback should return feedback by user`() {
        val userId = "user123"
        val feedbackList = listOf(
            Feedback(
                id = "id1",
                userId = userId,
                url = "https://example.com/article1",
                type = FeedbackType.DISAGREE_WITH_VERDICT,
                comment = "I disagree",
                originalVerdict = "CREDIBLE",
                submittedAt = Instant.now().toEpochMilli()
            ),
            Feedback(
                id = "id2",
                userId = userId,
                url = "https://example.com/article2",
                type = FeedbackType.FAKE_NEWS,
                comment = "This is fake",
                originalVerdict = "SUSPICIOUS",
                submittedAt = Instant.now().toEpochMilli() - 1000
            )
        )
        
        every { feedbackRepository.findByUserId(userId) } returns feedbackList
        
        val results = feedbackService.getUserFeedback(userId)
        
        assertEquals(2, results.size)
        assertEquals(feedbackList[0].id, results[0].id)
        assertEquals(feedbackList[1].id, results[1].id)
        
        verify { feedbackRepository.findByUserId(userId) }
    }
    
    @Test
    fun `getRecentFeedback should return recent feedback`() {
        val limit = 10
        val feedbackList = listOf(
            Feedback(
                id = "id1",
                userId = "user1",
                url = "https://example.com/article1",
                type = FeedbackType.DISAGREE_WITH_VERDICT,
                comment = "I disagree",
                originalVerdict = "CREDIBLE",
                submittedAt = Instant.now().toEpochMilli()
            ),
            Feedback(
                id = "id2",
                userId = "user2",
                url = "https://example.com/article2",
                type = FeedbackType.FAKE_NEWS,
                comment = "This is fake",
                originalVerdict = "SUSPICIOUS",
                submittedAt = Instant.now().toEpochMilli() - 1000
            )
        )
        
        every { feedbackRepository.findTop10ByOrderBySubmittedAtDesc() } returns feedbackList
        
        val results = feedbackService.getRecentFeedback(limit)
        
        assertEquals(2, results.size)
        assertEquals(feedbackList[0].id, results[0].id)
        assertEquals(feedbackList[1].id, results[1].id)
        
        verify { feedbackRepository.findTop10ByOrderBySubmittedAtDesc() }
    }
    
    @Test
    fun `getFeedbackStatistics should return aggregated statistics`() {
        val typeStats = mapOf(
            FeedbackType.DISAGREE_WITH_VERDICT to 10L,
            FeedbackType.FAKE_NEWS to 5L,
            FeedbackType.MISLEADING_CONTENT to 15L
        )
        
        val urlStats = mapOf(
            "https://example.com/article1" to 8L,
            "https://example.com/article2" to 12L,
            "https://example.com/article3" to 10L
        )
        
        every { feedbackRepository.countByType() } returns typeStats.entries.map { 
            FeedbackTypeCount(it.key.name, it.value) 
        }
        
        every { feedbackRepository.countByUrl(any()) } returns urlStats.entries.map {
            FeedbackUrlCount(it.key, it.value)
        }
        
        val result = feedbackService.getFeedbackStatistics(5)
        
        assertNotNull(result)
        assertEquals(3, result.byType.size)
        assertEquals(3, result.topUrls.size)
        assertEquals(10L, result.byType.find { it.type == FeedbackType.DISAGREE_WITH_VERDICT.name }?.count)
        assertEquals(12L, result.topUrls.find { it.url == "https://example.com/article2" }?.count)
        
        verify { feedbackRepository.countByType() }
        verify { feedbackRepository.countByUrl(5) }
    }
}