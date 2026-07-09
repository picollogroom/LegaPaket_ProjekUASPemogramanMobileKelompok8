package com.example.legapaket

import android.content.Intent
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
import com.example.legapaket.adapter.MemberAdapter
import com.example.legapaket.data.MemberRepository
import com.example.legapaket.model.Member
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class KelolaMember : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var etCari: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var tvTotal: TextView
    private lateinit var tvAktif: TextView
    private lateinit var tvNonaktif: TextView
    private lateinit var adapter: MemberAdapter

    private var semuaMember: List<Member> = emptyList()
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_kelola_member)

        recycler = findViewById(R.id.recyclerMember)
        etCari = findViewById(R.id.etCariMember)
        spinnerStatus = findViewById(R.id.spinnerStatusMember)
        tvTotal = findViewById(R.id.tvTotalMember)
        tvAktif = findViewById(R.id.tvMemberAktif)
        tvNonaktif = findViewById(R.id.tvMemberNonaktif)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<FloatingActionButton>(R.id.fabTambahMember).setOnClickListener {
            startActivity(Intent(this, TambahMember::class.java))
        }

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = MemberAdapter(
            emptyList(),
            onEdit = { member -> bukaEdit(member) },
            onDelete = { member -> konfirmasiHapus(member) }
        )
        recycler.adapter = adapter

        val statusOptions = listOf("Semua Status", "aktif", "nonaktif")
        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)
        spinnerStatus.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) { terapkanFilter() }
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
        listener = MemberRepository.listenMember { list ->
            semuaMember = list
            terapkanFilter()
        }
    }

    override fun onStop() {
        super.onStop()
        listener?.remove()
    }

    private fun terapkanFilter() {
        val keyword = etCari.text.toString().trim().lowercase()
        val statusFilter = spinnerStatus.selectedItem?.toString() ?: "Semua Status"

        var hasil = semuaMember
        if (statusFilter != "Semua Status") {
            hasil = hasil.filter { it.status == statusFilter }
        }
        if (keyword.isNotEmpty()) {
            hasil = hasil.filter { it.nama.lowercase().contains(keyword) || it.telepon.contains(keyword) }
        }
        adapter.submitList(hasil)

        tvTotal.text = semuaMember.size.toString()
        tvAktif.text = semuaMember.count { it.status == "aktif" }.toString()
        tvNonaktif.text = semuaMember.count { it.status == "nonaktif" }.toString()
    }

    private fun bukaEdit(member: Member) {
        val intent = Intent(this, TambahMember::class.java).apply {
            putExtra("member_id", member.id)
            putExtra("member_nama", member.nama)
            putExtra("member_telepon", member.telepon)
            putExtra("member_alamat", member.alamat)
            putExtra("member_kota", member.kota)
            putExtra("member_status", member.status)
        }
        startActivity(intent)
    }

    private fun konfirmasiHapus(member: Member) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Member")
            .setMessage("Yakin mau hapus member ${member.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    try {
                        MemberRepository.hapusMember(member.id)
                        Toast.makeText(this@KelolaMember, "Member dihapus", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@KelolaMember, e.message ?: "Gagal menghapus", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
