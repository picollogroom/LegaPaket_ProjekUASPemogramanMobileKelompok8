package com.example.legapaket

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.adapter.ResiTerbaruAdapter
import com.example.legapaket.data.PaketRepository
import com.example.legapaket.util.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainMenuAgen : AppCompatActivity() {

    private lateinit var recyclerResiTerbaru: RecyclerView
    private lateinit var tvKosongResiTerbaru: TextView
    private lateinit var resiTerbaruAdapter: ResiTerbaruAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_beranda_agen)

        findViewById<LinearLayout>(R.id.layoutSapa)?.let {
            (it.getChildAt(1) as? TextView)?.text = SessionManager.getNama(this).ifBlank { "Agen" }
        }

        findViewById<LinearLayout>(R.id.menuBuatResi).setOnClickListener {
            startActivity(Intent(this, activity_buat_resi::class.java))
        }
        findViewById<LinearLayout>(R.id.menuLaporan).setOnClickListener {
            startActivity(Intent(this, KelolaLaporan_admin::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener { konfirmasiLogout() }

        recyclerResiTerbaru = findViewById(R.id.recyclerResiTerbaru)
        tvKosongResiTerbaru = findViewById(R.id.tvKosongResiTerbaru)
        recyclerResiTerbaru.layoutManager = LinearLayoutManager(this)
        resiTerbaruAdapter = ResiTerbaruAdapter(emptyList()) { paket ->
            val intent = Intent(this, ActivityTrackingPaket::class.java)
            intent.putExtra("no_resi", paket.noResi)
            startActivity(intent)
        }
        recyclerResiTerbaru.adapter = resiTerbaruAdapter

        muatStats()
        muatResiTerbaru()
    }

    override fun onResume() {
        super.onResume()
        // refresh tiap kali balik ke halaman ini (misal abis Buat Resi), biar resi baru langsung nongol
        muatStats()
        muatResiTerbaru()
    }

    private fun muatResiTerbaru() {
        lifecycleScope.launch {
            try {
                val namaAgen = SessionManager.getNama(this@MainMenuAgen)
                val terbaru = PaketRepository.getAllPaket()
                    .filter { it.dibuatOleh == namaAgen }
                    .sortedByDescending { it.createdAt }
                    .take(5)

                resiTerbaruAdapter.submitList(terbaru)
                tvKosongResiTerbaru.visibility = if (terbaru.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                recyclerResiTerbaru.visibility = if (terbaru.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
            } catch (_: Exception) {
                // gagal load resi terbaru, biarkan kosong daripada crash
            }
        }
    }

    private fun muatStats() {
        lifecycleScope.launch {
            try {
                val semua = PaketRepository.getAllPaket()
                val hariIni = SimpleDateFormat("yyyyMMdd", Locale("id", "ID")).format(Date())
                val resiHariIni = semua.count { SimpleDateFormat("yyyyMMdd", Locale("id", "ID")).format(Date(it.createdAt)) == hariIni }
                val dalamProses = semua.count { it.status != "Terkirim" }
                val terkirim = semua.count { it.status == "Terkirim" }

                findViewById<TextView>(R.id.tvResiHariIni).text = resiHariIni.toString()
                findViewById<TextView>(R.id.tvDalamProses).text = dalamProses.toString()
                findViewById<TextView>(R.id.tvTerkirim).text = terkirim.toString()
            } catch (_: Exception) {
                // biarkan nilai dummy dari layout kalau gagal memuat, tidak perlu blocking UI
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