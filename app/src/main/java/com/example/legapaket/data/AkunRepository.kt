package com.example.legapaket.data

import com.example.legapaket.model.Akun
import com.example.legapaket.util.PasswordUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

/**
 * Semua operasi CRUD untuk collection "akun" (login, kelola akun, tambah akun).
 */
object AkunRepository {
    private val db get() = FirebaseFirestore.getInstance()
    private val collection get() = db.collection("akun")

    /** Hasil login: null kalau gagal, berisi pesan error lewat exception. */
    suspend fun login(username: String, password: String): Akun {
        val snapshot = collection
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) {
            throw Exception("Username tidak ditemukan")
        }

        val doc = snapshot.documents[0]
        val akun = doc.toObject(Akun::class.java)!!.copy(id = doc.id)

        if (akun.status == "nonaktif") {
            throw Exception("Akun kamu sudah dinonaktifkan, hubungi admin")
        }
        if (!PasswordUtil.matches(password, akun.passwordHash)) {
            throw Exception("Password salah")
        }
        return akun
    }

    suspend fun tambahAkun(nama: String, username: String, password: String, role: String): String {
        val existing = collection.whereEqualTo("username", username).limit(1).get().await()
        if (!existing.isEmpty) {
            throw Exception("Username sudah dipakai, pilih username lain")
        }
        val akun = Akun(
            nama = nama,
            username = username,
            passwordHash = PasswordUtil.hash(password),
            role = role,
            status = "aktif"
        )
        val ref = collection.add(akun).await()
        return ref.id
    }

    suspend fun updateAkun(id: String, nama: String, role: String, password: String?) {
        val updates = hashMapOf<String, Any>(
            "nama" to nama,
            "role" to role
        )
        if (!password.isNullOrBlank()) {
            updates["passwordHash"] = PasswordUtil.hash(password)
        }
        collection.document(id).update(updates).await()
    }

    suspend fun toggleStatus(id: String, statusBaru: String) {
        collection.document(id).update("status", statusBaru).await()
    }

    suspend fun hapusAkun(id: String) {
        collection.document(id).delete().await()
    }

    suspend fun getAllAkun(): List<Akun> {
        val snapshot = collection.orderBy("createdAt").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Akun::class.java)?.copy(id = doc.id)
        }
    }

    /** Realtime listener supaya list di KelolaAkun auto update tanpa refresh manual. */
    fun listenAkun(onChange: (List<Akun>) -> Unit): ListenerRegistration {
        return collection.orderBy("createdAt").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Akun::class.java)?.copy(id = doc.id)
            }
            onChange(list)
        }
    }
}
