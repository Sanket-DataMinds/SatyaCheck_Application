package com.satyacheck.backend.repository

import com.satyacheck.backend.model.entity.AnalysisRecord
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Repository for AnalysisRecord entities
 */
@Repository
interface AnalysisRecordRepository : MongoRepository<AnalysisRecord, String> {
    
    /**
     * Find records by misinformation status
     */
    fun findByIsMisinformation(isMisinformation: Boolean, pageable: Pageable): Page<AnalysisRecord>
    
    /**
     * Find records by language
     */
    fun findByLanguage(language: String, pageable: Pageable): Page<AnalysisRecord>
    
    /**
     * Find records by content type
     */
    fun findByContentType(contentType: String, pageable: Pageable): Page<AnalysisRecord>
    
    /**
     * Find records within a time range
     */
    fun findByAnalyzedAtBetween(start: Instant, end: Instant, pageable: Pageable): Page<AnalysisRecord>
    
    /**
     * Find records containing specific misinformation technique
     */
    fun findByMisinformationTechniquesContaining(technique: String, pageable: Pageable): Page<AnalysisRecord>
    
    /**
     * Find records containing specific category
     */
    fun findByCategoriesContaining(category: String, pageable: Pageable): Page<AnalysisRecord>
    
    /**
     * Find records containing specific keyword
     */
    fun findByKeywordsContaining(keyword: String, pageable: Pageable): Page<AnalysisRecord>
    
    /**
     * Find records containing specific topic
     */
    fun findByTopicsContaining(topic: String, pageable: Pageable): Page<AnalysisRecord>
    
    /**
     * Find records by risk level
     */
    fun findByRiskLevel(riskLevel: String, pageable: Pageable): Page<AnalysisRecord>
    
    /**
     * Find records by region
     */
    fun findByUserRegion(region: String, pageable: Pageable): Page<AnalysisRecord>
    
    /**
     * Find records not processed for trends
     */
    fun findByProcessedForTrends(processedForTrends: Boolean, pageable: Pageable): Page<AnalysisRecord>
    
    /**
     * Count records by language
     */
    fun countByLanguage(language: String): Long
    
    /**
     * Count records by misinformation status
     */
    fun countByIsMisinformation(isMisinformation: Boolean): Long
    
    /**
     * Count records by content type and misinformation status
     */
    fun countByContentTypeAndIsMisinformation(contentType: String, isMisinformation: Boolean): Long
    
    /**
     * Count records by time range
     */
    fun countByAnalyzedAtBetween(start: Instant, end: Instant): Long
    
    /**
     * Count records by misinformation technique
     */
    @Query(value = "{'misinformationTechniques': ?0}", count = true)
    fun countByMisinformationTechnique(technique: String): Long
}