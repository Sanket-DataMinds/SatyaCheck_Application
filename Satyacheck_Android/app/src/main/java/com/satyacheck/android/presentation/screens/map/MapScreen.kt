package com.satyacheck.android.presentation.screens.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.satyacheck.android.presentation.components.AppHeader
import com.satyacheck.android.presentation.components.AppHeaderWithNotifications
import com.satyacheck.android.presentation.components.MainLayout
import com.satyacheck.android.presentation.components.openAppDrawer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// Data model for local scam reports
data class ScamReport(
    val id: String,
    val type: String,
    val title: String,
    val description: String,
    val date: String,
    val latitude: Double,
    val longitude: Double
)

@Composable
fun MapScreen(
    navController: NavController,
    onNavigateToAnalyze: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToEducate: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Location state
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Scam reports state
    var nearbyScams by remember { mutableStateOf<List<ScamReport>>(emptyList()) }
    var selectedScam by remember { mutableStateOf<ScamReport?>(null) }
    
    // UI state
    var isMapFullScreen by remember { mutableStateOf(false) }
    
    // Check for location permission and fetch location
    LaunchedEffect(Unit) {
        delay(1000) // Simulate initial delay
        if (hasLocationPermission(context)) {
            fetchUserLocation(context) { location, errorMsg ->
                if (location != null) {
                    userLocation = location
                    
                    // Simulate fetching nearby scams (in a real app, this would be an API call)
                    nearbyScams = generateNearbyScams(location)
                } else {
                    error = errorMsg
                }
                isLoading = false
            }
        } else {
            isLoading = false
            showPermissionDialog = true
        }
    }
    
    // Handle permission dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Location Permission Required") },
            text = { Text("The map feature needs location permission to show you nearby scam alerts. Please grant this permission in your device settings.") },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    // In a real app, this would redirect to settings
                }) {
                    Text("Go to Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    MainLayout(
        currentRoute = "map",
        onNavigate = { route ->
            when (route) {
                "analyze" -> onNavigateToAnalyze()
                "dashboard" -> onNavigateToDashboard()
                "community" -> onNavigateToCommunity()
                "educate" -> onNavigateToEducate()
                "settings" -> onNavigateToSettings()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (isMapFullScreen) {
                // Full screen map view
                MapCard(
                    isLoading = isLoading,
                    error = error,
                    userLocation = userLocation,
                    nearbyScams = nearbyScams,
                    selectedScam = selectedScam,
                    onScamSelected = { scam -> selectedScam = scam },
                    onRefresh = {
                        isLoading = true
                        error = null
                        scope.launch {
                            delay(1000) // Simulate refresh
                            fetchUserLocation(context) { location, errorMsg ->
                                if (location != null) {
                                    userLocation = location
                                    nearbyScams = generateNearbyScams(location)
                                } else {
                                    error = errorMsg
                                }
                                isLoading = false
                            }
                        }
                    },
                    isFullScreen = true,
                    onToggleFullScreen = { isMapFullScreen = false },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else {
                // Main content area with map and alerts in a horizontal layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    // Map area - Takes top 2/3 of the screen height
                    MapCard(
                        isLoading = isLoading,
                        error = error,
                        userLocation = userLocation,
                        nearbyScams = nearbyScams,
                        selectedScam = selectedScam,
                        onScamSelected = { scam -> selectedScam = scam },
                        onRefresh = {
                            isLoading = true
                            error = null
                            scope.launch {
                                delay(1000) // Simulate refresh
                                fetchUserLocation(context) { location, errorMsg ->
                                    if (location != null) {
                                        userLocation = location
                                        nearbyScams = generateNearbyScams(location)
                                    } else {
                                        error = errorMsg
                                    }
                                    isLoading = false
                                }
                            }
                        },
                        isFullScreen = false,
                        onToggleFullScreen = { isMapFullScreen = true },
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Details area - Takes bottom 1/3 of the screen height
                    AlertDetailsCard(
                        selectedScam = selectedScam,
                        nearbyScams = nearbyScams,
                        onScamSelected = { scam -> selectedScam = scam },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun MapCard(
    isLoading: Boolean,
    error: String?,
    userLocation: Location?,
    nearbyScams: List<ScamReport>,
    selectedScam: ScamReport?,
    onScamSelected: (ScamReport) -> Unit,
    onRefresh: () -> Unit,
    isFullScreen: Boolean,
    onToggleFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onToggleFullScreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Locality Map",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    // Fullscreen toggle info
                    if (!isFullScreen) {
                        Text(
                            text = "Tap to expand",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    } else {
                        Text(
                            text = "Tap to minimize",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    
                    // Refresh button
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Map",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onRefresh() },
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Text(
                text = "View local scam alerts near your current location",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Map content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(Color(0xFFE0E0E0))
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        // Loading state
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Fetching your location...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    error != null -> {
                        // Error state
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onRefresh) {
                                Text("Try Again")
                            }
                        }
                    }
                    userLocation != null -> {
                        // Map with user location and scam pins
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Map placeholder (in a real app, this would be a Google Map or other map provider)
                            Text(
                                text = "Map centered at ${userLocation.latitude.format()}, ${userLocation.longitude.format()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 8.dp)
                            )
                            
                            // User location pin
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .align(Alignment.Center)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Your Location",
                                    modifier = Modifier.fillMaxSize(),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            // Scam pins
                            nearbyScams.forEach { scam ->
                                // Calculate relative position (this is a simplification)
                                // In a real app, these would be positioned based on geo-coordinates
                                val latDiff = scam.latitude - userLocation.latitude
                                val lngDiff = scam.longitude - userLocation.longitude
                                
                                // Place pins around the user's location based on the difference
                                // This is a simplified placement logic for demonstration
                                val xPercent = 0.5f + (lngDiff * 10).toFloat()
                                val yPercent = 0.5f + (latDiff * 10).toFloat()
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    ScamPin(
                                        scam = scam,
                                        isSelected = scam.id == selectedScam?.id,
                                        onClick = { onScamSelected(scam) },
                                        modifier = Modifier
                                            .align(
                                                when {
                                                    xPercent < 0.3f && yPercent < 0.3f -> Alignment.TopStart
                                                    xPercent < 0.3f && yPercent > 0.7f -> Alignment.BottomStart
                                                    xPercent > 0.7f && yPercent < 0.3f -> Alignment.TopEnd
                                                    xPercent > 0.7f && yPercent > 0.7f -> Alignment.BottomEnd
                                                    xPercent < 0.3f -> Alignment.CenterStart
                                                    xPercent > 0.7f -> Alignment.CenterEnd
                                                    yPercent < 0.3f -> Alignment.TopCenter
                                                    yPercent > 0.7f -> Alignment.BottomCenter
                                                    else -> Alignment.Center
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        // No location state
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Location information is unavailable",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onRefresh) {
                                Text("Refresh")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScamPin(
    scam: ScamReport,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pinColor = when (scam.type) {
        "phishing" -> Color(0xFFE57373) // Light Red
        "scam" -> Color(0xFFFFB74D)     // Light Orange
        else -> Color(0xFFFFF176)        // Light Yellow
    }
    
    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        // Pin marker
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(pinColor, CircleShape)
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = scam.title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun AlertDetailsCard(
    selectedScam: ScamReport?,
    nearbyScams: List<ScamReport>,
    onScamSelected: (ScamReport) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Local Dashboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Divider(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth()
            )
            
            if (selectedScam == null) {
                // Dashboard summary view when no scam is selected
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    // Statistics section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Phishing stat
                        StatisticItem(
                            count = nearbyScams.count { it.type == "phishing" },
                            label = "Phishing",
                            color = Color(0xFFE57373)
                        )
                        
                        // Scam stat
                        StatisticItem(
                            count = nearbyScams.count { it.type == "scam" },
                            label = "Fraud",
                            color = Color(0xFFFFB74D)
                        )
                        
                        // Suspicious stat
                        StatisticItem(
                            count = nearbyScams.count { it.type == "suspicious" },
                            label = "Suspicious",
                            color = Color(0xFFFFF176)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Recent alerts section
                    Text(
                        text = "Recent Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (nearbyScams.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recent alerts in your area",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // List most recent 2 scams
                        val recentScams = nearbyScams.sortedByDescending { 
                            SimpleDateFormat("d MMM yyyy", Locale.getDefault()).parse(it.date)?.time ?: 0
                        }.take(2)
                        
                        recentScams.forEach { scam ->
                            RecentAlertItem(
                                scam = scam,
                                onClick = { onScamSelected(scam) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Safety tips section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Safety Tips",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "• Stay alert in your neighborhood\n• Report suspicious activities\n• Share alerts with family & friends",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                // Report button
                Button(
                    onClick = { /* Report functionality will be added later */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Report New Incident")
                }
            } else {
                // Selected scam details
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    // Alert type chip
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (selectedScam.type) {
                                    "phishing" -> Color(0xFFE57373).copy(alpha = 0.2f)
                                    "scam" -> Color(0xFFFFB74D).copy(alpha = 0.2f)
                                    else -> Color(0xFFFFF176).copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (selectedScam.type) {
                                "phishing" -> "Phishing Attempt"
                                "scam" -> "Fraud Alert"
                                else -> "Suspicious Activity"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = when (selectedScam.type) {
                                "phishing" -> Color(0xFFB71C1C)
                                "scam" -> Color(0xFFE65100)
                                else -> Color(0xFFF57F17)
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Title
                    Text(
                        text = selectedScam.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Date
                    Text(
                        text = "Reported: ${selectedScam.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Description
                    Text(
                        text = selectedScam.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Safety tips
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Safety Tips",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = when (selectedScam.type) {
                                    "phishing" -> "• Never click on suspicious links\n• Verify sender's email address\n• Contact the company directly through official channels"
                                    "scam" -> "• Never send money to strangers\n• Research thoroughly before making payments\n• If it sounds too good to be true, it probably is"
                                    else -> "• Stay vigilant in this area\n• Report suspicious activity to authorities\n• Warn friends and family about this threat"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                // Report button
                Button(
                    onClick = { /* Report functionality will be added later */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Report Similar Incident")
                }
            }
        }
    }
}

// Helper functions
private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

private fun fetchUserLocation(
    context: Context,
    callback: (Location?, String?) -> Unit
) {
    // This is a simplified implementation for demonstration
    // In a real app, you would use FusedLocationProviderClient for better results
    try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        
        // Check if GPS is enabled
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            callback(null, "GPS is disabled. Please enable location services.")
            return
        }
        
        // In a real app, you would request the last known location or request location updates
        // For this demo, we'll create a simulated location
        val simulatedLocation = Location(LocationManager.GPS_PROVIDER).apply {
            // Simulated coordinates (Mumbai, India)
            latitude = 19.0760 + (Random.nextDouble() * 0.02 - 0.01)
            longitude = 72.8777 + (Random.nextDouble() * 0.02 - 0.01)
            accuracy = 10f
            time = System.currentTimeMillis()
        }
        
        callback(simulatedLocation, null)
    } catch (e: Exception) {
        callback(null, "Failed to get location: ${e.message}")
    }
}

private fun generateNearbyScams(location: Location): List<ScamReport> {
    // This would normally be a call to your backend API
    // For demonstration, we'll generate some random scams near the user's location
    val scams = mutableListOf<ScamReport>()
    val random = Random
    val scamTypes = listOf("phishing", "scam", "suspicious")
    
    // Generate between 3-7 nearby scams
    val count = random.nextInt(3, 8)
    
    for (i in 1..count) {
        // Create a random location within ~1-2km of the user
        val latOffset = (random.nextDouble() * 0.04 - 0.02)
        val lngOffset = (random.nextDouble() * 0.04 - 0.02)
        
        val scamType = scamTypes[random.nextInt(scamTypes.size)]
        
        val scamTitles = when (scamType) {
            "phishing" -> listOf(
                "Fake Bank SMS",
                "Suspicious Login Alert",
                "Fraudulent UPI Request",
                "Fake Government Message"
            )
            "scam" -> listOf(
                "Fake Job Offer",
                "Ponzi Investment Scheme",
                "Fake Shopping Website",
                "Fraudulent Charity"
            )
            else -> listOf(
                "Suspicious Door-to-Door Seller",
                "Unverified Financial Advisor",
                "Potential Identity Theft",
                "Unusual Activity Report"
            )
        }
        
        val scamDescriptions = when (scamType) {
            "phishing" -> listOf(
                "Multiple users reported receiving fake SMS messages claiming to be from a major bank, requesting personal details.",
                "Phishing emails claiming your account is locked and requesting verification of your details.",
                "Fake UPI payment requests asking users to scan a QR code to receive money.",
                "SMS messages pretending to be from government agencies requesting Aadhaar or PAN details."
            )
            "scam" -> listOf(
                "Reports of fake job offers requiring payment for training or certification.",
                "Investment scheme promising unrealistic returns, targeting elderly residents.",
                "Fake shopping website collecting payment for products never delivered.",
                "Door-to-door collections for non-existent charitable causes."
            )
            else -> listOf(
                "Unverified individuals requesting access to homes for 'inspections'.",
                "Person claiming to be a financial advisor offering unrealistic investment returns.",
                "Reports of suspicious individuals photographing ID documents at local businesses.",
                "Unusual activity reported near ATMs in this area, possibly card skimming."
            )
        }
        
        // Generate a random date within the last 30 days
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -random.nextInt(1, 30))
        val reportDate = calendar.time
        val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        
        scams.add(
            ScamReport(
                id = "scam-${i}",
                type = scamType,
                title = scamTitles[random.nextInt(scamTitles.size)],
                description = scamDescriptions[random.nextInt(scamDescriptions.size)],
                date = dateFormat.format(reportDate),
                latitude = location.latitude + latOffset,
                longitude = location.longitude + lngOffset
            )
        )
    }
    
    return scams
}

private fun Double.format(): String {
    return String.format("%.4f", this)
}

@Composable
fun StatisticItem(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.2f), CircleShape)
                .border(1.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RecentAlertItem(
    scam: ScamReport,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Alert icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = when (scam.type) {
                        "phishing" -> Color(0xFFE57373)
                        "scam" -> Color(0xFFFFB74D)
                        else -> Color(0xFFFFF176)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Alert details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = scam.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            
            Text(
                text = scam.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Arrow icon
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "View Details",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}
