package com.satyacheck.android.presentation.screens.onboarding

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.satyacheck.android.R
import com.satyacheck.android.presentation.components.TricolorShieldIcon
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Set up Google Sign-In
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .build()
    
    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task, viewModel)
        } else {
            // Sign in failed
            viewModel.handleGoogleSignInResult(false, "Google sign-in was cancelled")
        }
    }
    
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    
    // If authenticated, navigate to the app
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            viewModel.completeOnboarding(context)
            navController.navigate("analyze") {
                popUpTo("onboarding") { inclusive = true }
            }
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val pagerState = rememberPagerState { 4 } // Now we have 4 pages including auth
            val coroutineScope = rememberCoroutineScope()
            
            // Pager to navigate through onboarding screens
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> {
                        // Welcome page
                        OnboardingWelcomePage()
                    }
                    1 -> {
                        // Language selection page
                        val languageState = viewModel.selectedLanguage.collectAsState()
                        OnboardingLanguagePage(
                            selectedLanguage = languageState.value,
                            onLanguageSelected = viewModel::setLanguage
                        )
                    }
                    2 -> {
                        // Permissions page
                        OnboardingPermissionsPage(
                            locationPermissionGranted = viewModel.locationPermissionGranted.collectAsState().value,
                            onLocationPermissionChanged = viewModel::setLocationPermission
                        )
                    }
                    3 -> {
                        // Authentication page
                        OnboardingAuthPage(
                            onSignIn = viewModel::signIn,
                            onSignUp = viewModel::signUp,
                            onGoogleSignIn = {
                                val signInIntent = googleSignInClient.signInIntent
                                signInLauncher.launch(signInIntent)
                            },
                            onContinueAsGuest = viewModel::continueAsGuest,
                            isLoading = viewModel.isLoading.collectAsState().value,
                            errorMessage = viewModel.errorMessage.collectAsState().value
                        )
                    }
                }
            }
            
            // Navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button (hide on first page)
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    ) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(64.dp)) // Empty space for alignment
                }
                
                // Next/Finish button (hide on auth page)
                if (pagerState.currentPage < 3) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    ) {
                        Text(if (pagerState.currentPage == 2) "Go to Sign In" else "Next")
                    }
                }
            }
        }
    }
}

private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>, viewModel: OnboardingViewModel) {
    try {
        val account = task.getResult(ApiException::class.java)
        // Sign in was successful
        viewModel.handleGoogleSignInResult(true)
    } catch (e: ApiException) {
        // Sign in failed
        viewModel.handleGoogleSignInResult(false, "Google sign-in failed: ${e.statusCode}")
    }
}

@Composable
fun OnboardingWelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TricolorShieldIcon(
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Welcome to SatyaCheck",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your shield against misinformation",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "SatyaCheck helps you verify text, images, and audio for potential misinformation, keeping you safe in the digital world.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageOption(
    title: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun OnboardingPermissionsPage(
    locationPermissionGranted: Boolean,
    onLocationPermissionChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Required Permissions",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "To provide you with the best experience, SatyaCheck needs:",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Permission options
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = locationPermissionGranted,
                onCheckedChange = onLocationPermissionChanged
            )
            
            Text(
                text = "Location: To provide location-specific alerts about misinformation in your area",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Your privacy is our priority. We never share your data with third parties.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}
