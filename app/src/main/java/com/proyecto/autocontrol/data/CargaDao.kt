package com.proyecto.autocontrol.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CargaDao {
    @Insert
    suspend fun insertar(carga: Carga): Long

    @Delete
    suspend fun eliminar(carga: Carga)

    @Query("SELECT * FROM cargas WHERE autoId = :autoId ORDER BY odometro DESC")
    fun obtenerPorAuto(autoId: Long): Flow<List<Carga>>

    @Query("SELECT * FROM cargas WHERE autoId = :autoId ORDER BY odometro DESC LIMIT 1")
    suspend fun obtenerUltima(autoId: Long): Carga?
}