package com.proyecto.autocontrol

import android.content.Intent
import android.os.Bundle
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
import com.google.android.material.button.MaterialButton
import com.proyecto.autocontrol.data.AppDatabase
import com.proyecto.autocontrol.data.Auto
import com.proyecto.autocontrol.data.Carga
import com.proyecto.autocontrol.data.Mantencion
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DetalleAutoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AUTO_ID = "auto_id"
    }

    private lateinit var tvNombreAuto: TextView
    private lateinit var tvPatenteAuto: TextView
    private lateinit var tvRendimiento: TextView
    private lateinit var tvRendimientoDetalle: TextView
    private lateinit var tvProximaNombre: TextView
    private lateinit var tvProximaEstado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_auto)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        // Sin un id válido no hay nada que mostrar
        val autoId = intent.getLongExtra(EXTRA_AUTO_ID, -1L)
        if (autoId == -1L) {
            Toast.makeText(this, R.string.error_auto_no_encontrado, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = AppDatabase.obtener(this)

        tvNombreAuto = findViewById(R.id.tvNombreAuto)
        tvPatenteAuto = findViewById(R.id.tvPatenteAuto)
        tvRendimiento = findViewById(R.id.tvRendimiento)
        tvRendimientoDetalle = findViewById(R.id.tvRendimientoDetalle)
        tvProximaNombre = findViewById(R.id.tvProximaNombre)
        tvProximaEstado = findViewById(R.id.tvProximaEstado)

        findViewById<ImageButton>(R.id.btnVolver).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnRegistrarCarga).setOnClickListener {
            val intent = Intent(this, CargaFormActivity::class.java)
            intent.putExtra(CargaFormActivity.EXTRA_AUTO_ID, autoId)
            startActivity(intent)
        }
        findViewById<MaterialButton>(R.id.btnCargas).setOnClickListener {
            val intent = Intent(this, CargasActivity::class.java)
            intent.putExtra(CargasActivity.EXTRA_AUTO_ID, autoId)
            startActivity(intent)
        }
        findViewById<MaterialButton>(R.id.btnMantenciones).setOnClickListener {
            val intent = Intent(this, MantencionesActivity::class.java)
            intent.putExtra(MantencionesActivity.EXTRA_AUTO_ID, autoId)
            startActivity(intent)
        }

        // Escucha el auto, sus cargas y sus mantenciones; se actualiza solo si cambian
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    db.autoDao().observarPorId(autoId),
                    db.cargaDao().obtenerPorAuto(autoId),
                    db.mantencionDao().obtenerPorAuto(autoId)
                ) { auto, cargas, mantenciones -> Triple(auto, cargas, mantenciones) }
                    .collect { (auto, cargas, mantenciones) ->
                        if (auto == null) {
                            // El auto ya no existe (por ejemplo, fue eliminado)
                            finish()
                        } else {
                            mostrarRendimiento(auto, cargas)
                            mostrarProximaMantencion(auto, cargas, mantenciones)
                        }
                    }
            }
        }
    }

    private fun mostrarRendimiento(auto: Auto, cargas: List<Carga>) {
        tvNombreAuto.text = auto.nombre
        tvPatenteAuto.text = auto.patente

        // Las cargas vienen ordenadas de la más reciente a la más antigua
        val ultima = cargas.firstOrNull()
        if (ultima == null) {
            tvRendimiento.setText(R.string.sin_rendimiento)
            tvRendimientoDetalle.setText(R.string.sin_cargas_rendimiento)
            return
        }

        // Si es la primera carga, se compara con el km inicial del auto
        val odometroAnterior = cargas.getOrNull(1)?.odometro ?: auto.kmInicial
        val valor = Calculos.rendimiento(ultima.odometro, odometroAnterior, ultima.litros)

        if (valor == null) {
            tvRendimiento.setText(R.string.sin_rendimiento)
        } else {
            tvRendimiento.text = getString(R.string.formato_km_l, Formatos.unDecimal(valor))
        }
        tvRendimientoDetalle.setText(R.string.rendimiento_ultima_carga)
    }

    private fun mostrarProximaMantencion(auto: Auto, cargas: List<Carga>, mantenciones: List<Mantencion>) {
        val proxima = Calculos.proximaMantencion(mantenciones)
        if (proxima == null) {
            tvProximaNombre.setText(R.string.sin_mantenciones_pendientes)
            tvProximaEstado.visibility = View.GONE
            return
        }

        val odometro = Calculos.odometroActual(auto.kmInicial, cargas)
        val estado = Calculos.estadoMantencion(proxima, odometro)

        tvProximaNombre.text = proxima.nombre
        tvProximaEstado.visibility = View.VISIBLE
        tvProximaEstado.text = MantencionAdapter.textoEstado(this, estado, proxima.kmObjetivo - odometro)
        tvProximaEstado.setTextColor(MantencionAdapter.colorEstado(this, estado))
    }
}