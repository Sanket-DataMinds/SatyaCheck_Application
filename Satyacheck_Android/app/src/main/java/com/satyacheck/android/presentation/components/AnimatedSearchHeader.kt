package com.satyacheck.android.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * An enhanced animated header that smoothly transforms between a title and a search bar,
 * similar to the Google Drive app's animation. The title fades out and the search bar
 * grows to occupy approximately 65-70% of the top navigation bar width.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedSearchHeader(
    title: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    showMenuButton: Boolean = false,
    onMenuClicked: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onBackPressed: () -> Unit = {},
    showNotificationBell: Boolean = false,
    unreadNotificationsCount: Int = 0,
    onNotificationBellClicked: () -> Unit = {},
    placeholderText: String = "Search communities and posts",
    communityId: String? = null,
    hideSearchIcon: Boolean = false // Default to showing search icon for all screens
) {
    var isSearchMode by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    // Define the transition between title and search modes
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = isSearchMode
    
    val transition = updateTransition(transitionState, label = "Search Mode Transition")
    
    // Animate the corner radius of the search bar with spring animation for smoother feel
    val cornerRadius by transition.animateDp(
        label = "Corner Radius",
        transitionSpec = { 
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        }
    ) { state ->
        if (state) 28.dp else 0.dp
    }
    
    // Animate the width of the search bar with optimized timing
    val searchBarWidth by transition.animateFloat(
        label = "Search Bar Width",
        transitionSpec = { 
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        }
    ) { state ->
        if (state) 0.95f else 1f  // 95% width for all screens
    }
    
    // Animate the text alpha for a smoother fade effect
    val titleAlpha by transition.animateFloat(
        label = "Title Alpha",
        transitionSpec = { tween(durationMillis = 200) }
    ) { state ->
        if (state) 0f else 1f
    }
    
    // Animate the search icon position
    val searchIconOffset by transition.animateFloat(
        label = "Search Icon Offset",
        transitionSpec = { tween(durationMillis = 300) }
    ) { state ->
        if (state) 0f else 1f
    }
    
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Title (visible when not in search mode)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(titleAlpha)
                        .clickable(
                            enabled = hideSearchIcon,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { 
                            if (hideSearchIcon) {
                                isSearchMode = true 
                            }
                        }
                ) {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    )
                }
                
                // Search field (visible when in search mode)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (isSearchMode) searchBarWidth else 0f)
                        .alpha(if (isSearchMode) 1f else 0f)
                        .align(Alignment.Center)
                ) {
                    if (isSearchMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .clip(RoundedCornerShape(cornerRadius))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                .padding(vertical = 4.dp)
                        ) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { newValue ->
                                    searchQuery = newValue
                                    onQueryChange(newValue)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center)
                                    .focusRequester(focusRequester),
                                placeholder = { 
                                    Text(
                                        text = if (communityId != null) 
                                            "Search in this community" 
                                        else 
                                            placeholderText,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    ) 
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                textStyle = MaterialTheme.typography.bodyLarge,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        onSearch(searchQuery)
                                        focusManager.clearFocus()
                                    }
                                ),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(start = 2.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(
                                            onClick = { 
                                                searchQuery = ""
                                                onQueryChange("")
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        navigationIcon = {
            if (isSearchMode) {
                // Back button in search mode with animation
                IconButton(
                    onClick = { 
                        isSearchMode = false
                        searchQuery = ""
                        onQueryChange("")
                        focusManager.clearFocus()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else if (showMenuButton) {
                // Menu button in normal mode
                IconButton(onClick = onMenuClicked) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        actions = {
            if (!isSearchMode) {
                // Search icon in normal mode with smoother animation (hidden for Community)
                if (!hideSearchIcon) {
                    Box(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { isSearchMode = true }
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    // Spacer to provide proper spacing
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                if (showNotificationBell) {
                    // Check if current screen is Educate or Community to show Profile icon instead of bell
                    if (title == "Educate" || title == "Community") {
                        ProfilePictureWithDropdown(
                            profilePictureUrl = null,
                            onProfileClicked = onNotificationBellClicked,
                            onSignOutClicked = {}
                        )
                    } else {
                        NotificationBellWithBadge(
                            unreadCount = unreadNotificationsCount,
                            onClick = onNotificationBellClicked
                        )
                    }
                }
            }
            
            // When search mode is activated, request focus
            if (isSearchMode) {
                LaunchedEffect(Unit) {
                    delay(150)
                    focusRequester.requestFocus()
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

// NotificationBellWithBadge is now imported from NotificationBellWithBadge.kt
