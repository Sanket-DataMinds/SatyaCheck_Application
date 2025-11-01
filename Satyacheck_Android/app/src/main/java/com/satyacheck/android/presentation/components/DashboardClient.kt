package com.satyacheck.android.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.satyacheck.android.domain.model.Verdict
import com.satyacheck.android.presentation.components.charts.BarChart
import com.satyacheck.android.presentation.components.charts.PieChart
import com.satyacheck.android.presentation.components.charts.ProgressRing
import com.satyacheck.android.presentation.components.charts.StatCard
import com.satyacheck.android.presentation.screens.dashboard.DashboardViewModel

@Composable
fun DashboardClient(
    state: DashboardViewModel.DashboardState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                value = state.userAnalysisCount.toString(),
                label = "Total Analyses",
                modifier = Modifier.weight(1f)
            )
            
            StatCard(
                value = state.articlesReadCount.toString(),
                label = "Articles Read",
                modifier = Modifier.weight(1f)
            )
            
            StatCard(
                value = state.quizCompletedCount.toString(),
                label = "Quizzes Taken",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Credibility Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calculate credibility ratio
            val credibleCount = state.credibilityBreakdown[Verdict.CREDIBLE] ?: 0
            val totalAnalyzed = state.credibilityBreakdown.values.sum().coerceAtLeast(1)
            val credibilityPercentage = credibleCount.toFloat() / totalAnalyzed
            
            ProgressRing(
                percentage = credibilityPercentage,
                label = "Credibility Ratio",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Credibility Breakdown
        CredibilityBreakdown(
            credibilityData = state.credibilityBreakdown,
            title = "Credibility Analysis"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Weekly Analysis Chart
        BarChart(
            data = state.weeklyAnalysisData,
            title = "Your Weekly Analysis Activity"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Scam Categories Pie Chart
        PieChart(
            data = state.scamCategoryData,
            title = "Scam Categories Distribution"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Recent Reported Events
        ReportedEventsList(
            events = state.recentReportedEvents,
            title = "Recent Reported Scams"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Regional Hotspot Map (a simplified version using a bar chart)
        if (state.regionalReportData.isNotEmpty()) {
            // Convert to a sorted list and take top 5 for display
            val top5Regions = state.regionalReportData.entries
                .sortedByDescending { it.value }
                .take(5)
                .associate { it.key to it.value }
                
            BarChart(
                data = top5Regions,
                title = "Top Scam Hotspots"
            )
            
            Spacer(modifier = Modifier.height(80.dp)) // Extra space at bottom for scrolling
        }
    }
}
