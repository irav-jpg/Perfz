package com.example.perfz

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.perfz.databinding.FragmentVeremail1Binding

class VerifyEmail : Fragment() {

    private var _binding: FragmentVeremail1Binding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVeremail1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnVerificarCodigo.setOnClickListener {
            findNavController().navigate(R.id.action_veremail1Fragment_to_newpasswordFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun validateFields() {
        val code = binding.codeEditText.text.toString().trim()

        val isCodeValid = code.length == 6 && code.all { it.isDigit() }

        binding.tilCodigo.error = when {
            code.isEmpty() -> null
            !isCodeValid -> "El código debe ser de 6 números"
            else -> null
        }

        binding.btnVerificarCodigo.isEnabled = isCodeValid

    }

}