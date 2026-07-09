package com.example.legapaket

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.adapter.TripAdapter
import com.example.legapaket.data.TripRepository
import com.example.legapaket.model.Trip
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class RiwayatTrip : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var spinnerFilter: Spinner
    private lateinit var adapter: TripAdapter
    private var semuaTrip: List<Trip> = emptyList()
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_riwayat_trip)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recycler = findViewById(R.id.recyclerRiwayatTrip)
        spinnerFilter = findViewById(R.id.spinnerFilterStatusTrip)
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        recycler.layoutManager = LinearLayoutManager(this)
        adapter = TripAdapter(emptyList()) { trip -> tampilkanDetailTrip(trip) }
        recycler.adapter = adapter

        val statusArray = resources.getStringArray(R.array.status_trip_array)
        spinnerFilter.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusArray)
        spinnerFilter.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) { terapkanFilter() }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
    }

    override fun onStart() {
        super.onStart()
        listener = TripRepository.listenTrip { list ->
            semuaTrip = list.sortedByDescending { it.createdAt }
            terapkanFilter()
        }
    }

    override fun onStop() {
        super.onStop()
        listener?.remove()
    }

    private fun terapkanFilter() {
        val statusMap = mapOf(
            "Terjadwal" to "Dijadwalkan", "Berjalan" to "Berlangsung",
            "Selesai" to "Selesai", "Dibatalkan" to "Dibatalkan"
        )
        val filterLabel = spinnerFilter.selectedItem?.toString() ?: "Semua Status"
        val hasil = if (filterLabel == "Semua Status") semuaTrip
        else semuaTrip.filter { it.status == statusMap[filterLabel] }
        adapter.submitList(hasil)
    }

    private fun tampilkanDetailTrip(trip: Trip) {
        val opsi = arrayOf("Berlangsung", "Selesai", "Dibatalkan")
        AlertDialog.Builder(this)
            .setTitle("Update Status Trip")
            .setItems(opsi) { _, index ->
                lifecycleScope.launch {
                    TripRepository.updateStatusTrip(trip.id, opsi[index])
                }
            }
            .setNegativeButton("Tutup", null)
            .show()
    }
}
