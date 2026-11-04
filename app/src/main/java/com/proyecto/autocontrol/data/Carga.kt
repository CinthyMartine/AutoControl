package com.proyecto.autocontrol.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cargas",
    foreignKeys = [ForeignKey(
        entity = Auto::class,
        parentColumns = ["id"],
        childColumns = ["autoId"],
        onDelete = ForeignKey.CASCADE   // si se borra el auto, se borran sus cargas
    )],
    indices = [Index("autoId")]
)
data class Carga(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val autoId: Long,
    val fecha: Long,      // fecha en milisegundos
    val odometro: Int,    // km que marca el auto
    val litros: Double,
    val costo: Int        // en pesos
)