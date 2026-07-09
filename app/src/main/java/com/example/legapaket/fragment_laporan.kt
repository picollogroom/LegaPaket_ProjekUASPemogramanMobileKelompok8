package com.example.legapaket

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.adapter.LaporanAdapter
import com.example.legapaket.adapter.RiwayatLaporanAdapter
import com.example.legapaket.data.LaporanRepository
import com.example.legapaket.data.PaketRepository
import com.example.legapaket.model.Laporan
import com.example.legapaket.model.LaporanItem
import com.example.legapaket.model.Paket
import com.example.legapaket.util.SessionManager
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class fragment_laporan : AppCompatActivity() {

    private lateinit var spinnerJenisLaporan: Spinner
    private lateinit var etPeriodeAwal: EditText
    private lateinit var etPeriodeAkhir: EditText
    private lateinit var spinnerTipeData: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var spinnerKota: Spinner
    private lateinit var etCariResi: EditText
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: LaporanAdapter
    private lateinit var recyclerRiwayat: RecyclerView
    private lateinit var riwayatAdapter: RiwayatLaporanAdapter
    private lateinit var tvKosongRiwayat: TextView

    private var periodeAwalMillis: Long = 0
    private var periodeAkhirMillis: Long = System.currentTimeMillis()
    private var semuaPaket: List<Paket> = emptyList()
    private var riwayatListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kelola_laporan_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        spinnerJenisLaporan = findViewById(R.id.spinnerJenisLaporan)
        etPeriodeAwal = findViewById(R.id.etPeriodeAwal)
        etPeriodeAkhir = findViewById(R.id.etPeriodeAkhir)
        spinnerTipeData = findViewById(R.id.spinnerTipeData)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        spinnerKota = findViewById(R.id.spinnerKota)
        etCariResi = findViewById(R.id.etCariResi)
        recycler = findViewById(R.id.recyclerLaporan)
        recyclerRiwayat = findViewById(R.id.recyclerRiwayatLaporan)
        tvKosongRiwayat = findViewById(R.id.tvKosongRiwayatLaporan)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        spinnerJenisLaporan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.jenis_laporan_array))
        spinnerTipeData.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.tipe_data_array))
        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.status_laporan_array))
        spinnerKota.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.kota_filter_array))

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = LaporanAdapter(emptyList())
        recycler.adapter = adapter

        recyclerRiwayat.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        riwayatAdapter = RiwayatLaporanAdapter(emptyList()) { laporan -> bukaLaporanTersimpan(laporan) }
        recyclerRiwayat.adapter = riwayatAdapter

        etPeriodeAwal.setOnClickListener { pilihTanggal { millis -> periodeAwalMillis = millis; etPeriodeAwal.setText(formatTanggal(millis)) } }
        etPeriodeAkhir.setOnClickListener { pilihTanggal { millis -> periodeAkhirMillis = millis; etPeriodeAkhir.setText(formatTanggal(millis)) } }

        findViewById<Button>(R.id.btnGenerateLaporan).setOnClickListener { generateLaporan() }


        etCariResi.setOnClickListener { }
    }

    override fun onStart() {
        super.onStart()
        riwayatListener = LaporanRepository.listenLaporan { list ->
            riwayatAdapter.submitList(list)
            tvKosongRiwayat.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    override fun onStop() {
        super.onStop()
        riwayatListener?.remove()
    }

    private fun pilihTanggal(onSelected: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            cal.set(year, month, day, 0, 0)
            onSelected(cal.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun formatTanggal(millis: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(millis)
    }

    /**
     * Generate laporan baru: ambil data resi sesuai filter, hitung ringkasan,
     * SIMPAN sebagai record "Laporan" tersendiri ke Firestore, terus tampilin hasilnya di layar.
     */
    private fun generateLaporan() {
        lifecycleScope.launch {
            try {
                semuaPaket = PaketRepository.getAllPaket()

                var hasil = semuaPaket
                if (periodeAwalMillis > 0) hasil = hasil.filter { it.createdAt >= periodeAwalMillis }
                if (periodeAkhirMillis > 0) hasil = hasil.filter { it.createdAt <= periodeAkhirMillis }

                val statusFilter = spinnerStatus.selectedItem?.toString() ?: "Semua Status"
                val statusMap = mapOf("Diproses" to "Resi Dibuat", "Dikirim" to "Dalam Perjalanan", "Terkirim" to "Terkirim", "Dibatalkan" to "Dibatalkan")
                if (statusFilter != "Semua Status") {
                    hasil = hasil.filter { it.status == statusMap[statusFilter] }
                }

                val kotaFilter = spinnerKota.selectedItem?.toString() ?: "Semua Kota"
                if (kotaFilter != "Semua Kota") {
                    hasil = hasil.filter { it.kotaTujuan == kotaFilter }
                }

                val tipeData = spinnerTipeData.selectedItem?.toString() ?: "Semua Pengiriman"
                if (tipeData == "Pengiriman Terkirim Saja") {
                    hasil = hasil.filter { it.status == "Terkirim" }
                }

                tampilkanHasil(hasil)

                // ==== SIMPAN sebagai Laporan tersendiri ke Firestore ====
                val jenis = spinnerJenisLaporan.selectedItem?.toString() ?: "Laporan"
                val judul = if (periodeAwalMillis > 0 && periodeAkhirMillis > 0) {
                    "$jenis (${formatTanggal(periodeAwalMillis)} - ${formatTanggal(periodeAkhirMillis)})"
                } else {
                    "$jenis (${formatTanggal(System.currentTimeMillis())})"
                }

                val laporan = Laporan(
                    judul = judul,
                    periodeAwal = periodeAwalMillis,
                    periodeAkhir = periodeAkhirMillis,
                    filterStatus = statusFilter,
                    filterKota = kotaFilter,
                    filterTipeData = tipeData,
                    totalResi = hasil.size,
                    totalTerkirim = hasil.count { it.status == "Terkirim" },
                    totalPendapatan = hasil.sumOf { it.totalBiaya },
                    dibuatOleh = SessionManager.getNama(this@fragment_laporan),
                    items = hasil.map {
                        LaporanItem(
                            noResi = it.noResi,
                            kotaAsal = it.kotaAsal,
                            kotaTujuan = it.kotaTujuan,
                            totalBiaya = it.totalBiaya,
                            status = it.status
                        )
                    }.toMutableList()
                )
                LaporanRepository.simpanLaporan(laporan)
                Toast.makeText(this@fragment_laporan, "Laporan disimpan ke riwayat", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@fragment_laporan, "Gagal generate laporan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Buka kembali laporan yang udah pernah di-generate sebelumnya (dari riwayat), tanpa query ulang ke paket. */
    private fun bukaLaporanTersimpan(laporan: Laporan) {
        adapter.submitList(laporan.items.map {
            Paket(noResi = it.noResi, kotaAsal = it.kotaAsal, kotaTujuan = it.kotaTujuan, totalBiaya = it.totalBiaya, status = it.status)
        })
        findViewById<TextView>(R.id.tvTotalResi).text = laporan.totalResi.toString()
        findViewById<TextView>(R.id.tvTerkirim).text = laporan.totalTerkirim.toString()
        findViewById<TextView>(R.id.tvTotalPendapatan).text = "Rp %,d".format(laporan.totalPendapatan).replace(",", ".")
        Toast.makeText(this, "Menampilkan: ${laporan.judul}", Toast.LENGTH_SHORT).show()
    }

    private fun tampilkanHasil(hasil: List<Paket>) {
        adapter.submitList(hasil)
        findViewById<TextView>(R.id.tvTotalResi).text = hasil.size.toString()
        findViewById<TextView>(R.id.tvTerkirim).text = hasil.count { it.status == "Terkirim" }.toString()
        val totalPendapatan = hasil.sumOf { it.totalBiaya }
        findViewById<TextView>(R.id.tvTotalPendapatan).text = "Rp %,d".format(totalPendapatan).replace(",", ".")
    }
}