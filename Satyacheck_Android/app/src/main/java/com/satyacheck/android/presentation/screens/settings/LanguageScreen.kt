package com.satyacheck.android.presentation.screens.settings

import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.satyacheck.android.R
import com.satyacheck.android.presentation.MainActivity
import com.satyacheck.android.presentation.components.AppHeader
import com.satyacheck.android.utils.LanguageManager
import com.satyacheck.android.utils.LocaleHelper
import kotlinx.coroutines.runBlocking

// List of supported languages
private val SUPPORTED_LANGUAGES = listOf(
    LanguageManager.LANGUAGE_ENGLISH,
    LanguageManager.LANGUAGE_HINDI,
    LanguageManager.LANGUAGE_MARATHI
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    onBackPressed: () -> Unit,
    viewModel: LanguageViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current
    
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(state.currentLanguage) }
    
    // Handle language change effects
    LaunchedEffect(state.languageChanged) {
        if (state.languageChanged) {
            // Show success message
            Toast.makeText(
                context,
                context.getString(R.string.language_changed),
                Toast.LENGTH_SHORT
            ).show()
            
            // Refresh the current activity to apply changes immediately
            val activity = context as? MainActivity
            activity?.recreate()
            
            // Reset the flag after handling
            viewModel.resetLanguageChangedFlag()
        }
    }
    
    Scaffold(
        topBar = {
            AppHeader(
                title = stringResource(id = R.string.language_settings_title),
                showBackButton = true,
                onBackPressed = onBackPressed
            )
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SUPPORTED_LANGUAGES) { language ->
                    LanguageItem(
                        language = language,
                        isSelected = language == state.currentLanguage,
                        onSelect = {
                            if (language != state.currentLanguage) {
                                selectedLanguage = language
                                showConfirmDialog = true
                            }
                        }
                    )
                }
            }
            
            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { Text(stringResource(id = R.string.change_language_title)) },
                    text = { Text(stringResource(id = R.string.change_language_message)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                // Save and apply language change without runBlocking
                                viewModel.saveLanguage(selectedLanguage)
                                showConfirmDialog = false
                            }
                        ) {
                            Text(stringResource(id = R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text(stringResource(id = R.string.cancel))
                        }
                    }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageItem(
    language: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = getLanguageDisplayName(language),
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = language,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Helper function to convert language code to display name
private fun getLanguageDisplayName(languageCode: String): String {
    return when (languageCode) {
        LanguageManager.LANGUAGE_ENGLISH -> "English"
        LanguageManager.LANGUAGE_HINDI -> "हिन्दी (Hindi)"
        LanguageManager.LANGUAGE_MARATHI -> "मराठी (Marathi)"
        else -> languageCode
    }
}
