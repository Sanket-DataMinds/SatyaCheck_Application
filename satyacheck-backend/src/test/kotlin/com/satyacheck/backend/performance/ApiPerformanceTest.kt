package com.satyacheck.backend.performance

import com.fasterxml.jackson.databind.ObjectMapper
import com.satyacheck.backend.SatyacheckBackendApplication
import com.satyacheck.backend.model.AnalysisRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertTrue

/**
 * Performance tests for API endpoints
 * 
 * Note: These tests require a running application context and may take longer to run.
 * They're meant to be run selectively during performance testing phases, not with every build.
 */
@SpringBootTest(classes = [SatyacheckBackendApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiPerformanceTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    /**
     * Tests the URL analysis endpoint under simulated load
     */
    @Test
    fun `test URL analysis endpoint performance under load`() {
        val numRequests = 50
        val maxConcurrentRequests = 10
        val maxAcceptableTimePerRequest = 2000 // 2 seconds max per request

        val executor = Executors.newFixedThreadPool(maxConcurrentRequests)
        val latch = CountDownLatch(numRequests)
        val successCounter = AtomicInteger(0)
        val totalTimeMs = AtomicInteger(0)

        // List of test URLs to analyze
        val testUrls = listOf(
            "https://www.bbc.com/news/articles/c06pkkxgljeo",
            "https://edition.cnn.com/2023/01/01/politics/example-article/index.html",
            "https://www.thehindu.com/news/national/article123456.ece",
            "https://timesofindia.indiatimes.com/india/example-article/articleshow/12345678.cms",
            "https://indianexpress.com/article/india/example-article-12345678/"
        )

        // Submit requests in parallel
        repeat(numRequests) { i ->
            executor.submit {
                try {
                    val url = testUrls[i % testUrls.size]
                    val request = AnalysisRequest(url = url)
                    val startTime = System.currentTimeMillis()
                    
                    val result = mockMvc.perform(
                        post("/api/v1/analyze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    ).andReturn()
                    
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    
                    // Record metrics
                    totalTimeMs.addAndGet(duration.toInt())
                    
                    if (result.response.status == 200) {
                        successCounter.incrementAndGet()
                    }
                } catch (e: Exception) {
                    // Log exception but continue test
                    println("Request failed: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // Wait for all requests to complete or timeout
        latch.await(3, TimeUnit.MINUTES)
        executor.shutdown()

        // Calculate and verify metrics
        val successRate = successCounter.get().toDouble() / numRequests.toDouble()
        val averageTimePerRequest = totalTimeMs.get().toDouble() / numRequests.toDouble()

        println("Performance Test Results:")
        println("Total requests: $numRequests")
        println("Successful requests: ${successCounter.get()}")
        println("Success rate: ${successRate * 100}%")
        println("Average time per request: ${averageTimePerRequest}ms")

        // Assert performance criteria
        assertTrue(successRate >= 0.95, "Success rate should be at least 95%")
        assertTrue(averageTimePerRequest <= maxAcceptableTimePerRequest, 
            "Average time per request (${averageTimePerRequest}ms) exceeds maximum acceptable time (${maxAcceptableTimePerRequest}ms)")
    }
    
    /**
     * Tests the bulk analysis submission endpoint under simulated load
     */
    @Test
    fun `test bulk analysis submission endpoint performance under load`() {
        val numRequests = 20
        val maxConcurrentRequests = 5
        val maxAcceptableTimePerRequest = 1000 // 1 second max per request

        val executor = Executors.newFixedThreadPool(maxConcurrentRequests)
        val latch = CountDownLatch(numRequests)
        val successCounter = AtomicInteger(0)
        val totalTimeMs = AtomicInteger(0)

        // Submit requests in parallel
        repeat(numRequests) { i ->
            executor.submit {
                try {
                    val bulkRequest = mapOf(
                        "userId" to "perf-test-user-$i",
                        "urls" to listOf(
                            "https://www.example1.com/article$i",
                            "https://www.example2.com/article$i",
                            "https://www.example3.com/article$i"
                        ),
                        "notificationToken" to "test-token-$i"
                    )
                    
                    val startTime = System.currentTimeMillis()
                    
                    val result = mockMvc.perform(
                        post("/api/v1/analyze/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bulkRequest))
                    ).andReturn()
                    
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    
                    // Record metrics
                    totalTimeMs.addAndGet(duration.toInt())
                    
                    if (result.response.status == 202) {
                        successCounter.incrementAndGet()
                    }
                } catch (e: Exception) {
                    // Log exception but continue test
                    println("Bulk request failed: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // Wait for all requests to complete or timeout
        latch.await(1, TimeUnit.MINUTES)
        executor.shutdown()

        // Calculate and verify metrics
        val successRate = successCounter.get().toDouble() / numRequests.toDouble()
        val averageTimePerRequest = totalTimeMs.get().toDouble() / numRequests.toDouble()

        println("Bulk Analysis Performance Test Results:")
        println("Total requests: $numRequests")
        println("Successful requests: ${successCounter.get()}")
        println("Success rate: ${successRate * 100}%")
        println("Average time per request: ${averageTimePerRequest}ms")

        // Assert performance criteria
        assertTrue(successRate >= 0.95, "Success rate should be at least 95%")
        assertTrue(averageTimePerRequest <= maxAcceptableTimePerRequest, 
            "Average time per request (${averageTimePerRequest}ms) exceeds maximum acceptable time (${maxAcceptableTimePerRequest}ms)")
    }
    
    /**
     * Tests the feedback submission endpoint under simulated load
     */
    @Test
    fun `test feedback submission endpoint performance under load`() {
        val numRequests = 50
        val maxConcurrentRequests = 10
        val maxAcceptableTimePerRequest = 500 // 500ms max per request

        val executor = Executors.newFixedThreadPool(maxConcurrentRequests)
        val latch = CountDownLatch(numRequests)
        val successCounter = AtomicInteger(0)
        val totalTimeMs = AtomicInteger(0)

        // List of feedback types
        val feedbackTypes = listOf(
            "DISAGREE_WITH_VERDICT",
            "MISLEADING_CONTENT",
            "FAKE_NEWS",
            "OFFENSIVE_CONTENT",
            "POLITICAL_BIAS"
        )

        // Submit requests in parallel
        repeat(numRequests) { i ->
            executor.submit {
                try {
                    val feedbackRequest = mapOf(
                        "userId" to "perf-test-user-$i",
                        "url" to "https://www.example.com/article-${i % 10}",
                        "type" to feedbackTypes[i % feedbackTypes.size],
                        "comment" to "Performance test feedback comment $i",
                        "originalVerdict" to if (i % 2 == 0) "CREDIBLE" else "SUSPICIOUS"
                    )
                    
                    val startTime = System.currentTimeMillis()
                    
                    val result = mockMvc.perform(
                        post("/api/v1/feedback")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(feedbackRequest))
                    ).andReturn()
                    
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    
                    // Record metrics
                    totalTimeMs.addAndGet(duration.toInt())
                    
                    if (result.response.status == 201) {
                        successCounter.incrementAndGet()
                    }
                } catch (e: Exception) {
                    // Log exception but continue test
                    println("Feedback request failed: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // Wait for all requests to complete or timeout
        latch.await(1, TimeUnit.MINUTES)
        executor.shutdown()

        // Calculate and verify metrics
        val successRate = successCounter.get().toDouble() / numRequests.toDouble()
        val averageTimePerRequest = totalTimeMs.get().toDouble() / numRequests.toDouble()

        println("Feedback Submission Performance Test Results:")
        println("Total requests: $numRequests")
        println("Successful requests: ${successCounter.get()}")
        println("Success rate: ${successRate * 100}%")
        println("Average time per request: ${averageTimePerRequest}ms")

        // Assert performance criteria
        assertTrue(successRate >= 0.95, "Success rate should be at least 95%")
        assertTrue(averageTimePerRequest <= maxAcceptableTimePerRequest, 
            "Average time per request (${averageTimePerRequest}ms) exceeds maximum acceptable time (${maxAcceptableTimePerRequest}ms)")
    }
}