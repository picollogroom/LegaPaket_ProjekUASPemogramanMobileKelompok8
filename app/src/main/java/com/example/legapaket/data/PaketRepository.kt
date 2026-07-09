package com.example.legapaket.data

import com.example.legapaket.model.Paket
import com.example.legapaket.model.StatusHistory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

object PaketRepository {
    private val db get() = FirebaseFirestore.getInstance()
    private val collection get() = db.collection("paket")

    /**
     * Generate nomor resi unik format: LP-YYYYMMDD-XXXX
     * Dicek ke Firestore supaya tidak bentrok (dokumen id = noResi).
     */
    private suspend fun generateNoResi(): String {
        val tanggal = SimpleDateFormat("yyyyMMdd", Locale("id", "ID")).format(Date())
        var noResi: String
        var tries = 0
        do {
            val random = Random.nextInt(1000, 9999)
            noResi = "LP-$tanggal-$random"
            val exists = collection.document(noResi).get().await().exists()
            tries++
            if (!exists) break
        } while (tries < 10)
        return noResi
    }

    /** Hitung berat volume standar ekspedisi: (p x l x t) / 6000, dalam cm & kg */
    fun hitungBeratVolume(panjang: Double, lebar: Double, tinggi: Double): Double {
        return (panjang * lebar * tinggi) / 6000.0
    }

    /** Contoh tarif dasar per kg dari berat yang dipakai (max antara berat asli & volume) x jarak flat per kota. */
    fun hitungBiaya(beratDipakai: Double, tarifPerKg: Long = 12000): Long {
        val berat = if (beratDipakai < 1.0) 1.0 else beratDipakai
        return (berat * tarifPerKg).toLong()
    }

    suspend fun buatResi(paket: Paket): String {
        val noResi = generateNoResi()
        val paketFinal = paket.copy(
            noResi = noResi,
            status = "Resi Dibuat",
            riwayat = mutableListOf(
                StatusHistory(status = "Resi Dibuat", keterangan = "Paket terdaftar di sistem")
            )
        )
        collection.document(noResi).set(paketFinal).await()
        return noResi
    }

    suspend fun getByResi(noResi: String): Paket? {
        val doc = collection.document(noResi.trim().uppercase()).get().await()
        return if (doc.exists()) doc.toObject(Paket::class.java) else null
    }

    suspend fun updateStatus(noResi: String, statusBaru: String, keterangan: String = "") {
        val paket = getByResi(noResi) ?: throw Exception("Resi tidak ditemukan")
        paket.riwayat.add(StatusHistory(status = statusBaru, keterangan = keterangan))
        collection.document(noResi).update(
            mapOf(
                "status" to statusBaru,
                "riwayat" to paket.riwayat.map {
                    mapOf("status" to it.status, "waktu" to it.waktu, "keterangan" to it.keterangan)
                }
            )
        ).await()
    }

    suspend fun getAllPaket(): List<Paket> {
        val snapshot = collection.orderBy("createdAt").get().await()
        return snapshot.documents.mapNotNull { it.toObject(Paket::class.java) }
    }

    suspend fun getPaketByStatus(status: String): List<Paket> {
        val snapshot = collection.whereEqualTo("status", status).get().await()
        return snapshot.documents.mapNotNull { it.toObject(Paket::class.java) }
    }

    /** Paket yang belum ditugaskan ke trip manapun, untuk dipilih di Buat Trip. */
    suspend fun getPaketBelumAdaTrip(): List<Paket> {
        val snapshot = collection.whereEqualTo("tripId", null).get().await()
        return snapshot.documents.mapNotNull { it.toObject(Paket::class.java) }
    }

    fun listenAllPaket(onChange: (List<Paket>) -> Unit): ListenerRegistration {
        return collection.orderBy("createdAt").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            onChange(snapshot.documents.mapNotNull { it.toObject(Paket::class.java) })
        }
    }
}
