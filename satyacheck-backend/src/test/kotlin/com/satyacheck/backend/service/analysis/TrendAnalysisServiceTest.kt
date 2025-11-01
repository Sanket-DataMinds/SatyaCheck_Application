package com.satyacheck.backend.service.analysis

import com.satyacheck.backend.repository.TrendRepository
import com.satyacheck.backend.repository.UrlAnalysisRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for TrendAnalysisService
 */
@ExtendWith(SpringExtension::class)
class TrendAnalysisServiceTest {
    
    private lateinit var urlAnalysisRepository: UrlAnalysisRepository
    private lateinit var trendRepository: TrendRepository
    private lateinit var trendAnalysisService: TrendAnalysisService
    
    @BeforeEach
    fun setup() {
        urlAnalysisRepository = mockk()
        trendRepository = mockk()
        trendAnalysisService = TrendAnalysisService(urlAnalysisRepository, trendRepository)
        
        // Mock repository save
        every { trendRepository.save(any()) } answers { firstArg() }
    }
    
    @Test
    fun `updateTrends should analyze trends and save them`() {
        // Mock timestamp for consistent testing
        val now = Instant.now()
        val oneDayAgo = now.minusSeconds(86400)
        val twoDaysAgo = now.minusSeconds(172800)
        val threeDaysAgo = now.minusSeconds(259200)
        
        // Mock domain frequency data
        val domainFrequency = listOf(
            DomainFrequency("example.com", 10),
            DomainFrequency("news.com", 8),
            DomainFrequency("blog.com", 5)
        )
        
        // Mock verdict distribution data
        val verdictDistribution = mapOf(
            "CREDIBLE" to 15,
            "SUSPICIOUS" to 5,
            "UNRELIABLE" to 3
        )
        
        // Mock topic frequency data
        val topicFrequency = listOf(
            TopicFrequency("politics", 12),
            TopicFrequency("health", 8),
            TopicFrequency("technology", 6)
        )
        
        // Setup repository mock responses
        every { urlAnalysisRepository.findMostFrequentDomains(any(), any()) } returns domainFrequency
        every { urlAnalysisRepository.getVerdictDistribution(any(), any()) } returns verdictDistribution.entries.map {
            VerdictCount(it.key, it.value)
        }
        every { urlAnalysisRepository.findMostFrequentTopics(any(), any()) } returns topicFrequency
        
        // Call the method to test
        val result = trendAnalysisService.updateTrends()
        
        // Verify the result
        assertNotNull(result)
        assertEquals(domainFrequency.size, result.topDomains.size)
        assertEquals(verdictDistribution.size, result.verdictDistribution.size)
        assertEquals(topicFrequency.size, result.topTopics.size)
        
        // Verify top domains
        assertEquals("example.com", result.topDomains[0].domain)
        assertEquals(10, result.topDomains[0].count)
        
        // Verify verdict distribution
        assertEquals(15, result.verdictDistribution.find { it.verdict == "CREDIBLE" }?.count)
        assertEquals(5, result.verdictDistribution.find { it.verdict == "SUSPICIOUS" }?.count)
        assertEquals(3, result.verdictDistribution.find { it.verdict == "UNRELIABLE" }?.count)
        
        // Verify top topics
        assertEquals("politics", result.topTopics[0].topic)
        assertEquals(12, result.topTopics[0].count)
        
        // Verify that repository methods were called
        verify { urlAnalysisRepository.findMostFrequentDomains(any(), any()) }
        verify { urlAnalysisRepository.getVerdictDistribution(any(), any()) }
        verify { urlAnalysisRepository.findMostFrequentTopics(any(), any()) }
        verify { trendRepository.save(any()) }
    }
    
    @Test
    fun `getLatestTrends should return most recent trend data`() {
        val trend = Trend(
            id = "trend1",
            timestamp = Instant.now().toEpochMilli(),
            topDomains = listOf(
                DomainTrend("example.com", 10),
                DomainTrend("news.com", 8),
                DomainTrend("blog.com", 5)
            ),
            verdictDistribution = listOf(
                VerdictTrend("CREDIBLE", 15),
                VerdictTrend("SUSPICIOUS", 5),
                VerdictTrend("UNRELIABLE", 3)
            ),
            topTopics = listOf(
                TopicTrend("politics", 12),
                TopicTrend("health", 8),
                TopicTrend("technology", 6)
            )
        )
        
        every { trendRepository.findTopByOrderByTimestampDesc() } returns trend
        
        val result = trendAnalysisService.getLatestTrends()
        
        assertNotNull(result)
        assertEquals(trend.id, result.id)
        assertEquals(trend.timestamp, result.timestamp)
        assertEquals(trend.topDomains.size, result.topDomains.size)
        assertEquals(trend.verdictDistribution.size, result.verdictDistribution.size)
        assertEquals(trend.topTopics.size, result.topTopics.size)
        
        verify { trendRepository.findTopByOrderByTimestampDesc() }
    }
    
