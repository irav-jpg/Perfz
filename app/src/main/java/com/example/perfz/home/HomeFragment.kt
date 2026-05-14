package com.example.perfz.home

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.perfz.R
import com.example.perfz.core.repositories.Transaction
import com.example.perfz.databinding.FragmentHomeBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var transactionAdapter: TransactionAdapter
    private val viewModel by viewModels<HomeViewModel>()


    private var capitalInicial: Double = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)


        val prefs = requireContext().getSharedPreferences("PerfzPrefs", Context.MODE_PRIVATE)
        capitalInicial = prefs.getFloat("capital_inicial", 0.0f).toDouble()

        setupRecyclerView()
        setupPieChart()
        observeTransactions()


        binding.tvMoneyRemaining.setOnClickListener {
            showEditCapitalDialog()
        }


        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let { viewModel.startLoadingTransactions(it) }

        binding.fabAddTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addTransactionFragment)
        }
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter()
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setDrawEntryLabels(false)
            animateY(1000)


            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                textSize = 12f
                textColor = Color.BLACK
            }
        }
    }

    private fun showEditCapitalDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Asignar Capital Inicial")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        // Mostramos el capital actual para que el usuario sepa cuánto hay
        input.setText(capitalInicial.toString())
        builder.setView(input)

        builder.setPositiveButton("Guardar") { _, _ ->
            val textoInput = input.text.toString()
            if (textoInput.isNotEmpty()) {
                val nuevoCapital = textoInput.toDoubleOrNull() ?: 0.0


                capitalInicial = nuevoCapital


                val prefs = requireContext().getSharedPreferences("PerfzPrefs", Context.MODE_PRIVATE)
                prefs.edit().putFloat("capital_inicial", nuevoCapital.toFloat()).apply()


                val transaccionesActuales = viewModel.transactions.value
                updateBalanceUI(transaccionesActuales)
                updateChart(transaccionesActuales)

                android.widget.Toast.makeText(requireContext(), "Capital actualizado: $nuevoCapital", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun observeTransactions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transactions.collect { transactions ->
                if (transactions.isEmpty() && capitalInicial <= 0) {
                    binding.layoutChart.visibility = View.GONE
                    binding.tvMoneyRemaining.text = "$0.00"
                    binding.progressBalance.progress = 0
                } else {
                    binding.layoutChart.visibility = View.VISIBLE
                    updateChart(transactions)
                    updateBalanceUI(transactions)
                    transactionAdapter.submitList(transactions)
                }
            }
        }
    }

    private fun updateBalanceUI(transactions: List<Transaction>) {
        val totalGastos = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        val totalAbonos = transactions.filter { it.type == "income" }.sumOf { it.amount }


        val presupuestoTotal = capitalInicial + totalAbonos
        val saldoDisponible = presupuestoTotal - totalGastos

        binding.tvMoneyRemaining.text = String.format("$%.2f", saldoDisponible)


        if (presupuestoTotal > 0) {
            val porcentajeDisponible = ((saldoDisponible / presupuestoTotal) * 100).toInt()
            binding.progressBalance.progress = porcentajeDisponible.coerceIn(0, 100)
        } else {
            binding.progressBalance.progress = 0
        }
    }

    private fun updateChart(transactions: List<Transaction>) {
        val totalAbonos = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val expensesByCategory = transactions.filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val totalGastado = expensesByCategory.values.sum()


        val presupuestoTotal = capitalInicial + totalAbonos

        val entries = expensesByCategory.map {
            PieEntry(it.value.toFloat(), "${it.key}: $${it.value}")
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#FF7043"), Color.parseColor("#26A69A"),
                Color.parseColor("#FFD54F"), Color.parseColor("#42A5F5"),
                Color.parseColor("#9575CD"), Color.parseColor("#4DB6AC")
            )
            setDrawValues(false)
        }

        binding.pieChart.apply {
            data = PieData(dataSet)


            val porcentajeRealGastado = if (presupuestoTotal > 0) {
                ((totalGastado / presupuestoTotal) * 100).toInt()
            } else 0

            centerText = "$porcentajeRealGastado%\nGastado"
            setCenterTextSize(16f)

            invalidate()
        }
    }
}