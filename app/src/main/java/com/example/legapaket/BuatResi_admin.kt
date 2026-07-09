package com.example.legapaket

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.legapaket.data.MemberRepository
import com.example.legapaket.data.PaketRepository
import com.example.legapaket.model.Member
import com.example.legapaket.model.Paket
import com.example.legapaket.util.SessionManager
import kotlinx.coroutines.launch

class BuatResi_admin : AppCompatActivity() {

    private lateinit var spinnerMember: Spinner
    private lateinit var layoutInfoMember: LinearLayout
    private lateinit var tvInfoNama: TextView
    private lateinit var tvInfoTelp: TextView
    private lateinit var tvInfoAlamat: TextView

    private lateinit var etNamaPengirim: EditText
    private lateinit var etAlamatPengirim: EditText
    private lateinit var etTelpPengirim: EditText
    private lateinit var etNamaPenerima: EditText
    private lateinit var etAlamatPenerima: EditText
    private lateinit var etTelpPenerima: EditText
    private lateinit var spinnerKotaTujuan: Spinner
    private lateinit var spinnerJenisBarang: Spinner
    private lateinit var etPanjang: EditText
    private lateinit var etLebar: EditText
    private lateinit var etTinggi: EditText
    private lateinit var etBerat: EditText
    private lateinit var radioPembayaran: RadioGroup
    private lateinit var tvBeratVolume: TextView
    private lateinit var tvTotalBiaya: TextView
    private lateinit var btnHitung: Button
    private lateinit var btnBuatResi: Button

    private var daftarMember: List<Member> = emptyList()
    private var biayaTerhitung: Long = 0

    private val kotaList by lazy { listOf("Pilih Kota Tujuan") + resources.getStringArray(R.array.kota_tujuan_array) }
    private val jenisBarangList by lazy { listOf("Pilih Jenis Barang") + resources.getStringArray(R.array.jenis_barang_array) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_buat_resi_admin)

        spinnerMember = findViewById(R.id.spinnerMember)
        layoutInfoMember = findViewById(R.id.layoutInfoMember)
        tvInfoNama = findViewById(R.id.tvInfoNamaMember)
        tvInfoTelp = findViewById(R.id.tvInfoTelpMember)
        tvInfoAlamat = findViewById(R.id.tvInfoAlamatMember)

