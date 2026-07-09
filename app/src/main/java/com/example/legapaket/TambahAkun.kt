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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.legapaket.data.AkunRepository
import kotlinx.coroutines.launch

class TambahAkun : AppCompatActivity() {

    private lateinit var etNama: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvPreviewInisial: TextView
    private lateinit var tvPreviewNama: TextView
    private lateinit var tvPreviewRole: TextView
    private lateinit var tvRoleTerpilih: TextView
    private lateinit var btnSimpan: Button

    private lateinit var cardAdmin: LinearLayout
    private lateinit var cardAgen: LinearLayout
    private lateinit var cardWarehouse: LinearLayout
    private lateinit var cardKeuangan: LinearLayout

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_akun)

        etNama = findViewById(R.id.etNamaAkun)
        etUsername = findViewById(R.id.etUsernameAkun)
        etPassword = findViewById(R.id.etPasswordAkun)
        tvPreviewInisial = findViewById(R.id.tvPreviewInisial)
        tvPreviewNama = findViewById(R.id.tvPreviewNama)
        tvPreviewRole = findViewById(R.id.tvPreviewRole)
        tvRoleTerpilih = findViewById(R.id.tvRoleTerpilih)
        btnSimpan = findViewById(R.id.btnSimpanAkun)
        cardAdmin = findViewById(R.id.cardRoleAdmin)
        cardAgen = findViewById(R.id.cardRoleAgen)
        cardWarehouse = findViewById(R.id.cardRolewarehouse)
        cardKeuangan = findViewById(R.id.cardRolekeuangan)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        etNama.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val nama = s?.toString().orEmpty()
                tvPreviewNama.text = nama.ifBlank { "Nama Akun" }
                tvPreviewInisial.text = nama.trim().firstOrNull()?.uppercase() ?: "A"
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        pilihRole("admin")
        cardAdmin.setOnClickListener { pilihRole("admin") }
        cardAgen.setOnClickListener { pilihRole("agen") }
        cardWarehouse.setOnClickListener { pilihRole("warehouse") }
        cardKeuangan.setOnClickListener { pilihRole("keuangan") }

        findViewById<ImageView>(R.id.btnTogglePassword).setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            etPassword.inputType = if (isPasswordVisible)
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            etPassword.setSelection(etPassword.text.length)
        }

        btnSimpan.setOnClickListener { simpanAkun() }
    }

    private fun pilihRole(role: String) {
        tvRoleTerpilih.text = role
        tvPreviewRole.text = role.replaceFirstChar { it.uppercase() }

        val terpilih = mapOf(
            "admin" to cardAdmin, "agen" to cardAgen,
            "warehouse" to cardWarehouse, "keuangan" to cardKeuangan
        )
        terpilih.forEach { (r, card) ->
            val tv = card.getChildAt(0) as TextView
            if (r == role) {
                card.setBackgroundColor(getColor(R.color.banner))
                tv.setTextColor(getColor(android.R.color.white))
            } else {
                card.setBackgroundColor(android.graphics.Color.parseColor("#EEEEEE"))
                tv.setTextColor(android.graphics.Color.parseColor("#555555"))
            }
        }
    }

    private fun simpanAkun() {
        val nama = etNama.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()
        val role = tvRoleTerpilih.text.toString()

        if (nama.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        btnSimpan.isEnabled = false
        lifecycleScope.launch {
            try {
                AkunRepository.tambahAkun(nama, username, password, role)
                Toast.makeText(this@TambahAkun, "Akun berhasil dibuat", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@TambahAkun, e.message ?: "Gagal menyimpan akun", Toast.LENGTH_SHORT).show()
                btnSimpan.isEnabled = true
            }
        }
    }
}
