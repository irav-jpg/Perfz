package com.example.perfz.core.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

fun addTransaction(uid: String, transaction: Transaction, onComplete: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    // Generamos el ID del documento en el cliente para guardarlo dentro del objeto
    val docRef = db.collection("users").document(uid).collection("transactions").document()
    val finalTransaction = transaction.copy(id = docRef.id)

    docRef.set(finalTransaction)
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener { exception ->
            exception.printStackTrace()
            onComplete(false)
        }
}

fun getTransactionsRealtime(uid: String, onUpdate: (List<Transaction>) -> Unit) {
    FirebaseFirestore.getInstance()
        .collection("users").document(uid).collection("transactions")
        .orderBy("date", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                return@addSnapshotListener
            }
            val list = snapshot?.toObjects(Transaction::class.java) ?: emptyList()
            onUpdate(list)
        }
}