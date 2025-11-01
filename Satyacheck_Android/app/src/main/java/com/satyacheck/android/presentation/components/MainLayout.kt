package com.satyacheck.android.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * A standardized layout component that provides consistent spacing and padding
 * for screens with bottom navigation and a drawer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    drawerState: DrawerState,
    topBar: @Composable () -> Unit = {},
    showBottomNav: Boolean = true,
    showLabels: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    AppDrawer(
        drawerState = drawerState,
        currentRoute = currentRoute,
        onNavigate = onNavigate
    ) {
        Scaffold(
            topBar = topBar,
            bottomBar = {
                if (showBottomNav) {
                    // Add proper insets padding to prevent navigation overlapping with system bars
                    Box(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        BottomNav(
                            currentRoute = currentRoute,
                            onNavigate = onNavigate,
                            showLabels = showLabels
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            // Apply proper content padding
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                content = content
            )
        }
    }
}

/**
 * Opens the app drawer
 */
@OptIn(ExperimentalMaterial3Api::class)
fun openAppDrawer(drawerState: DrawerState, scope: kotlinx.coroutines.CoroutineScope) {
    scope.launch {
        drawerState.open()
    }
}
