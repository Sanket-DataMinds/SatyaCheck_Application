package com.satyacheck.backend.service.trends

import com.satyacheck.backend.model.entity.AnalysisRecord
import com.satyacheck.backend.model.entity.MisinformationTrend
import com.satyacheck.backend.repository.AnalysisRecordRepository
import com.satyacheck.backend.repository.TrendRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.logging.Logger

/**
 * Service for generating and retrieving misinformation trend data
 */
@Service
class TrendAnalysisService(
    private val analysisRecordRepository: AnalysisRecordRepository,
    private val trendRepository: TrendRepository,
    private val analysisRecordService: AnalysisRecordService
) {
    private val logger = Logger.getLogger(TrendAnalysisService::class.java.name)
    
    /**
     * Generate daily trends - scheduled to run at midnight every day
     */
    @Scheduled(cron = "0 0 0 * * ?")
    fun generateDailyTrends() {
        logger.info("Generating daily misinformation trends")
        
        // Get yesterday's date
        val yesterday = LocalDate.now().minusDays(1)
        
        // Generate trends by content type and language
        generateTrendsForDate(yesterday, "DAILY")
    }
    
    /**
     * Generate weekly trends - scheduled to run at 1 AM every Monday
     */
    @Scheduled(cron = "0 0 1 * * MON")
    fun generateWeeklyTrends() {
        logger.info("Generating weekly misinformation trends")
        
        // Get last week's end date (yesterday)
        val endDate = LocalDate.now().minusDays(1)
        
        // Generate trends for the past week
        generateTrendsForDateRange(endDate.minusDays(6), endDate, "WEEKLY")
    }
    
    /**
     * Generate monthly trends - scheduled to run at 2 AM on the 1st of each month
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    fun generateMonthlyTrends() {
        logger.info("Generating monthly misinformation trends")
        
        // Get last month's end date
        val endDate = LocalDate.now().minusDays(1)
        val startDate = endDate.withDayOfMonth(1)
        
        // Generate trends for the past month
        generateTrendsForDateRange(startDate, endDate, "MONTHLY")
    }
    
    /**
     * Process unprocessed records - scheduled to run every hour
     */
    @Scheduled(cron = "0 0 * * * ?")
    fun processUnprocessedRecords() {
        logger.info("Processing unprocessed records for trend analysis")
        
        val unprocessedRecords = analysisRecordService.getUnprocessedRecords(1000)
        
        if (unprocessedRecords.isEmpty()) {
            logger.info("No unprocessed records found")
            return
        }
        
        logger.info("Found ${unprocessedRecords.size} unprocessed records")
        
        unprocessedRecords.forEach { record ->
            // Mark as processed
            analysisRecordService.markAsProcessed(record)
        }
        
        // Generate trends for today (partial)
        generateTrendsForDate(LocalDate.now(), "DAILY")
    }
    
    /**
     * Generate trends for a specific date
     */
    fun generateTrendsForDate(date: LocalDate, period: String) {
        logger.info("Generating $period trends for date: $date")
        
        // Convert date to start and end instants
        val startInstant = date.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endInstant = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        
        // Get all records for the date
        val records = getRecordsForTimeRange(startInstant, endInstant)
        
        if (records.isEmpty()) {
            logger.info("No records found for date: $date")
            return
        }
        
        // Group records by content type and language
        val recordsByTypeAndLang = records.groupBy { 
            Pair(it.contentType ?: "UNKNOWN", it.language) 
        }
        
        // Process each group
        recordsByTypeAndLang.forEach { (typeAndLang, groupRecords) ->
            val (contentType, language) = typeAndLang
            
            // Generate trend for this group
            generateTrendForRecords(
                date,
                period,
                contentType,
                language,
                groupRecords
            )
        }
        
        // Also generate an overall trend for all content types and languages
        generateTrendForRecords(
            date,
            period,
            "ALL",
            "ALL",
            records
        )
    }
    
    /**
     * Generate trends for a date range
     */
    fun generateTrendsForDateRange(startDate: LocalDate, endDate: LocalDate, period: String) {
        logger.info("Generating $period trends for date range: $startDate to $endDate")
        
        // Convert dates to instants
        val startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        val endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        
        // Get all records for the date range
        val records = getRecordsForTimeRange(startInstant, endInstant)
        
        if (records.isEmpty()) {
            logger.info("No records found for date range: $startDate to $endDate")
            return
        }
        
        // Group records by content type and language
        val recordsByTypeAndLang = records.groupBy { 
            Pair(it.contentType ?: "UNKNOWN", it.language) 
        }
        
        // Process each group
        recordsByTypeAndLang.forEach { (typeAndLang, groupRecords) ->
            val (contentType, language) = typeAndLang
            
            // Generate trend for this group
            generateTrendForRecords(
                endDate, // Use end date as the trend date
                period,
                contentType,
                language,
                groupRecords
            )
        }
        
        // Also generate an overall trend for all content types and languages
        generateTrendForRecords(
            endDate, // Use end date as the trend date
            period,
            "ALL",
            "ALL",
            records
        )
    }
    
    /**
     * Generate trend for a group of records
     */
    private fun generateTrendForRecords(
        date: LocalDate,
        period: String,
        contentType: String,
        language: String,
        records: List<AnalysisRecord>
    ) {
        logger.info("Generating $period trend for date: $date, content type: $contentType, language: $language")
        
        val totalCount = records.size
        val misinfoCount = records.count { it.isMisinformation }
        val misinfoRate = if (totalCount > 0) misinfoCount.toDouble() / totalCount else 0.0
        
        // Collect top categories
        val categories = mutableMapOf<String, Int>()
        records.flatMap { it.categories }.forEach { category ->
            categories[category] = categories.getOrDefault(category, 0) + 1
        }
        
        // Collect top misinformation techniques
        val techniques = mutableMapOf<String, Int>()
        records.flatMap { it.misinformationTechniques }.forEach { technique ->
            techniques[technique] = techniques.getOrDefault(technique, 0) + 1
        }
        
        // Collect common keywords
        val keywords = mutableMapOf<String, Int>()
        records.flatMap { it.keywords }.forEach { keyword ->
            keywords[keyword] = keywords.getOrDefault(keyword, 0) + 1
        }
        
        // Collect regions
        val regions = mutableMapOf<String, Int>()
        records.mapNotNull { it.userRegion }.forEach { region ->
            regions[region] = regions.getOrDefault(region, 0) + 1
        }
        
        // Calculate average sentiment
        val sentiments = records.mapNotNull { it.sentimentScore }
        val avgSentiment = if (sentiments.isNotEmpty()) sentiments.average() else 0.0
        
        // Create trend entity
        val trend = MisinformationTrend(
            id = UUID.randomUUID().toString(),
            date = date,
            period = period,
            contentType = contentType,
            language = language,
            totalAnalyzed = totalCount,
            misinformationCount = misinfoCount,
            misinformationRate = misinfoRate,
            topCategories = categories.toList().sortedByDescending { it.second }.take(10).toMap(),
            topTechniques = techniques.toList().sortedByDescending { it.second }.take(10).toMap(),
            commonKeywords = keywords.toList().sortedByDescending { it.second }.take(20).map { it.first },
            sentimentAnalysis = mapOf(
                "average" to avgSentiment,
                "positive" to sentiments.count { it > 0.0 }.toDouble() / sentiments.size.coerceAtLeast(1),
                "negative" to sentiments.count { it < 0.0 }.toDouble() / sentiments.size.coerceAtLeast(1),
                "neutral" to sentiments.count { it == 0.0 }.toDouble() / sentiments.size.coerceAtLeast(1)
            ),
            regions = regions.toList().sortedByDescending { it.second }.take(10).toMap(),
            reliabilityScore = calculateReliabilityScore(totalCount)
        )
        
        // Save trend
        trendRepository.save(trend)
    }
    
    /**
     * Get trend data for a specific date and period
     */
    fun getTrendData(
        date: LocalDate,
        period: String,
        contentType: String? = null,
        language: String? = null
    ): List<MisinformationTrend> {
        return when {
            contentType != null && language != null ->
                trendRepository.findByDateAndPeriodAndContentTypeAndLanguage(date, period, contentType, language)
            contentType != null ->
                trendRepository.findByDateAndPeriodAndContentType(date, period, contentType)
            language != null ->
                trendRepository.findByDateAndPeriodAndLanguage(date, period, language)
            else ->
                trendRepository.findByDateAndPeriod(date, period)
        }
    }
    
    /**
     * Get trend data for a date range
     */
    fun getTrendDataForRange(
        startDate: LocalDate,
        endDate: LocalDate,
        period: String,
        contentType: String? = null,
        language: String? = null,
        page: Int = 0,
        size: Int = 100
    ): List<MisinformationTrend> {
        val pageable = org.springframework.data.domain.PageRequest.of(page, size)
        
        val trends = trendRepository.findByDateBetween(startDate, endDate, pageable).content
        
        return trends.filter { trend ->
            (contentType == null || trend.contentType == contentType) &&
            (language == null || trend.language == language) &&
            trend.period == period
        }
    }
    
    /**
     * Get records for a specific time range
     */
    private fun getRecordsForTimeRange(start: Instant, end: Instant): List<AnalysisRecord> {
        val pageable = org.springframework.data.domain.PageRequest.of(0, 10000)
        return analysisRecordRepository.findByAnalyzedAtBetween(start, end, pageable).content
    }
    
    /**
     * Calculate reliability score based on sample size
     */
    private fun calculateReliabilityScore(sampleSize: Int): Double {
        // Simple reliability calculation based on sample size
        return when {
            sampleSize >= 1000 -> 0.95
            sampleSize >= 500 -> 0.9
            sampleSize >= 100 -> 0.8
            sampleSize >= 50 -> 0.7
            sampleSize >= 20 -> 0.6
            sampleSize >= 10 -> 0.5
            else -> 0.3
        }
    }
}