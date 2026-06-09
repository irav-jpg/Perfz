package com.example.perfz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perfz.core.ResponseService
import com.example.perfz.core.repositories.Transaction
import com.example.perfz.core.repositories.addTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddTransactionViewModel : ViewModel() {
    private val _state = MutableStateFlow<ResponseService<String>?>(null)
    val state: StateFlow<ResponseService<String>?> = _state

    fun save(uid: String, amount: Double, category: String, desc: String, type: String) {
        if (amount <= 0) {
            _state.value = ResponseService.Error("Ingresa un monto válido")
            return
        }

        viewModelScope.launch {
            _state.value = ResponseService.Loading
            val transaction = Transaction(amount = amount, category = category, description = desc, type = type)

            addTransaction(uid, transaction) { success ->
                if (success) {
                    _state.value = ResponseService.Success("¡Guardado!")
                } else {
                    _state.value = ResponseService.Error("Error al guardar")
                }
                _state.value = null // Resetea el estado para habilitar futuros envíos
            }
        }
    }
}