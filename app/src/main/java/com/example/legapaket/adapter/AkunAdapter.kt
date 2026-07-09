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
import com.example.legapaket.model.Akun

class AkunAdapter(
    private var items: List<Akun>,
    private val onToggleStatus: (Akun) -> Unit,
    private val onEdit: (Akun) -> Unit,
    private val onDelete: (Akun) -> Unit
) : RecyclerView.Adapter<AkunAdapter.ViewHolder>() {

    fun submitList(newItems: List<Akun>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInisial: TextView = view.findViewById(R.id.tvInisial)
        val tvNama: TextView = view.findViewById(R.id.tvNamaAkun)
        val tvUsername: TextView = view.findViewById(R.id.tvUsernameAkun)
        val tvRole: TextView = view.findViewById(R.id.tvRoleAkun)
        val tvStatus: TextView = view.findViewById(R.id.tvStatusAkun)
        val btnMenu: ImageView = view.findViewById(R.id.btnMenuAkun)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_akun, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val akun = items[position]
        holder.tvInisial.text = akun.nama.trim().firstOrNull()?.uppercase() ?: "?"
        holder.tvNama.text = akun.nama
        holder.tvUsername.text = "@" + akun.username
        holder.tvRole.text = akun.role.replaceFirstChar { it.uppercase() }

        val aktif = akun.status == "aktif"
        holder.tvStatus.text = if (aktif) "Aktif" else "Nonaktif"
        holder.tvStatus.setBackgroundColor(Color.parseColor(if (aktif) "#4CAF50" else "#9E9E9E"))

        holder.btnMenu.setOnClickListener { anchor ->
            val popup = PopupMenu(anchor.context, anchor)
            popup.menu.add(if (aktif) "Nonaktifkan" else "Aktifkan")
            popup.menu.add("Edit")
            popup.menu.add("Hapus")
            popup.setOnMenuItemClickListener { item ->
                when (item.title) {
                    "Nonaktifkan", "Aktifkan" -> onToggleStatus(akun)
                    "Edit" -> onEdit(akun)
                    "Hapus" -> onDelete(akun)
                }
                true
            }
            popup.show()
        }
    }
}
