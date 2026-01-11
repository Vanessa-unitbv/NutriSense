package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.nutrisense.R
import com.example.nutrisense.viewmodel.AuthViewModel
import com.example.nutrisense.helpers.extensions.*
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        observeViewModel()
        setupBackPressHandler()
    }

    private fun initializeViews(view: View) {
        emailEditText = view.findViewById(R.id.et_email)
        passwordEditText = view.findViewById(R.id.tiet_password)
        loginButton = view.findViewById(R.id.btn_do_login)
        registerButton = view.findViewById(R.id.btn_go_to_register)
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            val email = emailEditText.getTextString()
            val password = passwordEditText.getTextString()
            goToRegister(email, password)
        }

        loginButton.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val email = emailEditText.getTextString()
        val password = passwordEditText.getTextString()

        emailEditText.clearErrorAndFocus()
        passwordEditText.clearErrorAndFocus()

        authViewModel.loginUser(email, password)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.loginState.collect { state ->
                when (state) {
                    is AuthViewModel.LoginState.Idle -> {
                        setButtonsEnabled(true)
                    }
                    is AuthViewModel.LoginState.Loading -> {
                        setButtonsEnabled(false)
                    }
                    is AuthViewModel.LoginState.Success -> {
                        setButtonsEnabled(true)
                        requireContext().showSuccessToast(
                            "Welcome, ${state.user.firstName ?: state.user.email}!"
                        )
                        goToProfile(state.user.email)
                        authViewModel.resetLoginState()
                    }
                    is AuthViewModel.LoginState.Error -> {
                        setButtonsEnabled(true)
                        requireContext().showErrorToast(state.message)
                        authViewModel.resetLoginState()
                    }
                }
            }
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        loginButton.isEnabled = enabled
        registerButton.isEnabled = enabled
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