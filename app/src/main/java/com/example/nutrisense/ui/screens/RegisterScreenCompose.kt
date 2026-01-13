package com.example.nutrisense.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrisense.ui.components.NutriSenseButton
import com.example.nutrisense.ui.components.NutriSenseTextField
import com.example.nutrisense.ui.theme.NutriSenseColors
import com.example.nutrisense.ui.theme.NutriSenseTheme

data class RegisterScreenState(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val age: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@Composable
fun RegisterScreenCompose(
    state: RegisterScreenState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBackToLoginClick: () -> Unit
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
                    text = "Complete Your Profile",
                    fontSize = 14.sp,
                    color = NutriSenseColors.TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Registration Card
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
                            text = "Almost There! 🎉",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tell us a bit about yourself",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = state.email,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f)
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

                        // First Name Field
                        NutriSenseTextField(
                            value = state.firstName,
                            onValueChange = onFirstNameChange,
                            label = "First Name"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Last Name Field
                        NutriSenseTextField(
                            value = state.lastName,
                            onValueChange = onLastNameChange,
                            label = "Last Name"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Age Field
                        NutriSenseTextField(
                            value = state.age,
                            onValueChange = onAgeChange,
                            label = "Age (optional)",
                            keyboardType = KeyboardType.Number
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Register Button
                        NutriSenseButton(
                            text = "Complete Registration",
                            onClick = onRegisterClick,
                            isLoading = state.isLoading
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Back to Login
                        TextButton(
                            onClick = onBackToLoginClick
                        ) {
                            Text(
                                text = "← Back to Login",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
