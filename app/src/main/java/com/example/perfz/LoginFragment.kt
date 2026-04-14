package com.example.perfz

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.perfz.databinding.FragmentLoginBinding
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels

class LoginFragment : Fragment() {

    private var _binding : FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<SigninViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupValidation()

        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.tvRegistrarse.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_peregisterFragment)
        }

        binding.tvReset.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_recpasswordFragment)
        }
    }

    private fun setupValidation(){
        binding.loginButton.isEnabled = false

        binding.emailEditText.addTextChangedListener{
            validateFields()
        }
        binding.passwordEditText.addTextChangedListener{
            validateFields()
        }
    }

    private fun validateFields(){
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        val isEmailValid = isValidEmail(email)
        val isPasswordValid = password.length >= 8

        binding.emailInputLayout.error = when {
            email.isEmpty() -> null
            !isEmailValid -> "Correo inválido"
            else -> null
        }

        binding.passwordInputLayout.error = when {
            password.isEmpty() -> null
            !isPasswordValid -> "Mínimo 8 caracteres"
            else -> null
        }

        binding.loginButton.isEnabled =
            email.isNotEmpty() && password.isNotEmpty() && isEmailValid && isPasswordValid

    }

    private fun isValidEmail(email: String) : Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}