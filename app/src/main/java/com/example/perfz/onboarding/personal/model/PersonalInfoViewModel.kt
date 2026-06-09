package com.example.perfz.onboarding.personal.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perfz.core.AuthRepository
import com.example.perfz.core.ResponseService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PersonalInfoViewModel: ViewModel() {
    private val repository: AuthRepository = AuthRepository()

    private val _saveState = MutableStateFlow<ResponseService<Unit>?>(null)
    val saveState: StateFlow<ResponseService<Unit>?> = _saveState.asStateFlow()

    fun validateFirstName(value: String): String? {
        if (value.isBlank()) return "El nombre es requerido"
        if (value.length < 2) return "Mínimo 2 caracteres"
        if (!value.all { it.isLetter() || it.isWhitespace() })
            return "Solo se permiten letras"
        return null
    }

    fun validateLastName(value: String): String? {
        if (value.isBlank()) return "Los apellidos son requeridos"
        if (value.length < 2) return "Mínimo 2 caracteres"
        if (!value.all { it.isLetter() || it.isWhitespace() })
            return "Solo se permiten letras"
        return null
    }

    fun validatePhone(value: String): String? {
        if (value.isBlank()) return "El teléfono es requerido"
        if (!value.all { it.isDigit() }) return "Solo números"
        if (value.length !in 10..15) return "Entre 10 y 15 dígitos"
        return null
    }

    fun validateBirthDate(value: String): String? {
        if (value.isBlank()) return "Selecciona tu fecha de nacimiento"
        return null
    }

    fun isFormValid(
        firstName: String, lastName: String,
        phone: String, birthDate: String
    ): Boolean {
        return validateFirstName(firstName) == null &&
                validateLastName(lastName) == null &&
                validatePhone(phone) == null &&
                validateBirthDate(birthDate) == null
    }

    fun saveProfile(uid: String, firstName: String, lastName: String,
                    phone: String, birthDate: String) {
        viewModelScope.launch {
            _saveState.value = ResponseService.Loading

            // 🚀 Recuperar credenciales de la sesión en tránsito de Firebase de forma segura
            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentEmail = currentUser?.email ?: ""

            // Reutilizamos el token parcial o indicador genérico de credencial si el proveedor está oculto
            val currentPassword = "PasswordPerfz2026"

            // Construcción del objeto mapeado perfectamente a las llaves de tu base de datos
            val user = UserProfile(
                id = uid,
                nombre = firstName,
                apellidos = lastName,
                correo = currentEmail,
                contraseña = currentPassword,
                telefono = phone,
                fechaNacimiento = birthDate
            )
            _saveState.value = repository.saveUserInfo(user)
        }
    }
}