package com.example.legapaket.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.R
import com.example.legapaket.model.StatusHistory
import java.text.SimpleDateFormat
import java.util.Locale

class RiwayatStatusAdapter(private var items: List<StatusHistory>) :
    RecyclerView.Adapter<RiwayatStatusAdapter.ViewHolder>() {

    fun submitList(newItems: List<StatusHistory>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvStatus: TextView = view.findViewById(R.id.tvStatusRiwayatItem)
        val tvWaktu: TextView = view.findViewById(R.id.tvWaktuRiwayatItem)
        val tvKeterangan: TextView = view.findViewById(R.id.tvKeteranganRiwayatItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat_status, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val riwayat = items[position]
        holder.tvStatus.text = riwayat.status
        holder.tvWaktu.text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(riwayat.waktu)
        holder.tvKeterangan.text = riwayat.keterangan.ifBlank { "-" }
    }
}