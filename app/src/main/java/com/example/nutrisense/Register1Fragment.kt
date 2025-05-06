package com.example.nutrisense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputEditText

class Register1Fragment : Fragment() {
    private val navArgs: Register1FragmentArgs by navArgs()
    private var emailEditText: EditText? = null
    private var passwordEditText: TextInputEditText? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_step_1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emailEditText = view.findViewById(R.id.et_email)
        passwordEditText = view.findViewById(R.id.tiet_password)

        val email: String? = navArgs.email
        val password: String? = navArgs.password

        emailEditText?.setText(email)
        passwordEditText?.setText(password)

        view.findViewById<Button>(R.id.btn_go_to_register_step_2).setOnClickListener {
            val email = emailEditText?.text.toString()
            goToRegisterStep2(email)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_register1Fragment_to_loginFragment)
        }
    }
    private fun goToRegisterStep2(email: String) {
        val action = Register1FragmentDirections.actionRegister1FragmentToRegister2Fragment(email)
        findNavController().navigate(action)
    }
}