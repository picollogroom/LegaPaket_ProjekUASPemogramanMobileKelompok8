package com.example.legapaket.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.R
import com.example.legapaket.model.Laporan
import java.text.SimpleDateFormat
import java.util.Locale

class RiwayatLaporanAdapter(
    private var items: List<Laporan>,
    private val onClick: (Laporan) -> Unit
) : RecyclerView.Adapter<RiwayatLaporanAdapter.ViewHolder>() {

    fun submitList(newItems: List<Laporan>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvJudul: TextView = view.findViewById(R.id.tvJudulRiwayatLaporan)
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggalRiwayatLaporan)
        val tvTotalResi: TextView = view.findViewById(R.id.tvTotalResiRiwayatLaporan)
        val tvTotalPendapatan: TextView = view.findViewById(R.id.tvTotalPendapatanRiwayatLaporan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat_laporan, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val laporan = items[position]
        holder.tvJudul.text = laporan.judul
        holder.tvTanggal.text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(laporan.tanggalDibuat)
        holder.tvTotalResi.text = "${laporan.totalResi} resi"
        holder.tvTotalPendapatan.text = "Rp %,d".format(laporan.totalPendapatan).replace(",", ".")
        holder.itemView.setOnClickListener { onClick(laporan) }
    }
}