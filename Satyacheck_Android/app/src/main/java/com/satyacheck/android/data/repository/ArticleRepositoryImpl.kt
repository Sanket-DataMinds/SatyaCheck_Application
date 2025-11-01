package com.satyacheck.android.data.repository

import com.satyacheck.android.data.mappers.EducationalContentMapper
import com.satyacheck.android.data.remote.api.SatyaCheckApiClient
import com.satyacheck.android.domain.model.Article
import com.satyacheck.android.domain.model.ArticleCategory
import com.satyacheck.android.domain.repository.IArticleRepository
import com.satyacheck.android.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository to manage educational articles with API integration
 * Uses a fallback mechanism to provide local content when the API is unavailable
 */
@Singleton
class ArticleRepositoryImpl @Inject constructor(
    private val apiClient: SatyaCheckApiClient,
    private val localArticleRepository: ArticleRepository // Inject the original repository for fallback
) : IArticleRepository {
    // Cache the articles to reduce API calls
    private var articlesCache: Map<String, List<Article>> = mutableMapOf()
    
    /**
     * Get all articles for the specified language from the backend API
     * Falls back to local data if the API call fails
     */
    override suspend fun getArticles(language: String): List<Article> {
        // Check cache first
        if (articlesCache.containsKey(language)) {
            return articlesCache[language] ?: emptyList()
        }
        
        return try {
            withContext(Dispatchers.IO) {
                val response = apiClient.apiService.getArticles(
                    language = language
                )
                
                // Check if response is successful
                if (response.isSuccessful && response.body() != null) {
                    val articles = response.body() ?: emptyList()
                    // Update cache
                    (articlesCache as MutableMap)[language] = articles
                    articles
                } else {
                    // Fallback to local data if API returns error
                    val fallbackArticles = localArticleRepository.getArticles(language)
                    (articlesCache as MutableMap)[language] = fallbackArticles
                    fallbackArticles
                }
            }
        } catch (e: Exception) {
            // Fallback to local data on any exception
            val fallbackArticles = localArticleRepository.getArticles(language)
            (articlesCache as MutableMap)[language] = fallbackArticles
            fallbackArticles
        }
    }
    
    /**
     * Get a specific article by its slug
     */
    override suspend fun getArticleBySlug(slug: String, language: String): Article? {
        // Try to get from cache first
        articlesCache[language]?.find { it.slug == slug }?.let { return it }
        
        // If not in cache, try to fetch directly from the API
        try {
            val response = withContext(Dispatchers.IO) {
                apiClient.apiService.getArticleBySlug(
                    slug = slug,
                    language = language
                )
            }
            
            if (response.isSuccessful && response.body() != null) {
                return response.body()
            }
        } catch (e: Exception) {
            // If direct fetch fails, continue with fallback approach
        }
        
        // If direct fetch fails, try to get from all articles (which will update cache)
        getArticles(language)
        
        // Then try to find in the updated cache
        return articlesCache[language]?.find { it.slug == slug } 
            ?: localArticleRepository.getArticleBySlug(slug, language) // Fallback
    }
    
    /**
     * Get articles filtered by category
     */
    override suspend fun getArticlesByCategory(
        category: ArticleCategory, 
        language: String
    ): List<Article> {
        return getArticles(language).filter { it.category == category }
    }
    
    /**
     * Clear the articles cache to force a refresh on next request
     */
    override fun clearCache() {
        articlesCache = mutableMapOf()
    }
}