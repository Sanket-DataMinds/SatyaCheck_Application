package com.satyacheck.backend.service.impl

import com.satyacheck.backend.model.dto.AnalysisRequest
import com.satyacheck.backend.model.dto.AnalysisResult
import com.satyacheck.backend.model.entity.Analysis
import com.satyacheck.backend.model.enum.Verdict
import com.satyacheck.backend.repository.AnalysisRepository
import com.satyacheck.backend.service.AnalysisService
import com.satyacheck.backend.service.api.GeminiService
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID
import java.util.logging.Logger

@Service
class AnalysisServiceImpl(
    private val geminiService: GeminiService,
    private val analysisRepository: AnalysisRepository
) : AnalysisService {
    private val logger = Logger.getLogger(AnalysisServiceImpl::class.java.name)

    /**
     * Generate a content hash for caching purposes
     */
    private fun generateContentHash(content: String, language: String): String {
        val contentToHash = "$content:$language"
        val bytes = MessageDigest.getInstance("SHA-256").digest(contentToHash.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
    
    @Cacheable(value = ["analysisResults"], key = "#root.method.name + '_' + #root.target.generateContentHash(#request.content, #request.language)")
    override suspend fun analyzeText(request: AnalysisRequest): AnalysisResult {
        logger.info("Cache miss for content: ${request.content.take(50)}... - performing analysis")

        // Call the Gemini API to analyze the text
        val (verdict, explanation) = geminiService.analyzeContent(
            content = request.content,
            language = request.language
        )

        // Create an AnalysisResult to return to the client
        val result = AnalysisResult(
            verdict = verdict,
            explanation = explanation
        )        // Store the analysis result in the database
        try {
            val analysis = Analysis(
                id = UUID.randomUUID().toString(),
                content = request.content,
                verdict = verdict,
                explanation = explanation,
                timestamp = LocalDateTime.now(),
                metadata = mapOf(
                    "contentType" to request.contentType
                ),
                language = request.language
            )
            
            analysisRepository.save(analysis)
            logger.info("Saved analysis to database with ID: ${analysis.id}")
        } catch (e: Exception) {
            logger.severe("Failed to save analysis to database: ${e.message}")
            // Continue execution - we still want to return the analysis result even if saving to DB fails
        }
        
        return result
    }
}
