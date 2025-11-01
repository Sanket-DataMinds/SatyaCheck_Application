package com.satyacheck.android.presentation.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyacheck.android.data.repository.UserPreferencesRepository
import com.satyacheck.android.domain.model.AnalysisResult
import com.satyacheck.android.domain.model.Verdict
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.random.Random

/**
 * ViewModel for the Dashboard screen, responsible for tracking user analysis statistics,
 * scam reports, and verification metrics.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
    // In a real app, we would also inject repositories here
    // private val analysisRepository: AnalysisRepository,
    // private val reportRepository: ReportRepository
) : ViewModel() {

    // State holder for dashboard data
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    init {
        loadDashboardData()
        // Load articles read count and keep it updated
        viewModelScope.launch {
            userPreferencesRepository.getReadArticles().collectLatest { readArticles ->
                _dashboardState.update { currentState ->
                    currentState.copy(articlesReadCount = readArticles.size)
                }
            }
        }
        // Load quiz completion count and keep it updated
        viewModelScope.launch {
            userPreferencesRepository.getCompletedQuizzes().collectLatest { completedQuizzes ->
                _dashboardState.update { currentState ->
                    currentState.copy(quizCompletedCount = completedQuizzes.size)
                }
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // In a real app, we would fetch this data from repositories
            // For now, we'll generate sample data
            
            // Generate user statistics
            val userAnalysisCount = 47
            val credibilityBreakdown = mapOf(
                Verdict.CREDIBLE to 18,
                Verdict.POTENTIALLY_MISLEADING to 14,
                Verdict.HIGH_MISINFORMATION_RISK to 9,
                Verdict.SCAM_ALERT to 6
            )
            
            // Generate weekly analysis data for chart
            val weeklyAnalysis = generateWeeklyAnalysisData()
            
            // Generate scam categories data for pie chart
            val scamCategories = mapOf(
                "Phishing" to 34,
                "Fake News" to 27,
                "Misinformation" to 19,
                "Deepfakes" to 12,
                "Other" to 8
            )
            
            // Generate regional scam report data
            val regionalReports = mapOf(
                "Delhi" to 156,
                "Mumbai" to 142,
                "Bangalore" to 98,
                "Hyderabad" to 85,
                "Chennai" to 76,
                "Kolkata" to 64,
                "Pune" to 52,
                "Ahmedabad" to 48
            )
            
            // Generate recent reported events
            val recentReports = listOf(
                ReportedEvent(
                    id = "1",
                    title = "WhatsApp Job Scam",
                    description = "Fake job offers promising high pay for minimal work",
                    date = "08 Sep 2025",
                    category = "Phishing",
                    reportCount = 28
                ),
                ReportedEvent(
                    id = "2",
                    title = "Vaccine Misinformation",
                    description = "False claims about vaccine side effects spreading on social media",
                    date = "05 Sep 2025",
                    category = "Misinformation",
                    reportCount = 42
                ),
                ReportedEvent(
                    id = "3",
                    title = "Banking SMS Fraud",
                    description = "SMS claiming bank account suspension requesting urgent action",
                    date = "03 Sep 2025",
                    category = "Phishing",
                    reportCount = 35
                ),
                ReportedEvent(
                    id = "4",
                    title = "AI-Generated Celebrity Video",
                    description = "Deepfake video of political figure making controversial statement",
                    date = "01 Sep 2025",
                    category = "Deepfakes",
                    reportCount = 24
                )
            )
            
            // Update the state
            _dashboardState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    userAnalysisCount = userAnalysisCount,
                    credibilityBreakdown = credibilityBreakdown,
                    weeklyAnalysisData = weeklyAnalysis,
                    scamCategoryData = scamCategories,
                    regionalReportData = regionalReports,
                    recentReportedEvents = recentReports
                )
            }
        }
    }
    
    private fun generateWeeklyAnalysisData(): Map<String, Int> {
        val formatter = DateTimeFormatter.ofPattern("dd MMM")
        val today = LocalDate.now()
        
        // Generate data for the last 7 days
        return (6 downTo 0).associate { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val dateStr = date.format(formatter)
            // Generate random count between 1-8
            dateStr to Random.nextInt(1, 9)
        }
    }

    // Data model for the dashboard state
    data class DashboardState(
        val isLoading: Boolean = true,
        val userAnalysisCount: Int = 0,
        val articlesReadCount: Int = 0,
        val quizCompletedCount: Int = 0,
        val credibilityBreakdown: Map<Verdict, Int> = emptyMap(),
        val weeklyAnalysisData: Map<String, Int> = emptyMap(),
        val scamCategoryData: Map<String, Int> = emptyMap(),
        val regionalReportData: Map<String, Int> = emptyMap(),
        val recentReportedEvents: List<ReportedEvent> = emptyList()
    )
    
    // Model for reported scam events
    data class ReportedEvent(
        val id: String,
        val title: String,
        val description: String,
        val date: String,
        val category: String,
        val reportCount: Int
    )
}
