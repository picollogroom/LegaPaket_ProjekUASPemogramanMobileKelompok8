package com.example.legapaket

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.legapaket.data.MemberRepository
import com.example.legapaket.data.PaketRepository
import com.example.legapaket.util.SessionManager
import kotlinx.coroutines.launch
import android.widget.Button
import androidx.appcompat.app.AlertDialog

class BerandaAdmin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_beranda_admin)

        findViewById<TextView>(R.id.tvNamaAdmin).text = SessionManager.getNama(this).ifBlank { "Admin" }

        findViewById<LinearLayout>(R.id.menuKelolaMember).setOnClickListener {
            startActivity(Intent(this, KelolaMember::class.java))
        }
        findViewById<LinearLayout>(R.id.menuLaporan).setOnClickListener {
            startActivity(Intent(this, KelolaLaporan_admin::class.java))
        }
        findViewById<LinearLayout>(R.id.menuUpdateStatus).setOnClickListener {
            startActivity(Intent(this, UpdateStatusPaket::class.java))
        }
        findViewById<LinearLayout>(R.id.menuKelolaAkun).setOnClickListener {
            startActivity(Intent(this, KelolaAkun::class.java))
        }
        findViewById<LinearLayout>(R.id.menuBuatResi).setOnClickListener {
            startActivity(Intent(this, BuatResi_admin::class.java))
        }
        findViewById<Button>(R.id.btnLogout).setOnClickListener { konfirmasiLogout()
        }
        muatStats()
    }

    override fun onResume() {
        super.onResume()
        muatStats()
    }

    private fun muatStats() {
        lifecycleScope.launch {
            try {
                val semuaPaket = PaketRepository.getAllPaket()
                val semuaMember = MemberRepository.getAllMember()

                findViewById<TextView>(R.id.tvStatTotalResi).text = semuaPaket.size.toString()
                findViewById<TextView>(R.id.tvStatTerkirim).text = semuaPaket.count { it.status == "Terkirim" }.toString()
                findViewById<TextView>(R.id.tvStatPending).text = semuaPaket.count { it.status != "Terkirim" }.toString()
                findViewById<TextView>(R.id.tvStatMember).text = semuaMember.size.toString()
                val totalPendapatan = semuaPaket.sumOf { it.totalBiaya }
                findViewById<TextView>(R.id.tvStatPendapatan).text = "Rp %,d".format(totalPendapatan).replace(",", ".")
            } catch (_: Exception) {
                // gagal load stats, biarkan nilai default dari layout
            }
        }
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
}
