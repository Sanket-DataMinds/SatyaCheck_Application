package com.satyacheck.android.utils

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Utility to load and display legal documents from assets
 */
object LegalDocumentManager {
    
    /**
     * Load a document from assets
     */
    fun loadDocumentFromAssets(context: Context, fileName: String): String {
        return try {
            val inputStream = context.assets.open(fileName)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append('\n')
            }
            
            reader.close()
            stringBuilder.toString()
        } catch (e: Exception) {
            "Error loading document: ${e.message}"
        }
    }
}

/**
 * Composable to display a legal document with consent checkbox
 */
@Composable
fun LegalDocumentScreen(
    title: String,
    assetFileName: String,
    requireConsent: Boolean = true,
    onConsentChanged: (Boolean) -> Unit = {},
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    val documentText = remember {
        LegalDocumentManager.loadDocumentFromAssets(context, assetFileName)
    }
    var hasConsented by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                // Back icon
                Text("<", style = MaterialTheme.typography.headlineMedium)
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Document content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                text = documentText,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
        }
        
        // Consent checkbox if required
        if (requireConsent) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = hasConsented,
                    onCheckedChange = { 
                        hasConsented = it
                        onConsentChanged(it)
                    }
                )
                
                Text(
                    text = "I have read and agree to the $title",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/**
 * Composable to handle multiple legal documents with consent
 */
@Composable
fun LegalConsentScreen(
    onAllConsentsGranted: () -> Unit,
    onCancel: () -> Unit
) {
    var privacyPolicyConsent by remember { mutableStateOf(false) }
    var termsOfServiceConsent by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
        
        // Privacy Policy consent
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = privacyPolicyConsent,
                onCheckedChange = { privacyPolicyConsent = it }
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "I have read and agree to the Privacy Policy",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            TextButton(onClick = {
                // Navigate to privacy policy screen
            }) {
                Text("View")
            }
        }
        
        // Terms of Service consent
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = termsOfServiceConsent,
                onCheckedChange = { termsOfServiceConsent = it }
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = "Terms of Service",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "I have read and agree to the Terms of Service",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            TextButton(onClick = {
                // Navigate to terms of service screen
            }) {
                Text("View")
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Decline")
            }
            
            Button(
                onClick = onAllConsentsGranted,
                enabled = privacyPolicyConsent && termsOfServiceConsent
            ) {
                Text("Accept & Continue")
            }
        }
    }
}
