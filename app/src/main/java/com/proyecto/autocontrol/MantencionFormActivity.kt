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
import com.proyecto.autocontrol.data.Mantencion
import kotlinx.coroutines.launch

class MantencionFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AUTO_ID = "auto_id"
    }

    private lateinit var db: AppDatabase
    private var autoId: Long = -1L

    private lateinit var tilNombre: TextInputLayout
    private lateinit var tilKmObjetivo: TextInputLayout
    private lateinit var etNombre: TextInputEditText
    private lateinit var etKmObjetivo: TextInputEditText
    private lateinit var btnGuardar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mantencion_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
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

        tilNombre = findViewById(R.id.tilNombreMantencion)
        tilKmObjetivo = findViewById(R.id.tilKmObjetivo)
        etNombre = findViewById(R.id.etNombreMantencion)
        etKmObjetivo = findViewById(R.id.etKmObjetivo)
        btnGuardar = findViewById(R.id.btnGuardarMantencion)

        // Muestra el odómetro actual como referencia
        lifecycleScope.launch {
            val auto = db.autoDao().obtenerPorId(autoId)
            if (auto == null) {
                Toast.makeText(this@MantencionFormActivity, R.string.error_auto_no_encontrado, Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            val odometro = db.cargaDao().obtenerUltima(autoId)?.odometro ?: auto.kmInicial
            tilKmObjetivo.helperText = getString(R.string.helper_odometro_actual, Formatos.miles(odometro))
        }

        btnGuardar.setOnClickListener { guardar() }
    }

    private fun guardar() {
        tilNombre.error = null
        tilKmObjetivo.error = null

        val nombre = etNombre.text.toString().trim()
        val kmTexto = etKmObjetivo.text.toString().trim()
        val km = kmTexto.toIntOrNull()

        var valido = true
        if (nombre.isEmpty()) {
            tilNombre.error = getString(R.string.error_nombre_mantencion_vacio)
            valido = false
        }
        if (kmTexto.isEmpty()) {
            tilKmObjetivo.error = getString(R.string.error_km_objetivo_vacio)
            valido = false
        } else if (km == null || km <= 0) {
            tilKmObjetivo.error = getString(R.string.error_km_objetivo_invalido)
            valido = false
        }
        if (!valido || km == null) return

        btnGuardar.isEnabled = false

        lifecycleScope.launch {
            try {
                db.mantencionDao().insertar(
                    Mantencion(autoId = autoId, nombre = nombre, kmObjetivo = km)
                )
                Toast.makeText(this@MantencionFormActivity, R.string.mantencion_guardada, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e("MantencionFormActivity", "Error al guardar la mantención", e)
                Toast.makeText(this@MantencionFormActivity, R.string.error_guardar, Toast.LENGTH_LONG).show()
                btnGuardar.isEnabled = true
            }
        }
    }
}