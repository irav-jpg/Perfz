package com.example.perfz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.perfz.core.FragmentCommunicator
import com.example.perfz.core.ResponseService
import com.example.perfz.databinding.FragmentAddTransactionBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AddTransactionFragment : Fragment() {

    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<AddTransactionViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }


        val categories = resources.getStringArray(R.array.transaction_categories)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCategory.adapter = adapter

        binding.btnSave.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener


            val type = if (binding.toggleGroupType.checkedButtonId == R.id.btnIncome) {
                "income"
            } else {
                "expense"
            }

            val amountStr = binding.etAmount.text.toString()
            val category = binding.spCategory.selectedItem?.toString() ?: "Otros"
            val description = binding.etDesc.text.toString()

            viewModel.save(
                uid,
                amountStr.toDoubleOrNull() ?: 0.0,
                category,
                description,
                type
            )
        }
        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ResponseService.Loading -> (activity as? FragmentCommunicator)?.manageLoader(true)
                    is ResponseService.Success -> {
                        (activity as? FragmentCommunicator)?.manageLoader(false)
                        findNavController().navigateUp()
                    }
                    is ResponseService.Error -> {
                        (activity as? FragmentCommunicator)?.manageLoader(false)
                        Snackbar.make(binding.root, state.error, Snackbar.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}