package com.example.perfz.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.perfz.core.repositories.Transaction
import com.example.perfz.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(getItem(position), onItemClick)
    }

    class ViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {


        fun bind(transaction: Transaction, onItemClick: (Transaction) -> Unit) {
            binding.tvTransactionName.text = transaction.category


            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("es", "MX"))
            val fechaFormateada = sdf.format(Date(transaction.date))
            binding.tvDate.text = fechaFormateada



            if (transaction.type == "expense") {
                binding.tvAmount.text = String.format("-$%.2f", transaction.amount)
                binding.tvAmount.setTextColor(Color.parseColor("#E53935"))
            } else {
                binding.tvAmount.text = String.format("+$%.2f", transaction.amount)
                binding.tvAmount.setTextColor(Color.parseColor("#26A69A"))
            }


            binding.tvEmoji.text = when (transaction.category) {
                "Comida" -> "🍕"
                "Transporte" -> "🚗"
                "Vivienda" -> "🏠"
                "Entretenimiento" -> "🍿"
                "Saldo mes anterior" -> "⏳"
                "Inversión", "Inversiones" -> "📈"
                "Ahorro Wishlist" -> "✨"


                "Salario" -> "💼"
                "Pago" -> "💵"
                "Transferencia" -> "📱"
                "Efectivo" -> "💰"
                "Premio" -> "🏆"


                else -> "💳"
            }


            itemView.setOnClickListener {
                onItemClick(transaction)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}