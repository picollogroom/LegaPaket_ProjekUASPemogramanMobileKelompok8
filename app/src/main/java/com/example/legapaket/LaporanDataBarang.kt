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
import com.example.legapaket.data.PaketRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LaporanDataBarang : AppCompatActivity() {

    private var periodeAwalMillis: Long = 0
    private var periodeAkhirMillis: Long = System.currentTimeMillis()
    private lateinit var etPeriodeAwal: EditText
    private lateinit var etPeriodeAkhir: EditText
    private lateinit var spinnerKategori: Spinner
    private lateinit var adapter: LaporanAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_laporan_data_barang)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        etPeriodeAwal = findViewById(R.id.etPeriodeAwalBarang)
        etPeriodeAkhir = findViewById(R.id.etPeriodeAkhirBarang)
        spinnerKategori = findViewById(R.id.spinnerKategoriBarang)

        findViewById<Spinner>(R.id.spinnerJenisLaporanBarang).adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.jenis_laporan_barang_array))
        spinnerKategori.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(R.array.kategori_barang_array))

        val recycler = findViewById<RecyclerView>(R.id.recyclerLaporanBarang)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = LaporanAdapter(emptyList())
        recycler.adapter = adapter

        etPeriodeAwal.setOnClickListener { pilihTanggal { millis -> periodeAwalMillis = millis; etPeriodeAwal.setText(formatTanggal(millis)) } }
        etPeriodeAkhir.setOnClickListener { pilihTanggal { millis -> periodeAkhirMillis = millis; etPeriodeAkhir.setText(formatTanggal(millis)) } }

        findViewById<Button>(R.id.btnGenerateLaporanBarang).setOnClickListener { generateLaporan() }
        findViewById<Button>(R.id.btnExportPdfBarang).setOnClickListener {
            Toast.makeText(this, "Export PDF: sambungkan ke skill/library PDF sesuai kebutuhan tugas kamu", Toast.LENGTH_LONG).show()
        }
        findViewById<Button>(R.id.btnExportExcelBarang).setOnClickListener {
            Toast.makeText(this, "Export Excel: sambungkan ke skill/library Excel sesuai kebutuhan tugas kamu", Toast.LENGTH_LONG).show()
        }

        generateLaporan()
    }

    private fun pilihTanggal(onSelected: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            cal.set(year, month, day, 0, 0)
            onSelected(cal.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun formatTanggal(millis: Long): String = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(millis)

    private fun generateLaporan() {
        lifecycleScope.launch {
            try {
                var hasil = PaketRepository.getAllPaket()
                if (periodeAwalMillis > 0) hasil = hasil.filter { it.createdAt >= periodeAwalMillis }
                if (periodeAkhirMillis > 0) hasil = hasil.filter { it.createdAt <= periodeAkhirMillis }

                val kategori = spinnerKategori.selectedItem?.toString() ?: "Semua Kategori"
                if (kategori != "Semua Kategori") {
                    hasil = hasil.filter { it.jenisBarang == kategori }
                }

                adapter.submitList(hasil)

                findViewById<TextView>(R.id.tvTotalBarangMasuk).text = hasil.count { it.status == "Resi Dibuat" }.toString()
                findViewById<TextView>(R.id.tvTotalBarangKeluar).text = hasil.count { it.status == "Dalam Perjalanan" || it.status == "Terkirim" }.toString()
                findViewById<TextView>(R.id.tvTotalSisaStok).text = hasil.count { it.status != "Terkirim" }.toString()
            } catch (e: Exception) {
                Toast.makeText(this@LaporanDataBarang, "Gagal memuat laporan barang", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
