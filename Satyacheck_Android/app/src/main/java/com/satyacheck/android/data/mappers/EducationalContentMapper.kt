package com.satyacheck.android.data.mappers

import com.satyacheck.android.data.remote.dto.EducationalContentDto
import com.satyacheck.android.domain.model.Article
import com.satyacheck.android.domain.model.ArticleCategory

/**
 * Mapper to convert between EducationalContentDto and Article domain model
 */
object EducationalContentMapper {
    
    /**
     * Maps EducationalContentDto to Article domain model
     */
    fun mapToArticle(dto: EducationalContentDto): Article {
        // Extract slug from id or use id as fallback
        val slug = dto.id.split("-").joinToString("-").lowercase()
        
        // Map category string to ArticleCategory enum
        val category = try {
            ArticleCategory.fromString(dto.category)
        } catch (e: IllegalArgumentException) {
            // Default to CORE_CONCEPTS if category cannot be mapped
            ArticleCategory.CORE_CONCEPTS
        }
        
        // Map image URL to local drawable resource if available, or default to a placeholder
        val imageResource = mapImageUrlToDrawable(dto.imageUrl)
        
        return Article(
            slug = slug,
            title = dto.title,
            description = dto.summary,
            image = imageResource,
            category = category,
            content = dto.content
        )
    }
    
    /**
     * Maps a list of EducationalContentDto to a list of Article domain models
     */
    fun mapToArticles(dtos: List<EducationalContentDto>): List<Article> {
        return dtos.map { mapToArticle(it) }
    }
    
    /**
     * Maps an image URL to a local drawable resource name
     * This provides fallback to local resources when backend images are not available
     */
    private fun mapImageUrlToDrawable(imageUrl: String?): String {
        if (imageUrl == null) return "article_digital_literacy" // Default placeholder
        
        // Try to map URL to local drawable based on keywords in the URL
        return when {
            imageUrl.contains("psychology") || imageUrl.contains("misinformation") -> "article_misinformation"
            imageUrl.contains("verification") || imageUrl.contains("fact") -> "article_verification"
            imageUrl.contains("digital") || imageUrl.contains("literacy") -> "article_digital_literacy"
            imageUrl.contains("phishing") -> "article_phishing"
            imageUrl.contains("scam") -> "article_scams"
            else -> "article_digital_literacy" // Default fallback
        }
    }
}