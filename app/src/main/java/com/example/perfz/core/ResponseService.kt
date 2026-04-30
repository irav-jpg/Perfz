package com.example.perfz.core

sealed class ResponseService<out T> { //Clase cerrada que solo puede tener dos valores
    data class Success<T>(val data: T): ResponseService<T>() //Clase sellada
    data class Error(val error: String): ResponseService<Nothing>()
    object Loading: ResponseService<Nothing>() //estado  -> objeto (no tiene inicialización)
}