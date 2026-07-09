package com.example.legapaket.util

import android.content.Context

/**
 * Menyimpan info akun yang sedang login memakai SharedPreferences.
 * Dipakai supaya tiap Activity tahu siapa yang login & apa role-nya,
 * tanpa harus query ulang ke Firestore terus-menerus.
 */
object SessionManager {
    private const val PREF_NAME = "lega_paket_session"
    private const val KEY_ID = "akun_id"
    private const val KEY_NAMA = "akun_nama"
    private const val KEY_USERNAME = "akun_username"
    private const val KEY_ROLE = "akun_role"

    fun saveSession(context: Context, id: String, nama: String, username: String, role: String) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit()
            .putString(KEY_ID, id)
            .putString(KEY_NAMA, nama)
            .putString(KEY_USERNAME, username)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun getAkunId(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_ID, "") ?: ""

    fun getNama(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_NAMA, "") ?: ""

    fun getUsername(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_USERNAME, "") ?: ""

    fun getRole(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getString(KEY_ROLE, "") ?: ""

    fun isLoggedIn(context: Context): Boolean = getAkunId(context).isNotEmpty()

    fun logout(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
}