        etNamaPengirim = findViewById(R.id.etNamaPengirim)
        etAlamatPengirim = findViewById(R.id.etAlamatPengirim)
        etTelpPengirim = findViewById(R.id.etTelpPengirim)
        etNamaPenerima = findViewById(R.id.etNamaPenerima)
        etAlamatPenerima = findViewById(R.id.etAlamatPenerima)
        etTelpPenerima = findViewById(R.id.etTelpPenerima)
        spinnerKotaTujuan = findViewById(R.id.spinnerKotaTujuan)
        spinnerJenisBarang = findViewById(R.id.spinnerJenisBarang)
        etPanjang = findViewById(R.id.etPanjang)
        etLebar = findViewById(R.id.etLebar)
        etTinggi = findViewById(R.id.etTinggi)
        etBerat = findViewById(R.id.etBerat)
        radioPembayaran = findViewById(R.id.radioGroupPembayaran)
        tvBeratVolume = findViewById(R.id.tvBeratVolume)
        tvTotalBiaya = findViewById(R.id.tvTotalBiaya)
        btnHitung = findViewById(R.id.btnHitungBiaya)
        btnBuatResi = findViewById(R.id.btnBuatResi)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        spinnerKotaTujuan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, kotaList)
        spinnerJenisBarang.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, jenisBarangList)

        layoutInfoMember.visibility = View.GONE
        muatMember()

        findViewById<Button>(R.id.btnGunakanMember).setOnClickListener {
            val posisi = spinnerMember.selectedItemPosition
            if (posisi in daftarMember.indices) {
                val member = daftarMember[posisi]
                etNamaPengirim.setText(member.nama)
                etAlamatPengirim.setText(member.alamat)
                etTelpPengirim.setText(member.telepon)
            }
        }

        spinnerMember.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (pos in daftarMember.indices) {
                    val m = daftarMember[pos]
                    tvInfoNama.text = m.nama
                    tvInfoTelp.text = m.telepon
                    tvInfoAlamat.text = m.alamat
                    layoutInfoMember.visibility = View.VISIBLE
                } else {
                    layoutInfoMember.visibility = View.GONE
                }
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }

        btnHitung.setOnClickListener { hitungBiaya() }
        btnBuatResi.setOnClickListener { buatResi() }
    }

    private fun muatMember() {
        lifecycleScope.launch {
            try {
                daftarMember = MemberRepository.getMemberAktif()
                val nama = listOf("Input Manual") + daftarMember.map { it.nama }
                spinnerMember.adapter = ArrayAdapter(this@BuatResi_admin, android.R.layout.simple_spinner_dropdown_item, nama)
            } catch (e: Exception) {
                Toast.makeText(this@BuatResi_admin, "Gagal memuat data member", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun hitungBiaya(): Boolean {
        val p = etPanjang.text.toString().toDoubleOrNull() ?: 0.0
        val l = etLebar.text.toString().toDoubleOrNull() ?: 0.0
        val t = etTinggi.text.toString().toDoubleOrNull() ?: 0.0
        val berat = etBerat.text.toString().toDoubleOrNull()

        if (berat == null || berat <= 0.0) {
            Toast.makeText(this, "Isi berat barang dulu", Toast.LENGTH_SHORT).show()
            return false
        }

        val beratVolume = PaketRepository.hitungBeratVolume(p, l, t)
        val beratDipakai = maxOf(berat, beratVolume)
        biayaTerhitung = PaketRepository.hitungBiaya(beratDipakai)

        tvBeratVolume.text = "Berat Volume: %.2f kg".format(beratVolume)
        tvTotalBiaya.text = "Rp %,d".format(biayaTerhitung).replace(",", ".")
        return true
    }

    private fun buatResi() {
        if (etNamaPengirim.text.isBlank() || etTelpPengirim.text.isBlank() || etAlamatPengirim.text.isBlank()) {
            Toast.makeText(this, "Lengkapi data pengirim", Toast.LENGTH_SHORT).show()
            return
        }
        if (etNamaPenerima.text.isBlank() || etTelpPenerima.text.isBlank() || etAlamatPenerima.text.isBlank()) {
            Toast.makeText(this, "Lengkapi data penerima", Toast.LENGTH_SHORT).show()
            return
        }
        if (spinnerKotaTujuan.selectedItemPosition == 0) {
            Toast.makeText(this, "Pilih kota tujuan", Toast.LENGTH_SHORT).show()
            return
        }
        if (spinnerJenisBarang.selectedItemPosition == 0) {
            Toast.makeText(this, "Pilih jenis barang", Toast.LENGTH_SHORT).show()
            return
        }
        val idPembayaran = radioPembayaran.checkedRadioButtonId
        if (idPembayaran == -1) {
            Toast.makeText(this, "Pilih metode pembayaran", Toast.LENGTH_SHORT).show()
            return
        }
        if (!hitungBiaya()) return

        val metodeBayar = when (idPembayaran) {
            R.id.radioCOD -> "COD"
            R.id.radioTransfer -> "Transfer"
            else -> "E-Wallet"
        }

        val posisiMember = spinnerMember.selectedItemPosition
        val memberId = if (posisiMember in daftarMember.indices) daftarMember[posisiMember].id else ""

        val paket = Paket(
            memberId = memberId,
            namaPengirim = etNamaPengirim.text.toString().trim(),
            alamatPengirim = etAlamatPengirim.text.toString().trim(),
            telpPengirim = etTelpPengirim.text.toString().trim(),
            namaPenerima = etNamaPenerima.text.toString().trim(),
            alamatPenerima = etAlamatPenerima.text.toString().trim(),
            telpPenerima = etTelpPenerima.text.toString().trim(),
            kotaAsal = "Bandung",
            kotaTujuan = spinnerKotaTujuan.selectedItem.toString(),
            jenisBarang = spinnerJenisBarang.selectedItem.toString(),
            panjang = etPanjang.text.toString().toDoubleOrNull() ?: 0.0,
            lebar = etLebar.text.toString().toDoubleOrNull() ?: 0.0,
            tinggi = etTinggi.text.toString().toDoubleOrNull() ?: 0.0,
            berat = etBerat.text.toString().toDoubleOrNull() ?: 0.0,
            beratVolume = PaketRepository.hitungBeratVolume(
                etPanjang.text.toString().toDoubleOrNull() ?: 0.0,
                etLebar.text.toString().toDoubleOrNull() ?: 0.0,
                etTinggi.text.toString().toDoubleOrNull() ?: 0.0
            ),
            metodePembayaran = metodeBayar,
            totalBiaya = biayaTerhitung,
            dibuatOleh = SessionManager.getNama(this)
        )

        btnBuatResi.isEnabled = false
        lifecycleScope.launch {
            try {
                val noResi = PaketRepository.buatResi(paket)
                AlertDialog.Builder(this@BuatResi_admin)
                    .setTitle("Resi Berhasil Dibuat")
                    .setMessage("Nomor Resi:\n$noResi")
                    .setPositiveButton("OK") { _, _ -> finish() }
                    .setCancelable(false)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@BuatResi_admin, e.message ?: "Gagal membuat resi", Toast.LENGTH_SHORT).show()
                btnBuatResi.isEnabled = true
            }
        }
    }
}
