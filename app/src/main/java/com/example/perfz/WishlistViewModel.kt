package com.example.perfz.wishlist

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.perfz.core.repositories.Transaction
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class WishlistViewModel : ViewModel() {

    private val _listaDeseos = MutableStateFlow<List<WishItem>>(emptyList())
    val listaDeseos: StateFlow<List<WishItem>> = _listaDeseos

    private val _transaccionesWishlist = MutableStateFlow<List<Transaction>>(emptyList())
    val transaccionesWishlist: StateFlow<List<Transaction>> = _transaccionesWishlist


    private val firebaseInstance = FirebaseDatabase.getInstance("https://perfz-395ae-default-rtdb.firebaseio.com/")
    private val dbRef = firebaseInstance.getReference("wishlist")
    private var usuarioUid: String = "perfz_dev_user"

    fun cargarWishlist(uid: String) {
        this.usuarioUid = uid
        dbRef.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<WishItem>()
                if (snapshot.exists()) {
                    for (postSnapshot in snapshot.children) {
                        val id = postSnapshot.child("id").getValue(String::class.java) ?: ""
                        val name = postSnapshot.child("name").getValue(String::class.java) ?: ""


                        val priceRaw = postSnapshot.child("price").value
                        val price = when (priceRaw) {
                            is Number -> priceRaw.toDouble()
                            is String -> priceRaw.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }

                        val savedAmountRaw = postSnapshot.child("savedAmount").value
                        val savedAmount = when (savedAmountRaw) {
                            is Number -> savedAmountRaw.toDouble()
                            is String -> savedAmountRaw.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }

                        val isPurchased = postSnapshot.child("isPurchased").getValue(Boolean::class.java) ?: false

                        items.add(WishItem(id, name, price, savedAmount, isPurchased))
                    }
                }
                _listaDeseos.value = items
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase_Perfz", "Error al leer datos: ${error.message}")
            }
        })
    }

    fun agregarDeseo(name: String, price: Double) {
        val id = UUID.randomUUID().toString()
        val nuevoItem = WishItem(id = id, name = name, price = price, savedAmount = 0.0, isPurchased = false)

        val listaActual = _listaDeseos.value.toMutableList()
        listaActual.add(nuevoItem)
        _listaDeseos.value = listaActual

        dbRef.child(usuarioUid).child(id).setValue(nuevoItem)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    Log.d("Firebase_Perfz", "¡Deseo guardado con éxito en la nube!")
                } else {
                    Log.e("Firebase_Perfz", "Fallo al guardar en Firebase: ${tarea.exception?.message}")
                }
            }
    }

    fun abonarAhorro(item: WishItem, monto: Double) {
        val nuevoAhorro = item.savedAmount + monto


        dbRef.child(usuarioUid).child(item.id).child("savedAmount").setValue(nuevoAhorro)


        val listaActual = _listaDeseos.value.map {
            if (it.id == item.id) it.copy(savedAmount = nuevoAhorro) else it
        }
        _listaDeseos.value = listaActual
    }

    fun actualizarEstadoCompra(item: WishItem, comprado: Boolean) {
        val listaActual = _listaDeseos.value.map {
            if (it.id == item.id) it.copy(isPurchased = comprado) else it
        }
        _listaDeseos.value = listaActual

        dbRef.child(usuarioUid).child(item.id).child("isPurchased").setValue(comprado)
    }

    fun eliminarDeseoPorId(id: String) {
        val listaActual = _listaDeseos.value.filter { it.id != id }
        _listaDeseos.value = listaActual

        dbRef.child(usuarioUid).child(id).removeValue()
    }

    fun restaurarDeseoDirecto(item: WishItem) {
        val listaActual = _listaDeseos.value.toMutableList()
        listaActual.add(item)
        _listaDeseos.value = listaActual

        dbRef.child(usuarioUid).child(item.id).setValue(item)
    }
}