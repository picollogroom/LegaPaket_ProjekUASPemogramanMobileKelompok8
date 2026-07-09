package com.example.legapaket.model

/**
 * Snapshot ringan dari satu paket pada saat laporan di-generate.
 * Disimpan langsung di dalam Laporan supaya histori laporan tetap akurat
 * walaupun data paket aslinya berubah/dihapus di kemudian hari.
 */
data class LaporanItem(
    var noResi: String = "",
    var kotaAsal: String = "",
    var kotaTujuan: String = "",
    var totalBiaya: Long = 0,
    var status: String = ""
)

/** Collection Firestore: "laporan" — satu dokumen = satu kali proses Generate Laporan */
data class Laporan(
    var id: String = "",
    var judul: String = "",
    var tanggalDibuat: Long = System.currentTimeMillis(),
    var periodeAwal: Long = 0,
    var periodeAkhir: Long = 0,
    var filterStatus: String = "",
    var filterKota: String = "",
    var filterTipeData: String = "",
    var totalResi: Int = 0,
    var totalTerkirim: Int = 0,
    var totalPendapatan: Long = 0,
    var dibuatOleh: String = "",
    var items: MutableList<LaporanItem> = mutableListOf()
)