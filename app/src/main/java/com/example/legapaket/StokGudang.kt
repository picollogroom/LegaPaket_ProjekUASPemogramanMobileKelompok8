package com.example.legapaket

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.adapter.StokAdapter
import com.example.legapaket.adapter.StokKota
import com.example.legapaket.data.PaketRepository
import kotlinx.coroutines.launch

class StokGudang : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stok_gudang)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<android.widget.ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerStokGudang)
        recycler.layoutManager = LinearLayoutManager(this)
        val adapter = StokAdapter(emptyList())
        recycler.adapter = adapter

        lifecycleScope.launch {
            try {
                // "Stok" = paket yang masih ada di gudang, belum berstatus Terkirim
                val paketDiGudang = PaketRepository.getAllPaket().filter { it.status != "Terkirim" }

                findViewById<TextView>(R.id.tvStokTotal).text = paketDiGudang.size.toString()
                val totalBerat = paketDiGudang.sumOf { maxOf(it.berat, it.beratVolume) }
                findViewById<TextView>(R.id.tvStokBeratTotal).text = "%.1f kg".format(totalBerat)

                val perKota = paketDiGudang.groupingBy { it.kotaTujuan }.eachCount()
                    .map { StokKota(it.key, it.value) }
                    .sortedByDescending { it.jumlah }
                adapter.submitList(perKota)
            } catch (_: Exception) {
            }
        }
    }
}
