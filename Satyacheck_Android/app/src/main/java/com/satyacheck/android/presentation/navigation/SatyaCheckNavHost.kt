package com.satyacheck.android.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.content.Intent
import com.satyacheck.android.presentation.screens.alerts.AlertsScreen
import com.satyacheck.android.presentation.screens.analyze.AnalyzeScreen
import com.satyacheck.android.presentation.screens.analysis.AnalysisResultScreen
import com.satyacheck.android.presentation.screens.auth.LoginScreen
import com.satyacheck.android.presentation.screens.auth.SignupScreen
import com.satyacheck.android.presentation.screens.awareness.AwarenessScreen
import com.satyacheck.android.presentation.screens.community.CommunityScreen
import com.satyacheck.android.presentation.screens.community.CommunitySlugScreen
import com.satyacheck.android.presentation.screens.dashboard.DashboardScreen
import com.satyacheck.android.presentation.screens.educate.ArticleDetailScreen
import com.satyacheck.android.presentation.screens.educate.EducateScreen
import com.satyacheck.android.presentation.screens.map.MapScreen
import com.satyacheck.android.presentation.screens.onboarding.OnboardingScreen
import com.satyacheck.android.presentation.screens.profile.ProfileScreen
import com.satyacheck.android.presentation.screens.report.ReportMisinformationScreen
import com.satyacheck.android.presentation.screens.settings.LanguageScreen
import com.satyacheck.android.presentation.screens.settings.SettingsScreen

@Composable
fun SatyaCheckNavHost(
    navController: NavHostController,
    startDestination: String
) {
    val context = LocalContext.current
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { defaultEnterTransition() },
        exitTransition = { defaultExitTransition() },
        popEnterTransition = { defaultPopEnterTransition() },
        popExitTransition = { defaultPopExitTransition() }
    ) {
        composable("onboarding") {
            OnboardingScreen(
                navController = navController
            )
        }
        
        composable("login") {
            LoginScreen(
                navController = navController
            )
        }
        
        composable("signup") {
            SignupScreen(
                navController = navController
            )
        }
        
        composable("analyze") {
            AnalyzeScreen(
                navController = navController
            )
        }
        
        composable("analysis") {
            AnalysisResultScreen(
                onBack = { navController.popBackStack() },
                onShare = { shareText ->
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        putExtra(Intent.EXTRA_SUBJECT, "SatyaCheck Analysis Result")
                    }
                    
                    val chooserIntent = Intent.createChooser(shareIntent, "Share Analysis")
                    context.startActivity(chooserIntent)
                }
            )
        }
        
        composable("dashboard") {
            DashboardScreen(
                navController = navController,
                onNavigateToAnalyze = { navController.navigate("analyze") },
                onNavigateToCommunity = { navController.navigate("community") },
                onNavigateToEducate = { navController.navigate("educate") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToMap = { navController.navigate("map") }
            )
        }
        
        composable("community") {
            CommunityScreen(
                onNavigateToAnalyze = { navController.navigate("analyze") },
                onNavigateToDashboard = { navController.navigate("dashboard") },
                onNavigateToEducate = { navController.navigate("educate") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToCommunityDetails = { slug ->
                    navController.navigate("community/$slug")
                },
                onNavigateToMap = { navController.navigate("map") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToUserProfile = { userId ->
                    navController.navigate("profile/$userId")
                }
            )
        }
        
        composable("community/{slug}",
            arguments = listOf(
                navArgument("slug") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("slug") ?: ""
            CommunitySlugScreen(
                slug = slug,
                onBackPressed = { navController.popBackStack() }
            )
        }
        
        composable("map") {
            MapScreen(
                navController = navController,
                onNavigateToAnalyze = { navController.navigate("analyze") },
                onNavigateToDashboard = { navController.navigate("dashboard") },
                onNavigateToCommunity = { navController.navigate("community") },
                onNavigateToEducate = { navController.navigate("educate") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        
        composable("alerts") {
            AlertsScreen(
                onNavigateToAnalyze = { navController.navigate("analyze") },
                onNavigateToDashboard = { navController.navigate("dashboard") },
                onNavigateToCommunity = { navController.navigate("community") },
                onNavigateToMap = { navController.navigate("map") },
                onNavigateToEducate = { navController.navigate("educate") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        
        composable("educate") {
            EducateScreen(
                onNavigateToAnalyze = { navController.navigate("analyze") },
                onNavigateToDashboard = { navController.navigate("dashboard") },
                onNavigateToCommunity = { navController.navigate("community") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToArticle = { slug ->
                    navController.navigate("educate/$slug")
                },
                onNavigateToMap = { navController.navigate("map") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToAwareness = { navController.navigate("educate/awareness") }
            )
        }
        
        composable("educate/{slug}",
            arguments = listOf(
                navArgument("slug") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString("slug") ?: ""
            // Use a regular viewModel() call and rely on the ViewModel's scope
            ArticleDetailScreen(
                slug = slug,
                onBackPressed = { navController.popBackStack() }
            )
        }
        
        composable("educate/awareness") {
            AwarenessScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onNavigateToAnalyze = { navController.navigate("analyze") },
                onNavigateToDashboard = { navController.navigate("dashboard") },
                onNavigateToCommunity = { navController.navigate("community") },
                onNavigateToEducate = { navController.navigate("educate") },
                onNavigateToLanguageSettings = { navController.navigate("language") },
                onNavigateToMap = { navController.navigate("map") }
            )
        }
        
        composable("language") {
            LanguageScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        
        composable("profile") {
            ProfileScreen(
                onBackPressed = { navController.popBackStack() },
                onEditProfile = { /* Navigate to edit profile screen if needed */ },
                onSettingsClicked = { navController.navigate("settings") },
                onSignOut = {
                    navController.navigate("onboarding") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            "profile/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(
                onBackPressed = { navController.popBackStack() },
                onEditProfile = { /* Only self profile can be edited */ },
                onSettingsClicked = { navController.navigate("settings") },
                onSignOut = {
                    navController.navigate("onboarding") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                userId = userId
            )
        }
        
        composable("report") {
            ReportMisinformationScreen(
                navController = navController,
                onNavigateToAnalyze = { navController.navigate("analyze") },
                onNavigateToDashboard = { navController.navigate("dashboard") },
                onNavigateToCommunity = { navController.navigate("community") },
                onNavigateToEducate = { navController.navigate("educate") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToMap = { navController.navigate("map") }
            )
        }
    }
}
