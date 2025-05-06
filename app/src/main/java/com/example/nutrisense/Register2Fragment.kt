package com.example.nutrisense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

class Register2Fragment:Fragment() {
    private val args: Register2FragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register_step_2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = args.email

        view.findViewById<Button>(R.id.btn_go_to_profile).setOnClickListener {
            goToProfile(email)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_register2Fragment_to_loginFragment)
        }
    }
    private fun goToProfile(email: String?) {
        val action = Register2FragmentDirections.actionRegister2FragmentToNavigationProfile(email ?: "")
        findNavController().navigate(action)
    }
}