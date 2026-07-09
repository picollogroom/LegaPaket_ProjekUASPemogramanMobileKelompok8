package com.example.legapaket.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.R
import com.example.legapaket.model.Paket

class ResiTripAdapter(
    private val items: List<Paket>,
    private val selected: MutableSet<String>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<ResiTripAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkBox: CheckBox = view.findViewById(R.id.checkResi)
        val tvNoResi: TextView = view.findViewById(R.id.tvNoResiTrip)
        val tvRute: TextView = view.findViewById(R.id.tvRuteResiTrip)
        val tvBerat: TextView = view.findViewById(R.id.tvBeratResiTrip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_resi_trip, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val paket = items[position]
        holder.tvNoResi.text = paket.noResi
        holder.tvRute.text = "${paket.kotaAsal} -> ${paket.kotaTujuan}"
        holder.tvBerat.text = "%.1f kg".format(maxOf(paket.berat, paket.beratVolume))

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = selected.contains(paket.noResi)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selected.add(paket.noResi) else selected.remove(paket.noResi)
            onSelectionChanged()
        }
    }
}
