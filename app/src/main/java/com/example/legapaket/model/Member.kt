package com.example.legapaket.model

/** Collection Firestore: "member" */
data class Member(
    var id: String = "",
    var nama: String = "",
    var telepon: String = "",
    var alamat: String = "",
    var kota: String = "",
    var status: String = "aktif", // "aktif" | "nonaktif"
    var createdAt: Long = System.currentTimeMillis()
)
