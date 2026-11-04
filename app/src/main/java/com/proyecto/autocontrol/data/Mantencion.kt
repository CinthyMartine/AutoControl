package com.proyecto.autocontrol.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mantenciones",
    foreignKeys = [ForeignKey(
        entity = Auto::class,
        parentColumns = ["id"],
        childColumns = ["autoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("autoId")]
)
data class Mantencion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val autoId: Long,
    val nombre: String,
    val kmObjetivo: Int,
    val completada: Boolean = false
)