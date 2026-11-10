package com.proyecto.autocontrol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proyecto.autocontrol.data.Carga

// Una carga junto con sus valores ya calculados
data class FilaCarga(
    val carga: Carga,
    val rendimiento: Double?,
    val costoPorKm: Double?
)

class CargaAdapter(
    private val alTocar: (Carga) -> Unit
) : ListAdapter<FilaCarga, CargaAdapter.CargaViewHolder>(Diferencia) {

    class CargaViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val tvFecha: TextView = vista.findViewById(R.id.tvFechaCarga)
        val tvRendimiento: TextView = vista.findViewById(R.id.tvRendimientoCarga)
        val tvOdometroLitros: TextView = vista.findViewById(R.id.tvOdometroLitros)
        val tvCosto: TextView = vista.findViewById(R.id.tvCostoCarga)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CargaViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carga, parent, false)
        return CargaViewHolder(vista)
    }

    override fun onBindViewHolder(holder: CargaViewHolder, position: Int) {
        val fila = getItem(position)
        val carga = fila.carga
        val contexto = holder.itemView.context

        holder.tvFecha.text = Formatos.fecha(carga.fecha)

        holder.tvRendimiento.text = if (fila.rendimiento != null) {
            contexto.getString(R.string.formato_km_l, Formatos.unDecimal(fila.rendimiento))
        } else {
            contexto.getString(R.string.sin_rendimiento)
        }

        holder.tvOdometroLitros.text = contexto.getString(
            R.string.detalle_odometro_litros,
            Formatos.miles(carga.odometro),
            Formatos.litros(carga.litros)
        )

        holder.tvCosto.text = if (fila.costoPorKm != null) {
            contexto.getString(
                R.string.detalle_costo,
                Formatos.pesos(carga.costo),
                Formatos.pesosDecimal(fila.costoPorKm)
            )
        } else {
            Formatos.pesos(carga.costo)
        }

        holder.itemView.setOnClickListener { alTocar(carga) }
    }

    object Diferencia : DiffUtil.ItemCallback<FilaCarga>() {
        override fun areItemsTheSame(viejo: FilaCarga, nuevo: FilaCarga) = viejo.carga.id == nuevo.carga.id
        override fun areContentsTheSame(viejo: FilaCarga, nuevo: FilaCarga) = viejo == nuevo
    }
}