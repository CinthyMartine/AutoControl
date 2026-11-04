package com.proyecto.autocontrol

import android.os.Bundle
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import android.content.Intent

class DetalleAutoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_AUTO_ID = "auto_id"
    }

    private lateinit var tvNombreAuto: TextView
    private lateinit var tvPatenteAuto: TextView
    private lateinit var tvRendimiento: TextView
    private lateinit var tvRendimientoDetalle: TextView

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
            // Se conecta en la etapa 7
            Toast.makeText(this, R.string.seccion_proximamente, Toast.LENGTH_SHORT).show()
        }

        // Escucha el auto y sus cargas al mismo tiempo; se actualiza solo si cambian
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    db.autoDao().observarPorId(autoId),
                    db.cargaDao().obtenerPorAuto(autoId)
                ) { auto, cargas -> auto to cargas }
                    .collect { (auto, cargas) ->
                        if (auto == null) {
                            // El auto ya no existe (por ejemplo, fue eliminado)
                            finish()
                        } else {
                            mostrarAuto(auto, cargas)
                        }
                    }
            }
        }
    }

    private fun mostrarAuto(auto: Auto, cargas: List<Carga>) {
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
            tvRendimiento.text = getString(R.string.formato_rendimiento, valor)
        }
        tvRendimientoDetalle.setText(R.string.rendimiento_ultima_carga)
    }
}