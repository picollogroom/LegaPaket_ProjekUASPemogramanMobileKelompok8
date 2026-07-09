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
import com.example.legapaket.util.SessionManager
import kotlinx.coroutines.launch
import android.widget.Button
import androidx.appcompat.app.AlertDialog

class BerandaKeuangan : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_beranda_keuangan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.tvNamaKeuangan).text = SessionManager.getNama(this).ifBlank { "Keuangan" }

        findViewById<LinearLayout>(R.id.menuBuatLaporanKeuangan).setOnClickListener {
            startActivity(Intent(this, KelolaLaporan_admin::class.java))
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener { konfirmasiLogout()
        }
        muatStats()
    }

    override fun onResume() {
        super.onResume()
        muatStats()
    }

    /**
     * Pendapatan dihitung dari total biaya paket yang statusnya "Terkirim" (uang sudah settle).
     * "Pengeluaran" belum ada modul tersendiri di UI saat ini, jadi disimulasikan dari
     * paket yang metode pembayarannya COD (dianggap biaya operasional talangan kurir) -
     * silakan sesuaikan rumusnya kalau nanti ada modul pengeluaran/beban operasional terpisah.
     */

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
                val pendapatan = semuaPaket.filter { it.status == "Terkirim" }.sumOf { it.totalBiaya }
                val pengeluaran = semuaPaket.filter { it.metodePembayaran == "COD" }.sumOf { it.totalBiaya }
                val saldoBersih = pendapatan - pengeluaran

                findViewById<TextView>(R.id.tvSaldoBersih).text = "Rp %,d".format(saldoBersih).replace(",", ".")
                findViewById<TextView>(R.id.tvTotalPendapatanKeuangan).text = "Rp %,d".format(pendapatan).replace(",", ".")
                findViewById<TextView>(R.id.tvTotalPengeluaranKeuangan).text = "Rp %,d".format(pengeluaran).replace(",", ".")
            } catch (_: Exception) {
            }
        }
    }
}
