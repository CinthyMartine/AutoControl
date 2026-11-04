package com.proyecto.autocontrol

import android.os.Bundle
import android.widget.TextView
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
import com.proyecto.autocontrol.data.Auto
import kotlinx.coroutines.launch
import android.widget.ImageButton

class AutoFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AUTO_ID = "auto_id"
    }

    private lateinit var db: AppDatabase
    private var autoEditando: Auto? = null

    private lateinit var tilNombre: TextInputLayout
    private lateinit var tilPatente: TextInputLayout
    private lateinit var tilKmInicial: TextInputLayout
    private lateinit var etNombre: TextInputEditText
    private lateinit var etPatente: TextInputEditText
    private lateinit var etKmInicial: TextInputEditText
    private lateinit var btnGuardar: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auto_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        db = AppDatabase.obtener(this)

        tilNombre = findViewById(R.id.tilNombre)
        tilPatente = findViewById(R.id.tilPatente)
        tilKmInicial = findViewById(R.id.tilKmInicial)
        etNombre = findViewById(R.id.etNombre)
        etPatente = findViewById(R.id.etPatente)
        etKmInicial = findViewById(R.id.etKmInicial)
        btnGuardar = findViewById(R.id.btnGuardar)

        // Si viene un id, estamos editando un auto existente
        val autoId = intent.getLongExtra(EXTRA_AUTO_ID, -1L)
        if (autoId != -1L) {
            findViewById<TextView>(R.id.tvTituloForm).setText(R.string.editar_auto)
            lifecycleScope.launch {
                val auto = db.autoDao().obtenerPorId(autoId)
                if (auto == null) {
                    Toast.makeText(this@AutoFormActivity, R.string.error_auto_no_encontrado, Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }
                autoEditando = auto
                // Solo rellenar la primera vez (al girar la pantalla se conserva lo escrito)
                if (savedInstanceState == null) {
                    etNombre.setText(auto.nombre)
                    etPatente.setText(auto.patente)
                    etKmInicial.setText(auto.kmInicial.toString())
                }
            }
        }

        findViewById<ImageButton>(R.id.btnVolver).setOnClickListener { finish() }
        btnGuardar.setOnClickListener { guardar() }
    }

    private fun guardar() {
        // Limpiar errores anteriores
        tilNombre.error = null
        tilPatente.error = null
        tilKmInicial.error = null

        val nombre = etNombre.text.toString().trim()
        val patente = etPatente.text.toString().trim().uppercase()
        val kmTexto = etKmInicial.text.toString().trim()
        val km = kmTexto.toIntOrNull()

        var valido = true
        if (nombre.isEmpty()) {
            tilNombre.error = getString(R.string.error_nombre_vacio)
            valido = false
        }
        if (patente.isEmpty()) {
            tilPatente.error = getString(R.string.error_patente_vacia)
            valido = false
        } else if (!patente.matches(Regex("[A-Z0-9-]{4,8}"))) {
            tilPatente.error = getString(R.string.error_patente_invalida)
            valido = false
        }
        if (kmTexto.isEmpty()) {
            tilKmInicial.error = getString(R.string.error_km_vacio)
            valido = false
        } else if (km == null || km < 0) {
            tilKmInicial.error = getString(R.string.error_km_invalido)
            valido = false
        }
        if (!valido || km == null) return

        btnGuardar.isEnabled = false // evita guardar dos veces por tocar rápido

        lifecycleScope.launch {
            try {
                val existente = autoEditando
                if (existente == null) {
                    db.autoDao().insertar(Auto(nombre = nombre, patente = patente, kmInicial = km))
                } else {
                    db.autoDao().actualizar(existente.copy(nombre = nombre, patente = patente, kmInicial = km))
                }
                Toast.makeText(this@AutoFormActivity, R.string.auto_guardado, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@AutoFormActivity, R.string.error_guardar, Toast.LENGTH_LONG).show()
                btnGuardar.isEnabled = true
            }
        }
    }
}