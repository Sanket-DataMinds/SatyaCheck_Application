package com.satyacheck.backend.service.analysis

import com.satyacheck.backend.repository.BulkAnalysisRepository
import com.satyacheck.backend.service.web.UrlAnalysisService
import io.mockk.*
import kotlinx.coroutines.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for BulkAnalysisService
 */
@ExtendWith(SpringExtension::class)
class BulkAnalysisServiceTest {
    
    private lateinit var urlAnalysisService: UrlAnalysisService
    private lateinit var bulkAnalysisRepository: BulkAnalysisRepository
    private lateinit var bulkAnalysisService: BulkAnalysisService
    
    @BeforeEach
    fun setup() {
        urlAnalysisService = mockk()
        bulkAnalysisRepository = mockk()
        bulkAnalysisService = BulkAnalysisService(urlAnalysisService, bulkAnalysisRepository)
        
        // Mock repository save
        every { bulkAnalysisRepository.save(any()) } answers { firstArg() }
        every { bulkAnalysisRepository.findById(any()) } returns Optional.empty()
    }
    
    @Test
    fun `submitBulkAnalysis should create and save analysis request`() {
        val userId = "user123"
        val urls = listOf("https://example.com/1", "https://example.com/2", "https://example.com/3")
        val notificationToken = "token123"
        
        val result = bulkAnalysisService.submitBulkAnalysis(userId, urls, notificationToken)
        
        assertNotNull(result)
        assertEquals(userId, result.userId)
        assertEquals(urls.size, result.urls.size)
        assertEquals(urls, result.urls)
        assertEquals(notificationToken, result.notificationToken)
        assertEquals(BulkAnalysisStatus.PENDING, result.status)
        
        verify { bulkAnalysisRepository.save(any()) }
    }
    
    @Test
    fun `startBulkAnalysis should process URLs and update status`() = runBlocking {
        val bulkId = UUID.randomUUID().toString()
        val userId = "user123"
        val urls = listOf("https://example.com/1", "https://example.com/2")
        val notificationToken = "token123"
        
        val bulkAnalysis = BulkAnalysis(
            id = bulkId,
            userId = userId,
            urls = urls,
            notificationToken = notificationToken,
            status = BulkAnalysisStatus.PENDING,
            results = emptyMap(),
            submittedAt = System.currentTimeMillis(),
            completedAt = null
        )
        
        // Mock URL analysis results
        every {
            urlAnalysisService.analyzeUrl(urls[0])
        } returns mockk(relaxed = true) {
            every { contentAnalysis?.verdict } returns "CREDIBLE"
        }
        
        every {
            urlAnalysisService.analyzeUrl(urls[1])
        } returns mockk(relaxed = true) {
            every { contentAnalysis?.verdict } returns "SUSPICIOUS"
        }
        
        // Mock finding the bulk analysis
        coEvery { 
            bulkAnalysisRepository.findById(bulkId) 
        } returns Optional.of(bulkAnalysis)
        
        // Mock saving the updated bulk analysis
        coEvery { 
            bulkAnalysisRepository.save(any()) 
        } answers { firstArg() }
        
        // Run the bulk analysis
        bulkAnalysisService.startBulkAnalysis(bulkId)
        
        // Allow coroutines to complete
        delay(500)
        
        // Verify the repository was called to update the status
        coVerify(exactly = 1) { 
            bulkAnalysisRepository.findById(bulkId)
        }
        
        // Verify that save was called at least twice (once for IN_PROGRESS and once for COMPLETED)
        coVerify(atLeast = 2) {
            bulkAnalysisRepository.save(any())
        }
        
        // Verify URL analysis was called for each URL
        coVerify(exactly = 1) {
            urlAnalysisService.analyzeUrl(urls[0])
        }
        coVerify(exactly = 1) {
            urlAnalysisService.analyzeUrl(urls[1])
        }
    }
    
    @Test
    fun `getBulkAnalysis should return analysis by id`() {
        val bulkId = UUID.randomUUID().toString()
        val userId = "user123"
        val urls = listOf("https://example.com/1", "https://example.com/2")
        
        val bulkAnalysis = BulkAnalysis(
            id = bulkId,
            userId = userId,
            urls = urls,
            notificationToken = "token123",
            status = BulkAnalysisStatus.COMPLETED,
            results = mapOf(
                "https://example.com/1" to "CREDIBLE",
                "https://example.com/2" to "SUSPICIOUS"
            ),
            submittedAt = System.currentTimeMillis() - 10000,
            completedAt = System.currentTimeMillis()
        )
        
        every { 
            bulkAnalysisRepository.findById(bulkId) 
        } returns Optional.of(bulkAnalysis)
        
        val result = bulkAnalysisService.getBulkAnalysis(bulkId)
        
        assertNotNull(result)
        assertEquals(bulkId, result.id)
        assertEquals(userId, result.userId)
        assertEquals(urls.size, result.urls.size)
        assertEquals(BulkAnalysisStatus.COMPLETED, result.status)
        assertEquals(2, result.results.size)
        assertTrue(result.completedAt != null)
        
        verify { bulkAnalysisRepository.findById(bulkId) }
    }
    
    @Test
    fun `getUserBulkAnalyses should return all analyses for user`() {
        val userId = "user123"
        val analyses = listOf(
            BulkAnalysis(
                id = UUID.randomUUID().toString(),
                userId = userId,
                urls = listOf("https://example.com/1"),
                notificationToken = "token123",
                status = BulkAnalysisStatus.COMPLETED,
                results = mapOf("https://example.com/1" to "CREDIBLE"),
                submittedAt = System.currentTimeMillis() - 20000,
                completedAt = System.currentTimeMillis() - 10000
            ),
            BulkAnalysis(
                id = UUID.randomUUID().toString(),
                userId = userId,
                urls = listOf("https://example.com/2", "https://example.com/3"),
                notificationToken = "token123",
                status = BulkAnalysisStatus.IN_PROGRESS,
                results = emptyMap(),
                submittedAt = System.currentTimeMillis() - 5000,
                completedAt = null
            )
        )
        
        every { 
            bulkAnalysisRepository.findByUserIdOrderBySubmittedAtDesc(userId) 
        } returns analyses
        
        val results = bulkAnalysisService.getUserBulkAnalyses(userId)
        
        assertEquals(2, results.size)
        assertEquals(analyses[0].id, results[0].id)
        assertEquals(analyses[1].id, results[1].id)
        
        verify { bulkAnalysisRepository.findByUserIdOrderBySubmittedAtDesc(userId) }
    }
}