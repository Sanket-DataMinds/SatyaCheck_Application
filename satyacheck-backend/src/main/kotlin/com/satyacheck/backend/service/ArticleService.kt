package com.satyacheck.backend.service

import com.satyacheck.backend.model.entity.Article

interface ArticleService {
    fun getArticles(language: String): List<Article>
    fun getArticleBySlug(slug: String, language: String): Article?
    fun getArticlesByCategory(category: String, language: String): List<Article>
}