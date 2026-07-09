package com.example.legapaket.model

/** Collection Firestore: "trip" */
data class Trip(
    var id: String = "",
    var kotaAsal: String = "",
    var kotaTujuan: String = "",
    var waktuBerangkat: Long = 0,
    var estimasiTiba: Long = 0,
    var armada: String = "",
    var sopir: String = "",
    var catatan: String = "",
    var daftarResi: MutableList<String> = mutableListOf(),
    var totalBerat: Double = 0.0,
    var status: String = "Dijadwalkan", // "Dijadwalkan" | "Berlangsung" | "Selesai" | "Dibatalkan"
    var dibuatOleh: String = "",
    var createdAt: Long = System.currentTimeMillis()
)
