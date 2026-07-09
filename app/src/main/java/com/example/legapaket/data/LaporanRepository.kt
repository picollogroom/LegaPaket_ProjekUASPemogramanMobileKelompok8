package com.example.legapaket.data

import com.example.legapaket.model.Laporan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object LaporanRepository {
    private val db get() = FirebaseFirestore.getInstance()
    private val collection get() = db.collection("laporan")

    suspend fun simpanLaporan(laporan: Laporan): String {
        val ref = collection.add(laporan).await()
        return ref.id
    }

    suspend fun getAllLaporan(): List<Laporan> {
        val snapshot = collection.orderBy("tanggalDibuat", Query.Direction.DESCENDING).get().await()
        return snapshot.documents.mapNotNull { it.toObject(Laporan::class.java)?.copy(id = it.id) }
    }

    suspend fun hapusLaporan(id: String) {
        collection.document(id).delete().await()
    }

    fun listenLaporan(onChange: (List<Laporan>) -> Unit): ListenerRegistration {
        return collection.orderBy("tanggalDibuat", Query.Direction.DESCENDING).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            onChange(snapshot.documents.mapNotNull { it.toObject(Laporan::class.java)?.copy(id = it.id) })
        }
    }
}