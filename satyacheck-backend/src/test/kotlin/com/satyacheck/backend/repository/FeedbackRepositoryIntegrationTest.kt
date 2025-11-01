package com.satyacheck.backend.repository

import com.satyacheck.backend.service.feedback.Feedback
import com.satyacheck.backend.service.feedback.FeedbackType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for FeedbackRepository using TestContainers
 */
@DataMongoTest
@Import(MongoTestConfiguration::class)
class FeedbackRepositoryIntegrationTest {

    @Autowired
    private lateinit var feedbackRepository: FeedbackRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun setup() {
        // Clear the collection before each test
        mongoTemplate.dropCollection(Feedback::class.java)
    }

    @AfterEach
    fun cleanup() {
        // Clean up after each test
        mongoTemplate.dropCollection(Feedback::class.java)
    }

    @Test
    fun `should save and retrieve feedback`() {
        val feedback = Feedback(
            id = null, // Let MongoDB generate the ID
            userId = "user123",
            url = "https://example.com/article",
            type = FeedbackType.DISAGREE_WITH_VERDICT,
            comment = "I disagree with the verdict",
            originalVerdict = "CREDIBLE",
            submittedAt = Instant.now().toEpochMilli()
        )

        // Save the feedback
        val savedFeedback = feedbackRepository.save(feedback)

        // Verify the saved feedback has an ID
        assertNotNull(savedFeedback.id)

        // Retrieve the feedback by ID
        val retrieved = feedbackRepository.findById(savedFeedback.id!!).orElse(null)

        // Verify the retrieved feedback
        assertNotNull(retrieved)
        assertEquals(savedFeedback.id, retrieved.id)
        assertEquals("user123", retrieved.userId)
        assertEquals("https://example.com/article", retrieved.url)
        assertEquals(FeedbackType.DISAGREE_WITH_VERDICT, retrieved.type)
        assertEquals("I disagree with the verdict", retrieved.comment)
    }

    @Test
    fun `should find feedback by URL`() {
        // Create multiple feedback entries
        val feedback1 = Feedback(
            userId = "user1",
            url = "https://example.com/article1",
            type = FeedbackType.DISAGREE_WITH_VERDICT,
            comment = "Comment 1",
            originalVerdict = "CREDIBLE",
            submittedAt = Instant.now().toEpochMilli()
        )

        val feedback2 = Feedback(
            userId = "user2",
            url = "https://example.com/article1",
            type = FeedbackType.MISLEADING_CONTENT,
            comment = "Comment 2",
            originalVerdict = "CREDIBLE",
            submittedAt = Instant.now().toEpochMilli() - 1000
        )

        val feedback3 = Feedback(
            userId = "user3",
            url = "https://example.com/article2",
            type = FeedbackType.FAKE_NEWS,
            comment = "Comment 3",
            originalVerdict = "SUSPICIOUS",
            submittedAt = Instant.now().toEpochMilli() - 2000
        )

        // Save all feedback
        feedbackRepository.save(feedback1)
        feedbackRepository.save(feedback2)
        feedbackRepository.save(feedback3)

        // Find feedback by URL
        val resultsForArticle1 = feedbackRepository.findByUrl("https://example.com/article1")
        val resultsForArticle2 = feedbackRepository.findByUrl("https://example.com/article2")

        // Verify results
        assertEquals(2, resultsForArticle1.size)
        assertEquals(1, resultsForArticle2.size)

        // Check that the results match the expected feedback
        assertTrue(resultsForArticle1.any { it.userId == "user1" })
        assertTrue(resultsForArticle1.any { it.userId == "user2" })
        assertTrue(resultsForArticle2.any { it.userId == "user3" })
    }

    @Test
    fun `should find feedback by user ID`() {
        // Create multiple feedback entries
        val feedback1 = Feedback(
            userId = "user123",
            url = "https://example.com/article1",
            type = FeedbackType.DISAGREE_WITH_VERDICT,
            comment = "Comment 1",
            originalVerdict = "CREDIBLE",
            submittedAt = Instant.now().toEpochMilli()
        )

        val feedback2 = Feedback(
            userId = "user123",
            url = "https://example.com/article2",
            type = FeedbackType.MISLEADING_CONTENT,
            comment = "Comment 2",
            originalVerdict = "CREDIBLE",
            submittedAt = Instant.now().toEpochMilli() - 1000
        )

        val feedback3 = Feedback(
            userId = "user456",
            url = "https://example.com/article3",
            type = FeedbackType.FAKE_NEWS,
            comment = "Comment 3",
            originalVerdict = "SUSPICIOUS",
            submittedAt = Instant.now().toEpochMilli() - 2000
        )

        // Save all feedback
        feedbackRepository.save(feedback1)
        feedbackRepository.save(feedback2)
        feedbackRepository.save(feedback3)

        // Find feedback by user ID
        val resultsForUser123 = feedbackRepository.findByUserId("user123")
        val resultsForUser456 = feedbackRepository.findByUserId("user456")

        // Verify results
        assertEquals(2, resultsForUser123.size)
        assertEquals(1, resultsForUser456.size)

        // Check that the results match the expected feedback
        assertTrue(resultsForUser123.all { it.userId == "user123" })
        assertTrue(resultsForUser456.all { it.userId == "user456" })
    }

