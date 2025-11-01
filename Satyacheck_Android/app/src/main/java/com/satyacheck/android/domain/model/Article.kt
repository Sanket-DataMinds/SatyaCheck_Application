package com.satyacheck.android.domain.model

/**
 * Data model for educational articles
 */
data class Article(
    val slug: String,                // Unique identifier for the URL (e.g., "psychology-of-misinformation")
    val title: String,               // The title of the article
    val description: String,         // A short summary for the article card
    val image: String,               // Resource ID for the feature image
    val imageHint: String = "",      // A hint for AI image generation tools (used for web only)
    val category: ArticleCategory,   // Used to group articles
    val content: String              // The full text content of the article, with markdown-like formatting
)

/**
 * Categories for educational articles
 */
enum class ArticleCategory {
    CORE_CONCEPTS,
    COMMON_SCAMS;
    
    override fun toString(): String {
        return when (this) {
            CORE_CONCEPTS -> "core-concepts"
            COMMON_SCAMS -> "common-scams"
        }
    }
    
    companion object {
        fun fromString(value: String): ArticleCategory {
            return when (value.lowercase()) {
                "core-concepts" -> CORE_CONCEPTS
                "common-scams" -> COMMON_SCAMS
                else -> throw IllegalArgumentException("Unknown article category: $value")
            }
        }
    }
}
