package com.example.perfz.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.perfz.core.repositories.Transaction
import com.example.perfz.databinding.ItemTransactionBinding

class TransactionAdapter : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {

            binding.tvTransactionName.text = transaction.category
            binding.tvDate.text = "13 Mayo 2026"


            if (transaction.type == "expense") {
                binding.tvAmount.text = String.format("-$%.2f", transaction.amount)
                binding.tvAmount.setTextColor(Color.parseColor("#E53935")) // Rojo de tu XML
            } else {
                binding.tvAmount.text = String.format("+$%.2f", transaction.amount)
                binding.tvAmount.setTextColor(Color.parseColor("#26A69A"))
            }


            binding.tvEmoji.text = when (transaction.category) {
                "Comida" -> "🍕"
                "Transporte" -> "🚗"
                "Vivienda" -> "🏠"
                "Entretenimiento" -> "🍿"
                else -> "💰"
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {

            return oldItem.description == newItem.description && oldItem.amount == newItem.amount
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}