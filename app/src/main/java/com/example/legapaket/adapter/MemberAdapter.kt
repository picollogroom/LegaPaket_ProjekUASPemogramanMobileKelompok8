package com.example.legapaket.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.R
import com.example.legapaket.model.Member

class MemberAdapter(
    private var items: List<Member>,
    private val onEdit: (Member) -> Unit,
    private val onDelete: (Member) -> Unit
) : RecyclerView.Adapter<MemberAdapter.ViewHolder>() {

    fun submitList(newItems: List<Member>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInisial: TextView = view.findViewById(R.id.tvInisialMember)
        val tvNama: TextView = view.findViewById(R.id.tvNamaMemberItem)
        val tvTelp: TextView = view.findViewById(R.id.tvTelpMemberItem)
        val tvKota: TextView = view.findViewById(R.id.tvKotaMemberItem)
        val tvStatus: TextView = view.findViewById(R.id.tvStatusMemberItem)
        val btnMenu: ImageView = view.findViewById(R.id.btnMenuMember)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = items[position]
        holder.tvInisial.text = member.nama.trim().firstOrNull()?.uppercase() ?: "?"
        holder.tvNama.text = member.nama
        holder.tvTelp.text = member.telepon
        holder.tvKota.text = member.kota

        val aktif = member.status == "aktif"
        holder.tvStatus.text = if (aktif) "Aktif" else "Nonaktif"
        holder.tvStatus.setBackgroundColor(Color.parseColor(if (aktif) "#4CAF50" else "#9E9E9E"))

        holder.btnMenu.setOnClickListener { anchor ->
            val popup = PopupMenu(anchor.context, anchor)
            popup.menu.add("Edit")
            popup.menu.add("Hapus")
            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Edit" -> onEdit(member)
                    "Hapus" -> onDelete(member)
                }
                true
            }
            popup.show()
        }
    }
}
