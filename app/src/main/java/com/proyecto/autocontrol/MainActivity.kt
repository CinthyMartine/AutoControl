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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.proyecto.autocontrol.data.AppDatabase
import com.proyecto.autocontrol.data.Auto
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adapter: AutoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        db = AppDatabase.obtener(this)

        val rvAutos = findViewById<RecyclerView>(R.id.rvAutos)
        val tvVacio = findViewById<TextView>(R.id.tvVacio)

        adapter = AutoAdapter(
            alTocar = { auto ->
                val intent = Intent(this, DetalleAutoActivity::class.java)
                intent.putExtra(DetalleAutoActivity.EXTRA_AUTO_ID, auto.id)
                startActivity(intent)
            },
            alMantenerPresionado = { auto -> mostrarOpciones(auto) }
        )
        rvAutos.layoutManager = GridLayoutManager(this, 2)
        rvAutos.adapter = adapter

        findViewById<ImageButton>(R.id.btnAgregarAuto).setOnClickListener {
            startActivity(Intent(this, AutoFormActivity::class.java))
        }

        // Escucha la base de datos: la lista se actualiza sola al agregar, editar o eliminar
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                db.autoDao().obtenerTodos().collect { autos ->
                    adapter.submitList(autos)
                    val vacio = autos.isEmpty()
                    tvVacio.visibility = if (vacio) View.VISIBLE else View.GONE
                    rvAutos.visibility = if (vacio) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun mostrarOpciones(auto: Auto) {
        val opciones = arrayOf(getString(R.string.editar), getString(R.string.eliminar))
        MaterialAlertDialogBuilder(this)
            .setTitle(auto.nombre)
            .setItems(opciones) { _, posicion ->
                when (posicion) {
                    0 -> {
                        val intent = Intent(this, AutoFormActivity::class.java)
                        intent.putExtra(AutoFormActivity.EXTRA_AUTO_ID, auto.id)
                        startActivity(intent)
                    }
                    1 -> confirmarEliminar(auto)
                }
            }
            .show()
    }

    private fun confirmarEliminar(auto: Auto) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.eliminar_auto_titulo)
            .setMessage(getString(R.string.eliminar_auto_mensaje, auto.nombre))
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.eliminar) { _, _ ->
                lifecycleScope.launch {
                    try {
                        db.autoDao().eliminar(auto)
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, R.string.error_guardar, Toast.LENGTH_LONG).show()
                    }
                }
            }
            .show()
    }
}