package com.satyacheck.backend.repository

import com.satyacheck.backend.model.entity.Article
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleRepository : MongoRepository<Article, String> {
    fun findBySlugAndLanguage(slug: String, language: String): Article?
    fun findByLanguage(language: String): List<Article>
    fun findByCategoryAndLanguage(category: String, language: String): List<Article>
}