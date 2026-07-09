package com.example.legapaket

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.legapaket.data.MemberRepository
import com.example.legapaket.model.Member
import kotlinx.coroutines.launch

/**
 * Dipakai untuk tambah member baru maupun edit (kirim extra "member_id" untuk mode edit).
 */
class TambahMember : AppCompatActivity() {

    private lateinit var etNama: EditText
    private lateinit var etTelp: EditText
    private lateinit var etAlamat: EditText
    private lateinit var etKota: EditText
    private lateinit var tvPreviewInisial: TextView
    private lateinit var tvPreviewNama: TextView
    private lateinit var tvPreviewStatus: TextView
    private lateinit var tvStatusTerpilih: TextView
    private lateinit var cardAktif: LinearLayout
    private lateinit var cardNonaktif: LinearLayout
    private lateinit var btnSimpan: Button

    private var editId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_member)

        etNama = findViewById(R.id.etNamaMember)
        etTelp = findViewById(R.id.etTelpMember)
        etAlamat = findViewById(R.id.etAlamatMember)
        etKota = findViewById(R.id.etKotaMember)
        tvPreviewInisial = findViewById(R.id.tvPreviewInisialMember)
        tvPreviewNama = findViewById(R.id.tvPreviewNamaMember)
        tvPreviewStatus = findViewById(R.id.tvPreviewStatusMember)
        tvStatusTerpilih = findViewById(R.id.tvStatusTerpilih)
        cardAktif = findViewById(R.id.cardStatusAktif)
        cardNonaktif = findViewById(R.id.cardStatusNonaktif)
        btnSimpan = findViewById(R.id.btnSimpanMember)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        etNama.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nama = s?.toString().orEmpty()
                tvPreviewNama.text = nama.ifBlank { "Nama Member" }
                tvPreviewInisial.text = nama.trim().firstOrNull()?.uppercase() ?: "M"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        pilihStatus("aktif")
        cardAktif.setOnClickListener { pilihStatus("aktif") }
        cardNonaktif.setOnClickListener { pilihStatus("nonaktif") }

        editId = intent.getStringExtra("member_id")
        if (editId != null) {
            findViewById<TextView>(R.id.tvJudulMember).text = "Edit Member"
            etNama.setText(intent.getStringExtra("member_nama"))
            etTelp.setText(intent.getStringExtra("member_telepon"))
            etAlamat.setText(intent.getStringExtra("member_alamat"))
            etKota.setText(intent.getStringExtra("member_kota"))
            pilihStatus(intent.getStringExtra("member_status") ?: "aktif")
        }

        btnSimpan.setOnClickListener { simpanMember() }
    }

    private fun pilihStatus(status: String) {
        tvStatusTerpilih.text = status
        tvPreviewStatus.text = status.replaceFirstChar { it.uppercase() }
        val aktif = status == "aktif"
        cardAktif.setBackgroundColor(getColor(if (aktif) R.color.banner else android.R.color.transparent))
        cardNonaktif.setBackgroundColor(getColor(if (!aktif) R.color.banner else android.R.color.transparent))
        (cardAktif.getChildAt(0) as? TextView)?.setTextColor(if (aktif) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#555555"))
        (cardNonaktif.getChildAt(0) as? TextView)?.setTextColor(if (!aktif) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#555555"))
        if (!aktif) cardNonaktif.setBackgroundColor(getColor(R.color.banner)) else cardAktif.setBackgroundColor(getColor(R.color.banner))
    }

    private fun simpanMember() {
        val nama = etNama.text.toString().trim()
        val telp = etTelp.text.toString().trim()
        val alamat = etAlamat.text.toString().trim()
        val kota = etKota.text.toString().trim()
        val status = tvStatusTerpilih.text.toString()

        if (nama.isEmpty() || telp.isEmpty() || alamat.isEmpty() || kota.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        btnSimpan.isEnabled = false
        val member = Member(nama = nama, telepon = telp, alamat = alamat, kota = kota, status = status)

        lifecycleScope.launch {
            try {
                if (editId != null) {
                    MemberRepository.updateMember(editId!!, member)
                    Toast.makeText(this@TambahMember, "Member diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    MemberRepository.tambahMember(member)
                    Toast.makeText(this@TambahMember, "Member berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@TambahMember, e.message ?: "Gagal menyimpan", Toast.LENGTH_SHORT).show()
                btnSimpan.isEnabled = true
            }
        }
    }
}
