package com.satyacheck.android.presentation

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.satyacheck.android.presentation.navigation.SatyaCheckNavHost
import com.satyacheck.android.presentation.theme.SatyaCheckTheme
import com.satyacheck.android.presentation.theme.ThemeViewModel
import com.satyacheck.android.utils.LanguageManager
import com.satyacheck.android.utils.LocaleHelper
import com.satyacheck.android.utils.PermissionHelpers
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var permissionHelpers: PermissionHelpers
    
    @Inject
    lateinit var backendConnectionTester: com.satyacheck.android.utils.BackendConnectionTester
    
    @Inject
    lateinit var embeddedServer: com.satyacheck.android.utils.EmbeddedServer
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Set up optimized splash screen exit animation
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            // Enable hardware layer for smooth animation
            splashScreenView.iconView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            
            // Create optimized scale and fade animation
            val scaleX = splashScreenView.iconView.scaleX
            val scaleY = splashScreenView.iconView.scaleY
            
            splashScreenView.iconView.animate()
                .scaleX(scaleX * 1.15f)
                .scaleY(scaleY * 1.15f)
                .alpha(0f)
                .setDuration(350L)
                .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                .withEndAction {
                    // Clean up hardware layer
                    splashScreenView.iconView.setLayerType(android.view.View.LAYER_TYPE_NONE, null)
                    splashScreenView.remove()
                }
        }
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Enable hardware acceleration for smooth animations
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        
        // Apply current language from LocaleHelper
        val language = LocaleHelper.getLanguagePreference(this)
        updateLocale(language)
        
        // Start the embedded server for offline API functionality
        startEmbeddedServer()
        
        setContent {
            // Get the ThemeViewModel to access the current theme mode
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()
            
            // Get the MainViewModel for notifications
            val mainViewModel: MainViewModel = hiltViewModel()
            val unreadAlertsCount by mainViewModel.unreadAlertsCount.collectAsState()
            
            SatyaCheckTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                
                // Get the MainViewModel for notifications
                val mainViewModel: MainViewModel = hiltViewModel()
                val unreadAlertsCount by mainViewModel.unreadAlertsCount.collectAsState()
                
                // Register permission launcher using the modern ActivityResultCallback API
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    // Forward results to our PermissionHelpers
                    permissionHelpers.handleActivityResultPermissions(permissions)
                }
                
                // Provide the launcher to our permission helpers
                LaunchedEffect(Unit) {
                    permissionHelpers.setPermissionLauncher(permissionLauncher)
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SatyaCheckNavHost(
                        navController = navController,
                        startDestination = determineStartDestination()
                    )
                }
            }
        }
    }
    
    private fun determineStartDestination(): String {
        val preferences = getSharedPreferences("satya_check_prefs", MODE_PRIVATE)
        val onboardingCompleted = preferences.getBoolean("onboarding_completed", false)
        
        return if (onboardingCompleted) {
            "analyze"
        } else {
            "onboarding"
        }
    }
    
    /**
     * Override attachBaseContext to apply the saved language preference
     * This ensures proper localization even when the activity is recreated
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }
    
    /**
     * Update the application locale without restarting the app
     * This is used for instant language changes
     * 
     * @param language The language code to apply
     */
    private fun updateLocale(language: String) {
        val config = LanguageManager.applyLanguage(this, language)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
    
    /**
     * Called when configuration changes, like orientation or locale
     * This ensures that language changes are maintained during configuration changes
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val language = LocaleHelper.getLanguagePreference(this)
        updateLocale(language)
    }
    
    /**
     * Starts the embedded server for offline API functionality
     * This allows the app to work without external server dependencies
     */
    private fun startEmbeddedServer() {
        try {
            val serverStarted = embeddedServer.startServer()
            if (serverStarted) {
                android.util.Log.d("MainActivity", "Embedded server started successfully")
            } else {
                android.util.Log.e("MainActivity", "Failed to start embedded server")
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error starting embedded server", e)
        }
    }
    
    /**
     * Clean up resources when the app is destroyed
     */
    override fun onDestroy() {
        super.onDestroy()
        embeddedServer.stopServer()
    }
}
