package com.example.legapaket.util

import java.security.MessageDigest

/**
 * Hashing password sederhana pakai SHA-256.
 * Catatan: untuk aplikasi production sebaiknya pakai bcrypt/argon2 di server,
 * tapi karena tidak ada backend server (langsung ke Firestore), SHA-256 + ini
 * sudah cukup untuk kebutuhan tugas/skripsi. Jangan simpan password plain-text.
 */
object PasswordUtil {
    fun hash(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun matches(password: String, hash: String): Boolean {
        return hash(password) == hash
    }
}
