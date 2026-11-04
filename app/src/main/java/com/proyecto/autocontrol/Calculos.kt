package com.proyecto.autocontrol

object Calculos {

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
}