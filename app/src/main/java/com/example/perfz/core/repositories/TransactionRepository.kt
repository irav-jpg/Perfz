package com.example.perfz.core.repositories

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

fun addTransaction(uid: String, transaction: Transaction, onComplete: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()


    db.collection("users").document(uid)
        .collection("transactions")
        .add(transaction)
        .addOnSuccessListener { onComplete(true) }
        .addOnFailureListener { onComplete(false) }
}


fun getTransactionsRealtime(uid: String, onUpdate: (List<Transaction>) -> Unit) {
    FirebaseFirestore.getInstance()
        .collection("users").document(uid).collection("transactions")
        .orderBy("date", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, _ ->
            val list = snapshot?.toObjects(Transaction::class.java) ?: emptyList()
            onUpdate(list)
        }
}