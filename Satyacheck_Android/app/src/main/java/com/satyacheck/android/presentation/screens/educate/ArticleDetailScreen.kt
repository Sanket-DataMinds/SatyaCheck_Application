package com.satyacheck.android.presentation.screens.educate

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.satyacheck.android.domain.model.Article
import com.satyacheck.android.presentation.components.AppHeader
import com.satyacheck.android.utils.LanguageManager

@Composable
fun ArticleErrorState(
    error: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Error Loading Article",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Retry")
        }
    }
}

@Composable
fun ArticleNotFoundState(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Not Found",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Article Not Found",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "We couldn't find the article you're looking for. It may have been removed or the link might be incorrect.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Go Back")
        }
    }
}

@Composable
fun ArticleLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // You might want to add a loading animation here
            Text(
                text = "Loading Article...",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ArticleContent(
    article: Article,
    isRead: Boolean,
    onShareArticle: () -> Unit,
    onToggleBookmark: () -> Unit,
    isBookmarked: Boolean,
    onMarkAsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Article title
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Date and read status
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Since we don't have a date field, we can either remove this or use a placeholder
            Text(
                text = "Educational Article",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            if (isRead) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Read",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "Read",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Share button
            IconButton(onClick = onShareArticle) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Bookmark button
            IconButton(onClick = onToggleBookmark) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (isBookmarked) "Remove Bookmark" else "Add Bookmark",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // Mark as read button (only if not already read)
            if (!isRead) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onMarkAsRead,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Mark as Read")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Divider
        Divider()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Article content
        if (article.content != null) {
            Text(
                text = article.content,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                text = "No content available for this article.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        // Add more spacing at the bottom for better readability
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ArticleDetailScreen(
    slug: String,
    onBackPressed: () -> Unit,
    educateViewModel: EducateViewModel = hiltViewModel(),
    repositoryProvider: ArticleRepositoryProvider = hiltViewModel(),
    articleDetailViewModel: ArticleDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ArticleDetailViewModel(
                    repositoryProvider.articleRepository,
                    slug
                ) as T
            }
        }
    )
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val language = LanguageManager.getCurrentLanguageCode(context)

    // Load the article
    LaunchedEffect(slug, language) {
        articleDetailViewModel.loadArticle(language)
    }

    // Get article from view model
    val article = articleDetailViewModel.article

    // Check if the article is already marked as read
    val isRead = educateViewModel.isArticleRead(slug)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppHeader(
                title = article?.title ?: "",
                showBackButton = true,
                onBackPressed = onBackPressed
            )
        }
    ) { paddingValues ->

        if (articleDetailViewModel.isLoading) {
            ArticleLoadingState(
                modifier = Modifier.padding(paddingValues)
            )
        } else if (articleDetailViewModel.error != null) {
            ArticleErrorState(
                error = articleDetailViewModel.error ?: "Unknown error occurred",
                modifier = Modifier.padding(paddingValues),
                onRetry = {
                    articleDetailViewModel.loadArticle(language)
                }
            )
        } else if (article == null) {
            ArticleNotFoundState(
                modifier = Modifier.padding(paddingValues),
                onBack = onBackPressed
            )
        } else {
            // Article is loaded successfully
            val isBookmarked = educateViewModel.isArticleBookmarked(slug)
            
            ArticleContent(
                article = article,
                isRead = isRead,
                onShareArticle = {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "Check out this article: ${article.title}")
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Article"))
                },
                onToggleBookmark = {
                    if (isBookmarked) {
                        educateViewModel.removeBookmark(slug)
                    } else {
                        educateViewModel.addBookmark(slug)
                    }
                },
                isBookmarked = isBookmarked,
                onMarkAsRead = {
                    educateViewModel.markArticleAsRead(slug)
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}