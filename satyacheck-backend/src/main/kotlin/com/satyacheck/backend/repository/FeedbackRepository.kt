package com.satyacheck.backend.repository

import com.satyacheck.backend.model.entity.AnalysisFeedback
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository for AnalysisFeedback entities
 */
@Repository
interface FeedbackRepository : MongoRepository<AnalysisFeedback, String> {

    /**
     * Find feedback by analysis ID
     */
    fun findByAnalysisId(analysisId: String): List<AnalysisFeedback>
    
    /**
     * Find feedback for a specific analysis type
     */
    fun findByAnalysisType(analysisType: String): List<AnalysisFeedback>
    
    /**
     * Find feedback by user ID
     */
    fun findByUserId(userId: String): List<AnalysisFeedback>
    
    /**
     * Find feedback by rating
     */
    fun findByRating(rating: Int): List<AnalysisFeedback>
    
    /**
     * Find feedback with specific reason code
     */
    fun findByReasonCodesContaining(reasonCode: String): List<AnalysisFeedback>
    
    /**
     * Find feedback by content language
     */
    fun findByContentLanguage(language: String): List<AnalysisFeedback>
    
    /**
     * Find feedback by correctness evaluation
     */
    fun findByWasCorrect(wasCorrect: Boolean): List<AnalysisFeedback>
    
    /**
     * Find feedback by analyzed content (partial match)
     */
    @Query("{'analyzedContent': {'\$regex': ?0, '\$options': 'i'}}")
    fun findByAnalyzedContentContaining(contentFragment: String): List<AnalysisFeedback>
}