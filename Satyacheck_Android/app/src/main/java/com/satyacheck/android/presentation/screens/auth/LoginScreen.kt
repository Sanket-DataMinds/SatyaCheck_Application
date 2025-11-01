package com.satyacheck.android.presentation.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.satyacheck.android.R
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
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
            navController.navigate("analyze") {
                popUpTo("login") { inclusive = true }
            }
        }
    }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Logo or Icon
        Image(
            painter = painterResource(id = R.drawable.satya_logo),
            contentDescription = "SatyaCheck Logo",
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = stringResource(R.string.auth_welcome_back),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle
        Text(
            text = stringResource(R.string.auth_sign_in_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Show error message if any
        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Form Fields
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.auth_email)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.auth_password)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sign In Button
        Button(
            onClick = { viewModel.signIn(email, password) },
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = stringResource(R.string.auth_sign_in),
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Toggle to Sign Up
        TextButton(
            onClick = { navController.navigate("signup") }
        ) {
            Text(
                text = stringResource(R.string.auth_dont_have_account),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Or divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = "OR",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Divider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Google Sign In Button
        OutlinedButton(
            onClick = {
                val signInIntent = googleSignInClient.signInIntent
                signInLauncher.launch(signInIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White
            )
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.auth_continue_with_google),
                fontSize = 16.sp,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Continue as Guest
        TextButton(
            onClick = {
                viewModel.continueAsGuest()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.auth_continue_as_guest),
                fontSize = 16.sp
            )
        }
    }
}

private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>, viewModel: AuthViewModel) {
    try {
        val account = task.getResult(ApiException::class.java)
        // Sign in was successful
        viewModel.handleGoogleSignInResult(true)
    } catch (e: ApiException) {
        // Sign in failed
        viewModel.handleGoogleSignInResult(false, "Google sign-in failed: ${e.statusCode}")
    }
}
