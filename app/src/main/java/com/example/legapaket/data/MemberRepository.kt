package com.example.legapaket.data

import com.example.legapaket.model.Member
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

object MemberRepository {
    private val db get() = FirebaseFirestore.getInstance()
    private val collection get() = db.collection("member")

    suspend fun tambahMember(member: Member): String {
        val ref = collection.add(member).await()
        return ref.id
    }

    suspend fun updateMember(id: String, member: Member) {
        val updates = hashMapOf<String, Any>(
            "nama" to member.nama,
            "telepon" to member.telepon,
            "alamat" to member.alamat,
            "kota" to member.kota,
            "status" to member.status
        )
        collection.document(id).update(updates).await()
    }

    suspend fun hapusMember(id: String) {
        collection.document(id).delete().await()
    }

    suspend fun getAllMember(): List<Member> {
        val snapshot = collection.orderBy("createdAt").get().await()
        return snapshot.documents.mapNotNull { it.toObject(Member::class.java)?.copy(id = it.id) }
    }

    suspend fun getMemberAktif(): List<Member> {
        val snapshot = collection.whereEqualTo("status", "aktif").get().await()
        return snapshot.documents.mapNotNull { it.toObject(Member::class.java)?.copy(id = it.id) }
    }

    fun listenMember(onChange: (List<Member>) -> Unit): ListenerRegistration {
        return collection.orderBy("createdAt").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            onChange(snapshot.documents.mapNotNull { it.toObject(Member::class.java)?.copy(id = it.id) })
        }
    }
}
