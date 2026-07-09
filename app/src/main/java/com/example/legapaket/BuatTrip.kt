package com.example.legapaket

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.legapaket.adapter.ResiTripAdapter
import com.example.legapaket.data.PaketRepository
import com.example.legapaket.data.TripRepository
import com.example.legapaket.model.Paket
import com.example.legapaket.model.Trip
import com.example.legapaket.util.SessionManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BuatTrip : AppCompatActivity() {

    private lateinit var spinnerKotaAsal: Spinner
    private lateinit var spinnerKotaTujuan: Spinner
    private lateinit var etWaktuBerangkat: EditText
    private lateinit var etEstimasiTiba: EditText
    private lateinit var spinnerArmada: Spinner
    private lateinit var spinnerSopir: Spinner
    private lateinit var recyclerResi: RecyclerView
    private lateinit var etCatatan: EditText
    private lateinit var tvJumlahResiDipilih: TextView
    private lateinit var tvTotalResi: TextView
    private lateinit var tvTotalBerat: TextView
    private lateinit var btnBuatTrip: Button

    private var waktuBerangkatMillis: Long = 0
    private var estimasiTibaMillis: Long = 0
    private var paketTersedia: List<Paket> = emptyList()
    private val resiTerpilih = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_buat_trip)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        spinnerKotaAsal = findViewById(R.id.spinnerKotaAsal)
        spinnerKotaTujuan = findViewById(R.id.spinnerKotaTujuanTrip)
        etWaktuBerangkat = findViewById(R.id.etWaktuBerangkat)
        etEstimasiTiba = findViewById(R.id.etEstimasiTiba)
        spinnerArmada = findViewById(R.id.spinnerArmada)
        spinnerSopir = findViewById(R.id.spinnerSopir)
        recyclerResi = findViewById(R.id.recyclerResiTrip)
        etCatatan = findViewById(R.id.etCatatanTrip)
        tvJumlahResiDipilih = findViewById(R.id.tvJumlahResiDipilih)
        tvTotalResi = findViewById(R.id.tvTotalResiTrip)
        tvTotalBerat = findViewById(R.id.tvTotalBeratTrip)
        btnBuatTrip = findViewById(R.id.btnBuatTrip)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val kotaArray = resources.getStringArray(R.array.kota_tujuan_array)
        spinnerKotaAsal.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kotaArray)
        spinnerKotaTujuan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kotaArray)

        etWaktuBerangkat.setOnClickListener { pilihTanggalWaktu { millis -> waktuBerangkatMillis = millis; etWaktuBerangkat.setText(formatTanggal(millis)) } }
        etEstimasiTiba.setOnClickListener { pilihTanggalWaktu { millis -> estimasiTibaMillis = millis; etEstimasiTiba.setText(formatTanggal(millis)) } }

        recyclerResi.layoutManager = LinearLayoutManager(this)
        muatPaketTersedia()

        btnBuatTrip.setOnClickListener { buatTrip() }
    }

    private fun muatPaketTersedia() {
        lifecycleScope.launch {
            try {
                paketTersedia = PaketRepository.getPaketBelumAdaTrip()
                val adapter = ResiTripAdapter(paketTersedia, resiTerpilih) { updateRingkasan() }
                recyclerResi.adapter = adapter
                updateRingkasan()
            } catch (e: Exception) {
                Toast.makeText(this@BuatTrip, "Gagal memuat daftar resi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateRingkasan() {
        tvJumlahResiDipilih.text = "${resiTerpilih.size} resi dipilih"
        tvTotalResi.text = resiTerpilih.size.toString()
        val totalBerat = paketTersedia.filter { resiTerpilih.contains(it.noResi) }
            .sumOf { maxOf(it.berat, it.beratVolume) }
        tvTotalBerat.text = "%.1f kg".format(totalBerat)
    }

    private fun pilihTanggalWaktu(onSelected: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                cal.set(year, month, day, hour, minute)
                onSelected(cal.timeInMillis)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun formatTanggal(millis: Long): String {
        return SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(millis)
    }

    private fun buatTrip() {
        if (spinnerKotaAsal.selectedItem == spinnerKotaTujuan.selectedItem) {
            Toast.makeText(this, "Kota asal dan tujuan tidak boleh sama", Toast.LENGTH_SHORT).show()
            return
        }
        if (waktuBerangkatMillis == 0L) {
            Toast.makeText(this, "Pilih waktu keberangkatan", Toast.LENGTH_SHORT).show()
            return
        }
        if (resiTerpilih.isEmpty()) {
            Toast.makeText(this, "Pilih minimal 1 resi untuk trip ini", Toast.LENGTH_SHORT).show()
            return
        }

        val totalBerat = paketTersedia.filter { resiTerpilih.contains(it.noResi) }
            .sumOf { maxOf(it.berat, it.beratVolume) }

        val trip = Trip(
            kotaAsal = spinnerKotaAsal.selectedItem.toString(),
            kotaTujuan = spinnerKotaTujuan.selectedItem.toString(),
            waktuBerangkat = waktuBerangkatMillis,
            estimasiTiba = estimasiTibaMillis,
            armada = spinnerArmada.selectedItem?.toString() ?: "",
            sopir = spinnerSopir.selectedItem?.toString() ?: "",
            catatan = etCatatan.text.toString().trim(),
            daftarResi = resiTerpilih.toMutableList(),
            totalBerat = totalBerat,
            status = "Dijadwalkan",
            dibuatOleh = SessionManager.getNama(this)
        )

        btnBuatTrip.isEnabled = false
        lifecycleScope.launch {
            try {
                TripRepository.buatTrip(trip)
                AlertDialog.Builder(this@BuatTrip)
                    .setTitle("Trip Berhasil Dibuat")
                    .setMessage("${resiTerpilih.size} resi sudah ditugaskan ke trip ini.")
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .setCancelable(false)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@BuatTrip, e.message ?: "Gagal membuat trip", Toast.LENGTH_SHORT).show()
                btnBuatTrip.isEnabled = true
            }
        }
    }
}
