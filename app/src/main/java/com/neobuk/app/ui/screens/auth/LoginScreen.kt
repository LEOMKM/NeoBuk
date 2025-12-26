package com.neobuk.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.neobuk.app.ui.components.NeoBukLogoLarge
import com.neobuk.app.ui.theme.Tokens
import com.neobuk.app.ui.theme.AppTextStyles
import com.neobuk.app.viewmodels.AuthViewModel
import com.neobuk.app.viewmodels.AuthState

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    var phoneOrEmail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Observe auth state
    val authState by authViewModel.authState.collectAsState()
    
    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Loading -> {
                isLoading = true
                errorMessage = null
            }
            is AuthState.Authenticated -> {
                isLoading = false
                onLoginSuccess()
            }
            is AuthState.Error -> {
                isLoading = false
                errorMessage = (authState as AuthState.Error).message
            }
            else -> {
                isLoading = false
            }
        }
    }
    
    // Form validation
    val isFormValid = phoneOrEmail.isNotBlank() && password.length >= 6

    // Main Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            NeoBukLogoLarge()
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Content Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Tokens.HorizontalPadding),
                shape = RoundedCornerShape(Tokens.CardRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(Tokens.HorizontalPadding)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(Tokens.VerticalRhythmMedium))
                    
                    // Header
                    Text(
                        text = "Welcome Back",
                        style = AppTextStyles.pageTitle,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Login to your account",
                        style = AppTextStyles.body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(Tokens.VerticalRhythmMedium))
                    
                    // Error Message
                    errorMessage?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = AppTextStyles.body,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(Tokens.VerticalRhythmSmall))
                    }
                    
                    // Phone/Email Input
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Phone or Email",
                            style = AppTextStyles.bodyBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = phoneOrEmail,
                            onValueChange = { 
                                phoneOrEmail = it
                                errorMessage = null // Clear error on input
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Tokens.InputRadius),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            placeholder = { 
                                Text(
                                    "e.g. 0712345678 or email@example.com", 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                ) 
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Tokens.VerticalRhythmSmall))
                    
                    // Password Input
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Password",
                            style = AppTextStyles.bodyBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { 
                                password = it
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Tokens.InputRadius),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            enabled = !isLoading,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Password Visibility",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            placeholder = { 
                                Text(
                                    "••••••••", 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                ) 
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Tokens.VerticalRhythmSmall))
                    
                    // Remember Me & Forgot Password
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                enabled = !isLoading,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Text(
                                text = "Remember me",
                                style = AppTextStyles.body,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Text(
                            text = "Forgot Password?",
                            style = AppTextStyles.bodyBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { /* TODO: Implement forgot password */ }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Tokens.VerticalRhythmMedium))
                    
                    // Login Button
                    Button(
                        onClick = {
                            authViewModel.login(
                                emailOrPhone = phoneOrEmail.trim(),
                                password = password,
                                onSuccess = { /* Handled by LaunchedEffect */ },
                                onError = { /* Handled by LaunchedEffect */ }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Tokens.ButtonHeight),
                        shape = RoundedCornerShape(Tokens.InputRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = isFormValid && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                style = AppTextStyles.buttonLarge,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Tokens.VerticalRhythmMedium))

                    // Sign Up Link
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "New here? ",
                            style = AppTextStyles.body,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Create a business account",
                            style = AppTextStyles.bodyBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable(enabled = !isLoading) { 
                                onNavigateToSignup() 
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Tokens.VerticalRhythmMedium))
                }
            }
        }
    }
}
