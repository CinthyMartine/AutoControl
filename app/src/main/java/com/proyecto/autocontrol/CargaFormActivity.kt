package com.proyecto.autocontrol

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.proyecto.autocontrol.data.AppDatabase
import com.proyecto.autocontrol.data.Carga
import kotlinx.coroutines.launch
import android.widget.ImageButton

class CargaFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AUTO_ID = "auto_id"
    }

    private lateinit var db: AppDatabase
    private var autoId: Long = -1L

    private lateinit var tilOdometro: TextInputLayout
    private lateinit var tilLitros: TextInputLayout
    private lateinit var tilCosto: TextInputLayout
    private lateinit var etOdometro: TextInputEditText
    private lateinit var etLitros: TextInputEditText
    private lateinit var etCosto: TextInputEditText
    private lateinit var btnGuardarCarga: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_carga_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        autoId = intent.getLongExtra(EXTRA_AUTO_ID, -1L)
        if (autoId == -1L) {
            Toast.makeText(this, R.string.error_auto_no_encontrado, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = AppDatabase.obtener(this)

        tilOdometro = findViewById(R.id.tilOdometro)
        tilLitros = findViewById(R.id.tilLitros)
        tilCosto = findViewById(R.id.tilCosto)
        etOdometro = findViewById(R.id.etOdometro)
        etLitros = findViewById(R.id.etLitros)
        etCosto = findViewById(R.id.etCosto)
        btnGuardarCarga = findViewById(R.id.btnGuardarCarga)

        // Muestra el último odómetro como ayuda bajo el campo
        lifecycleScope.launch {
            val auto = db.autoDao().obtenerPorId(autoId)
            if (auto == null) {
                Toast.makeText(this@CargaFormActivity, R.string.error_auto_no_encontrado, Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            val minimo = db.cargaDao().obtenerUltima(autoId)?.odometro ?: auto.kmInicial
            tilOdometro.helperText = getString(R.string.helper_ultimo_odometro, minimo)
        }

        findViewById<ImageButton>(R.id.btnVolver).setOnClickListener { finish() }
        btnGuardarCarga.setOnClickListener { guardar() }
    }

    // Acepta "40,5" o "40.5"
    private fun leerDecimal(texto: String): Double? =
        texto.replace(',', '.').toDoubleOrNull()

    private fun guardar() {
        tilOdometro.error = null
        tilLitros.error = null
        tilCosto.error = null

        val odometroTexto = etOdometro.text.toString().trim()
        val litrosTexto = etLitros.text.toString().trim()
        val costoTexto = etCosto.text.toString().trim()

        val odometro = odometroTexto.toIntOrNull()
        val litros = leerDecimal(litrosTexto)
        val costo = costoTexto.toIntOrNull()

        var valido = true

        if (odometroTexto.isEmpty()) {
            tilOdometro.error = getString(R.string.error_odometro_vacio)
            valido = false
        } else if (odometro == null || odometro < 0) {
            tilOdometro.error = getString(R.string.error_km_invalido)
            valido = false
        }

        if (litrosTexto.isEmpty()) {
            tilLitros.error = getString(R.string.error_litros_vacio)
            valido = false
        } else if (litros == null || litros <= 0.0 || litros > 200.0) {
            tilLitros.error = getString(R.string.error_litros_invalido)
            valido = false
        }

        if (costoTexto.isEmpty()) {
            tilCosto.error = getString(R.string.error_costo_vacio)
            valido = false
        } else if (costo == null || costo <= 0) {
            tilCosto.error = getString(R.string.error_costo_invalido)
            valido = false
        }

        if (!valido || odometro == null || litros == null || costo == null) return

        btnGuardarCarga.isEnabled = false // evita guardar dos veces

        lifecycleScope.launch {
            try {
                val auto = db.autoDao().obtenerPorId(autoId)
                if (auto == null) {
                    Toast.makeText(this@CargaFormActivity, R.string.error_auto_no_encontrado, Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                // El odómetro debe avanzar respecto al último registro
                val minimo = db.cargaDao().obtenerUltima(autoId)?.odometro ?: auto.kmInicial
                if (odometro <= minimo) {
                    tilOdometro.error = getString(R.string.error_odometro_menor, minimo)
                    btnGuardarCarga.isEnabled = true
                    return@launch
                }

                db.cargaDao().insertar(
                    Carga(
                        autoId = autoId,
                        fecha = System.currentTimeMillis(),
                        odometro = odometro,
                        litros = litros,
                        costo = costo
                    )
                )
                Toast.makeText(this@CargaFormActivity, R.string.carga_guardada, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("CargaFormActivity", "Error al guardar la carga", e)
                Toast.makeText(this@CargaFormActivity, R.string.error_guardar, Toast.LENGTH_LONG).show()
                btnGuardarCarga.isEnabled = true
            }
        }
    }
}