package com.example.nutrisense.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrisense.R
import com.example.nutrisense.ui.components.NutriSenseButton
import com.example.nutrisense.ui.components.NutriSenseOutlinedButton
import com.example.nutrisense.ui.components.NutriSenseTextField
import com.example.nutrisense.ui.theme.NutriSenseColors
import com.example.nutrisense.ui.theme.NutriSenseTheme

data class LoginScreenState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)

@Composable
fun LoginScreenCompose(
    state: LoginScreenState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    NutriSenseTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = NutriSenseColors.Background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // Logo/App Name
                Card(
                    modifier = Modifier.size(120.dp),
                    shape = RoundedCornerShape(60.dp),
                    colors = CardDefaults.cardColors(containerColor = NutriSenseColors.CardOrange),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🥗",
                            fontSize = 48.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "NutriSense",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = NutriSenseColors.Brown
                )

                Text(
                    text = "Your Personal Nutrition Assistant",
                    fontSize = 14.sp,
                    color = NutriSenseColors.TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Login Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NutriSenseColors.CardOrange),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome Back! 👋",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Sign in to continue",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Error Message
                        state.errorMessage?.let { error ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = NutriSenseColors.Error.copy(alpha = 0.2f)
                                )
                            ) {
                                Text(
                                    text = "❌ $error",
                                    modifier = Modifier.padding(12.dp),
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Email Field
                        NutriSenseTextField(
                            value = state.email,
                            onValueChange = onEmailChange,
                            label = "Email",
                            keyboardType = KeyboardType.Email,
                            isError = state.emailError != null,
                            errorMessage = state.emailError
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password Field
                        NutriSenseTextField(
                            value = state.password,
                            onValueChange = onPasswordChange,
                            label = "Password",
                            isPassword = true,
                            isError = state.passwordError != null,
                            errorMessage = state.passwordError
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Login Button
                        NutriSenseButton(
                            text = "Login",
                            onClick = onLoginClick,
                            isLoading = state.isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Divider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = Color.White.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "  OR  ",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Divider(
                                modifier = Modifier.weight(1f),
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Register Button
                        OutlinedButton(
                            onClick = onRegisterClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp)
                        ) {
                            Text(
                                text = "Create New Account",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Track your nutrition, achieve your goals! 💪",
                    fontSize = 12.sp,
                    color = NutriSenseColors.TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

