package com.proyecto.autocontrol.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "autos")
data class Auto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val patente: String,
    val kmInicial: Int
)