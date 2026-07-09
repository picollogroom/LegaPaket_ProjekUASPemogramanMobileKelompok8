package com.example.legapaket

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.legapaket.data.PaketRepository
import com.example.legapaket.model.Paket
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ActivityTrackingPaket : AppCompatActivity() {

    // Urutan tahap tetap, harus sama persis dengan status yang dipakai di PaketRepository
    private val urutanStatus = listOf(
        "Resi Dibuat", "Dalam Perjalanan", "Tiba di Gudang Tujuan", "Diantar", "Terkirim"
    )

    private lateinit var etNomorResi: EditText
    private lateinit var layoutInfoPaket: View
    private lateinit var layoutTimeline: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tracking_paket)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etNomorResi = findViewById(R.id.etNomorResi)
        layoutInfoPaket = findViewById(R.id.layoutInfoPaket)
        layoutTimeline = findViewById(R.id.layoutTimeline)

        layoutInfoPaket.visibility = View.GONE
        layoutTimeline.visibility = View.GONE

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnCariResi).setOnClickListener { cariResi() }
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
                    Toast.makeText(this@ActivityTrackingPaket, "Resi tidak ditemukan", Toast.LENGTH_SHORT).show()
                    layoutInfoPaket.visibility = View.GONE
                    layoutTimeline.visibility = View.GONE
                    return@launch
                }
                tampilkanPaket(paket)
            } catch (e: Exception) {
                Toast.makeText(this@ActivityTrackingPaket, "Gagal mencari resi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun tampilkanPaket(paket: Paket) {
        layoutInfoPaket.visibility = View.VISIBLE
        layoutTimeline.visibility = View.VISIBLE

        findViewById<TextView>(R.id.tvNoResi).text = paket.noResi
        findViewById<TextView>(R.id.tvRute).text = "${paket.kotaAsal} -> ${paket.kotaTujuan}"
        findViewById<TextView>(R.id.tvStatusBadge).text = paket.status
        findViewById<TextView>(R.id.tvNamaPengirim).text = paket.namaPengirim
        findViewById<TextView>(R.id.tvNamaPenerima).text = paket.namaPenerima
        findViewById<TextView>(R.id.tvBerat).text = "%.1f kg".format(maxOf(paket.berat, paket.beratVolume))
        findViewById<TextView>(R.id.tvBiaya).text = "Rp %,d".format(paket.totalBiaya).replace(",", ".")
        findViewById<TextView>(R.id.tvJenisBarang).text = paket.jenisBarang
        findViewById<TextView>(R.id.tvPembayaran).text = paket.metodePembayaran

        val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

        for (i in urutanStatus.indices) {
            val stepIndex = i + 1
            val iconId = resources.getIdentifier("iconStep$stepIndex", "id", packageName)
            val lineId = resources.getIdentifier("lineStep$stepIndex", "id", packageName)
            val waktuId = resources.getIdentifier("tvWaktuStep$stepIndex", "id", packageName)

            val icon = findViewById<ImageView>(iconId)
            val line = if (lineId != 0) findViewById<View>(lineId) else null
            val tvWaktu = findViewById<TextView>(waktuId)

            val riwayatCocok = paket.riwayat.find { it.status == urutanStatus[i] }
            val sudahTerjadi = riwayatCocok != null

            icon.setBackgroundColor(if (sudahTerjadi) Color.parseColor("#E4007F") else Color.parseColor("#CCCCCC"))
            line?.setBackgroundColor(if (sudahTerjadi) Color.parseColor("#E4007F") else Color.parseColor("#CCCCCC"))

            tvWaktu.text = if (riwayatCocok != null) {
                "${formatter.format(riwayatCocok.waktu)}" + if (riwayatCocok.keterangan.isNotBlank()) " — ${riwayatCocok.keterangan}" else ""
            } else {
                "Menunggu"
            }
        }
    }
}