    @Test
    fun `should find top 10 by order of submission date`() {
        // Create multiple feedback entries with different timestamps
        val feedbackEntries = (1..15).map { i ->
            Feedback(
                userId = "user$i",
                url = "https://example.com/article$i",
                type = FeedbackType.values()[i % FeedbackType.values().size],
                comment = "Comment $i",
                originalVerdict = if (i % 2 == 0) "CREDIBLE" else "SUSPICIOUS",
                submittedAt = Instant.now().toEpochMilli() - (i * 1000L)
            )
        }

        // Save all feedback
        feedbackEntries.forEach { feedbackRepository.save(it) }

        // Find top 10 feedback by submission date
        val topResults = feedbackRepository.findTop10ByOrderBySubmittedAtDesc()

        // Verify we get exactly 10 results
        assertEquals(10, topResults.size)

        // Verify they are in descending order by submittedAt
        val timestamps = topResults.map { it.submittedAt }
        val sortedTimestamps = timestamps.sortedDescending()
        assertEquals(sortedTimestamps, timestamps)

        // Verify we got the most recent 10 feedbacks (user1 to user10)
        val userIds = topResults.map { it.userId }.toSet()
        (1..10).forEach { i ->
            assertTrue(userIds.contains("user$i"))
        }
    }

    @Test
    fun `should count feedback by type`() {
        // Create feedback with different types
        val feedbackEntries = listOf(
            Feedback(
                userId = "user1",
                url = "https://example.com/article1",
                type = FeedbackType.DISAGREE_WITH_VERDICT,
                comment = "Comment 1",
                originalVerdict = "CREDIBLE",
                submittedAt = Instant.now().toEpochMilli()
            ),
            Feedback(
                userId = "user2",
                url = "https://example.com/article2",
                type = FeedbackType.DISAGREE_WITH_VERDICT,
                comment = "Comment 2",
                originalVerdict = "CREDIBLE",
                submittedAt = Instant.now().toEpochMilli() - 1000
            ),
            Feedback(
                userId = "user3",
                url = "https://example.com/article3",
                type = FeedbackType.FAKE_NEWS,
                comment = "Comment 3",
                originalVerdict = "SUSPICIOUS",
                submittedAt = Instant.now().toEpochMilli() - 2000
            ),
            Feedback(
                userId = "user4",
                url = "https://example.com/article4",
                type = FeedbackType.MISLEADING_CONTENT,
                comment = "Comment 4",
                originalVerdict = "CREDIBLE",
                submittedAt = Instant.now().toEpochMilli() - 3000
            )
        )

        // Save all feedback
        feedbackEntries.forEach { feedbackRepository.save(it) }

        // Count feedback by type
        val typeCount = feedbackRepository.countByType()

        // Verify counts
        assertEquals(3, typeCount.size)
        val disagreeCount = typeCount.find { it.type == FeedbackType.DISAGREE_WITH_VERDICT.name }
        val fakeNewsCount = typeCount.find { it.type == FeedbackType.FAKE_NEWS.name }
        val misleadingCount = typeCount.find { it.type == FeedbackType.MISLEADING_CONTENT.name }

        assertNotNull(disagreeCount)
        assertNotNull(fakeNewsCount)
        assertNotNull(misleadingCount)
        assertEquals(2L, disagreeCount.count)
        assertEquals(1L, fakeNewsCount.count)
        assertEquals(1L, misleadingCount.count)
    }

    @Test
    fun `should count feedback by URL`() {
        // Create feedback with different URLs
        val feedbackEntries = listOf(
            Feedback(
                userId = "user1",
                url = "https://example.com/article1",
                type = FeedbackType.DISAGREE_WITH_VERDICT,
                comment = "Comment 1",
                originalVerdict = "CREDIBLE",
                submittedAt = Instant.now().toEpochMilli()
            ),
            Feedback(
                userId = "user2",
                url = "https://example.com/article1",
                type = FeedbackType.MISLEADING_CONTENT,
                comment = "Comment 2",
                originalVerdict = "CREDIBLE",
                submittedAt = Instant.now().toEpochMilli() - 1000
            ),
            Feedback(
                userId = "user3",
                url = "https://example.com/article1",
                type = FeedbackType.FAKE_NEWS,
                comment = "Comment 3",
                originalVerdict = "SUSPICIOUS",
                submittedAt = Instant.now().toEpochMilli() - 2000
            ),
            Feedback(
                userId = "user4",
                url = "https://example.com/article2",
                type = FeedbackType.MISLEADING_CONTENT,
                comment = "Comment 4",
                originalVerdict = "CREDIBLE",
                submittedAt = Instant.now().toEpochMilli() - 3000
            ),
            Feedback(
                userId = "user5",
                url = "https://example.com/article3",
                type = FeedbackType.DISAGREE_WITH_VERDICT,
                comment = "Comment 5",
                originalVerdict = "CREDIBLE",
                submittedAt = Instant.now().toEpochMilli() - 4000
            )
        )

        // Save all feedback
        feedbackEntries.forEach { feedbackRepository.save(it) }

        // Count feedback by URL with limit of 2
        val urlCount = feedbackRepository.countByUrl(2)

        // Verify we get at most 2 results
        assertEquals(2, urlCount.size)

        // Verify the counts are correct and in descending order
        assertEquals("https://example.com/article1", urlCount[0].url)
        assertEquals(3L, urlCount[0].count)
        assertEquals("https://example.com/article2", urlCount[1].url)
        assertEquals(1L, urlCount[1].count)
    }
}