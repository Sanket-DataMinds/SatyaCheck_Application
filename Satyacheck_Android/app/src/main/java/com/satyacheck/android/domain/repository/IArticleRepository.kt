package com.satyacheck.android.domain.repository

import com.satyacheck.android.domain.model.Article
import com.satyacheck.android.domain.model.ArticleCategory

/**
 * Interface for article repository to facilitate dependency injection and testing
 */
interface IArticleRepository {
    /**
     * Get all articles for the specified language
     */
    suspend fun getArticles(language: String = "en"): List<Article>
    
    /**
     * Get a specific article by its slug
     */
    suspend fun getArticleBySlug(slug: String, language: String = "en"): Article?
    
    /**
     * Get articles filtered by category
     */
    suspend fun getArticlesByCategory(category: ArticleCategory, language: String = "en"): List<Article>
    
    /**
     * Clear the articles cache to force a refresh on next request
     */
    fun clearCache()
}