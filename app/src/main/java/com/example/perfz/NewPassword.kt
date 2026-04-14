package com.example.perfz

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener // IMPORTANTE
import androidx.navigation.fragment.findNavController
import com.example.perfz.databinding.FragmentNewpasswordBinding

class NewPassword : Fragment() {

    private var _binding: FragmentNewpasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewpasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.newpassEditText.addTextChangedListener {
            validateFields()
        }

        binding.confpassEditText.addTextChangedListener {
            validateFields()
        }

        binding.btnActualizarPass.setOnClickListener {
            findNavController().navigate(R.id.action_newpassword_to_login)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack(R.id.recpasswordFragment, false)
        }

        validateFields()
    }

    private fun validateFields() {
        val password = binding.newpassEditText.text.toString().trim()
        val confirmPassword = binding.confpassEditText.text.toString().trim()

        val isPasswordValid = password.length == 8
        val doPasswordsMatch = password == confirmPassword

        binding.btnActualizarPass.isEnabled = isPasswordValid && doPasswordsMatch

        binding.tilNewPass.error = when {
            password.isEmpty() -> null
            !isPasswordValid -> "Debe tener exactamente 8 caracteres"
            else -> null
        }

        binding.tilConfirmPass.error = when {
            confirmPassword.isEmpty() -> null
            !doPasswordsMatch -> "Las contraseñas no coinciden"
            else -> null
        }
    }
}