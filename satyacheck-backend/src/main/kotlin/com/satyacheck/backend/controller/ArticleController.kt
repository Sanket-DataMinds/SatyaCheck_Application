package com.satyacheck.backend.controller

import com.satyacheck.backend.model.dto.ApiResponse
import com.satyacheck.backend.model.entity.Article
import com.satyacheck.backend.service.ArticleService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/public/articles")
class ArticleController(private val articleService: ArticleService) {

    // Public endpoint - accessible to all
    @GetMapping
    fun getAllArticles(@RequestParam(defaultValue = "en") language: String): ResponseEntity<ApiResponse<List<Article>>> { 
        val articles = articleService.getArticles(language)
        return ResponseEntity.ok(ApiResponse.success(articles, "Articles retrieved successfully"))
    }

    // Public endpoint - accessible to all
    @GetMapping("/{slug}")
    fun getArticleBySlug(
        @PathVariable slug: String,
        @RequestParam(defaultValue = "en") language: String
    ): ResponseEntity<ApiResponse<Article>> {
        val article = articleService.getArticleBySlug(slug, language)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(ApiResponse.success(article, "Article retrieved successfully"))
    }

    // Public endpoint - accessible to all
    @GetMapping("/category/{category}")
    fun getArticlesByCategory(
        @PathVariable category: String,
        @RequestParam(defaultValue = "en") language: String
    ): ResponseEntity<ApiResponse<List<Article>>> {
        val articles = articleService.getArticlesByCategory(category, language)
        return ResponseEntity.ok(ApiResponse.success(articles, "Articles retrieved successfully"))
    }
}
