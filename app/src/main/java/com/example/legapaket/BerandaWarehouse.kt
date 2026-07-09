package com.example.legapaket

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.legapaket.data.PaketRepository
import com.example.legapaket.data.TripRepository
import com.example.legapaket.util.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Button
import androidx.appcompat.app.AlertDialog

class BerandaWarehouse : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_beranda_warehouse)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.tvNamaWarehouse).text = SessionManager.getNama(this).ifBlank { "Warehouse" }

        findViewById<LinearLayout>(R.id.menuBuatTrip).setOnClickListener {
            startActivity(Intent(this, BuatTrip::class.java))
        }
        findViewById<LinearLayout>(R.id.menuLaporanBarang).setOnClickListener {
            startActivity(Intent(this, LaporanDataBarang::class.java))
        }
        findViewById<LinearLayout>(R.id.menuRiwayatTrip).setOnClickListener {
            startActivity(Intent(this, RiwayatTrip::class.java))
        }
        findViewById<TextView>(R.id.tvLihatSemuaTrip).setOnClickListener {
            startActivity(Intent(this, RiwayatTrip::class.java))
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener { konfirmasiLogout()
        }
        muatStats()
    }

    override fun onResume() {
        super.onResume()
        muatStats()
    }

    private fun konfirmasiLogout() {
        AlertDialog.Builder(this)
            .setTitle("Keluar Akun")
            .setMessage("Yakin mau logout?")
            .setPositiveButton("Logout") { _, _ ->
                SessionManager.logout(this)
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    private fun muatStats() {
        lifecycleScope.launch {
            try {
                val semuaPaket = PaketRepository.getAllPaket()
                val semuaTrip = TripRepository.getAllTrip()
                val hariIni = SimpleDateFormat("yyyyMMdd", Locale("id", "ID")).format(Date())

                val tripHariIni = semuaTrip.count {
                    SimpleDateFormat("yyyyMMdd", Locale("id", "ID")).format(Date(it.createdAt)) == hariIni
                }
                val barangMasuk = semuaPaket.count { it.status == "Resi Dibuat" }
                val barangKeluar = semuaPaket.count { it.status == "Dalam Perjalanan" || it.status == "Terkirim" }

                findViewById<TextView>(R.id.tvStatTripHariIni).text = tripHariIni.toString()
                findViewById<TextView>(R.id.tvStatBarangMasuk).text = barangMasuk.toString()
                findViewById<TextView>(R.id.tvStatBarangKeluar).text = barangKeluar.toString()
                findViewById<TextView>(R.id.tvStatTotalStok).text = semuaPaket.count { it.status != "Terkirim" }.toString()
            } catch (_: Exception) {
            }
        }
    }
}
