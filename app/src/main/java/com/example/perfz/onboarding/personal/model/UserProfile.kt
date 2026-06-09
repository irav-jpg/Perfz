package com.example.perfz.onboarding.personal.model

data class UserProfile(
    val id: String = "",
    val nombre: String = "",
    val apellidos: String = "",
    val correo: String = "",
    val contraseña: String = "",
    val telefono: String = "",
    val fechaNacimiento: String = ""
)