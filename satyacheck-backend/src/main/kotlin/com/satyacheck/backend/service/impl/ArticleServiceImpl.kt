package com.satyacheck.backend.service.impl

import com.satyacheck.backend.model.entity.Article
import com.satyacheck.backend.repository.ArticleRepository
import com.satyacheck.backend.service.ArticleService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import java.util.logging.Logger

@Service
class ArticleServiceImpl(
    private val articleRepository: ArticleRepository
) : ArticleService {
    private val logger = Logger.getLogger(ArticleServiceImpl::class.java.name)

    @Cacheable(value = ["articles"], key = "#language")
    override fun getArticles(language: String): List<Article> {
        logger.info("Cache miss for all articles with language: $language - fetching from database")
        return articleRepository.findByLanguage(language)
    }

    @Cacheable(value = ["articleBySlug"], key = "#slug + '_' + #language")
    override fun getArticleBySlug(slug: String, language: String): Article? {
        logger.info("Cache miss for article with slug: $slug and language: $language - fetching from database")
        return articleRepository.findBySlugAndLanguage(slug, language)
    }

    @Cacheable(value = ["articlesByCategory"], key = "#category + '_' + #language")
    override fun getArticlesByCategory(category: String, language: String): List<Article> {
        logger.info("Cache miss for articles with category: $category and language: $language - fetching from database")
        return articleRepository.findByCategoryAndLanguage(category, language)
    }
    
    @Caching(evict = [
        CacheEvict(value = ["articles"], allEntries = true),
        CacheEvict(value = ["articleBySlug"], allEntries = true),
        CacheEvict(value = ["articlesByCategory"], allEntries = true)
    ])
    fun clearArticleCaches() {
        logger.info("Cleared all article caches")
    }
}