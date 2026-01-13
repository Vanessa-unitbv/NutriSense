package com.example.nutrisense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nutrisense.R
import com.example.nutrisense.viewmodel.AuthViewModel
import com.example.nutrisense.helpers.extensions.*
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Register2Fragment : Fragment() {

    private val args: Register2FragmentArgs by navArgs()
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var ageEditText: TextInputEditText
    private lateinit var registerButton: Button
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_step_2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupClickListeners()
        observeViewModel()
        setupBackPressHandler()
        showEmailPasswordInfo()
    }

    private fun initializeViews(view: View) {
        firstNameEditText = view.findViewById(R.id.FirstName)
        lastNameEditText = view.findViewById(R.id.LastName)
        ageEditText = view.findViewById(R.id.Age)
        registerButton = view.findViewById(R.id.btn_go_to_profile)
        btnLogout = view.findViewById(R.id.btn_logout)
    }

    private fun showEmailPasswordInfo() {
        val email = args.email ?: "No email"
        requireContext().showToast("Registering with: $email", false)
    }

    private fun setupClickListeners() {
        registerButton.setOnClickListener {
            performRegistration()
        }

        btnLogout.setOnClickListener {
            goToLogin()
        }
    }

    private fun performRegistration() {
        val email = args.email ?: ""
        val password = args.password ?: ""
        val firstName = firstNameEditText.getTextString()
        val lastName = lastNameEditText.getTextString()
        val ageText = ageEditText.getTextString()

        if (email.isEmpty() || password.isEmpty()) {
            requireContext().showErrorToast("Missing email or password. Please go back to login screen.")
            return
        }

        // Validare vârstă - dacă este completată trebuie să fie validă
        if (ageText.isNotEmpty()) {
            if (!ageEditText.validateNumberField("Age", 1f, 150f)) {
                return
            }
        }

        val age = if (ageText.isNotEmpty()) ageText.toIntOrNull() else null

        authViewModel.registerUser(
            email = email,
            password = password,
            firstName = if (firstName.isNotEmpty()) firstName else null,
            lastName = if (lastName.isNotEmpty()) lastName else null,
            age = age
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.registerState.collect { state ->
                when (state) {
                    is AuthViewModel.RegisterState.Idle -> {
                        registerButton.isEnabled = true
                    }
                    is AuthViewModel.RegisterState.Loading -> {
                        registerButton.isEnabled = false
                    }
                    is AuthViewModel.RegisterState.Success -> {
                        registerButton.isEnabled = true
                        requireContext().showSuccessToast(
                            "Account created successfully! Welcome, ${state.user.firstName ?: state.user.email}!"
                        )
                        goToProfile(state.user.email)
                        authViewModel.resetRegisterState()
                    }
                    is AuthViewModel.RegisterState.Error -> {
                        registerButton.isEnabled = true
                        requireContext().showErrorToast(state.message)
                        authViewModel.resetRegisterState()
                    }
                }
            }
        }
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