package com.example.perfz.core

import com.example.perfz.onboarding.personal.model.UserProfile // Import del modelo de datos
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(): Authentication {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()


    override suspend fun requestLogin(
        email: String, password: String
    ): ResponseService<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { ResponseService.Success(it) }
                ?: ResponseService.Error("Usuario no encontrado")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            ResponseService.Error("Correo o contraseña incorrectos")
        } catch (e: FirebaseAuthException) {
            ResponseService.Error(e.localizedMessage ?: "Error de autenticación")
        } catch (e: Exception) {
            ResponseService.Error("Error inesperado. Intenta de nuevo")
        }
    }


    override suspend fun requestSignUp(
        email: String,
        password: String
    ): ResponseService<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { ResponseService.Success(it) }
                ?: ResponseService.Error("No se pudo crear el usuario")
        } catch (e: FirebaseAuthUserCollisionException) {
            ResponseService.Error("Este correo ya está registrado, intenta con otro")
        } catch (e: FirebaseAuthWeakPasswordException) {
            ResponseService.Error("La contraseña es muy débil")
        } catch (e: Exception) {
            ResponseService.Error("Error inesperado: ${e.localizedMessage}")
        }
    }


    suspend fun saveUserInfo(user: UserProfile): ResponseService<Unit> = withContext(Dispatchers.IO) {
        try {

            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()

            ResponseService.Success(Unit)
        } catch (e: Exception) {
            ResponseService.Error("Error al guardar el perfil: ${e.localizedMessage}")
        }
    }
}