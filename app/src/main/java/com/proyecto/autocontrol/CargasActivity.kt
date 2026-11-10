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
import com.proyecto.autocontrol.data.Auto
import com.proyecto.autocontrol.data.Carga
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class CargasActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AUTO_ID = "auto_id"
    }

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cargas)
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
        val tvNombreAuto = findViewById<TextView>(R.id.tvNombreAutoCargas)
        val tvSinCargas = findViewById<TextView>(R.id.tvSinCargas)
        val rvCargas = findViewById<RecyclerView>(R.id.rvCargas)

        val adapter = CargaAdapter(alTocar = { carga -> mostrarOpciones(carga) })
        rvCargas.layoutManager = LinearLayoutManager(this)
        rvCargas.adapter = adapter

        findViewById<ImageButton>(R.id.btnVolver).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnAgregarCarga).setOnClickListener {
            val intent = Intent(this, CargaFormActivity::class.java)
            intent.putExtra(CargaFormActivity.EXTRA_AUTO_ID, autoId)
            startActivity(intent)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    db.autoDao().observarPorId(autoId),
                    db.cargaDao().obtenerPorAuto(autoId)
                ) { auto, cargas -> auto to cargas }
                    .collect { (auto, cargas) ->
                        if (auto == null) {
                            finish()
                            return@collect
                        }
                        tvNombreAuto.text = auto.nombre

                        val filas = calcularFilas(auto, cargas)
                        adapter.submitList(filas)

                        val vacio = filas.isEmpty()
                        tvSinCargas.visibility = if (vacio) View.VISIBLE else View.GONE
                        rvCargas.visibility = if (vacio) View.GONE else View.VISIBLE
                    }
            }
        }
    }

    /**
     * Las cargas vienen de la más reciente a la más antigua.
     * Cada carga se compara con la siguiente de la lista (la anterior en el tiempo);
     * la más antigua se compara con el km inicial del auto.
     */
    private fun calcularFilas(auto: Auto, cargas: List<Carga>): List<FilaCarga> {
        return cargas.mapIndexed { indice, carga ->
            val odometroAnterior = cargas.getOrNull(indice + 1)?.odometro ?: auto.kmInicial
            val kmRecorridos = carga.odometro - odometroAnterior
            FilaCarga(
                carga = carga,
                rendimiento = Calculos.rendimiento(carga.odometro, odometroAnterior, carga.litros),
                costoPorKm = Calculos.costoPorKm(carga.costo, kmRecorridos)
            )
        }
    }

    private fun mostrarOpciones(carga: Carga) {
        val opciones = arrayOf(getString(R.string.eliminar))
        MaterialAlertDialogBuilder(this)
            .setTitle(Formatos.fecha(carga.fecha))
            .setItems(opciones) { _, _ -> confirmarEliminar(carga) }
            .show()
    }

    private fun confirmarEliminar(carga: Carga) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.eliminar_carga_titulo)
            .setMessage(
                getString(
                    R.string.eliminar_carga_mensaje,
                    Formatos.fecha(carga.fecha),
                    Formatos.miles(carga.odometro)
                )
            )
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.eliminar) { _, _ ->
                lifecycleScope.launch {
                    try {
                        db.cargaDao().eliminar(carga)
                        Toast.makeText(this@CargasActivity, R.string.carga_eliminada, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("CargasActivity", "Error al eliminar la carga", e)
                        Toast.makeText(this@CargasActivity, R.string.error_guardar, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .show()
    }
}