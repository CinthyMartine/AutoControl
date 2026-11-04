package com.proyecto.autocontrol

import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

object Formatos {

    private val chile: Locale = Locale.forLanguageTag("es-CL")

    // 10500 -> "10.500"
    fun miles(numero: Int): String =
        NumberFormat.getIntegerInstance(chile).format(numero)

    // 50000 -> "$50.000"
    fun pesos(monto: Int): String = "$" + miles(monto)

    // 95.238 -> "$95,2"
    fun pesosDecimal(monto: Double): String =
        "$" + String.format(chile, "%.1f", monto)

    // 12.5 -> "12,5"
    fun unDecimal(numero: Double): String =
        String.format(chile, "%.1f", numero)

    // 40.5 -> "40,5" ; 40.0 -> "40"
    fun litros(litros: Double): String =
        NumberFormat.getNumberInstance(chile).apply { maximumFractionDigits = 2 }.format(litros)

    // milisegundos -> "4 nov 2026"
    fun fecha(millis: Long): String =
        DateFormat.getDateInstance(DateFormat.MEDIUM, chile).format(Date(millis))
}