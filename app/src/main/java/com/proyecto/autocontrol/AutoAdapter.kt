package com.proyecto.autocontrol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.proyecto.autocontrol.data.Auto

class AutoAdapter(
    private val alTocar: (Auto) -> Unit,
    private val alMantenerPresionado: (Auto) -> Unit
) : ListAdapter<Auto, AutoAdapter.AutoViewHolder>(Diferencia) {

    class AutoViewHolder(vista: View) : RecyclerView.ViewHolder(vista) {
        val tvNombre: TextView = vista.findViewById(R.id.tvNombre)
        val tvPatente: TextView = vista.findViewById(R.id.tvPatente)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutoViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_auto, parent, false)
        return AutoViewHolder(vista)
    }

    override fun onBindViewHolder(holder: AutoViewHolder, position: Int) {
        val auto = getItem(position)
        holder.tvNombre.text = auto.nombre
        holder.tvPatente.text = auto.patente
        holder.itemView.setOnClickListener { alTocar(auto) }
        holder.itemView.setOnLongClickListener {
            alMantenerPresionado(auto)
            true
        }
    }

    // Le dice a la lista qué cambió, para actualizar solo eso
    object Diferencia : DiffUtil.ItemCallback<Auto>() {
        override fun areItemsTheSame(viejo: Auto, nuevo: Auto) = viejo.id == nuevo.id
        override fun areContentsTheSame(viejo: Auto, nuevo: Auto) = viejo == nuevo
    }
}