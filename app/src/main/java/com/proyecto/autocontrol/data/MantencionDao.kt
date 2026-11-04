package com.proyecto.autocontrol.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MantencionDao {
    @Insert
    suspend fun insertar(mantencion: Mantencion): Long

    @Update
    suspend fun actualizar(mantencion: Mantencion)

    @Delete
    suspend fun eliminar(mantencion: Mantencion)

    @Query("SELECT * FROM mantenciones WHERE autoId = :autoId ORDER BY completada, kmObjetivo")
    fun obtenerPorAuto(autoId: Long): Flow<List<Mantencion>>
}