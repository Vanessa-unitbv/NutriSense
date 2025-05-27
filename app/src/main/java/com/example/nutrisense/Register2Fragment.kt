package com.example.nutrisense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nutrisense.viewmodel.AuthViewModel
import com.google.android.material.textfield.TextInputEditText

class Register2Fragment : Fragment() {

    private val args: Register2FragmentArgs by navArgs()
    private lateinit var authViewModel: AuthViewModel
    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var ageEditText: TextInputEditText
    private lateinit var registerButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_step_2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        firstNameEditText = view.findViewById(R.id.FirstName)
        lastNameEditText = view.findViewById(R.id.LastName)
        ageEditText = view.findViewById(R.id.Age)
        registerButton = view.findViewById(R.id.btn_go_to_profile)

        setupClickListeners()
        setupBackPressHandler()
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            performRegistration()
        }
    }

    private fun performRegistration() {
        val email = args.email ?: ""
        val password = args.password ?: ""
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val ageText = ageEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Missing data from previous step", Toast.LENGTH_LONG).show()
            return
        }

        var age: Int? = null
        if (ageText.isNotEmpty()) {
            try {
                age = ageText.toInt()
                if (age!! < 13 || age > 120) {
                    ageEditText.error = "Age must be between 13 and 120 years"
                    return
                }
            } catch (e: NumberFormatException) {
                ageEditText.error = "Age must be a valid number"
                return
            }
        }

        registerButton.isEnabled = false

        authViewModel.registerUser(
            email = email,
            password = password,
            firstName = if (firstName.isNotEmpty()) firstName else null,
            lastName = if (lastName.isNotEmpty()) lastName else null,
            age = age,
            onSuccess = { user ->
                registerButton.isEnabled = true
                Toast.makeText(
                    context,
                    "Account created successfully! Welcome, ${user.firstName ?: user.email}!",
                    Toast.LENGTH_LONG
                ).show()
                goToProfile(user.email)
            },
            onError = { errorMessage ->
                registerButton.isEnabled = true
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_register2Fragment_to_loginFragment)
        }
    }

    private fun goToProfile(email: String) {
        val action = Register2FragmentDirections.actionRegister2FragmentToNavigationProfile(email)
        findNavController().navigate(action)
    }
}