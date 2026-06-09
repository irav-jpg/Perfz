package com.example.perfz.home

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeViewModel>()

    private lateinit var transactionAdapter: TransactionAdapter

    private var todasLasTransacciones: List<Transaction> = emptyList()
    private var mesSeleccionadoActual: String = ""

    private val mesAnioFormatter = SimpleDateFormat("MMMM yyyy", Locale("es", "MX")).apply {
        timeZone = TimeZone.getDefault()
    }

    private val wishlistViewModel by activityViewModels<com.example.perfz.wishlist.WishlistViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)


        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val emailName = firebaseUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
        val finalName = firebaseUser?.displayName ?: (emailName ?: "Usuario")
        binding.tvWelcome.text = "¡Hola, $finalName!"


        val tvProfileLetter = view.findViewById<TextView>(R.id.tvProfileLetter)
        val btnProfileClick = view.findViewById<View>(R.id.btnProfileClick)


        val primeraLetra = finalName.trim().firstOrNull()?.toString()?.uppercase() ?: "U"
        tvProfileLetter?.text = primeraLetra


        btnProfileClick?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }


        mesSeleccionadoActual = mesAnioFormatter.format(Date())

        setupRecyclerView()
        setupPieChart()
        observeTransactions()


        binding.btnQuickAdd.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addTransactionFragment)
        }


        val btnNavMarket = view.findViewById<ImageView>(R.id.btnNavMarket)
        val btnNavHome = view.findViewById<ImageView>(R.id.btnNavHome)
        val btnNavProfile = view.findViewById<ImageView>(R.id.btnNavProfile)

        btnNavMarket?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_marketFragment)
        }

        btnNavHome?.setOnClickListener {
            if (todasLasTransacciones.isNotEmpty()) {
                val meses = todasLasTransacciones.sortedBy { it.date }.map { mesAnioFormatter.format(Date(it.date)) }.distinct()
                val indexMesActual = meses.indexOf(mesAnioFormatter.format(Date()))
                if (indexMesActual != -1) {
                    binding.spMonthFilter.setSelection(indexMesActual)
                }
            }
        }

        btnNavProfile?.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_wishlistFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val uid = firebaseUser?.uid ?: "perfz_dev_user"
        viewModel.startLoadingTransactions(uid)
    }

    private fun setupRecyclerView() {

        transactionAdapter = TransactionAdapter { transaccionSeleccionada ->
            mostrarDialogoDetalle(transaccionSeleccionada)
        }
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
            isNestedScrollingEnabled = false
        }
    }


    private fun mostrarDialogoDetalle(transaction: Transaction) {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val dialogView = layoutInflater.inflate(R.layout.dialog_transaction_detail, null)
        dialog.setContentView(dialogView)

        val tvCategory = dialogView.findViewById<TextView>(R.id.tvDetailCategory)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvDetailDate)
        val tvAmount = dialogView.findViewById<TextView>(R.id.tvDetailAmount)
        val tvDescription = dialogView.findViewById<TextView>(R.id.tvDetailDescription)
        val btnClose = dialogView.findViewById<View>(R.id.btnCloseDetail)

        tvCategory.text = transaction.category


        val formatter = SimpleDateFormat("dd 'de' MMMM yyyy, hh:mm a", Locale("es", "MX"))
        tvDate.text = formatter.format(Date(transaction.date))

        if (transaction.type == "income") {
            tvAmount.text = String.format("+$%.2f", transaction.amount)
            tvAmount.setTextColor(Color.parseColor("#26A69A"))
        } else {
            tvAmount.text = String.format("-$%.2f", transaction.amount)
            tvAmount.setTextColor(Color.parseColor("#E53935"))
        }

        if (!transaction.description.isNullOrBlank()) {
            tvDescription.text = transaction.description
        } else {
            tvDescription.text = "Esta transacción no incluye ninguna descripción adicional."
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            setHoleColor(Color.TRANSPARENT)
            setDrawEntryLabels(false)
            animateY(500)

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

    private fun observeTransactions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transactions.collect { transactionsFromFirebase ->
                todasLasTransacciones = transactionsFromFirebase.sortedByDescending { it.date }

                todasLasTransacciones.forEach {
                    android.util.Log.d("Prueba_Matematica", "Categoría: ${it.category}, Monto: ${it.amount}, Tipo: ${it.type}")
                }

                setupMonthSpinner(todasLasTransacciones)
                procesarFlujoFinanciero(mesSeleccionadoActual)
            }
        }
    }

    private fun setupMonthSpinner(transactions: List<Transaction>) {
        val meses = transactions.sortedBy { it.date }.map { mesAnioFormatter.format(Date(it.date)) }.distinct().toMutableList()
        if (meses.isEmpty()) meses.add(mesAnioFormatter.format(Date()))

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, meses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spMonthFilter.adapter = adapter

        val indexMesActual = meses.indexOf(mesSeleccionadoActual)
        if (indexMesActual != -1) {
            binding.spMonthFilter.setSelection(indexMesActual)
        } else {
            val indexHoy = meses.indexOf(mesAnioFormatter.format(Date()))
            if (indexHoy != -1) binding.spMonthFilter.setSelection(indexHoy) else binding.spMonthFilter.setSelection(meses.size - 1)
        }

        binding.spMonthFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                mesSeleccionadoActual = meses[pos]
                procesarFlujoFinanciero(mesSeleccionadoActual)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun procesarFlujoFinanciero(mesSeleccionado: String) {
        val grupos = todasLasTransacciones.groupBy { mesAnioFormatter.format(Date(it.date)) }
        val mesesOrdenadosCronologicamente = grupos.keys.sortedBy { key ->
            grupos[key]?.minOfOrNull { it.date } ?: 0L
        }

        var saldoAcumuladoPasado = 0.0
        var ingresosMesActual = 0.0
        var gastosMesActual = 0.0
        var inversionesMesActual = 0.0
        var ahorrosWishlistMesActual = 0.0

        for (mes in mesesOrdenadosCronologicamente) {
            val tMes = grupos[mes] ?: emptyList()
            val abonos = tMes.filter { it.type == "income" }.sumOf { it.amount }

            val deudas = tMes.filter { it.type == "expense" && it.category != "Inversión" && it.category != "Inversiones" && it.category != "Ahorro Wishlist" }.sumOf { it.amount }
            val inv = tMes.filter { it.type == "expense" && (it.category == "Inversión" || it.category == "Inversiones") }.sumOf { it.amount }
            val aw = tMes.filter { it.type == "expense" && it.category == "Ahorro Wishlist" }.sumOf { it.amount }

            if (mes == mesSeleccionado) {
                ingresosMesActual = abonos
                gastosMesActual = deudas
                inversionesMesActual = inv
                ahorrosWishlistMesActual = aw
                break
            } else {
                saldoAcumuladoPasado += (abonos - (deudas + inv + aw))
            }
        }

        val transaccionesDelMes = todasLasTransacciones.filter {
            mesAnioFormatter.format(Date(it.date)) == mesSeleccionado
        }.toMutableList()

        if (saldoAcumuladoPasado != 0.0) {
            val transaccionRemanente = Transaction(
                id = "remanente_acumulado",
                category = "Saldo mes anterior",
                description = if (saldoAcumuladoPasado > 0) "Remanente positivo heredado" else "Déficit arrastrado",
                amount = Math.abs(saldoAcumuladoPasado),
                type = if (saldoAcumuladoPasado > 0) "income" else "expense",
                date = if (transaccionesDelMes.isNotEmpty()) transaccionesDelMes.last().date - 1000 else System.currentTimeMillis()
            )
            transaccionesDelMes.add(transaccionRemanente)
        }

        val presupuestoTotalDisponible = (if (saldoAcumuladoPasado > 0) saldoAcumuladoPasado else 0.0) + ingresosMesActual
        val deudasTotalesEfectivas = (if (saldoAcumuladoPasado < 0) Math.abs(saldoAcumuladoPasado) else 0.0) + gastosMesActual + inversionesMesActual + ahorrosWishlistMesActual

        val saldoFinalDisponible = presupuestoTotalDisponible - deudasTotalesEfectivas

        binding.tvMoneyRemaining.text = String.format("$%.2f", saldoFinalDisponible)

        transactionAdapter.submitList(transaccionesDelMes.sortedByDescending { it.date })

        renderizarGraficaMensual(transaccionesDelMes, saldoFinalDisponible)
    }

    private fun renderizarGraficaMensual(transacciones: List<Transaction>, saldoDisponible: Double) {
        val gastosPorCategoria = transacciones.filter { it.type == "expense" && it.id != "remanente_acumulado" }
            .groupBy { it.category }
            .mapValues { it.value.sumOf { it.amount } }

        val entries = ArrayList<PieEntry>()
        val colores = ArrayList<Int>()

        val colorInversion = Color.parseColor("#42A5F5")
        val colorWishlist = Color.parseColor("#9575CD")
        val colorDisponible = Color.parseColor("#36BBA7")

        val paletaGastos = listOf(
            Color.parseColor("#FF7043"),
            Color.parseColor("#FFD54F"),
            Color.parseColor("#EC407A")
        )

        var gastosIdx = 0
        var totalConsumidoMes = 0.0

        gastosPorCategoria.forEach { (categoria, monto) ->
            when (categoria) {
                "Inversión", "Inversiones" -> {
                    entries.add(PieEntry(monto.toFloat(), "$categoria: $$monto"))
                    colores.add(colorInversion)
                }
                "Ahorro Wishlist" -> {
                    entries.add(PieEntry(monto.toFloat(), "Wishlist: $$monto"))
                    colores.add(colorWishlist)
                }
                else -> {
                    entries.add(PieEntry(monto.toFloat(), "$categoria: $$monto"))
                    colores.add(paletaGastos[gastosIdx % paletaGastos.size])
                    gastosIdx++
                }
            }
            totalConsumidoMes += monto
        }

        if (saldoDisponible > 0) {
            entries.add(PieEntry(saldoDisponible.toFloat(), "Disponible: $${String.format("%.2f", saldoDisponible)}"))
            colores.add(colorDisponible)
        }

        if (entries.isEmpty() && todasLasTransacciones.isEmpty()) {
            entries.add(PieEntry(1f, "Disponible: $0.00"))
            colores.add(Color.parseColor("#E0E0E0"))
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colores
            setDrawValues(false)
        }

        binding.pieChart.apply {
            data = PieData(dataSet)

            val presupuestoBaseTotal = totalConsumidoMes + if (saldoDisponible > 0) saldoDisponible else 0.0
            val porcentajeConsumido = if (presupuestoBaseTotal > 0) ((totalConsumidoMes / presupuestoBaseTotal) * 100).toInt() else 0

            centerText = "$porcentajeConsumido%\nUtilizado"
            setCenterTextSize(16f)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}