package com.example.perfz.onboarding.personal.model

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.perfz.R
import androidx.navigation.fragment.findNavController
import com.example.perfz.core.FragmentCommunicator
import com.example.perfz.core.ResponseService
import com.example.perfz.databinding.FragmentPersonalInfoBinding
import com.example.perfz.home.HomeActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

class PersonalInfoFragment : Fragment() {

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<PersonalInfoViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator

        setupValidation()
        setupDatePicker()
        setupClickListeners()
        observeState()

        return binding.root
    }

    private fun setupValidation() {
        binding.btnContinuar.isEnabled = false

        binding.etNombre.addTextChangedListener { validateAndEnable() }
        binding.etApellidos.addTextChangedListener { validateAndEnable() }
        binding.etCelular.addTextChangedListener { validateAndEnable() }
        binding.etFechaNacimiento.addTextChangedListener { validateAndEnable() }
    }

    private fun validateAndEnable() {
        val firstName = binding.etNombre.text.toString().trim()
        val lastName = binding.etApellidos.text.toString().trim()
        val phone = binding.etCelular.text.toString().trim()
        val birthDate = binding.etFechaNacimiento.text.toString().trim()

        binding.nombreTil.error = viewModel.validateFirstName(firstName)
        binding.apellidosTil.error = viewModel.validateLastName(lastName)
        binding.celularTil.error = viewModel.validatePhone(phone) // Corregido el TIL correspondiente
        binding.fechaNacimientoTil.error = viewModel.validateBirthDate(birthDate) // Corregida validación cruzada

        binding.btnContinuar.isEnabled =
            viewModel.isFormValid(firstName, lastName, phone, birthDate)
    }

    private fun setupDatePicker() {
        binding.etFechaNacimiento.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val formatted = "%04d-%02d-%02d".format(year, month + 1, day)
                    binding.etFechaNacimiento.setText(formatted)
                },
                cal.get(Calendar.YEAR) - 18,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnContinuar.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Snackbar.make(binding.root, "Sesión inválida", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            viewModel.saveProfile(
                uid = uid,
                firstName = binding.etNombre.text.toString().trim(),
                lastName = binding.etApellidos.text.toString().trim(),
                phone = binding.etCelular.text.toString().trim(),
                birthDate = binding.etFechaNacimiento.text.toString().trim()
            )
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnContinuar.isEnabled = false
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            val intent = Intent(requireContext(), HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.btnContinuar.isEnabled = true
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        }
                        null -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}