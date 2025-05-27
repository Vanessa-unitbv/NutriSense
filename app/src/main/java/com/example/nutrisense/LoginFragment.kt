package com.example.nutrisense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.nutrisense.viewmodel.AuthViewModel
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

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        emailEditText = view.findViewById(R.id.et_email)
        passwordEditText = view.findViewById(R.id.tiet_password)
        loginButton = view.findViewById(R.id.btn_do_login)
        registerButton = view.findViewById(R.id.btn_go_to_register)

        setupClickListeners()
        setupBackPressHandler()
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            goToRegister(email, password)
        }

        loginButton.setOnClickListener {
            performLogin()
        }
    }

    private fun performLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return
        }

        setButtonsEnabled(false)

        authViewModel.loginUser(
            email = email,
            password = password,
            onSuccess = { user ->
                setButtonsEnabled(true)
                Toast.makeText(context, "Welcome, ${user.firstName ?: user.email}!", Toast.LENGTH_SHORT).show()
                goToProfile(user.email)
            },
            onError = { errorMessage ->
                setButtonsEnabled(true)
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
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
        val action = LoginFragmentDirections.actionLoginFragmentToRegister1Fragment(email, password)
        findNavController().navigate(action)
    }

    private fun goToProfile(email: String) {
        val action = LoginFragmentDirections.actionLoginFragmentToNavigationProfile(email)
        findNavController().navigate(action)
    }
}