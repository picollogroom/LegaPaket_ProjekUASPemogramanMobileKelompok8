package com.example.legapaket.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.R
import com.example.legapaket.model.Trip

class TripAdapter(
    private var items: List<Trip>,
    private val onClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.ViewHolder>() {

    fun submitList(newItems: List<Trip>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRute: TextView = view.findViewById(R.id.tvRuteTripItem)
        val tvStatus: TextView = view.findViewById(R.id.tvStatusTripItem)
        val tvArmadaSopir: TextView = view.findViewById(R.id.tvArmadaSopirItem)
        val tvJumlahResi: TextView = view.findViewById(R.id.tvJumlahResiItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trip = items[position]
        holder.tvRute.text = "${trip.kotaAsal} -> ${trip.kotaTujuan}"
        holder.tvArmadaSopir.text = "${trip.armada} - ${trip.sopir}"
        holder.tvJumlahResi.text = "${trip.daftarResi.size} resi - %.1f kg".format(trip.totalBerat)

        val warna = when (trip.status) {
            "Selesai" -> "#4CAF50"
            "Berlangsung" -> "#2196F3"
            "Dibatalkan" -> "#9E9E9E"
            else -> "#F57F17" // Dijadwalkan
        }
        holder.tvStatus.text = trip.status
        holder.tvStatus.setBackgroundColor(Color.parseColor(warna))

        holder.itemView.setOnClickListener { onClick(trip) }
    }
}