    @Test
    fun `getTrendsHistory should return historical trend data`() {
        val trends = listOf(
            Trend(
                id = "trend1",
                timestamp = Instant.now().minusSeconds(86400).toEpochMilli(), // 1 day ago
                topDomains = listOf(DomainTrend("example.com", 10)),
                verdictDistribution = listOf(VerdictTrend("CREDIBLE", 15)),
                topTopics = listOf(TopicTrend("politics", 12))
            ),
            Trend(
                id = "trend2",
                timestamp = Instant.now().toEpochMilli(), // now
                topDomains = listOf(DomainTrend("example.com", 12)),
                verdictDistribution = listOf(VerdictTrend("CREDIBLE", 18)),
                topTopics = listOf(TopicTrend("politics", 14))
            )
        )
        
        every { trendRepository.findByTimestampGreaterThanOrderByTimestampAsc(any()) } returns trends
        
        val result = trendAnalysisService.getTrendsHistory(7) // last 7 days
        
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals(trends[0].id, result[0].id)
        assertEquals(trends[1].id, result[1].id)
        
        verify { trendRepository.findByTimestampGreaterThanOrderByTimestampAsc(any()) }
    }
    
    @Test
    fun `getDomainTrend should return trend data for specific domain`() {
        val domain = "example.com"
        val domainTrends = listOf(
            DomainTrendHistory(
                timestamp = Instant.now().minusSeconds(172800).toEpochMilli(), // 2 days ago
                count = 8
            ),
            DomainTrendHistory(
                timestamp = Instant.now().minusSeconds(86400).toEpochMilli(), // 1 day ago
                count = 10
            ),
            DomainTrendHistory(
                timestamp = Instant.now().toEpochMilli(), // now
                count = 12
            )
        )
        
        every { trendRepository.findDomainTrendHistory(domain, any()) } returns domainTrends
        
        val result = trendAnalysisService.getDomainTrend(domain, 7) // last 7 days
        
        assertNotNull(result)
        assertEquals(domain, result.domain)
        assertEquals(3, result.history.size)
        assertEquals(domainTrends[0].timestamp, result.history[0].timestamp)
        assertEquals(domainTrends[0].count, result.history[0].count)
        
        verify { trendRepository.findDomainTrendHistory(domain, any()) }
    }
    
    @Test
    fun `getVerdictTrend should return trend data for verdicts`() {
        val verdictTrends = mapOf(
            "CREDIBLE" to listOf(
                VerdictTrendHistory(
                    timestamp = Instant.now().minusSeconds(172800).toEpochMilli(), // 2 days ago
                    count = 12
                ),
                VerdictTrendHistory(
                    timestamp = Instant.now().minusSeconds(86400).toEpochMilli(), // 1 day ago
                    count = 15
                ),
                VerdictTrendHistory(
                    timestamp = Instant.now().toEpochMilli(), // now
                    count = 18
                )
            ),
            "SUSPICIOUS" to listOf(
                VerdictTrendHistory(
                    timestamp = Instant.now().minusSeconds(172800).toEpochMilli(), // 2 days ago
                    count = 4
                ),
                VerdictTrendHistory(
                    timestamp = Instant.now().minusSeconds(86400).toEpochMilli(), // 1 day ago
                    count = 5
                ),
                VerdictTrendHistory(
                    timestamp = Instant.now().toEpochMilli(), // now
                    count = 7
                )
            )
        )
        
        every { trendRepository.findVerdictTrendHistory(any()) } returns verdictTrends.entries.flatMap { entry ->
            entry.value.map { VerdictTrendResult(entry.key, it.timestamp, it.count) }
        }
        
        val result = trendAnalysisService.getVerdictTrend(7) // last 7 days
        
        assertNotNull(result)
        assertEquals(2, result.size)
        assertEquals("CREDIBLE", result[0].verdict)
        assertEquals(3, result[0].history.size)
        assertEquals("SUSPICIOUS", result[1].verdict)
        assertEquals(3, result[1].history.size)
        
        verify { trendRepository.findVerdictTrendHistory(any()) }
    }
    
    @Test
    fun `getTopicTrend should return trend data for specific topic`() {
        val topic = "politics"
        val topicTrends = listOf(
            TopicTrendHistory(
                timestamp = Instant.now().minusSeconds(172800).toEpochMilli(), // 2 days ago
                count = 10
            ),
            TopicTrendHistory(
                timestamp = Instant.now().minusSeconds(86400).toEpochMilli(), // 1 day ago
                count = 12
            ),
            TopicTrendHistory(
                timestamp = Instant.now().toEpochMilli(), // now
                count = 14
            )
        )
        
        every { trendRepository.findTopicTrendHistory(topic, any()) } returns topicTrends
        
        val result = trendAnalysisService.getTopicTrend(topic, 7) // last 7 days
        
        assertNotNull(result)
        assertEquals(topic, result.topic)
        assertEquals(3, result.history.size)
        assertEquals(topicTrends[0].timestamp, result.history[0].timestamp)
        assertEquals(topicTrends[0].count, result.history[0].count)
        
        verify { trendRepository.findTopicTrendHistory(topic, any()) }
    }
}