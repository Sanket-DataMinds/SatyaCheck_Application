package com.satyacheck.android.presentation.screens.educate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.satyacheck.android.domain.model.Article
import com.satyacheck.android.domain.repository.IArticleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

/**
 * ViewModel for the ArticleDetailScreen, responsible for loading a specific article
 * This uses an AssistedInject factory pattern to accept the article slug as a parameter
 */
class ArticleDetailViewModel @AssistedInject constructor(
    private val articleRepository: IArticleRepository,
    @Assisted private val slug: String
) : ViewModel() {
    
    // Article state
    var article by mutableStateOf<Article?>(null)
        private set
    
    var isLoading by mutableStateOf(false)
        private set
        
    var error by mutableStateOf<String?>(null)
        private set
    
    // Factory for creating this ViewModel with parameters
    @AssistedFactory
    interface Factory {
        fun create(slug: String): ArticleDetailViewModel
    }
    
    // Provider for the ViewModelProvider.Factory
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            factory: Factory,
            slug: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return factory.create(slug) as T
            }
        }
    }
    
    /**
     * Loads the article with the specified slug and language
     */
    fun loadArticle(language: String = "en") {
        viewModelScope.launch {
            isLoading = true
            error = null
            
            try {
                article = articleRepository.getArticleBySlug(slug, language)
                
                if (article == null) {
                    error = "Article not found"
                }
            } catch (e: Exception) {
                error = e.message ?: "Error loading article"
            } finally {
                isLoading = false
            }
        }
    }
}