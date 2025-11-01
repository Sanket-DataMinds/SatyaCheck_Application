package com.satyacheck.android.presentation.screens.educate

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.satyacheck.android.domain.model.Article
import com.satyacheck.android.domain.model.ArticleCategory
import com.satyacheck.android.presentation.components.AnimatedSearchHeader
import com.satyacheck.android.presentation.components.AppHeader
import com.satyacheck.android.presentation.components.MainLayout
import com.satyacheck.android.presentation.components.openAppDrawer
import com.satyacheck.android.presentation.screens.educate.components.ArticleSearchResultItem
import com.satyacheck.android.utils.LanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducateScreen(
    onNavigateToAnalyze: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToArticle: (String) -> Unit,
    onNavigateToMap: () -> Unit = {},
    onNavigateToProfile: () -> Unit = { /* Navigate to profile */ },
    onNavigateToAwareness: () -> Unit = { /* Navigate to awareness */ },
    viewModel: EducateViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val language = LanguageManager.getCurrentLanguageCode(context)
    
    // Load articles
    LaunchedEffect(language) {
        viewModel.loadArticles(language)
    }
    
    MainLayout(
        currentRoute = "educate",
        onNavigate = { route ->
            when (route) {
                "analyze" -> onNavigateToAnalyze()
                "dashboard" -> onNavigateToDashboard()
                "community" -> onNavigateToCommunity()
                "settings" -> onNavigateToSettings()
                "map" -> onNavigateToMap()
            }
        },
        snackbarHostState = snackbarHostState,
        drawerState = drawerState,
        topBar = {
            AnimatedSearchHeader(
                title = "Educate",
                onQueryChange = { query -> viewModel.updateSearchQuery(query) },
                onSearch = { query -> viewModel.submitSearch() },
                showMenuButton = true,
                onMenuClicked = { openAppDrawer(drawerState, scope) },
                showNotificationBell = true,
                unreadNotificationsCount = 0,
                onNotificationBellClicked = { onNavigateToProfile() },
                placeholderText = "Search articles and guides",
                hideSearchIcon = true
            )
        }
    ) {
        if (viewModel.isSearchActive && viewModel.searchQuery.isNotEmpty()) {
            // Show search results
            if (viewModel.filteredArticles.isEmpty()) {
                // Show no results found
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No articles found for '${viewModel.searchQuery}'",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Show search results
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                    
                    items(viewModel.filteredArticles) { article ->
                        ArticleSearchResultItem(
                            article = article,
                            onArticleClick = onNavigateToArticle
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        } else {
            // Show normal content with pull-to-refresh
            val swipeRefreshState = rememberSwipeRefreshState(
                isRefreshing = viewModel.isRefreshing
            )
            
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    scope.launch {
                        viewModel.refreshArticles(language)
                    }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                // Learning Progress Card
                LearningProgressCard(
                    viewModel = viewModel,
                    onNavigateToAwareness = onNavigateToAwareness
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Core Concepts Accordion Section
                ArticleAccordion(
                    title = "Core Concepts",
                    articles = viewModel.coreConcepts,
                    onArticleClick = onNavigateToArticle
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Common Scams Accordion Section
                ArticleAccordion(
                    title = "Common Scams",
                    articles = viewModel.commonScams,
                    onArticleClick = onNavigateToArticle
                )
                
                // Add spacer at the bottom to prevent content from being hidden behind the navigation bar
                Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun LearningProgressCard(
    viewModel: EducateViewModel = hiltViewModel(),
    onNavigateToAwareness: () -> Unit = {}
) {
    // Calculate progress based on articles read and total articles
    val totalArticles = viewModel.allArticles.size
    val articlesRead = viewModel.articlesReadCount
    val progress = if (totalArticles > 0) articlesRead.toFloat() / totalArticles else 0f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Your Learning Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Overall Completion",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Articles Read
                StatItem(
                    value = articlesRead.toString(),
                    label = "Articles Read"
                )
                
                // Quizzes Taken
                StatItem(
                    value = viewModel.quizCompletedCount.toString(),
                    label = "Quizzes Taken"
                )
                
                // Verification Skill
                StatItem(
                    value = "Beginner",
                    label = "Verification Skill"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { onNavigateToAwareness() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Check Your Awareness")
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ArticleAccordion(
    title: String,
    articles: List<Article>,
    onArticleClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // Accordion Header (always visible)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
        }
        
        // Accordion Content (visible when expanded)
        if (expanded) {
            if (articles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading articles...")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(((articles.size + 1) / 2 * 140).coerceAtMost(420).dp)
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    content = {
                        items(articles) { article ->
                            ArticleCard(article = article, onClick = { onArticleClick(article.slug) })
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(
        article.image, "drawable", context.packageName
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Article Image
            if (resourceId != 0) {
                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder if image not found
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(
                            if (article.title.contains("Job and Employment", ignoreCase = true))
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (article.title.contains("Job and Employment", ignoreCase = true)) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BusinessCenter,
                                contentDescription = article.title,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    } else {
                        Text(
                            text = article.title.take(1),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Article Title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
