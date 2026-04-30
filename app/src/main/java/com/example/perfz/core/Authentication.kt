package com.example.perfz.core

import com.google.firebase.auth.FirebaseUser

interface Authentication {
    suspend fun requestLogin(email: String, password: String): ResponseService<FirebaseUser> //para que se puedan manejar los 3 estados
    suspend fun requestSignUp(email: String, password: String): ResponseService<FirebaseUser>
}