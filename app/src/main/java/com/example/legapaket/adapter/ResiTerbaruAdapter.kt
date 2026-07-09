package com.example.legapaket.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.R
import com.example.legapaket.model.Paket

class ResiTerbaruAdapter(
    private var items: List<Paket>,
    private val onClick: (Paket) -> Unit
) : RecyclerView.Adapter<ResiTerbaruAdapter.ViewHolder>() {

    fun submitList(newItems: List<Paket>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNoResi: TextView = view.findViewById(R.id.tvNoResiTerbaru)
        val tvRute: TextView = view.findViewById(R.id.tvRuteTerbaru)
        val tvStatus: TextView = view.findViewById(R.id.tvStatusTerbaru)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_resi_terbaru, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val paket = items[position]
        holder.tvNoResi.text = paket.noResi
        holder.tvRute.text = "${paket.kotaAsal} -> ${paket.kotaTujuan}"
        holder.tvStatus.text = paket.status

        // Warna badge disesuaikan sama status: hijau kalau sudah terkirim, oranye kalau masih proses
        if (paket.status == "Terkirim") {
            holder.tvStatus.setTextColor(Color.parseColor("#388E3C"))
            holder.tvStatus.setBackgroundColor(Color.parseColor("#E8F5E9"))
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#F57F17"))
            holder.tvStatus.setBackgroundColor(Color.parseColor("#FFF8E1"))
        }

        holder.itemView.setOnClickListener { onClick(paket) }
    }
}