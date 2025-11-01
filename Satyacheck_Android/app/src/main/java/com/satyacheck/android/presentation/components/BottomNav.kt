package com.satyacheck.android.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.satyacheck.android.R
import com.satyacheck.android.presentation.navigation.NavigationItem

/**
 * Modern Google-style bottom navigation bar with animated selections and clean visuals
 *
 * @param currentRoute The currently active route
 * @param onNavigate Callback when a navigation item is selected
 * @param showLabels Whether to show labels for the navigation items (Google's newer apps often hide them)
 */
@Composable
fun BottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    showLabels: Boolean = true
) {
    val items = listOf(
        NavigationItem(
            title = stringResource(id = R.string.nav_analyze),
            route = "analyze",
            icon = "analyze"
        ),
        NavigationItem(
            title = stringResource(id = R.string.nav_educate),
            route = "educate",
            icon = "educate"
        ),
        NavigationItem(
            title = stringResource(id = R.string.nav_community),
            route = "community",
            icon = "community"
        ),
        NavigationItem(
            title = stringResource(id = R.string.nav_map),
            route = "map",
            icon = "map"
        )
    )
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (showLabels) 80.dp else 64.dp) // Increased height to accommodate text and icon
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    icon = {
                        // Use a Column to properly align icon and text
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Fixed size icon to ensure consistent spacing
                            Icon(
                                imageVector = getIconForNavItem(item.icon),
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(bottom = if (showLabels) 2.dp else 0.dp)
                            )
                            
                            // Add a small spacer if labels are shown
                            if (showLabels) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    },
                    label = {
                        AnimatedVisibility(
                            visible = showLabels,
                            enter = fadeIn(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                                    expandVertically(animationSpec = tween(150, easing = FastOutSlowInEasing)),
                            exit = fadeOut(animationSpec = tween(150, easing = FastOutSlowInEasing)) +
                                    shrinkVertically(animationSpec = tween(150, easing = FastOutSlowInEasing))
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp, // Slightly smaller text to prevent overlap
                                    lineHeight = 14.sp
                                ),
                                textAlign = TextAlign.Center,
                                maxLines = 1 // Ensure text is single line
                            )
                        }
                    },
                    selected = currentRoute == item.route,
                    onClick = { onNavigate(item.route) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
