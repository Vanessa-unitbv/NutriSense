package com.example.nutrisense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    onRegisterClick: (email: String, password: String) -> Unit,
    isLoading: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "NutriSense",
            fontSize = 32.sp,
            color = Color(0xFF6200EE),
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Your Personal Nutrition Guide",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = ""
            },
            label = { Text("Email Address") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = emailError.isNotEmpty(),
            supportingText = {
                if (emailError.isNotEmpty()) {
                    Text(emailError, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = ""
            },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = passwordError.isNotEmpty(),
            supportingText = {
                if (passwordError.isNotEmpty()) {
                    Text(passwordError, color = MaterialTheme.colorScheme.error)
                }
            },
            trailingIcon = {
                val image = if (passwordVisible) "👁️" else "👁️‍🗨️"
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(image)
                }
            }
        )

        Button(
            onClick = {
                var isValid = true
                if (email.isEmpty()) {
                    emailError = "Email cannot be empty!"
                    isValid = false
                } else if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+".toRegex())) {
                    emailError = "Invalid email format!"
                    isValid = false
                }

                if (password.isEmpty()) {
                    passwordError = "Password cannot be empty!"
                    isValid = false
                } else if (password.length < 6) {
                    passwordError = "Password must be at least 6 characters!"
                    isValid = false
                }

                if (isValid) {
                    onLoginClick(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(bottom = 12.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6200EE)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Login", fontSize = 16.sp)
            }
        }

        OutlinedButton(
            onClick = {
                var isValid = true
                if (email.isEmpty()) {
                    emailError = "Email cannot be empty!"
                    isValid = false
                } else if (!email.matches("[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+".toRegex())) {
                    emailError = "Invalid email format!"
                    isValid = false
                }

                if (password.isEmpty()) {
                    passwordError = "Password cannot be empty!"
                    isValid = false
                } else if (password.length < 6) {
                    passwordError = "Password must be at least 6 characters!"
                    isValid = false
                }

                if (isValid) {
                    onRegisterClick(email, password)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF6200EE)
            )
        ) {
            Text("Create New Account", fontSize = 16.sp)
        }
    }
}

@Composable
fun PreviewLoginScreen() {
    LoginScreen(
        onLoginClick = { _, _ -> },
        onRegisterClick = { _, _ -> }
    )
}
