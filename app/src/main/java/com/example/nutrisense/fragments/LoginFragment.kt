package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.nutrisense.ui.screens.LoginScreenCompose
import com.example.nutrisense.ui.screens.LoginScreenState
import com.example.nutrisense.ui.theme.NutriSenseTheme
import com.example.nutrisense.viewmodel.AuthViewModel
import com.example.nutrisense.helpers.extensions.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                var email by remember { mutableStateOf("") }
                var password by remember { mutableStateOf("") }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var emailError by remember { mutableStateOf<String?>(null) }
                var passwordError by remember { mutableStateOf<String?>(null) }
                var isLoading by remember { mutableStateOf(false) }

                val loginState by authViewModel.loginState.collectAsStateWithLifecycle()

                LaunchedEffect(loginState) {
                    when (loginState) {
                        is AuthViewModel.LoginState.Loading -> {
                            isLoading = true
                            errorMessage = null
                        }
                        is AuthViewModel.LoginState.Success -> {
                            isLoading = false
                            val user = (loginState as AuthViewModel.LoginState.Success).user
                            requireContext().showSuccessToast("Welcome, ${user.firstName ?: user.email}!")
                            goToProfile(user.email)
                            authViewModel.resetLoginState()
                        }
                        is AuthViewModel.LoginState.Error -> {
                            isLoading = false
                            errorMessage = (loginState as AuthViewModel.LoginState.Error).message
                            authViewModel.resetLoginState()
                        }
                        is AuthViewModel.LoginState.Idle -> {
                            isLoading = false
                        }
                    }
                }

                NutriSenseTheme {
                    LoginScreenCompose(
                        state = LoginScreenState(
                            email = email,
                            password = password,
                            isLoading = isLoading,
                            errorMessage = errorMessage,
                            emailError = emailError,
                            passwordError = passwordError
                        ),
                        onEmailChange = {
                            email = it
                            emailError = null
                            errorMessage = null
                        },
                        onPasswordChange = {
                            password = it
                            passwordError = null
                            errorMessage = null
                        },
                        onLoginClick = {
                            var valid = true
                            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Please enter a valid email"
                                valid = false
                            }
                            if (password.length < 6) {
                                passwordError = "Password must be at least 6 characters"
                                valid = false
                            }
                            if (valid) {
                                authViewModel.loginUser(email, password)
                            }
                        },
                        onRegisterClick = {
                            var valid = true
                            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Please enter a valid email"
                                valid = false
                            }
                            if (password.length < 6) {
                                passwordError = "Password must be at least 6 characters"
                                valid = false
                            }
                            if (valid) {
                                checkEmailExistsAndNavigate(email, password)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPressHandler()
    }

    private fun checkEmailExistsAndNavigate(email: String, password: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val emailExists = authViewModel.checkIfEmailExists(email)
                if (emailExists) {
                    requireContext().showErrorToast("Email already registered. Please login.")
                } else {
                    goToRegister(email, password)
                }
            } catch (e: Exception) {
                requireContext().showErrorToast("Error: ${e.message}")
            }
        }
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
    }

    private fun goToRegister(email: String, password: String) {
        val action = LoginFragmentDirections.actionLoginFragmentToRegister2Fragment(email, password)
        findNavController().navigate(action)
    }

    private fun goToProfile(email: String) {
        val action = LoginFragmentDirections.actionLoginFragmentToNavigationProfile(email)
        findNavController().navigate(action)
    }
}