package com.satyacheck.android.domain.repository

import com.satyacheck.android.domain.model.AnalysisResult
import com.satyacheck.android.utils.Result

interface AnalysisRepository {
    /**
     * Analyzes a food item for authenticity
     *
     * @param foodName The name of the food item
     * @param region The region where the food was purchased
     * @param price The price paid for the food
     * @param description Additional description or context about the food
     * @param userRating The user's subjective rating of the food (1-5)
     * @return Result<AnalysisResult> with analysis results or error
     */
    suspend fun analyzeFoodItem(
        foodName: String,
        region: String,
        price: Double,
        description: String,
        userRating: Int
    ): Result<AnalysisResult>
}