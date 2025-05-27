package com.example.nutrisense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nutrisense.data.entity.User
import com.example.nutrisense.viewmodel.AuthViewModel

class ProfileFragment : Fragment() {

    private val args: ProfileFragmentArgs by navArgs()
    private lateinit var authViewModel: AuthViewModel
    private lateinit var emailEditText: EditText
    private lateinit var logoutButton: Button
    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        emailEditText = view.findViewById(R.id.et_email)
        logoutButton = view.findViewById(R.id.btn_logout)

        loadUserData()

        setupClickListeners()
        setupBackPressHandler()
    }

    private fun loadUserData() {
        val email = args.email

        authViewModel.getUserByEmail(
            email = email,
            onSuccess = { user ->
                currentUser = user
                updateUI(user)
            },
            onError = { errorMessage ->
                Toast.makeText(context, "Error loading profile: $errorMessage", Toast.LENGTH_LONG).show()
                emailEditText.setText(email)
            }
        )
    }

    private fun updateUI(user: User?) {
        if (user != null) {
            emailEditText.setText(user.email)

            val welcomeText = if (!user.firstName.isNullOrEmpty()) {
                "${user.firstName}'s Profile"
            } else {
                "My Profile"
            }

            view?.findViewById<TextView>(R.id.tv_title_profile)?.text = welcomeText
        } else {
            emailEditText.setText(args.email)
        }
    }

    private fun setupClickListeners() {
        logoutButton.setOnClickListener {
            performLogout()
        }
    }

    private fun performLogout() {
        Toast.makeText(context, "Successfully logged out", Toast.LENGTH_SHORT).show()
        goToLogin()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
    }

    private fun goToLogin() {
        findNavController().navigate(R.id.loginFragment)
    }
}