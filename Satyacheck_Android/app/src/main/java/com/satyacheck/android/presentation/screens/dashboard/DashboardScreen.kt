package com.satyacheck.android.presentation.screens.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.satyacheck.android.R
import com.satyacheck.android.presentation.components.AppHeaderWithNotifications
import com.satyacheck.android.presentation.components.DashboardClient
import com.satyacheck.android.presentation.components.MainLayout
import com.satyacheck.android.presentation.components.openAppDrawer

@Composable
fun DashboardScreen(
    navController: NavController,
    onNavigateToAnalyze: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToEducate: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMap: () -> Unit = {}
) {
    val viewModel = hiltViewModel<DashboardViewModel>()
    val state by viewModel.dashboardState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    MainLayout(
        currentRoute = "dashboard",
        onNavigate = { route ->
            when (route) {
                "analyze" -> onNavigateToAnalyze()
                "community" -> onNavigateToCommunity()
                "educate" -> onNavigateToEducate()
                "settings" -> onNavigateToSettings()
                "map" -> onNavigateToMap()
            }
        },
        snackbarHostState = snackbarHostState,
        drawerState = drawerState,
        topBar = {
            AppHeaderWithNotifications(
                title = "SatyaCheck",
                navController = navController,
                showMenuButton = true,
                onMenuClicked = { openAppDrawer(drawerState, scope) },
                useAnimatedTitle = true
            )
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                DashboardClient(state = state)
            }
        }
    }
}
