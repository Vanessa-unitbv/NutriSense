package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.nutrisense.R
import com.example.nutrisense.viewmodel.AuthViewModel
import com.example.nutrisense.helpers.extensions.*
import com.google.android.material.textfield.TextInputEditText

class LoginFragment : Fragment() {

    private lateinit var authViewModel: AuthViewModel
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

        initializeComponents()
        initializeViews(view)
        setupClickListeners()
        setupBackPressHandler()
    }

    private fun initializeComponents() {
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
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

            if (validateRegistrationInput(email, password)) {
                goToRegister(email, password)
            }
        }

        loginButton.setOnClickListener {
            performLogin()
        }
    }

    private fun validateRegistrationInput(email: String, password: String): Boolean {
        emailEditText.clearErrorAndFocus()
        passwordEditText.clearErrorAndFocus()

        if (email.isEmpty()) {
            emailEditText.setErrorAndFocus("Email is required for registration")
            return false
        }

        if (!email.isValidEmail()) {
            emailEditText.setErrorAndFocus("Invalid email format")
            return false
        }

        if (password.isEmpty()) {
            passwordEditText.setErrorAndFocus("Password is required for registration")
            return false
        }

        if (!password.isValidPassword()) {
            passwordEditText.setErrorAndFocus("Password must be at least 6 characters for registration")
            return false
        }

        return true
    }

    private fun performLogin() {
        val email = emailEditText.getTextString()
        val password = passwordEditText.getTextString()

        emailEditText.clearErrorAndFocus()
        passwordEditText.clearErrorAndFocus()

        if (email.isEmpty()) {
            emailEditText.setErrorAndFocus("Email is required")
            return
        }

        if (password.isEmpty()) {
            passwordEditText.setErrorAndFocus("Password is required")
            return
        }

        setButtonsEnabled(false)

        authViewModel.loginUser(
            email = email,
            password = password,
            onSuccess = { user ->
                setButtonsEnabled(true)
                requireContext().showSuccessToast("Welcome, ${user.firstName ?: user.email}!")
                goToProfile(user.email)
            },
            onError = { errorMessage ->
                setButtonsEnabled(true)
                requireContext().showErrorToast(errorMessage)
            }
        )
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