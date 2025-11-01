package com.satyacheck.backend.controller

import com.satyacheck.backend.model.entity.AnalysisFeedback
import com.satyacheck.backend.service.feedback.AccuracyMetrics
import com.satyacheck.backend.service.feedback.FeedbackService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.logging.Logger

/**
 * Controller for feedback operations
 */
@RestController
@RequestMapping("/api/v1/feedback")
class FeedbackController(
    private val feedbackService: FeedbackService
) {
    private val logger = Logger.getLogger(FeedbackController::class.java.name)

    /**
     * Submit new feedback
     */
    @PostMapping
    fun submitFeedback(@RequestBody feedback: AnalysisFeedback): ResponseEntity<AnalysisFeedback> {
        logger.info("Received feedback submission for analysis ${feedback.analysisId}")
        val savedFeedback = feedbackService.submitFeedback(feedback)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedFeedback)
    }

    /**
     * Update existing feedback
     */
    @PutMapping("/{id}")
    fun updateFeedback(
        @PathVariable id: String,
        @RequestBody feedback: AnalysisFeedback
    ): ResponseEntity<AnalysisFeedback> {
        logger.info("Received feedback update for ID $id")
        val updatedFeedback = feedbackService.updateFeedback(id, feedback)
        return ResponseEntity.ok(updatedFeedback)
    }

    /**
     * Get feedback by ID
     */
    @GetMapping("/{id}")
    fun getFeedbackById(@PathVariable id: String): ResponseEntity<AnalysisFeedback> {
        return try {
            val feedback = feedbackService.getFeedbackById(id)
            ResponseEntity.ok(feedback)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Get all feedback with pagination
     */
    @GetMapping
    fun getAllFeedback(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<Page<AnalysisFeedback>> {
        val direction = Sort.Direction.valueOf(sortDirection)
        val pageable = PageRequest.of(page, size, Sort.by(direction, sortBy))
        
        val feedbackPage = feedbackService.getAllFeedback(pageable)
        return ResponseEntity.ok(feedbackPage)
    }

    /**
     * Get feedback by analysis ID
     */
    @GetMapping("/analysis/{analysisId}")
    fun getFeedbackByAnalysisId(@PathVariable analysisId: String): ResponseEntity<List<AnalysisFeedback>> {
        val feedback = feedbackService.getFeedbackByAnalysisId(analysisId)
        return ResponseEntity.ok(feedback)
    }

    /**
     * Get feedback by analysis type
     */
    @GetMapping("/type/{analysisType}")
    fun getFeedbackByAnalysisType(@PathVariable analysisType: String): ResponseEntity<List<AnalysisFeedback>> {
        val feedback = feedbackService.getFeedbackByAnalysisType(analysisType)
        return ResponseEntity.ok(feedback)
    }

    /**
     * Get feedback by user ID
     */
    @GetMapping("/user/{userId}")
    fun getFeedbackByUserId(@PathVariable userId: String): ResponseEntity<List<AnalysisFeedback>> {
        val feedback = feedbackService.getFeedbackByUserId(userId)
        return ResponseEntity.ok(feedback)
    }

    /**
     * Get feedback by language
     */
    @GetMapping("/language/{language}")
    fun getFeedbackByLanguage(@PathVariable language: String): ResponseEntity<List<AnalysisFeedback>> {
        val feedback = feedbackService.getFeedbackByLanguage(language)
        return ResponseEntity.ok(feedback)
    }

    /**
     * Get accuracy metrics
     */
    @GetMapping("/metrics/accuracy")
    fun getAccuracyMetrics(): ResponseEntity<AccuracyMetrics> {
        val metrics = feedbackService.calculateAccuracyMetrics()
        return ResponseEntity.ok(metrics)
    }

    /**
     * Delete feedback by ID
     */
    @DeleteMapping("/{id}")
    fun deleteFeedback(@PathVariable id: String): ResponseEntity<Map<String, String>> {
        try {
            feedbackService.deleteFeedback(id)
            return ResponseEntity.ok(mapOf("message" to "Feedback deleted successfully"))
        } catch (e: Exception) {
            logger.warning("Error deleting feedback $id: ${e.message}")
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to delete feedback"))
        }
    }
}