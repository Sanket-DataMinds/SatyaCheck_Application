package com.satyacheck.android.data.repository

import com.satyacheck.android.data.remote.api.SatyaCheckApiClient
import com.satyacheck.android.domain.model.AnalysisResult
import com.satyacheck.android.domain.model.Verdict
import com.satyacheck.android.domain.repository.AnalysisRepository
import com.satyacheck.android.utils.Result
import com.satyacheck.android.utils.TextAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AnalysisRepository that uses both backend API and local TextAnalyzer
 */
@Singleton
class AnalysisRepositoryImpl @Inject constructor(
    private val textAnalyzer: TextAnalyzer,
    private val apiClient: SatyaCheckApiClient
) : AnalysisRepository {
    
    override suspend fun analyzeFoodItem(
        foodName: String,
        region: String,
        price: Double,
        description: String,
        userRating: Int
    ): Result<AnalysisResult> {
        // Combine all parameters into a single prompt for analysis
        val prompt = buildString {
            append("Analyze the authenticity of this food item:\n")
            append("Food name: $foodName\n")
            append("Region purchased: $region\n")
            append("Price paid: $price\n")
            if (description.isNotBlank()) {
                append("Additional context: $description\n")
            }
            append("User rating (1-5): $userRating\n")
        }
        
        return try {
            // Try to use the backend API first
            withContext(Dispatchers.IO) {
                try {
                    val response = apiClient.apiService.analyzeContent(
                        content = prompt,
                        language = "en",
                        source = "mobile-app",
                        contentType = "food-item",
                        userRegion = region
                    )
                    
                    if (response.isSuccessful && response.body() != null) {
                        val analysis = response.body()!!.analysis
                        
                        // Map the API verdict to our app's Verdict enum
                        val verdict = when(analysis.verdict.lowercase()) {
                            "credible" -> Verdict.CREDIBLE
                            "potentially misleading" -> Verdict.POTENTIALLY_MISLEADING
                            "high misinformation risk" -> Verdict.HIGH_MISINFORMATION_RISK
                            "scam alert" -> Verdict.SCAM_ALERT
                            else -> Verdict.POTENTIALLY_MISLEADING
                        }
                        
                        Result.Success(
                            AnalysisResult(
                                verdict = verdict,
                                explanation = analysis.explanation
                            )
                        )
                    } else {
                        // Return error response instead of falling back to Gemini API
                        val errorCode = response.code()
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        Result.Error(Exception("Backend API error (Code: $errorCode): $errorBody"))
                    }
                } catch (e: Exception) {
                    // Return error response instead of falling back to Gemini API
                    Result.Error(Exception("Connection error to backend: ${e.message}"))
                }
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}