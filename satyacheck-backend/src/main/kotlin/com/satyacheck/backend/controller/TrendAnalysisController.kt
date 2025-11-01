package com.satyacheck.backend.controller

import com.satyacheck.backend.model.entity.AnalysisRecord
import com.satyacheck.backend.model.entity.MisinformationTrend
import com.satyacheck.backend.service.trends.AnalysisRecordService
import com.satyacheck.backend.service.trends.TrendAnalysisService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.logging.Logger

/**
 * Controller for trend analysis operations
 */
@RestController
@RequestMapping("/api/v1/trends")
class TrendAnalysisController(
    private val trendAnalysisService: TrendAnalysisService,
    private val analysisRecordService: AnalysisRecordService
) {
    private val logger = Logger.getLogger(TrendAnalysisController::class.java.name)

    /**
     * Get trend data for a specific date and period
     */
    @GetMapping("/date/{date}")
    fun getTrendByDate(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestParam(defaultValue = "DAILY") period: String,
        @RequestParam(required = false) contentType: String?,
        @RequestParam(required = false) language: String?
    ): ResponseEntity<List<MisinformationTrend>> {
        logger.info("Getting $period trend data for date: $date")
        
        val trends = trendAnalysisService.getTrendData(date, period, contentType, language)
        
        return if (trends.isEmpty()) {
            ResponseEntity.notFound().build()
        } else {
            ResponseEntity.ok(trends)
        }
    }

    /**
     * Get trend data for a date range
     */
    @GetMapping("/range")
    fun getTrendsByRange(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
        @RequestParam(defaultValue = "DAILY") period: String,
        @RequestParam(required = false) contentType: String?,
        @RequestParam(required = false) language: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "100") size: Int
    ): ResponseEntity<List<MisinformationTrend>> {
        logger.info("Getting $period trend data for range: $startDate to $endDate")
        
        val trends = trendAnalysisService.getTrendDataForRange(
            startDate, endDate, period, contentType, language, page, size
        )
        
        return ResponseEntity.ok(trends)
    }

    /**
     * Find similar content from analysis records
     */
    @PostMapping("/similar-content")
    fun findSimilarContent(
        @RequestParam content: String,
        @RequestParam(defaultValue = "5") limit: Int
    ): ResponseEntity<List<AnalysisRecord>> {
        logger.info("Finding similar content for analysis")
        
        val records = analysisRecordService.findSimilarRecords(content, limit)
        
        return ResponseEntity.ok(records)
    }

    /**
     * Generate trends for a specific date (admin endpoint)
     */
    @PostMapping("/admin/generate/daily/{date}")
    fun generateDailyTrends(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<Map<String, String>> {
        logger.info("Generating daily trends for date: $date")
        
        trendAnalysisService.generateTrendsForDate(date, "DAILY")
        
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "message" to "Generated daily trends for $date"
        ))
    }

    /**
     * Generate weekly trends (admin endpoint)
     */
    @PostMapping("/admin/generate/weekly/{endDate}")
    fun generateWeeklyTrends(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<Map<String, String>> {
        logger.info("Generating weekly trends for end date: $endDate")
        
        val startDate = endDate.minusDays(6)
        trendAnalysisService.generateTrendsForDateRange(startDate, endDate, "WEEKLY")
        
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "message" to "Generated weekly trends for period ending on $endDate"
        ))
    }

    /**
     * Generate monthly trends (admin endpoint)
     */
    @PostMapping("/admin/generate/monthly/{year}/{month}")
    fun generateMonthlyTrends(
        @PathVariable year: Int,
        @PathVariable month: Int
    ): ResponseEntity<Map<String, String>> {
        logger.info("Generating monthly trends for year: $year, month: $month")
        
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        
        trendAnalysisService.generateTrendsForDateRange(startDate, endDate, "MONTHLY")
        
        return ResponseEntity.ok(mapOf(
            "status" to "success",
            "message" to "Generated monthly trends for $year-$month"
        ))
    }
}