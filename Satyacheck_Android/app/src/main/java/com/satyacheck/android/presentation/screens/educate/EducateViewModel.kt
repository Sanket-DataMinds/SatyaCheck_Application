package com.satyacheck.android.presentation.screens.educate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyacheck.android.data.repository.UserPreferencesRepository
import com.satyacheck.android.domain.model.Article
import com.satyacheck.android.domain.model.ArticleCategory
import com.satyacheck.android.domain.repository.IArticleRepository
import com.satyacheck.android.utils.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EducateViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val articleRepository: IArticleRepository
) : ViewModel() {
    // Search state
    var searchQuery by mutableStateOf("")
        private set
    var isSearchActive by mutableStateOf(false)
        private set
    
    // Article state
    var allArticles by mutableStateOf<List<Article>>(emptyList())
        private set
    var coreConcepts by mutableStateOf<List<Article>>(emptyList())
        private set
    var commonScams by mutableStateOf<List<Article>>(emptyList())
        private set
    var filteredArticles by mutableStateOf<List<Article>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    
    // Read articles tracking
    var readArticles by mutableStateOf<Set<String>>(emptySet())
        private set
    var articlesReadCount by mutableStateOf(0)
        private set
    
    // Quiz completion tracking
    var completedQuizzes by mutableStateOf<Set<String>>(emptySet())
        private set
    var quizCompletedCount by mutableStateOf(0)
        private set
    
    // Article rotation tracking
    private var displayedArticleIndices by mutableStateOf<Pair<List<Int>, List<Int>>>(
        Pair(emptyList(), emptyList())
    )
    var isRefreshing by mutableStateOf(false)
        private set
    
    init {
        // Load read articles from preferences when ViewModel is initialized
        viewModelScope.launch {
            userPreferencesRepository.getReadArticles().collectLatest { articles ->
                readArticles = articles
                articlesReadCount = articles.size
            }
        }
        // Load completed quizzes from preferences when ViewModel is initialized
        viewModelScope.launch {
            userPreferencesRepository.getCompletedQuizzes().collectLatest { quizzes ->
                completedQuizzes = quizzes
                quizCompletedCount = quizzes.size
            }
        }
    }
    
    // Load articles with rotation
    fun loadArticles(languageCode: String) {
        isLoading = true
        viewModelScope.launch {
            try {
                allArticles = articleRepository.getArticles(languageCode)
                val allCoreConcepts = articleRepository.getArticlesByCategory(ArticleCategory.CORE_CONCEPTS, languageCode)
                val allCommonScams = articleRepository.getArticlesByCategory(ArticleCategory.COMMON_SCAMS, languageCode)
                
                // Apply rotation logic to show 6 articles from each category
                rotateArticles(allCoreConcepts, allCommonScams)
            } finally {
                isLoading = false
            }
        }
    }
    
    // Rotate articles to show different sets
    private fun rotateArticles(allCoreConcepts: List<Article>, allCommonScams: List<Article>) {
        val coreConceptsCount = allCoreConcepts.size
        val commonScamsCount = allCommonScams.size
        
        // Generate random indices ensuring no immediate repetition
        val newCoreIndices = generateRotationIndices(coreConceptsCount, 6, displayedArticleIndices.first)
        val newScamIndices = generateRotationIndices(commonScamsCount, 6, displayedArticleIndices.second)
        
        // Update displayed articles
        coreConcepts = newCoreIndices.map { allCoreConcepts[it] }
        commonScams = newScamIndices.map { allCommonScams[it] }
        
        // Store current indices for next rotation
        displayedArticleIndices = Pair(newCoreIndices, newScamIndices)
    }
    
    // Generate rotation indices avoiding recent repetition
    private fun generateRotationIndices(totalCount: Int, displayCount: Int, previousIndices: List<Int>): List<Int> {
        val availableIndices = (0 until totalCount).toMutableList()
        
        // Remove previously displayed indices to avoid immediate repetition
        availableIndices.removeAll(previousIndices)
        
        // If we don't have enough new articles, add back some old ones
        if (availableIndices.size < displayCount) {
            val neededIndices = displayCount - availableIndices.size
            val reusableIndices = previousIndices.shuffled().take(neededIndices)
            availableIndices.addAll(reusableIndices)
        }
        
        return availableIndices.shuffled().take(displayCount)
    }
    
    // Refresh articles (pull-to-refresh functionality)
    fun refreshArticles(languageCode: String = "en") {
        isRefreshing = true
        viewModelScope.launch {
            try {
                // Small delay for smooth refresh animation
                kotlinx.coroutines.delay(500)
                
                val allCoreConcepts = articleRepository.getArticlesByCategory(ArticleCategory.CORE_CONCEPTS, languageCode)
                val allCommonScams = articleRepository.getArticlesByCategory(ArticleCategory.COMMON_SCAMS, languageCode)
                
                // Rotate to new articles
                rotateArticles(allCoreConcepts, allCommonScams)
            } finally {
                isRefreshing = false
            }
        }
    }
    
    // Update search query
    fun updateSearchQuery(query: String) {
        searchQuery = query
        isSearchActive = query.isNotEmpty()
        
        if (query.isEmpty()) {
            filteredArticles = emptyList()
            return
        }
        
        // Filter articles based on search query
        filteredArticles = allArticles.filter { article ->
            article.title.contains(query, ignoreCase = true) ||
            article.description?.contains(query, ignoreCase = true) == true ||
            article.content?.contains(query, ignoreCase = true) == true
        }
    }
    
    // Submit search (can be used for analytics or other purposes)
    fun submitSearch() {
        // Log search event or perform other operations
    }
    
    // Clear search
    fun clearSearch() {
        searchQuery = ""
        isSearchActive = false
        filteredArticles = emptyList()
    }
    
    // Navigate to Awareness Screen
    fun navigateToAwareness() {
        // This will be called when the Check Awareness button is clicked
        // The actual navigation will be handled by the NavController in the UI
    }
    
    // Mark an article as read
    fun markArticleAsRead(articleSlug: String) {
        if (!readArticles.contains(articleSlug)) {
            viewModelScope.launch {
                userPreferencesRepository.addReadArticle(articleSlug)
            }
        }
    }
    
    // Check if an article has been read
    fun isArticleRead(articleSlug: String): Boolean {
        return readArticles.contains(articleSlug)
    }
    
    // Track bookmarked articles
    private var bookmarkedArticles = mutableSetOf<String>()
    
    // Add an article to bookmarks
    fun addBookmark(articleSlug: String) {
        bookmarkedArticles.add(articleSlug)
        // In a real app, you would persist this to preferences/database
    }
    
    // Remove an article from bookmarks
    fun removeBookmark(articleSlug: String) {
        bookmarkedArticles.remove(articleSlug)
        // In a real app, you would persist this to preferences/database
    }
    
    // Check if an article is bookmarked
    fun isArticleBookmarked(articleSlug: String): Boolean {
        return bookmarkedArticles.contains(articleSlug)
    }
}
