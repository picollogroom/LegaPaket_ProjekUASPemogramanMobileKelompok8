package com.example.legapaket

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.legapaket.data.AkunRepository
import com.example.legapaket.util.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var buttonMasuk: Button
    private lateinit var progressLogin: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        buttonMasuk = findViewById(R.id.buttonmasuk)
        progressLogin = findViewById(R.id.progressLogin)

        // Kalau session masih ada, langsung lempar ke beranda sesuai role (skip login)
        if (SessionManager.isLoggedIn(this)) {
            goToBeranda(SessionManager.getRole(this))
            return
        }

        buttonMasuk.setOnClickListener { doLogin() }

        findViewById<Button>(R.id.buttonLacak).setOnClickListener {
            startActivity(Intent(this, ActivityTrackingPaket::class.java))
        }
    }

    private fun doLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username dan password wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val akun = AkunRepository.login(username, password)
                SessionManager.saveSession(
                    this@LoginActivity,
                    id = akun.id,
                    nama = akun.nama,
                    username = akun.username,
                    role = akun.role
                )
                Toast.makeText(this@LoginActivity, "Selamat datang, ${akun.nama}", Toast.LENGTH_SHORT).show()
                goToBeranda(akun.role)
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, e.message ?: "Login gagal", Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun goToBeranda(role: String) {
        val target = when (role) {
            "admin" -> BerandaAdmin::class.java
            "keuangan" -> BerandaKeuangan::class.java
            "warehouse" -> BerandaWarehouse::class.java
            "agen" -> MainMenuAgen::class.java
            else -> null
        }
        if (target == null) {
            Toast.makeText(this, "Role akun tidak dikenali: $role", Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(this, target))
        finish()
    }

    private fun setLoading(loading: Boolean) {
        progressLogin.visibility = if (loading) View.VISIBLE else View.GONE
        buttonMasuk.isEnabled = !loading
    }
}
