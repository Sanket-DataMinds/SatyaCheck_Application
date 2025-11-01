package com.satyacheck.android.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.satyacheck.android.utils.LegalDocumentScreen
import com.satyacheck.android.utils.PermissionHelper
import kotlinx.coroutines.launch

/**
 * Onboarding screen that handles user introduction, permissions, and legal agreements
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    
    // States for permissions and legal consents
    var notificationPermissionGranted by remember { mutableStateOf(false) }
    var cameraPermissionGranted by remember { mutableStateOf(false) }
    var microphonePermissionGranted by remember { mutableStateOf(false) }
    var privacyPolicyAccepted by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top indicator
        LinearProgressIndicator(
            progress = (pagerState.currentPage + 1) / 4f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = false
        ) { page ->
            when (page) {
                0 -> WelcomePage(
                    onContinue = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
                
                1 -> PermissionsPage(
                    onAllPermissionsGranted = {
                        notificationPermissionGranted = true
                        cameraPermissionGranted = true
                        microphonePermissionGranted = true
                        scope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    }
                )
                
                2 -> LegalDocumentsPage(
                    onAgreementComplete = {
                        privacyPolicyAccepted = true
                        termsAccepted = true
                        scope.launch {
                            pagerState.animateScrollToPage(3)
                        }
                    }
                )
                
                3 -> CompletionPage(
                    onComplete = {
                        // Save onboarding completion status
                        viewModel.completeOnboarding(
                            notificationPermissionGranted,
                            cameraPermissionGranted,
                            microphonePermissionGranted,
                            privacyPolicyAccepted,
                            termsAccepted
                        )
                        // Navigate to main app
                        navController.navigate("analyze") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun WelcomePage(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to SatyaCheck",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your trusted partner in verifying information and fighting misinformation",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onContinue,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Get Started")
        }
    }
}

@Composable
fun PermissionsPage(onAllPermissionsGranted: () -> Unit) {
    var cameraPermissionGranted by remember { mutableStateOf(false) }
    var microphonePermissionGranted by remember { mutableStateOf(false) }
    var notificationPermissionGranted by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Required Permissions",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Text(
            text = "SatyaCheck needs the following permissions to function properly:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Camera permission
        PermissionHelper.RequestPermissionWithRationale(
            permission = android.Manifest.permission.CAMERA,
            rationaleText = "Camera access is needed to analyze images for misinformation detection",
            onPermissionResult = { granted ->
                cameraPermissionGranted = granted
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Microphone permission
        PermissionHelper.RequestPermissionWithRationale(
            permission = android.Manifest.permission.RECORD_AUDIO,
            rationaleText = "Microphone access is needed to analyze audio for misinformation detection",
            onPermissionResult = { granted ->
                microphonePermissionGranted = granted
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Notification permission
        PermissionHelper.RequestPermissionWithRationale(
            permission = android.Manifest.permission.POST_NOTIFICATIONS,
            rationaleText = "Notifications are used to alert you about important updates and analysis results",
            onPermissionResult = { granted ->
                notificationPermissionGranted = granted
            }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onAllPermissionsGranted,
            enabled = cameraPermissionGranted && 
                    microphonePermissionGranted && notificationPermissionGranted
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun LegalDocumentsPage(onAgreementComplete: () -> Unit) {
    var showPrivacyPolicy by remember { mutableStateOf(false) }
    var showTermsOfService by remember { mutableStateOf(false) }
    var privacyPolicyAccepted by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }
    
    if (showPrivacyPolicy) {
        LegalDocumentScreen(
            title = "Privacy Policy",
            assetFileName = "privacy_policy.md",
            requireConsent = true,
            onConsentChanged = { 
                privacyPolicyAccepted = it
            },
            onBackPressed = {
                showPrivacyPolicy = false
            }
        )
    } else if (showTermsOfService) {
        LegalDocumentScreen(
            title = "Terms of Service",
            assetFileName = "terms_of_service.md",
            requireConsent = true,
            onConsentChanged = { 
                termsAccepted = it
            },
            onBackPressed = {
                showTermsOfService = false
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Legal Agreements",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            Text(
                text = "Before using SatyaCheck, please review and agree to our legal terms:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Privacy Policy
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Text(
                        text = "Our privacy policy explains how we collect, use, and protect your personal information.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = privacyPolicyAccepted,
                                onCheckedChange = { privacyPolicyAccepted = it }
                            )
                            
                            Text(
                                text = "I agree",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Button(
                            onClick = { showPrivacyPolicy = true }
                        ) {
                            Text("Read")
                        }
                    }
                }
            }
            
            // Terms of Service
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Terms of Service",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Text(
                        text = "Our terms of service outline the rules and guidelines for using SatyaCheck.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = termsAccepted,
                                onCheckedChange = { termsAccepted = it }
                            )
                            
                            Text(
                                text = "I agree",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Button(
                            onClick = { showTermsOfService = true }
                        ) {
                            Text("Read")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onAgreementComplete,
                enabled = privacyPolicyAccepted && termsAccepted,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun CompletionPage(onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "All Set!",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "You're now ready to use SatyaCheck. Let's start verifying information!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onComplete,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Get Started")
        }
    }
}
