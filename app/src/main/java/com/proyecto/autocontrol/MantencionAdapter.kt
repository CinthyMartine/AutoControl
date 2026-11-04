package com.proyecto.autocontrol

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proyecto.autocontrol.data.Mantencion

// Una mantención junto con su estado ya calculado
data class FilaMantencion(
    val mantencion: Mantencion,
    val estado: EstadoMantencion,
    val kmFaltantes: Int
)

class MantencionAdapter(
    private val alTocar: (Mantencion) -> Unit
) : ListAdapter<FilaMantencion, MantencionAdapter.MantencionViewHolder>(Diferencia) {

    companion object {
        // Estas funciones también se usan en el detalle del auto

        fun etiquetaEstado(contexto: Context, estado: EstadoMantencion): String =
            contexto.getString(
                when (estado) {
                    EstadoMantencion.PENDIENTE -> R.string.estado_pendiente
                    EstadoMantencion.PROXIMA -> R.string.estado_proxima
                    EstadoMantencion.ATRASADA -> R.string.estado_atrasada
                    EstadoMantencion.COMPLETADA -> R.string.estado_completada
                }
            )

        fun textoEstado(contexto: Context, estado: EstadoMantencion, kmFaltantes: Int): String =
            when (estado) {
                EstadoMantencion.COMPLETADA -> contexto.getString(R.string.estado_completada)
                EstadoMantencion.ATRASADA ->
                    if (kmFaltantes == 0) contexto.getString(R.string.corresponde_ahora)
                    else contexto.getString(R.string.atrasada_km, Formatos.miles(-kmFaltantes))
                else -> contexto.getString(R.string.faltan_km, Formatos.miles(kmFaltantes))
            }

        fun colorEstado(contexto: Context, estado: EstadoMantencion): Int =
            ContextCompat.getColor(
                contexto,
                when (estado) {
                    EstadoMantencion.PENDIENTE -> R.color.texto_secundario
                    EstadoMantencion.PROXIMA -> R.color.naranjo
                    EstadoMantencion.ATRASADA -> R.color.rojo
                    EstadoMantencion.COMPLETADA -> R.color.verde
                }
            )
    }

    class MantencionViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val contenido: View = vista.findViewById(R.id.contenidoMantencion)
        val tvNombre: TextView = vista.findViewById(R.id.tvNombreMantencion)
        val tvEstado: TextView = vista.findViewById(R.id.tvEstadoMantencion)
        val tvDetalle: TextView = vista.findViewById(R.id.tvDetalleMantencion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MantencionViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mantencion, parent, false)
        return MantencionViewHolder(vista)
    }

    override fun onBindViewHolder(holder: MantencionViewHolder, position: Int) {
        val fila = getItem(position)
        val contexto = holder.itemView.context
        val mantencion = fila.mantencion

        holder.tvNombre.text = mantencion.nombre
        holder.tvEstado.text = etiquetaEstado(contexto, fila.estado)
        holder.tvEstado.setTextColor(colorEstado(contexto, fila.estado))

        val correspondeA = contexto.getString(
            R.string.corresponde_a_los_km,
            Formatos.miles(mantencion.kmObjetivo)
        )
        holder.tvDetalle.text = if (fila.estado == EstadoMantencion.COMPLETADA) {
            correspondeA
        } else {
            contexto.getString(
                R.string.detalle_mantencion,
                textoEstado(contexto, fila.estado, fila.kmFaltantes),
                correspondeA
            )
        }

        // Las completadas se ven más suaves
        holder.contenido.alpha = if (fila.estado == EstadoMantencion.COMPLETADA) 0.6f else 1f

        holder.itemView.setOnClickListener { alTocar(mantencion) }
    }

    object Diferencia : DiffUtil.ItemCallback<FilaMantencion>() {
        override fun areItemsTheSame(viejo: FilaMantencion, nuevo: FilaMantencion) =
            viejo.mantencion.id == nuevo.mantencion.id
        override fun areContentsTheSame(viejo: FilaMantencion, nuevo: FilaMantencion) =
            viejo == nuevo
    }
}