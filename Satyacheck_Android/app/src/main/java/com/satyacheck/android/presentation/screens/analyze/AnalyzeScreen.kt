package com.satyacheck.android.presentation.screens.analyze

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import com.satyacheck.android.domain.model.AnalysisResult
import com.satyacheck.android.domain.model.Verdict
import com.satyacheck.android.presentation.components.AppHeaderWithNotifications
import com.satyacheck.android.presentation.components.MainLayout
import com.satyacheck.android.presentation.components.openAppDrawer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeScreen(
    navController: NavController
) {
    val viewModel = hiltViewModel<AnalyzeViewModel>()
    val state = viewModel.state.collectAsState().value
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Audio permission state
    var showAudioPermissionDialog by remember { mutableStateOf(false) }
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    var showCameraPermissionDialog by remember { mutableStateOf(false) }
    
    // Setup gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.updateSelectedImageUri(it)
            viewModel.showError("✅ Image selected! Tap 'Analyze' to check for misinformation.")
        }
    }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            // Create a URI from the bitmap
            viewModel.saveBitmapAndAnalyze(bitmap)
        }
    }
    
    // Audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.requestAudioPermission()
        } else {
            showAudioPermissionDialog = true
        }
    }
    
    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.showCameraDialog()
        } else {
            showCameraPermissionDialog = true
        }
    }
    
    MainLayout(
        currentRoute = "analyze",
        onNavigate = { route -> navController.navigate(route) },
        snackbarHostState = viewModel.snackbarHostState,
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
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                Text(
                    text = "Content Analysis",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Analysis input card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Input field for text to analyze
                        OutlinedTextField(
                            value = state.inputText,
                            onValueChange = { text -> viewModel.updateInputText(text) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            placeholder = { Text("Enter text or paste a link to analyze") },
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Show selected image preview
                        state.selectedImageUri?.let { imageUri ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = imageUri,
                                        contentDescription = "Selected image",
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "📸 Image Selected",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Ready for misinformation analysis",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.updateSelectedImageUri(null) }
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Remove image",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        
                        // Action buttons (Camera, Gallery, Audio) in a row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Camera button
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { 
                                        // Check camera permission first
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.CAMERA
                                            ) == PackageManager.PERMISSION_GRANTED) {
                                            // Permission already granted, show camera dialog
                                            viewModel.showCameraDialog()
                                        } else {
                                            // Request permission
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Camera,
                                        contentDescription = "Camera",
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Camera",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            
                            // Gallery button
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { 
                                        galleryLauncher.launch("image/*")
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Photo,
                                        contentDescription = "Gallery",
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Gallery",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            
                            // Audio button
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { 
                                        // Check permission first
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED) {
                                            // Permission already granted, start recording
                                            viewModel.requestAudioPermission()
                                        } else {
                                            // Request permission
                                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (state.isRecording) 
                                                MaterialTheme.colorScheme.error 
                                            else 
                                                MaterialTheme.colorScheme.primaryContainer
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Mic,
                                        contentDescription = "Audio",
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    if (state.isRecording) "Recording..." else "Audio",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Centered Analyze button with optimized loading
                        Button(
                            onClick = { 
                                viewModel.analyzeContent()
                            },
                            enabled = !state.isLoading,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(48.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyze", style = MaterialTheme.typography.labelLarge)
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Analyze",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyze", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // API Debug button
                        TextButton(
                            onClick = { viewModel.testApiConnection() },
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Test API",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Test Backend Connection",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Analysis Progress Indicator (show when loading)
                if (state.isLoading) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Analyzing Content...",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "AI processing in progress",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
                
                // Accessibility service toggle
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Accessibility Service",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Automatically scan text on your screen for misinformation",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Switch(
                            checked = state.accessibilityServiceEnabled,
                            onCheckedChange = { newValue -> 
                                if (newValue) {
                                    // Show explanation dialog before enabling
                                    showAccessibilityDialog = true
                                } else {
                                    // Directly try to disable
                                    viewModel.toggleAccessibilityService(false)
                                }
                            },
                            enabled = true
                        )
                    }
                }
                
                // Analysis result (only show if there is a result)
                if (state.analysisResult != null) {
                    AnalysisResultCard(
                        result = state.analysisResult!!,
                        originalText = state.inputText,
                        onReport = { navController.navigate("report") }
                    )
                }
                
                // Add appropriate bottom spacing to prevent content from being hidden behind the navigation bar
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
    
    // Camera dialog
    if (state.showCameraDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideCameraDialog() },
            title = { Text("Take a Photo") },
            text = { Text("Capture an image to analyze for potential misinformation") },
            confirmButton = {
                Button(
                    onClick = { 
                        viewModel.hideCameraDialog()
                        cameraLauncher.launch(null) 
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Camera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Camera")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideCameraDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Permission dialog for audio
    if (showAudioPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showAudioPermissionDialog = false },
            title = { Text("Microphone Permission Required") },
            text = { 
                Text("To analyze audio content, SatyaCheck needs access to your microphone. " +
                     "Please grant this permission in Settings.")
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showAudioPermissionDialog = false
                        // Open app settings
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAudioPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Dialog for accessibility service explanation
    if (showAccessibilityDialog) {
        AlertDialog(
            onDismissRequest = { showAccessibilityDialog = false },
            title = { Text("Accessibility Service") },
            text = { 
                Column {
                    Text(
                        "The Accessibility Service allows Satya Check to automatically analyze " +
                        "text content on your screen for potential misinformation."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This helps protect you from scams and false information while browsing. " +
                        "You'll need to enable the service in your device's Accessibility Settings."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Important: SatyaCheck only analyzes content when you explicitly request it, " +
                        "ensuring your privacy is protected.",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showAccessibilityDialog = false
                        viewModel.toggleAccessibilityService(true)
                    }
                ) {
                    Text("Enable Service")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccessibilityDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }
    
    // Camera permission dialog
    if (showCameraPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showCameraPermissionDialog = false },
            title = { Text("Camera Permission Required") },
            text = { 
                Text("To analyze images for misinformation, SatyaCheck needs access to your camera. " +
                     "Please grant this permission in Settings.")
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showCameraPermissionDialog = false
                        // Open app settings
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AnalysisResultCard(
    result: AnalysisResult,
    originalText: String,
    onReport: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Verdict indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored circle indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(getVerdictColor(result.verdict))
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = getVerdictText(result.verdict),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getVerdictColor(result.verdict)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Original content
            Text(
                text = "Original Content",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = originalText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Explanation
            Text(
                text = "Explanation",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = result.explanation,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Guidance based on verdict
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = getGuidanceText(result.verdict),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onReport,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Report")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = {
                        // Create share intent with analysis results
                        val shareText = buildString {
                            append("🔍 SatyaCheck Analysis Results\n\n")
                            append("Verdict: ${getVerdictText(result.verdict)}\n\n")
                            append("Analyzed Content:\n")
                            append("\"${originalText.take(200)}${if (originalText.length > 200) "..." else ""}\"\n\n")
                            append("Analysis: ${result.explanation}\n\n")
                            append("📱 Analyzed using SatyaCheck - Stay safe from misinformation!")
                        }
                        
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            putExtra(Intent.EXTRA_SUBJECT, "SatyaCheck Analysis - ${getVerdictText(result.verdict)}")
                        }
                        
                        val chooserIntent = Intent.createChooser(shareIntent, "Share Analysis Results")
                        context.startActivity(chooserIntent)
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Share")
                }
            }
        }
    }
}

// Helper functions
private fun getVerdictColor(verdict: Verdict): Color {
    return when (verdict) {
        Verdict.CREDIBLE -> Color(0xFF4CAF50) // Green
        Verdict.POTENTIALLY_MISLEADING -> Color(0xFFFFC107) // Amber
        Verdict.HIGH_MISINFORMATION_RISK -> Color(0xFFFF9800) // Orange
        Verdict.SCAM_ALERT -> Color(0xFFF44336) // Red
        Verdict.UNKNOWN -> Color(0xFF9E9E9E) // Gray
    }
}

private fun getVerdictText(verdict: Verdict): String {
    return when (verdict) {
        Verdict.CREDIBLE -> "Credible"
        Verdict.POTENTIALLY_MISLEADING -> "Potentially Misleading"
        Verdict.HIGH_MISINFORMATION_RISK -> "High Misinformation Risk"
        Verdict.SCAM_ALERT -> "Scam Alert"
        Verdict.UNKNOWN -> "Unknown"
    }
}

private fun getGuidanceText(verdict: Verdict): String {
    return when (verdict) {
        Verdict.CREDIBLE -> "This content appears to be credible. However, it's always good practice to verify information from multiple sources."
        Verdict.POTENTIALLY_MISLEADING -> "This content may be misleading. Consider checking additional sources before sharing or acting on this information."
        Verdict.HIGH_MISINFORMATION_RISK -> "This content has a high risk of containing misinformation. We strongly recommend verifying with official sources before believing or sharing."
        Verdict.SCAM_ALERT -> "This appears to be a scam or fraudulent content. Do not share personal information, click on any links, or respond to any requests in this content."
        Verdict.UNKNOWN -> "We couldn't determine the credibility of this content. Please verify with trusted sources before relying on or sharing this information."
    }
}
