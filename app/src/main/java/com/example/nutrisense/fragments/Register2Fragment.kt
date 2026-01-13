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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nutrisense.R
import com.example.nutrisense.ui.screens.RegisterScreenCompose
import com.example.nutrisense.ui.screens.RegisterScreenState
import com.example.nutrisense.ui.theme.NutriSenseTheme
import com.example.nutrisense.viewmodel.AuthViewModel
import com.example.nutrisense.helpers.extensions.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Register2Fragment : Fragment() {

    private val args: Register2FragmentArgs by navArgs()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                var firstName by remember { mutableStateOf("") }
                var lastName by remember { mutableStateOf("") }
                var age by remember { mutableStateOf("") }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var isLoading by remember { mutableStateOf(false) }

                val registerState by authViewModel.registerState.collectAsStateWithLifecycle()

                LaunchedEffect(registerState) {
                    when (registerState) {
                        is AuthViewModel.RegisterState.Loading -> {
                            isLoading = true
                            errorMessage = null
                        }
                        is AuthViewModel.RegisterState.Success -> {
                            isLoading = false
                            val user = (registerState as AuthViewModel.RegisterState.Success).user
                            requireContext().showSuccessToast("Welcome, ${user.firstName ?: user.email}!")
                            goToProfile(user.email)
                            authViewModel.resetRegisterState()
                        }
                        is AuthViewModel.RegisterState.Error -> {
                            isLoading = false
                            errorMessage = (registerState as AuthViewModel.RegisterState.Error).message
                            authViewModel.resetRegisterState()
                        }
                        is AuthViewModel.RegisterState.Idle -> {
                            isLoading = false
                        }
                    }
                }

                NutriSenseTheme {
                    RegisterScreenCompose(
                        state = RegisterScreenState(
                            email = args.email ?: "",
                            firstName = firstName,
                            lastName = lastName,
                            age = age,
                            isLoading = isLoading,
                            errorMessage = errorMessage
                        ),
                        onFirstNameChange = { firstName = it },
                        onLastNameChange = { lastName = it },
                        onAgeChange = { age = it },
                        onRegisterClick = {
                            val email = args.email ?: ""
                            val password = args.password ?: ""

                            if (email.isEmpty() || password.isEmpty()) {
                                errorMessage = "Missing email or password"
                                return@RegisterScreenCompose
                            }

                            val ageInt = if (age.isNotEmpty()) age.toIntOrNull() else null
                            if (age.isNotEmpty() && (ageInt == null || ageInt < 1 || ageInt > 150)) {
                                errorMessage = "Age must be between 1 and 150"
                                return@RegisterScreenCompose
                            }

                            authViewModel.registerUser(
                                email = email,
                                password = password,
                                firstName = firstName.ifBlank { null },
                                lastName = lastName.ifBlank { null },
                                age = ageInt
                            )
                        },
                        onBackToLoginClick = { goToLogin() }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBackPressHandler()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            goToLogin()
        }
    }

    private fun goToLogin() {
        try {
            findNavController().navigate(R.id.action_register2Fragment_to_loginFragment)
        } catch (e: Exception) {
            requireActivity().finish()
        }
    }

    private fun goToProfile(email: String) {
        val action = Register2FragmentDirections.actionRegister2FragmentToNavigationProfile(email)
        findNavController().navigate(action)
    }
}