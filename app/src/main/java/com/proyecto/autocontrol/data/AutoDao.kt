package com.proyecto.autocontrol.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoDao {
    @Insert
    suspend fun insertar(auto: Auto): Long

    @Update
    suspend fun actualizar(auto: Auto)

    @Delete
    suspend fun eliminar(auto: Auto)

    @Query("SELECT * FROM autos ORDER BY nombre")
    fun obtenerTodos(): Flow<List<Auto>>

    @Query("SELECT * FROM autos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Auto?

    @Query("SELECT * FROM autos WHERE id = :id")
    fun observarPorId(id: Long): Flow<Auto?>
}