package com.example.legapaket.data

import com.example.legapaket.model.Trip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

object TripRepository {
    private val db get() = FirebaseFirestore.getInstance()
    private val collection get() = db.collection("trip")

    suspend fun buatTrip(trip: Trip): String {
        val ref = collection.add(trip).await()
        // tandai semua paket yang dipilih supaya tidak double-assign ke trip lain
        val batch = db.batch()
        trip.daftarResi.forEach { noResi ->
            val paketRef = db.collection("paket").document(noResi)
            batch.update(paketRef, mapOf("tripId" to ref.id, "status" to "Dalam Perjalanan"))
        }
        batch.commit().await()
        return ref.id
    }

    suspend fun updateStatusTrip(id: String, status: String) {
        collection.document(id).update("status", status).await()
    }

    suspend fun getAllTrip(): List<Trip> {
        val snapshot = collection.orderBy("createdAt").get().await()
        return snapshot.documents.mapNotNull { it.toObject(Trip::class.java)?.copy(id = it.id) }
    }

    fun listenTrip(onChange: (List<Trip>) -> Unit): ListenerRegistration {
        return collection.orderBy("createdAt").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            onChange(snapshot.documents.mapNotNull { it.toObject(Trip::class.java)?.copy(id = it.id) })
        }
    }
}
