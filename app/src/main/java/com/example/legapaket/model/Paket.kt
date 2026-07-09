package com.example.legapaket.model

/**
 * Satu titik riwayat status paket, dipakai untuk timeline tracking.
 */
data class StatusHistory(
    var status: String = "",   // "Resi Dibuat", "Dalam Perjalanan", "Tiba di Gudang Tujuan", "Diantar", "Terkirim"
    var waktu: Long = System.currentTimeMillis(),
    var keterangan: String = ""
)

/** Collection Firestore: "paket" (id dokumen = nomor resi) */
data class Paket(
    var noResi: String = "",
    var memberId: String = "",
    var namaPengirim: String = "",
    var alamatPengirim: String = "",
    var telpPengirim: String = "",
    var namaPenerima: String = "",
    var alamatPenerima: String = "",
    var telpPenerima: String = "",
    var kotaAsal: String = "",
    var kotaTujuan: String = "",
    var jenisBarang: String = "",
    var panjang: Double = 0.0,
    var lebar: Double = 0.0,
    var tinggi: Double = 0.0,
    var berat: Double = 0.0,
    var beratVolume: Double = 0.0,
    var metodePembayaran: String = "", // "COD" | "Transfer" | "E-Wallet"
    var totalBiaya: Long = 0,
    var status: String = "Resi Dibuat",
    var tripId: String? = null,
    var dibuatOleh: String = "",
    var createdAt: Long = System.currentTimeMillis(),
    var riwayat: MutableList<StatusHistory> = mutableListOf()
)
