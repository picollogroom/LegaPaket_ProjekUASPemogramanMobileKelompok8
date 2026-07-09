package com.example.legapaket.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.R
import com.example.legapaket.model.Paket

class LaporanAdapter(private var items: List<Paket>) : RecyclerView.Adapter<LaporanAdapter.ViewHolder>() {

    fun submitList(newItems: List<Paket>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNoResi: TextView = view.findViewById(R.id.tvNoResiLaporan)
        val tvKota: TextView = view.findViewById(R.id.tvKotaLaporan)
        val tvBiaya: TextView = view.findViewById(R.id.tvBiayaLaporan)
        val tvStatus: TextView = view.findViewById(R.id.tvStatusLaporan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_laporan, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val paket = items[position]
        holder.tvNoResi.text = paket.noResi
        holder.tvKota.text = "${paket.kotaAsal} -> ${paket.kotaTujuan}"
        holder.tvBiaya.text = "Rp %,d".format(paket.totalBiaya).replace(",", ".")
        holder.tvStatus.text = paket.status
    }
}
