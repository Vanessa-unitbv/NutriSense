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
import androidx.navigation.fragment.navArgs
import com.example.nutrisense.viewmodel.AuthViewModel
import com.google.android.material.textfield.TextInputEditText

class Register1Fragment : Fragment() {

    private val navArgs: Register1FragmentArgs by navArgs()
    private lateinit var authViewModel: AuthViewModel
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var nextButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_step_1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        emailEditText = view.findViewById(R.id.et_email)
        passwordEditText = view.findViewById(R.id.tiet_password)
        nextButton = view.findViewById(R.id.btn_go_to_register_step_2)

        val email = navArgs.email
        val password = navArgs.password

        if (!email.isNullOrEmpty()) {
            emailEditText.setText(email)
        }
        if (!password.isNullOrEmpty()) {
            passwordEditText.setText(password)
        }

        setupClickListeners()
        setupBackPressHandler()
    }

    private fun setupClickListeners() {
        nextButton.setOnClickListener {
            validateAndProceed()
        }
    }

    private fun validateAndProceed() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Invalid email format"
            return
        }

        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            return
        }

        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            return
        }

        nextButton.isEnabled = false

        authViewModel.getUserByEmail(
            email = email,
            onSuccess = { existingUser ->
                nextButton.isEnabled = true
                if (existingUser != null) {
                    Toast.makeText(context, "Email is already registered", Toast.LENGTH_LONG).show()
                } else {
                    goToRegisterStep2(email, password)
                }
            },
            onError = { errorMessage ->
                nextButton.isEnabled = true
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_register1Fragment_to_loginFragment)
        }
    }

    private fun goToRegisterStep2(email: String, password: String) {
        val action = Register1FragmentDirections.actionRegister1FragmentToRegister2Fragment(email, password)
        findNavController().navigate(action)
    }
}