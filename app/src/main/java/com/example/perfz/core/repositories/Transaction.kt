package com.example.perfz.core.repositories

data class Transaction(
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val type: String = "expense"
)