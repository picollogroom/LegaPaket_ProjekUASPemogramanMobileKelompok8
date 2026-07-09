package com.example.legapaket

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.adapter.RiwayatStatusAdapter
import com.example.legapaket.data.PaketRepository
import com.example.legapaket.model.Paket
import kotlinx.coroutines.launch

class UpdateStatusPaket : AppCompatActivity() {

    // Urutan tahap tetap, harus sama persis dengan yang dipakai di ActivityTrackingPaket & PaketRepository
    private val urutanStatus = listOf(
        "Resi Dibuat", "Dalam Perjalanan", "Tiba di Gudang Tujuan", "Diantar", "Terkirim"
    )

    private lateinit var etNomorResi: EditText
    private lateinit var layoutInfoPaket: View
    private lateinit var layoutRiwayat: View
    private lateinit var radioGroupStatus: RadioGroup
    private lateinit var etKeterangan: EditText
    private lateinit var btnUpdate: Button
    private lateinit var riwayatAdapter: RiwayatStatusAdapter

    private var paketAktif: Paket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_status_paket)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etNomorResi = findViewById(R.id.etNomorResiUpdate)
        layoutInfoPaket = findViewById(R.id.layoutInfoPaketUpdate)
        layoutRiwayat = findViewById(R.id.layoutRiwayatUpdate)
        radioGroupStatus = findViewById(R.id.radioGroupStatusBaru)
        etKeterangan = findViewById(R.id.etKeteranganUpdate)
        btnUpdate = findViewById(R.id.btnUpdateStatus)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val recyclerRiwayat = findViewById<RecyclerView>(R.id.recyclerRiwayatUpdate)
        recyclerRiwayat.layoutManager = LinearLayoutManager(this)
        riwayatAdapter = RiwayatStatusAdapter(emptyList())
        recyclerRiwayat.adapter = riwayatAdapter

        layoutInfoPaket.visibility = View.GONE
        layoutRiwayat.visibility = View.GONE

        findViewById<Button>(R.id.btnCariResiUpdate).setOnClickListener { cariResi() }
        val noResiDariIntent = intent.getStringExtra("no_resi")
        if (!noResiDariIntent.isNullOrBlank()) {
            etNomorResi.setText(noResiDariIntent)
            cariResi()
        }
        btnUpdate.setOnClickListener { updateStatus() }
    }

    private fun cariResi() {
        val noResi = etNomorResi.text.toString().trim().uppercase()
        if (noResi.isEmpty()) {
            Toast.makeText(this, "Masukkan nomor resi", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val paket = PaketRepository.getByResi(noResi)
                if (paket == null) {
                    Toast.makeText(this@UpdateStatusPaket, "Resi tidak ditemukan", Toast.LENGTH_SHORT).show()
                    layoutInfoPaket.visibility = View.GONE
                    layoutRiwayat.visibility = View.GONE
                    return@launch
                }
                tampilkanPaket(paket)
            } catch (e: Exception) {
                Toast.makeText(this@UpdateStatusPaket, "Gagal mencari resi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun tampilkanPaket(paket: Paket) {
        paketAktif = paket
        layoutInfoPaket.visibility = View.VISIBLE
        layoutRiwayat.visibility = View.VISIBLE

        findViewById<TextView>(R.id.tvNoResiUpdate).text = paket.noResi
        findViewById<TextView>(R.id.tvStatusSaatIni).text = paket.status
        findViewById<TextView>(R.id.tvRuteUpdate).text = "${paket.kotaAsal} -> ${paket.kotaTujuan}"
        findViewById<TextView>(R.id.tvPenerimaUpdate).text = "Penerima: ${paket.namaPenerima} (${paket.telpPenerima})"

        riwayatAdapter.submitList(paket.riwayat.sortedByDescending { it.waktu })

        // Susun pilihan status: cuma tahap-tahap SETELAH status sekarang yang boleh dipilih,
        // supaya status paket gak bisa "mundur" secara gak sengaja.
        radioGroupStatus.removeAllViews()
        val indexSaatIni = urutanStatus.indexOf(paket.status)

        if (indexSaatIni == -1 || indexSaatIni == urutanStatus.lastIndex) {
            // status gak dikenali / udah di tahap terakhir (Terkirim) -> gak ada lagi yang bisa di-update
            btnUpdate.isEnabled = false
            btnUpdate.text = "Paket Sudah Terkirim"
            return
        }

        btnUpdate.isEnabled = true
        btnUpdate.text = "Update Status"

        for (i in (indexSaatIni + 1) until urutanStatus.size) {
            val radio = RadioButton(this).apply {
                id = View.generateViewId()
                text = urutanStatus[i]
                textSize = 14f
                setPadding(4, 12, 4, 12)
            }
            radioGroupStatus.addView(radio)
            if (i == indexSaatIni + 1) radio.isChecked = true // default: tahap berikutnya langsung
        }
    }

    private fun updateStatus() {
        val paket = paketAktif ?: return
        val radioTerpilih = findViewById<RadioButton>(radioGroupStatus.checkedRadioButtonId)
        if (radioTerpilih == null) {
            Toast.makeText(this, "Pilih status selanjutnya dulu", Toast.LENGTH_SHORT).show()
            return
        }
        val statusBaru = radioTerpilih.text.toString()
        val keterangan = etKeterangan.text.toString().trim()

        btnUpdate.isEnabled = false
        lifecycleScope.launch {
            try {
                PaketRepository.updateStatus(paket.noResi, statusBaru, keterangan)
                Toast.makeText(this@UpdateStatusPaket, "Status berhasil diupdate ke \"$statusBaru\"", Toast.LENGTH_SHORT).show()
                etKeterangan.setText("")
                // refresh tampilan biar keliatan histori & pilihan tahap berikutnya yang baru
                val paketTerbaru = PaketRepository.getByResi(paket.noResi)
                if (paketTerbaru != null) tampilkanPaket(paketTerbaru)
            } catch (e: Exception) {
                Toast.makeText(this@UpdateStatusPaket, e.message ?: "Gagal update status", Toast.LENGTH_SHORT).show()
            } finally {
                btnUpdate.isEnabled = true
            }
        }
    }
}