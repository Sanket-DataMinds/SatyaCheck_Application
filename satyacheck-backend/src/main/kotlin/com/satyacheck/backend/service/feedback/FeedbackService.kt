package com.satyacheck.backend.service.feedback

import com.satyacheck.backend.model.entity.AnalysisFeedback
import com.satyacheck.backend.repository.FeedbackRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import java.util.logging.Logger

/**
 * Service for managing analysis feedback
 */
@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository
) {
    private val logger = Logger.getLogger(FeedbackService::class.java.name)
    
    /**
     * Submit new feedback
     */
    fun submitFeedback(feedback: AnalysisFeedback): AnalysisFeedback {
        logger.info("Submitting feedback for analysis ${feedback.analysisId}")
        
        val feedbackWithId = if (feedback.id == null) {
            feedback.copy(id = UUID.randomUUID().toString())
        } else {
            feedback
        }
        
        return feedbackRepository.save(feedbackWithId)
    }
    
    /**
     * Update existing feedback
     */
    fun updateFeedback(id: String, feedback: AnalysisFeedback): AnalysisFeedback {
        logger.info("Updating feedback $id")
        
        val existingFeedback = feedbackRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Feedback with ID $id not found") }
        
        val updatedFeedback = existingFeedback.copy(
            rating = feedback.rating,
            wasCorrect = feedback.wasCorrect,
            userEvaluation = feedback.userEvaluation,
            reasonCodes = feedback.reasonCodes,
            comment = feedback.comment,
            updatedAt = Instant.now()
        )
        
        return feedbackRepository.save(updatedFeedback)
    }
    
    /**
     * Get feedback by ID
     */
    fun getFeedbackById(id: String): AnalysisFeedback {
        return feedbackRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Feedback with ID $id not found") }
    }
    
    /**
     * Get all feedback with pagination
     */
    fun getAllFeedback(pageable: Pageable): Page<AnalysisFeedback> {
        return feedbackRepository.findAll(pageable)
    }
    
    /**
     * Get feedback by analysis ID
     */
    fun getFeedbackByAnalysisId(analysisId: String): List<AnalysisFeedback> {
        return feedbackRepository.findByAnalysisId(analysisId)
    }
    
    /**
     * Get feedback by analysis type
     */
    fun getFeedbackByAnalysisType(analysisType: String): List<AnalysisFeedback> {
        return feedbackRepository.findByAnalysisType(analysisType)
    }
    
    /**
     * Get feedback by user ID
     */
    fun getFeedbackByUserId(userId: String): List<AnalysisFeedback> {
        return feedbackRepository.findByUserId(userId)
    }
    
    /**
     * Get feedback by language
     */
    fun getFeedbackByLanguage(language: String): List<AnalysisFeedback> {
        return feedbackRepository.findByContentLanguage(language)
    }
    
    /**
     * Calculate accuracy metrics based on feedback
     */
    fun calculateAccuracyMetrics(): AccuracyMetrics {
        val allFeedback = feedbackRepository.findAll()
        
        val feedbackWithCorrectness = allFeedback.filter { it.wasCorrect != null }
        
        if (feedbackWithCorrectness.isEmpty()) {
            return AccuracyMetrics(
                totalFeedbackCount = allFeedback.size,
                feedbackWithCorrectnessCount = 0,
                correctCount = 0,
                incorrectCount = 0,
                overallAccuracy = 0.0,
                averageRating = allFeedback.map { it.rating }.average().takeIf { !it.isNaN() } ?: 0.0
            )
        }
        
        val correctCount = feedbackWithCorrectness.count { it.wasCorrect == true }
        val incorrectCount = feedbackWithCorrectness.count { it.wasCorrect == false }
        
        return AccuracyMetrics(
            totalFeedbackCount = allFeedback.size,
            feedbackWithCorrectnessCount = feedbackWithCorrectness.size,
            correctCount = correctCount,
            incorrectCount = incorrectCount,
            overallAccuracy = correctCount.toDouble() / feedbackWithCorrectness.size,
            averageRating = allFeedback.map { it.rating }.average()
        )
    }
    
    /**
     * Delete feedback by ID
     */
    fun deleteFeedback(id: String) {
        logger.info("Deleting feedback $id")
        feedbackRepository.deleteById(id)
    }
}

/**
 * Data class for accuracy metrics calculated from feedback
 */
data class AccuracyMetrics(
    val totalFeedbackCount: Int,
    val feedbackWithCorrectnessCount: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val overallAccuracy: Double,
    val averageRating: Double
)