package com.example.perfz.home

import androidx.lifecycle.ViewModel
import com.example.perfz.core.repositories.Transaction
import com.example.perfz.core.repositories.getTransactionsRealtime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    fun startLoadingTransactions(uid: String) {
        getTransactionsRealtime(uid) { list ->
            _transactions.value = list
        }
    }
}