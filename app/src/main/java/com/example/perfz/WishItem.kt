package com.example.perfz.wishlist

data class WishItem(
    val id: String,
    val name: String,
    val price: Double,
    var savedAmount: Double = 0.0,
    var isPurchased: Boolean = false
)