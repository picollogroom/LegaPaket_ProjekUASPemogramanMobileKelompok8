package com.example.legapaket.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.R

data class StokKota(val kota: String, val jumlah: Int)

class StokAdapter(private var items: List<StokKota>) : RecyclerView.Adapter<StokAdapter.ViewHolder>() {

    fun submitList(newItems: List<StokKota>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvKota: TextView = view.findViewById(R.id.tvKotaStok)
        val tvJumlah: TextView = view.findViewById(R.id.tvJumlahStok)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stok_kota, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvKota.text = items[position].kota
        holder.tvJumlah.text = "${items[position].jumlah} paket"
    }
}
