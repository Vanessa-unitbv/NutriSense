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
import com.google.android.material.textfield.TextInputEditText

class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btn_go_to_register).setOnClickListener{
            val email = view.findViewById<EditText>(R.id.et_email).text.toString()
            val password = view.findViewById<TextInputEditText>(R.id.tiet_password).text.toString()
            goToRegister(email, password)
        }
        view.findViewById<Button>(R.id.btn_do_login).setOnClickListener {
            val email = view.findViewById<EditText>(R.id.et_email).text.toString()
            doLogin(email)
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
    }

    private fun goToRegister(email: String, password: String) {
        val action = LoginFragmentDirections.actionLoginFragmentToRegister1Fragment(email, password)
        findNavController().navigate(action)
    }

    private fun doLogin(email: String) {
        val action = LoginFragmentDirections.actionLoginFragmentToNavigationProfile(email)
        findNavController().navigate(action)
    }
}