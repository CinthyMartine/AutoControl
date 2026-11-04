package com.proyecto.autocontrol

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.proyecto.autocontrol.data.AppDatabase
import com.proyecto.autocontrol.data.Mantencion
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MantencionesActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AUTO_ID = "auto_id"
    }

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mantenciones)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        val autoId = intent.getLongExtra(EXTRA_AUTO_ID, -1L)
        if (autoId == -1L) {
            Toast.makeText(this, R.string.error_auto_no_encontrado, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db = AppDatabase.obtener(this)
        val tvNombreAuto = findViewById<TextView>(R.id.tvNombreAutoMantenciones)
        val tvSinMantenciones = findViewById<TextView>(R.id.tvSinMantenciones)
        val rvMantenciones = findViewById<RecyclerView>(R.id.rvMantenciones)

        val adapter = MantencionAdapter(alTocar = { mantencion -> mostrarOpciones(mantencion) })
        rvMantenciones.layoutManager = LinearLayoutManager(this)
        rvMantenciones.adapter = adapter

        findViewById<ImageButton>(R.id.btnVolver).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnAgregarMantencion).setOnClickListener {
            val intent = Intent(this, MantencionFormActivity::class.java)
            intent.putExtra(MantencionFormActivity.EXTRA_AUTO_ID, autoId)
            startActivity(intent)
        }

        // Escucha el auto, sus cargas (para el odómetro) y sus mantenciones
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    db.autoDao().observarPorId(autoId),
                    db.cargaDao().obtenerPorAuto(autoId),
                    db.mantencionDao().obtenerPorAuto(autoId)
                ) { auto, cargas, mantenciones -> Triple(auto, cargas, mantenciones) }
                    .collect { (auto, cargas, mantenciones) ->
                        if (auto == null) {
                            finish()
                            return@collect
                        }
                        tvNombreAuto.text = auto.nombre

                        val odometro = Calculos.odometroActual(auto.kmInicial, cargas)
                        val filas = mantenciones.map { m ->
                            FilaMantencion(
                                mantencion = m,
                                estado = Calculos.estadoMantencion(m, odometro),
                                kmFaltantes = m.kmObjetivo - odometro
                            )
                        }
                        adapter.submitList(filas)

                        val vacio = filas.isEmpty()
                        tvSinMantenciones.visibility = if (vacio) View.VISIBLE else View.GONE
                        rvMantenciones.visibility = if (vacio) View.GONE else View.VISIBLE
                    }
            }
        }
    }

    private fun mostrarOpciones(mantencion: Mantencion) {
        val opcionEstado = getString(
            if (mantencion.completada) R.string.marcar_pendiente else R.string.marcar_completada
        )
        val opciones = arrayOf(opcionEstado, getString(R.string.eliminar))

        MaterialAlertDialogBuilder(this)
            .setTitle(mantencion.nombre)
            .setItems(opciones) { _, posicion ->
                when (posicion) {
                    0 -> cambiarEstado(mantencion)
                    1 -> confirmarEliminar(mantencion)
                }
            }
            .show()
    }

    private fun cambiarEstado(mantencion: Mantencion) {
        lifecycleScope.launch {
            try {
                db.mantencionDao().actualizar(mantencion.copy(completada = !mantencion.completada))
            } catch (e: Exception) {
                Log.e("MantencionesActivity", "Error al actualizar la mantención", e)
                Toast.makeText(this@MantencionesActivity, R.string.error_guardar, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmarEliminar(mantencion: Mantencion) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.eliminar_mantencion_titulo)
            .setMessage(getString(R.string.eliminar_mantencion_mensaje, mantencion.nombre))
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.eliminar) { _, _ ->
                lifecycleScope.launch {
                    try {
                        db.mantencionDao().eliminar(mantencion)
                    } catch (e: Exception) {
                        Log.e("MantencionesActivity", "Error al eliminar la mantención", e)
                        Toast.makeText(this@MantencionesActivity, R.string.error_guardar, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .show()
    }
}