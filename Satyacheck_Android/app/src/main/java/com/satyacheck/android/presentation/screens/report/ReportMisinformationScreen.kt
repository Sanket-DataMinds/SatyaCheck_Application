package com.satyacheck.android.presentation.screens.report

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.satyacheck.android.presentation.components.AppHeaderWithNotifications
import com.satyacheck.android.presentation.components.MainLayout
import com.satyacheck.android.presentation.components.openAppDrawer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportMisinformationScreen(
    navController: NavController,
    onNavigateToAnalyze: () -> Unit,
    onNavigateToCommunity: () -> Unit,
    onNavigateToEducate: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMap: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {}
) {
    val viewModel = hiltViewModel<ReportMisinformationViewModel>()
    val state by viewModel.state.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Dropdown menu state
    var isExpanded by remember { mutableStateOf(false) }
    
    MainLayout(
        currentRoute = "report",
        onNavigate = { route ->
            when (route) {
                "analyze" -> onNavigateToAnalyze()
                "community" -> onNavigateToCommunity()
                "educate" -> onNavigateToEducate()
                "settings" -> onNavigateToSettings()
                "map" -> onNavigateToMap()
                "dashboard" -> onNavigateToDashboard()
            }
        },
        snackbarHostState = viewModel.snackbarHostState,
        drawerState = drawerState,
        topBar = {
            AppHeaderWithNotifications(
                title = "Report Misinformation",
                navController = navController,
                showMenuButton = true,
                onMenuClicked = { openAppDrawer(drawerState, scope) }
            )
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (state.isSubmissionSuccessful) {
                SuccessScreen(onReportMore = {
                    viewModel.updateDescription("")
                    viewModel.updateContentLink("")
                    viewModel.updateCategory("")
                })
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    // Main Card with the report form
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Card Title with icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.size(8.dp))
                                
                                Text(
                                    text = "Submit a Report",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Card Description
                            Text(
                                text = "Help keep our community safe by reporting misinformation. Please provide as much detail as possible.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Link to Content
                            OutlinedTextField(
                                value = state.contentLink,
                                onValueChange = { viewModel.updateContentLink(it) },
                                label = { Text("Link to Content (Optional)") },
                                placeholder = { Text("https://example.com/article") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Uri,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Description
                            OutlinedTextField(
                                value = state.description,
                                onValueChange = { viewModel.updateDescription(it) },
                                label = { Text("Description of Content *") },
                                placeholder = { Text("Describe the misinformation, where you saw it, and why you believe it's false") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                )
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Category Dropdown
                            ExposedDropdownMenuBox(
                                expanded = isExpanded,
                                onExpandedChange = { isExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = state.category,
                                    onValueChange = {},
                                    label = { Text("Category *") },
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = isExpanded,
                                    onDismissRequest = { isExpanded = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    viewModel.categories.forEach { category ->
                                        DropdownMenuItem(
                                            text = { Text(category) },
                                            onClick = {
                                                viewModel.updateCategory(category)
                                                isExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Submit Button
                            Button(
                                onClick = { viewModel.submitReport() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isSubmitting
                            ) {
                                if (state.isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Submit Report")
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Information about how reports are used
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "How Your Reports Help",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "• Reports appear in the SatyaCheck dashboard\n" +
                                       "• Local alerts are shown on the safety map\n" +
                                       "• Trending scams are highlighted in community alerts\n" +
                                       "• Your reports help make the internet safer for everyone",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Extra space at the bottom for better scrolling
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun SuccessScreen(
    onReportMore: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Report Submitted Successfully",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Thank you for helping keep our community safe. Your report has been submitted and will be reviewed by our team.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onReportMore
            ) {
                Text("Report More Misinformation")
            }
        }
    }
}
