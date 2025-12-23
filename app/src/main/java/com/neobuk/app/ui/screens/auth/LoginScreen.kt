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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neobuk.app.ui.components.NeoBukLogoLarge
import com.neobuk.app.ui.theme.Tokens
import com.neobuk.app.ui.theme.AppTextStyles

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    // Main Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp) // Lift it slightly
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
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Subtle shadow
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
                    
                    // Email Input
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Email",
                            style = AppTextStyles.bodyBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(Tokens.InputRadius),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            placeholder = { Text("Enter your email", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
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
                            onValueChange = { password = it },
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(Tokens.InputRadius),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
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
                            placeholder = { Text("••••••••", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) }
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
                            modifier = Modifier.clickable { }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Tokens.VerticalRhythmMedium))
                    
                    // Login Button
                    Button(
                        onClick = onLoginSuccess,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Tokens.ButtonHeight),
                        shape = RoundedCornerShape(Tokens.InputRadius), // Matching input radius for button often looks good, or use MaterialTheme.colorScheme.surfaceRadius/2
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "Sign In",
                            style = AppTextStyles.buttonLarge,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(Tokens.VerticalRhythmMedium))

                    // Sign Up Link (Added)
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
                            modifier = Modifier.clickable { onNavigateToSignup() }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Tokens.VerticalRhythmMedium))
                }
            }
        }
    }
}
