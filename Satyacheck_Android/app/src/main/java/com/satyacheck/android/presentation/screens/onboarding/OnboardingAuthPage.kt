package com.satyacheck.android.presentation.screens.onboarding

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.satyacheck.android.R

@Composable
fun OnboardingAuthPage(
    onSignIn: (email: String, password: String) -> Unit,
    onSignUp: (name: String, email: String, password: String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    val context = LocalContext.current
    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
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
        
        // Title based on auth mode
        Text(
            text = if (authMode == AuthMode.SIGN_IN) "Welcome Back" else "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Subtitle
        Text(
            text = if (authMode == AuthMode.SIGN_IN) 
                "Sign in to continue using SatyaCheck" 
            else 
                "Join SatyaCheck to combat misinformation",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Show error message if any
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // Form Fields
        if (authMode == AuthMode.SIGN_UP) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
        )
        
        if (authMode == AuthMode.SIGN_UP) {
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                isError = confirmPassword.isNotEmpty() && password != confirmPassword
            )
            
            if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                Text(
                    text = "Passwords don't match",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 4.dp)
                        .align(Alignment.Start)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sign In/Up Button
        Button(
            onClick = {
                if (authMode == AuthMode.SIGN_IN) {
                    onSignIn(email, password)
                } else {
                    onSignUp(name, email, password)
                }
            },
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty() && 
                     (authMode == AuthMode.SIGN_IN || (name.isNotEmpty() && password == confirmPassword)),
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
                    text = if (authMode == AuthMode.SIGN_IN) "Sign In" else "Create Account",
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Toggle between Sign In and Sign Up
        TextButton(
            onClick = {
                authMode = if (authMode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN
            }
        ) {
            Text(
                text = if (authMode == AuthMode.SIGN_IN) 
                    "Don't have an account? Sign Up" 
                else 
                    "Already have an account? Sign In",
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
            onClick = onGoogleSignIn,
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
                text = "Continue with Google",
                fontSize = 16.sp,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Continue as Guest
        TextButton(
            onClick = onContinueAsGuest,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Continue as Guest",
                fontSize = 16.sp
            )
        }
    }
}

enum class AuthMode {
    SIGN_IN, SIGN_UP
}
