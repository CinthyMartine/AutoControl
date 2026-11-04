package com.proyecto.autocontrol

import com.proyecto.autocontrol.data.Carga
import com.proyecto.autocontrol.data.Mantencion

enum class EstadoMantencion { PENDIENTE, PROXIMA, ATRASADA, COMPLETADA }

object Calculos {

    // Desde cuántos km antes se considera "próxima"
    const val KM_AVISO = 1000

    /**
     * Rendimiento en km/l entre dos registros del odómetro.
     * Devuelve null si no se puede calcular (km o litros inválidos).
     */
    fun rendimiento(odometroActual: Int, odometroAnterior: Int, litros: Double): Double? {
        val km = odometroActual - odometroAnterior
        if (km <= 0 || litros <= 0.0) return null
        return km / litros
    }

    /**
     * Costo por kilómetro recorrido.
     * Devuelve null si no hay km recorridos.
     */
    fun costoPorKm(costo: Int, kmRecorridos: Int): Double? {
        if (kmRecorridos <= 0) return null
        return costo.toDouble() / kmRecorridos
    }

    /** Odómetro más reciente del auto: la última carga o, si no hay, el km inicial. */
    fun odometroActual(kmInicial: Int, cargas: List<Carga>): Int =
        cargas.maxOfOrNull { it.odometro } ?: kmInicial

    /** Estado de una mantención según el odómetro actual. */
    fun estadoMantencion(mantencion: Mantencion, odometroActual: Int): EstadoMantencion {
        if (mantencion.completada) return EstadoMantencion.COMPLETADA
        val faltan = mantencion.kmObjetivo - odometroActual
        return when {
            faltan <= 0 -> EstadoMantencion.ATRASADA
            faltan <= KM_AVISO -> EstadoMantencion.PROXIMA
            else -> EstadoMantencion.PENDIENTE
        }
    }

    /** La mantención pendiente con el kilometraje más cercano. */
    fun proximaMantencion(mantenciones: List<Mantencion>): Mantencion? =
        mantenciones.filter { !it.completada }.minByOrNull { it.kmObjetivo }
}