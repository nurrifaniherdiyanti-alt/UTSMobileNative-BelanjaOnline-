package com.example.belanjaonline

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    private lateinit var etNamaPembeli: EditText
    private lateinit var etNamaProduk: EditText
    private lateinit var etHargaSatuan: EditText
    private lateinit var etJumlahBarang: EditText
    private lateinit var spinnerKota: Spinner
    private lateinit var spinnerMember: Spinner
    private lateinit var btnHitung: Button
    private lateinit var btnReset: Button
    private lateinit var cardOutput: MaterialCardView
    private lateinit var tvOutNamaPembeli: TextView
    private lateinit var tvOutNamaProduk: TextView
    private lateinit var tvOutHargaSatuan: TextView
    private lateinit var tvOutJumlahBarang: TextView
    private lateinit var tvOutSubtotal: TextView
    private lateinit var tvOutDiskon: TextView
    private lateinit var tvOutOngkir: TextView
    private lateinit var tvOutTotalBayar: TextView
    private lateinit var tvOutStatusPromo: TextView
    private lateinit var tvOutKeteranganPromo: TextView

    private val ongkirMap = mapOf(
        "Jakarta" to 0, "Bandung" to 15000, "Surabaya" to 25000,
        "Medan" to 35000, "Makassar" to 40000, "Yogyakarta" to 20000, "Bali" to 30000
    )
    private val diskonMap = mapOf(
        "Regular" to 0.0, "Silver" to 0.05, "Gold" to 0.10, "Platinum" to 0.15, "VIP" to 0.20
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        initViews()
        setupSpinners()
        btnHitung.setOnClickListener { if (validateInput()) hitungTransaksi() }
        btnReset.setOnClickListener  { resetForm() }
    }

    private fun initViews() {
        etNamaPembeli        = findViewById(R.id.etNamaPembeli)
        etNamaProduk         = findViewById(R.id.etNamaProduk)
        etHargaSatuan        = findViewById(R.id.etHargaSatuan)
        etJumlahBarang       = findViewById(R.id.etJumlahBarang)
        spinnerKota          = findViewById(R.id.spinnerKota)
        spinnerMember        = findViewById(R.id.spinnerMember)
        btnHitung            = findViewById(R.id.btnHitung)
        btnReset             = findViewById(R.id.btnReset)
        cardOutput           = findViewById(R.id.cardOutput)
        tvOutNamaPembeli     = findViewById(R.id.tvOutNamaPembeli)
        tvOutNamaProduk      = findViewById(R.id.tvOutNamaProduk)
        tvOutHargaSatuan     = findViewById(R.id.tvOutHargaSatuan)
        tvOutJumlahBarang    = findViewById(R.id.tvOutJumlahBarang)
        tvOutSubtotal        = findViewById(R.id.tvOutSubtotal)
        tvOutDiskon          = findViewById(R.id.tvOutDiskon)
        tvOutOngkir          = findViewById(R.id.tvOutOngkir)
        tvOutTotalBayar      = findViewById(R.id.tvOutTotalBayar)
        tvOutStatusPromo     = findViewById(R.id.tvOutStatusPromo)
        tvOutKeteranganPromo = findViewById(R.id.tvOutKeteranganPromo)
    }

    private fun setupSpinners() {
        val kota   = listOf("Jakarta","Bandung","Surabaya","Medan","Makassar","Yogyakarta","Bali")
        val member = listOf("Regular","Silver","Gold","Platinum","VIP")
        spinnerKota.adapter   = ArrayAdapter(this, R.layout.spinner_item, kota).also   { it.setDropDownViewResource(R.layout.spinner_dropdown_item) }
        spinnerMember.adapter = ArrayAdapter(this, R.layout.spinner_item, member).also { it.setDropDownViewResource(R.layout.spinner_dropdown_item) }
    }

    private fun validateInput(): Boolean {
        var ok = true
        if (etNamaPembeli.text.isBlank())  { etNamaPembeli.error  = "Wajib diisi"; ok = false }
        if (etNamaProduk.text.isBlank())   { etNamaProduk.error   = "Wajib diisi"; ok = false }
        if (etHargaSatuan.text.isBlank())  { etHargaSatuan.error  = "Wajib diisi"; ok = false }
        if (etJumlahBarang.text.isBlank()) { etJumlahBarang.error = "Wajib diisi"; ok = false }
        return ok
    }

    private fun hitungTransaksi() {
        val nama   = etNamaPembeli.text.toString()
        val produk = etNamaProduk.text.toString()
        val harga  = etHargaSatuan.text.toString().toDoubleOrNull() ?: 0.0
        val jumlah = etJumlahBarang.text.toString().toIntOrNull() ?: 0
        val kota   = spinnerKota.selectedItem.toString()
        val member = spinnerMember.selectedItem.toString()
        val subtotal = harga * jumlah
        val pct      = diskonMap[member] ?: 0.0
        val diskon   = subtotal * pct
        val ongkir   = (ongkirMap[kota] ?: 0).toDouble()
        val total    = subtotal - diskon + ongkir

        tvOutNamaPembeli.text  = nama
        tvOutNamaProduk.text   = produk
        tvOutHargaSatuan.text  = formatRp(harga)
        tvOutJumlahBarang.text = "$jumlah pcs"
        tvOutSubtotal.text     = formatRp(subtotal)
        tvOutDiskon.text       = "${(pct * 100).toInt()}% — ${formatRp(diskon)}"
        tvOutOngkir.text       = if (ongkir == 0.0) "GRATIS" else formatRp(ongkir)
        tvOutTotalBayar.text   = formatRp(total)

        val (status, ket) = promoInfo(member, subtotal)
        tvOutStatusPromo.text     = status
        tvOutKeteranganPromo.text = ket
        tvOutStatusPromo.setTextColor(
            ContextCompat.getColor(this,
                if (status == "PROMO AKTIF") R.color.promo_active else R.color.promo_inactive)
        )

        cardOutput.visibility = View.VISIBLE
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(cardOutput, "alpha", 0f, 1f).setDuration(400),
                ObjectAnimator.ofFloat(cardOutput, "translationY", 60f, 0f).setDuration(400)
            )
            start()
        }
    }

    private fun promoInfo(member: String, subtotal: Double) = when {
        member == "VIP"                              -> "PROMO AKTIF" to "VIP: Diskon 20% + Priority Shipping!"
        member == "Platinum"                         -> "PROMO AKTIF" to "Platinum: Diskon 15% + Cashback poin!"
        member == "Gold"    && subtotal >= 500000.0  -> "PROMO AKTIF" to "Gold: Diskon 10% + Gratis hadiah!"
        member == "Silver"  && subtotal >= 200000.0  -> "PROMO AKTIF" to "Silver: Diskon 5% berlaku!"
        member == "Regular" && subtotal >= 1000000.0 -> "PROMO AKTIF" to "Belanja > 1 Juta: Voucher 50rb!"
        else -> "TIDAK ADA PROMO" to "Upgrade member untuk diskon lebih besar."
    }

    private fun formatRp(n: Double) = "Rp %,.0f".format(n).replace(",", ".")

    private fun resetForm() {
        etNamaPembeli.text.clear()
        etNamaProduk.text.clear()
        etHargaSatuan.text.clear()
        etJumlahBarang.text.clear()
        spinnerKota.setSelection(0)
        spinnerMember.setSelection(0)
        cardOutput.visibility = View.GONE
    }
}