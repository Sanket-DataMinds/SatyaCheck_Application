package com.satyacheck.backend.repository

import com.satyacheck.backend.model.entity.MisinformationTrend
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * Repository for MisinformationTrend entities
 */
@Repository
interface TrendRepository : MongoRepository<MisinformationTrend, String> {
    
    /**
     * Find trends by date
     */
    fun findByDate(date: LocalDate): List<MisinformationTrend>
    
    /**
     * Find trends by date range
     */
    fun findByDateBetween(startDate: LocalDate, endDate: LocalDate, pageable: Pageable): Page<MisinformationTrend>
    
    /**
     * Find trends by period
     */
    fun findByPeriod(period: String, pageable: Pageable): Page<MisinformationTrend>
    
    /**
     * Find trends by content type
     */
    fun findByContentType(contentType: String, pageable: Pageable): Page<MisinformationTrend>
    
    /**
     * Find trends by language
     */
    fun findByLanguage(language: String, pageable: Pageable): Page<MisinformationTrend>
    
    /**
     * Find trends by date and period
     */
    fun findByDateAndPeriod(date: LocalDate, period: String): List<MisinformationTrend>
    
    /**
     * Find trends by date, period and content type
     */
    fun findByDateAndPeriodAndContentType(date: LocalDate, period: String, contentType: String): List<MisinformationTrend>
    
    /**
     * Find trends by date, period and language
     */
    fun findByDateAndPeriodAndLanguage(date: LocalDate, period: String, language: String): List<MisinformationTrend>
    
    /**
     * Find trends by date, period, content type and language
     */
    fun findByDateAndPeriodAndContentTypeAndLanguage(
        date: LocalDate,
        period: String,
        contentType: String,
        language: String
    ): List<MisinformationTrend>
}