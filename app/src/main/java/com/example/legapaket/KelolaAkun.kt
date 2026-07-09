package com.example.legapaket

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.adapter.AkunAdapter
import com.example.legapaket.data.AkunRepository
import com.example.legapaket.model.Akun
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class KelolaAkun : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var etCari: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var tvTotal: TextView
    private lateinit var tvAktif: TextView
    private lateinit var tvNonaktif: TextView
    private lateinit var adapter: AkunAdapter

    private var semuaAkun: List<Akun> = emptyList()
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kelola_akun)

        recycler = findViewById(R.id.recyclerAkun)
        etCari = findViewById(R.id.etCariAkun)
        spinnerRole = findViewById(R.id.spinnerFilterRole)
        tvTotal = findViewById(R.id.tvTotalAkun)
        tvAktif = findViewById(R.id.tvAkunAktif)
        tvNonaktif = findViewById(R.id.tvAkunNonaktif)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<FloatingActionButton>(R.id.fabTambahAkun).setOnClickListener {
            startActivity(android.content.Intent(this, TambahAkun::class.java))
        }

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = AkunAdapter(
            emptyList(),
            onToggleStatus = { akun -> toggleStatus(akun) },
            onEdit = { akun -> editAkun(akun) },
            onDelete = { akun -> konfirmasiHapus(akun) }
        )
        recycler.adapter = adapter

        val roleOptions = listOf("Semua Role", "admin", "keuangan", "warehouse", "agen")
        spinnerRole.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roleOptions)
        spinnerRole.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                terapkanFilter()
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }

        etCari.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { terapkanFilter() }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onStart() {
        super.onStart()
        listener = AkunRepository.listenAkun { list ->
            semuaAkun = list
            terapkanFilter()
        }
    }

    override fun onStop() {
        super.onStop()
        listener?.remove()
    }

    private fun terapkanFilter() {
        val keyword = etCari.text.toString().trim().lowercase()
        val roleFilter = spinnerRole.selectedItem?.toString() ?: "Semua Role"

        var hasil = semuaAkun
        if (roleFilter != "Semua Role") {
            hasil = hasil.filter { it.role == roleFilter }
        }
        if (keyword.isNotEmpty()) {
            hasil = hasil.filter { it.nama.lowercase().contains(keyword) || it.username.lowercase().contains(keyword) }
        }
        adapter.submitList(hasil)

        tvTotal.text = semuaAkun.size.toString()
        tvAktif.text = semuaAkun.count { it.status == "aktif" }.toString()
        tvNonaktif.text = semuaAkun.count { it.status == "nonaktif" }.toString()
    }

    private fun toggleStatus(akun: Akun) {
        val statusBaru = if (akun.status == "aktif") "nonaktif" else "aktif"
        lifecycleScope.launch {
            try {
                AkunRepository.toggleStatus(akun.id, statusBaru)
            } catch (e: Exception) {
                Toast.makeText(this@KelolaAkun, e.message ?: "Gagal update status", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editAkun(akun: Akun) {
        // Dialog sederhana untuk ubah nama & role; ganti password dikosongkan kalau tidak diubah
        val view = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)
        val input = EditText(this).apply {
            setText(akun.nama)
            hint = "Nama akun"
        }
        AlertDialog.Builder(this)
            .setTitle("Edit Akun (@${akun.username})")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val namaBaru = input.text.toString().trim()
                if (namaBaru.isEmpty()) return@setPositiveButton
                lifecycleScope.launch {
                    try {
                        AkunRepository.updateAkun(akun.id, namaBaru, akun.role, password = null)
                        Toast.makeText(this@KelolaAkun, "Akun diperbarui", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@KelolaAkun, e.message ?: "Gagal update", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun konfirmasiHapus(akun: Akun) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Akun")
            .setMessage("Yakin mau hapus akun @${akun.username}? Tindakan ini tidak bisa dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    try {
                        AkunRepository.hapusAkun(akun.id)
                        Toast.makeText(this@KelolaAkun, "Akun dihapus", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@KelolaAkun, e.message ?: "Gagal menghapus", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
