package com.example.legapaket.model

/**
 * Merepresentasikan satu dokumen di collection Firestore "akun".
 * Role yang valid: "admin", "keuangan", "warehouse", "agen"
 */
data class Akun(
    var id: String = "",
    var nama: String = "",
    var username: String = "",
    var passwordHash: String = "",
    var role: String = "",
    var status: String = "aktif", // "aktif" | "nonaktif"
    var createdAt: Long = System.currentTimeMillis()
) {
    // Firestore butuh constructor kosong untuk toObject(), data class sudah menyediakan
    // lewat default value di atas.
}
