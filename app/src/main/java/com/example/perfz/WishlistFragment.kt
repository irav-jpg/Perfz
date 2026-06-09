package com.example.perfz.wishlist

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.perfz.R
import com.example.perfz.databinding.FragmentWishlistBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class WishlistFragment : Fragment(R.layout.fragment_wishlist) {

    private lateinit var binding: FragmentWishlistBinding
    private lateinit var wishlistAdapter: WishlistAdapter
    private val viewModel by activityViewModels<WishlistViewModel>()


    private val addTransactionViewModel by viewModels<com.example.perfz.AddTransactionViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding = FragmentWishlistBinding.bind(view)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }


        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "perfz_dev_user"
        viewModel.cargarWishlist(currentUid)

        setupRecyclerView()

        binding.fabAddWish.setOnClickListener {
            mostrarFormularioAgregarDeseo()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.listaDeseos.collect { lista ->
                wishlistAdapter.submitList(lista)
            }
        }
    }

    private fun setupRecyclerView() {
        wishlistAdapter = WishlistAdapter(
            onCheckedChange = { item, isChecked ->
                viewModel.actualizarEstadoCompra(item, isChecked)
            },
            onItemClick = { item ->
                mostrarFormularioAbonarAhorro(item)
            }
        )

        binding.rvWishlist.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = wishlistAdapter
        }

        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val itemEliminado = viewModel.listaDeseos.value[position]


                viewModel.eliminarDeseoPorId(itemEliminado.id)

                Snackbar.make(binding.root, "${itemEliminado.name} eliminado", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer") {
                        viewModel.restaurarDeseoDirecto(itemEliminado)
                    }.show()
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvWishlist)
    }

    private fun mostrarFormularioAbonarAhorro(item: WishItem) {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_add_saving, null)
        dialog.setContentView(view)

        val tvTitle = view.findViewById<TextView>(R.id.tvSavingTitle)
        val etAmount = view.findViewById<EditText>(R.id.etSavingAmount)
        val btnConfirm = view.findViewById<Button>(R.id.btnSaveSaving)

        tvTitle.text = "Abonar a:\n${item.name}"

        btnConfirm.setOnClickListener {
            val montoStr = etAmount.text.toString().trim()
            if (montoStr.isNotEmpty()) {
                val monto = montoStr.toDoubleOrNull() ?: 0.0
                if (monto > 0) {
                    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "perfz_dev_user"


                    viewModel.abonarAhorro(item, monto)


                    addTransactionViewModel.save(
                        uid = currentUid,
                        amount = monto,
                        category = "Ahorro Wishlist",
                        desc = "Ahorro: ${item.name}",
                        type = "expense"
                    )

                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun mostrarFormularioAgregarDeseo() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_add_wish, null)
        dialog.setContentView(view)

        val etName = view.findViewById<EditText>(R.id.etWishName)
        val etPrice = view.findViewById<EditText>(R.id.etWishPrice)
        val btnSave = view.findViewById<Button>(R.id.btnSaveWish)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val priceStr = etPrice.text.toString().trim()

            if (name.isNotEmpty() && priceStr.isNotEmpty()) {
                val price = priceStr.toDoubleOrNull() ?: 0.0
                viewModel.agregarDeseo(name, price)
                dialog.dismiss()
            } else {
                etName.error = "Completa los campos"
            }
        }
        dialog.show()
    }
}