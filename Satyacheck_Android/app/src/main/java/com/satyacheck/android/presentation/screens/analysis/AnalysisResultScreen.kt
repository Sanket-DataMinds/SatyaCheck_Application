package com.satyacheck.android.presentation.screens.analysis

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.satyacheck.android.R
import com.satyacheck.android.domain.model.Verdict
import com.satyacheck.android.presentation.components.AppHeader

@Composable
fun AnalysisResultScreen(
    onBack: () -> Unit,
    onShare: (String) -> Unit,
    viewModel: AnalysisResultViewModel = hiltViewModel()
) {
    val state = viewModel.state
    
    LaunchedEffect(key1 = true) {
        viewModel.loadAnalysisResult()
    }
    
    Scaffold(
        topBar = {
            AppHeader(title = stringResource(id = R.string.analysis_result_title))
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        
        if (state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = state.error)
            }
            return@Scaffold
        }
        
        state.analysisResult?.let { result ->
            val verdictInfo = getVerdictInfo(result.verdict)
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Back button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    IconButton(onClick = { onShare(getShareText(result)) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(id = R.string.share)
                        )
                    }
                }
                
                // Verdict Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = verdictInfo.backgroundColor
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = verdictInfo.icon,
                            contentDescription = null,
                            tint = verdictInfo.textColor,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Text(
                            text = getVerdictText(result.verdict),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = verdictInfo.textColor
                        )
                    }
                }
                
                // Original Content
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.analysis_original_content),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = state.originalContent ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Explanation
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.analysis_explanation),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Text(
                            text = result.explanation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Guidance for Scam Alerts
                if (result.verdict == Verdict.SCAM_ALERT && state.originalContent?.lowercase()?.contains("bank") == true) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Guidance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Text(
                                text = stringResource(id = R.string.analysis_guidance_yes),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

private fun getShareText(result: com.satyacheck.android.domain.model.AnalysisResult): String {
    return """
        SatyaCheck Analysis Result:
        Verdict: ${getVerdictText(result.verdict)}
        
        Explanation:
        ${result.explanation}
        
        Analyzed with SatyaCheck - Your shield against misinformation
    """.trimIndent()
}
