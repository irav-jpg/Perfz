package com.example.perfz.wishlist

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.perfz.R

class WishlistAdapter(
    private val onCheckedChange: (WishItem, Boolean) -> Unit,
    private val onItemClick: (WishItem) -> Unit
) : ListAdapter<WishItem, WishlistAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cbPurchased: CheckBox = view.findViewById(R.id.cbPurchased)
        val tvName: TextView = view.findViewById(R.id.nameItem)
        val tvPrice: TextView = view.findViewById(R.id.priceItem)
        val tvSavedStatus: TextView = view.findViewById(R.id.tvSavedStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wishlist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.tvName.text = item.name
        holder.tvPrice.text = String.format("$%.2f", item.price)
        holder.tvSavedStatus.text = String.format("Ahorrado: $%.2f", item.savedAmount)

        holder.cbPurchased.setOnCheckedChangeListener(null)
        holder.cbPurchased.isChecked = item.isPurchased

        if (item.isPurchased) {
            holder.tvName.paintFlags = holder.tvName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvName.setTextColor(android.graphics.Color.parseColor("#9E9E9E"))
            holder.tvPrice.setTextColor(android.graphics.Color.parseColor("#9E9E9E"))
            holder.tvSavedStatus.setTextColor(android.graphics.Color.parseColor("#9E9E9E"))
        } else {
            holder.tvName.paintFlags = holder.tvName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvName.setTextColor(android.graphics.Color.parseColor("#1A1A1A"))
            holder.tvPrice.setTextColor(android.graphics.Color.parseColor("#36BBA7"))
            holder.tvSavedStatus.setTextColor(android.graphics.Color.parseColor("#757575"))
        }

        holder.cbPurchased.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChange(item, isChecked)
        }

        holder.itemView.setOnClickListener {
            if (!item.isPurchased) {
                onItemClick(item)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<WishItem>() {
        override fun areItemsTheSame(oldItem: WishItem, newItem: WishItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: WishItem, newItem: WishItem): Boolean = oldItem == newItem
    }
}